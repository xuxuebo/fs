package com.qgutech.fs.processor;

import com.google.gson.Gson;
import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessorTypeEnum;
import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.PropertiesUtils;
import com.qgutech.fs.utils.RedisKey;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import redis.clients.jedis.JedisCommands;

import java.util.*;

public class ProcessorExecutor extends TimerTask implements InitializingBean {

    private static final Log LOG = LogFactory.getLog(ProcessorExecutor.class);
    private static final Gson gson = new Gson();
    private static final String TIME_SECOND_UNIT = "s";
    private static final String TIME_MINUTE_UNIT = "m";
    private static final String TIME_HOUR_UNIT = "h";

    private ProcessorFactory processorFactory;
    private ThreadPoolTaskExecutor taskExecutor;
    private JedisCommands commonJedis;
    private int maxDoingListSize;
    private int minAvailablePoolSize;
    private long lockExpireTime;
    private long timerDelay;
    private long timerPeriod;
    private Map<String, Integer> maxDoingListSizeMap;
    private String repeatProcessTimeInterval;
    private int maxAllowFailCnt;
    private Map<Integer, Long> roundNextExecuteTimeMap;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(processorFactory, "ProcessorFactory is null!");
        Assert.notNull(taskExecutor, "TaskExecutor is null!");
        Assert.notNull(commonJedis, "CommonJedis is null!");
        if (maxDoingListSizeMap == null) {
            maxDoingListSizeMap = new HashMap<String, Integer>();
        }

        if (StringUtils.isEmpty(repeatProcessTimeInterval)
                || "null".equals(repeatProcessTimeInterval)) {
            return;
        }

        String[] repeatProcessTimes = repeatProcessTimeInterval.split(",");
        if (repeatProcessTimes.length <= 0) {
            return;
        }

