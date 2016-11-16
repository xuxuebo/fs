package com.qgutech.fs.processor;

import com.google.gson.Gson;
import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.utils.PropertiesUtils;
import com.qgutech.fs.utils.RedisKey;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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

    private ProcessorFactory processorFactory;
    private ThreadPoolTaskExecutor taskExecutor;
    private JedisCommands commonJedis;
    private int maxDoingListSize;
    private int minAvailablePoolSize;
    private long lockExpireTime;
    private long timerDelay;
    private long timerPeriod;
    private Map<String, Integer> maxDoingListSizeMap;


    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(processorFactory, "ProcessorFactory is null!");
        Assert.notNull(taskExecutor, "TaskExecutor is null!");
        Assert.notNull(commonJedis, "CommonJedis is null!");
        if (maxDoingListSizeMap == null) {
            maxDoingListSizeMap = new HashMap<String, Integer>();
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

            final String doingQueueName = queueName + RedisKey.FS_DOING_LIST_SUFFIX;
            String lockKey = doingQueueName + RedisKey.FS_DOING_LIST_LOCK_SUFFIX;
            boolean lockFlag = getLock(lockKey);
            if (!lockFlag) {
                continue;
            }

            Long size = commonJedis.scard(doingQueueName);
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

            commonJedis.sadd(doingQueueName, fsFileId);
            commonJedis.expire(lockKey, 0);
            LOG.info("Processing fsFile[fsFile:" + fsFileJson + ",queue:" + queueName + "] start!");
            taskExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        FsFile fsFile = gson.fromJson(fsFileJson, FsFile.class);
                        Processor processor = processorFactory.acquireProcessor(fsFile.getProcessor());
                        processor.process(fsFile);
                    } catch (Throwable e) {
                        LOG.error("Error occurred when executing process fsFile[fsFile:" + fsFileJson + "]!", e);
                    } finally {
                        commonJedis.srem(doingQueueName, fsFileId);
                        commonJedis.expire(RedisKey.FS_FILE_CONTENT_PREFIX + fsFileId, 0);
                        LOG.info("Processing fsFile[fsFile:" + fsFileJson + ",queue:" + queueName + "] end!");
                    }
                }
            });
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
            final String doingQueueName = queueName + RedisKey.FS_DOING_LIST_SUFFIX;
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
}