        maxAllowFailCnt = repeatProcessTimes.length;
        roundNextExecuteTimeMap = new HashMap<Integer, Long>(repeatProcessTimes.length);
        for (int i = 0; i < repeatProcessTimes.length; i++) {
            String time = repeatProcessTimes[i];
            if (StringUtils.isEmpty(time)) {
                throw new RuntimeException("repeatProcessTimeInterval["
                        + repeatProcessTimeInterval + "] is illegally!");
            }

            String numTime = time.substring(0, time.length() - 1);
            if (!NumberUtils.isNumber(numTime)) {
                throw new RuntimeException("repeatProcessTimeInterval["
                        + repeatProcessTimeInterval + "] is illegally!");
            }

            long timeMills = Long.parseLong(numTime);
            String unit = time.substring(time.length() - 1, time.length());
            if (TIME_SECOND_UNIT.equals(unit)) {
                timeMills = timeMills * 1000;
            } else if (TIME_MINUTE_UNIT.equals(unit)) {
                timeMills = timeMills * 60 * 1000;
            } else if (TIME_HOUR_UNIT.equals(unit)) {
                timeMills = timeMills * 60 * 60 * 1000;
            } else {
                throw new RuntimeException("repeatProcessTimeInterval["
                        + repeatProcessTimeInterval + "] is illegally!");
            }

            roundNextExecuteTimeMap.put(i + 1, timeMills);
        }
    }

    @Override
    public void run() {
        if (PropertiesUtils.isConvert()) {
            execute();
        }
    }

    private int getMaxDoingListSize(String queueName) {
        Integer maxDoingListSize = maxDoingListSizeMap.get(queueName);
        if (maxDoingListSize != null && maxDoingListSize > 0) {
            return maxDoingListSize;
        } else {
            return this.maxDoingListSize;
        }
    }

    private void execute() {
        Set<String> queueNames = commonJedis.smembers(RedisKey.FS_QUEUE_NAME_LIST);
        if (CollectionUtils.isEmpty(queueNames)) {
            return;
        }

        int availableCount = taskExecutor.getMaxPoolSize() - taskExecutor.getActiveCount();
        if (availableCount <= minAvailablePoolSize) {
            return;
        }

        for (final String queueName : queueNames) {
            if (!PropertiesUtils.isDocConvert() && (RedisKey.FS_ZIP_DOC_QUEUE_LIST.equals(queueName)
                    || RedisKey.FS_DOC_QUEUE_LIST.equals(queueName))) {
                continue;
            }

            int activeCount = taskExecutor.getMaxPoolSize() - taskExecutor.getActiveCount();
            if (activeCount <= minAvailablePoolSize) {
                continue;
            }

            String doingQueueName = queueName + RedisKey.FS_DOING_LIST_SUFFIX;
            String lockKey = doingQueueName + RedisKey.FS_DOING_LIST_LOCK_SUFFIX;
            boolean lockFlag = getLock(lockKey);
            if (!lockFlag) {
                continue;
            }

            final String doingList = doingQueueName + FsConstants.UNDERLINE
                    + PropertiesUtils.getServerHost();
            Long size = commonJedis.scard(doingList);
            if (size >= getMaxDoingListSize(queueName)) {
                commonJedis.expire(lockKey, 0);
                continue;
            }

            final String fsFileId = commonJedis.rpop(queueName);
            if (StringUtils.isEmpty(fsFileId)) {
                commonJedis.expire(lockKey, 0);
                continue;
            }

            final String fsFileJson = commonJedis.get(RedisKey.FS_FILE_CONTENT_PREFIX + fsFileId);
            if (StringUtils.isEmpty(fsFileJson)) {
                commonJedis.expire(lockKey, 0);
                continue;
            }

            commonJedis.sadd(doingList, fsFileId);
            commonJedis.expire(lockKey, 0);
            LOG.info("Processing fsFile[fsFile:" + fsFileJson + ",queue:" + queueName + "] start!");
            taskExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        executeTask(fsFileId, fsFileJson, doingList);
                    } catch (Throwable e) {
                        LOG.error("Error occurred when executing process fsFile[fsFile:" + fsFileJson + "]!", e);
                    } finally {
                        LOG.info("Processing fsFile[fsFile:" + fsFileJson + ",queue:" + queueName + "] end!");
                    }
                }
            });
        }
    }

    protected void executeTask(String fsFileId, String fsFileJson, String doingList) {
        FsFile fsFile = null;
        try {
            fsFile = gson.fromJson(fsFileJson, FsFile.class);
            Processor processor = processorFactory.acquireProcessor(fsFile.getProcessor());
            processor.process(fsFile);
            commonJedis.expire(RedisKey.FS_REPEAT_EXECUTE_CNT_ + fsFileId, 0);
            commonJedis.expire(RedisKey.FS_FILE_CONTENT_PREFIX + fsFileId, 0);
        } catch (Throwable e) {
            LOG.error("Error occurred when executing process fsFile[fsFile:" + fsFileJson + "]!", e);
            Long executeCnt = commonJedis.incr(RedisKey.FS_REPEAT_EXECUTE_CNT_ + fsFileId);
            if (executeCnt == null || executeCnt == 0 || executeCnt > maxAllowFailCnt) {
                commonJedis.expire(RedisKey.FS_REPEAT_EXECUTE_CNT_ + fsFileId, 0);
                commonJedis.expire(RedisKey.FS_FILE_CONTENT_PREFIX + fsFileId, 0);
                return;
            }

            if (fsFile == null) {
                return;
            }

            Long nextExecuteTime = System.currentTimeMillis()
                    + roundNextExecuteTimeMap.get(executeCnt.intValue());
            commonJedis.zadd(RedisKey.FS_REPEAT_EXECUTE_QUEUE_NAME
                    , nextExecuteTime.doubleValue()
                    , fsFileId + FsConstants.VERTICAL_LINE + fsFile.getProcessor());
        } finally {
            commonJedis.srem(doingList, fsFileId);
        }
    }

    public void init() {
        if (!PropertiesUtils.isConvert()) {
            return;
        }

        Set<String> queueNames = commonJedis.smembers(RedisKey.FS_QUEUE_NAME_LIST);
        if (CollectionUtils.isEmpty(queueNames)) {
            start();
            return;
        }

        for (String queueName : queueNames) {
            String doingQueueName = queueName + RedisKey.FS_DOING_LIST_SUFFIX
                    + FsConstants.UNDERLINE + PropertiesUtils.getServerHost();
            Long doingCount = commonJedis.scard(doingQueueName);
            if (doingCount == null || doingCount == 0l) {
                continue;
            }

            String lockKey = doingQueueName + RedisKey.FS_DOING_LIST_LOCK_SUFFIX;
            boolean lockFlag = getLock(lockKey);
            if (!lockFlag) {
                continue;
            }

            Set<String> doingFsFileIds = commonJedis.smembers(doingQueueName);
            if (CollectionUtils.isNotEmpty(doingFsFileIds)) {
                String[] fsFileIdArray = doingFsFileIds.toArray(new String[doingFsFileIds.size()]);
                commonJedis.rpush(queueName, fsFileIdArray);
                commonJedis.srem(doingQueueName, fsFileIdArray);
            }

            commonJedis.expire(lockKey, 0);
        }

        start();
    }

    private void start() {
        Timer timer = new Timer();
        timer.schedule(this, timerDelay, timerPeriod);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (!getLock(RedisKey.FS_REPEAT_EXECUTE_LOCK)) {
                        return;
                    }

                    Set<String> tasks = commonJedis.zrangeByScore(
                            RedisKey.FS_REPEAT_EXECUTE_QUEUE_NAME, 0, System.currentTimeMillis());
                    if (CollectionUtils.isEmpty(tasks)) {
                        commonJedis.expire(RedisKey.FS_REPEAT_EXECUTE_LOCK, 0);
                        return;
                    }

                    for (String task : tasks) {
                        String fsFileId = task.substring(0, task.indexOf(FsConstants.VERTICAL_LINE));
                        String processor = task.substring(task.indexOf(FsConstants.VERTICAL_LINE) + 1);
                        commonJedis.lpush(getProcessQueueName(processor), fsFileId);
                    }

                    commonJedis.expire(RedisKey.FS_REPEAT_EXECUTE_LOCK, 0);
                } catch (Throwable e) {
                    LOG.error("Exception occurred when getting the fsFiles from the repeat queue list!", e);
                }
            }
        }, timerDelay, timerPeriod);
    }

    protected String getProcessQueueName(String processor) {
        switch (ProcessorTypeEnum.valueOf(processor)) {
            case VID:
                return RedisKey.FS_VIDEO_QUEUE_LIST;
            case AUD:
                return RedisKey.FS_AUDIO_QUEUE_LIST;
            case DOC:
                return RedisKey.FS_DOC_QUEUE_LIST;
            case ZVID:
                return RedisKey.FS_ZIP_VIDEO_QUEUE_LIST;
            case ZAUD:
                return RedisKey.FS_ZIP_AUDIO_QUEUE_LIST;
            case ZIMG:
                return RedisKey.FS_ZIP_IMAGE_QUEUE_LIST;
            case ZDOC:
                return RedisKey.FS_ZIP_DOC_QUEUE_LIST;
            default:
                throw new RuntimeException("This processor[" + processor + "] is not supported!");
        }
    }

    protected boolean getLock(String key) {
        Long lockFlag = commonJedis.setnx(key, System.currentTimeMillis() + "");
        if (lockFlag != null && lockFlag == 1l) {
            commonJedis.expire(key, (int) (lockExpireTime / 1000));
            return true;
        }

        String millSeconds = commonJedis.get(key);
        if (millSeconds == null) {
            lockFlag = commonJedis.setnx(key, System.currentTimeMillis() + "");
            if (lockFlag != null && lockFlag == 1l) {
                commonJedis.expire(key, (int) (lockExpireTime / 1000));
                return true;
            } else {
                return false;
            }
        }

        long currentMillSeconds = System.currentTimeMillis();
        if (currentMillSeconds - Long.valueOf(millSeconds) >= lockExpireTime) {
            String redisLockTime = commonJedis.getSet(key, currentMillSeconds + "");
            if (redisLockTime == null || redisLockTime.equals(millSeconds)) {
                commonJedis.expire(key, (int) (lockExpireTime / 1000));
                return true;
            }
        }

        return false;
    }

    public ProcessorFactory getProcessorFactory() {
        return processorFactory;
    }

    public void setProcessorFactory(ProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    public ThreadPoolTaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public JedisCommands getCommonJedis() {
        return commonJedis;
    }

    public void setCommonJedis(JedisCommands commonJedis) {
        this.commonJedis = commonJedis;
    }

    public int getMaxDoingListSize() {
        return maxDoingListSize;
    }

    public void setMaxDoingListSize(int maxDoingListSize) {
        this.maxDoingListSize = maxDoingListSize;
    }

    public int getMinAvailablePoolSize() {
        return minAvailablePoolSize;
    }

    public void setMinAvailablePoolSize(int minAvailablePoolSize) {
        this.minAvailablePoolSize = minAvailablePoolSize;
    }

    public long getLockExpireTime() {
        return lockExpireTime;
    }

    public void setLockExpireTime(long lockExpireTime) {
        this.lockExpireTime = lockExpireTime;
    }

    public long getTimerPeriod() {
        return timerPeriod;
    }

    public void setTimerPeriod(long timerPeriod) {
        this.timerPeriod = timerPeriod;
    }

    public long getTimerDelay() {
        return timerDelay;
    }

    public void setTimerDelay(long timerDelay) {
        this.timerDelay = timerDelay;
    }

    public Map<String, Integer> getMaxDoingListSizeMap() {
        return maxDoingListSizeMap;
    }

    public void setMaxDoingListSizeMap(Map<String, Integer> maxDoingListSizeMap) {
        this.maxDoingListSizeMap = maxDoingListSizeMap;
    }

    public String getRepeatProcessTimeInterval() {
        return repeatProcessTimeInterval;
    }

    public void setRepeatProcessTimeInterval(String repeatProcessTimeInterval) {
        this.repeatProcessTimeInterval = repeatProcessTimeInterval;
    }
}
