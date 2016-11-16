package com.qgutech.fs.processor;

import com.qgutech.fs.utils.PropertiesUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import redis.clients.jedis.JedisCommands;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ProcessorExecutor extends TimerTask {

    private static final Log LOG = LogFactory.getLog(ProcessorExecutor.class);

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
    public void run() {

    }


    public void init() {
        if (!PropertiesUtils.isConvert()) {
            return;
        }

     /*   Set<String> queueNames = jedisXfsPool.smembers(XfsConstant.REDIS_KEY_QUEUE_NAME_LIST);
        if (CollectionUtils.isEmpty(queueNames)) {
            start();
            return;
        }

        for (String queueName : queueNames) {
            final String doingQueueName = queueName + XfsConstant.REDIS_KEY_DOING_LIST_SUFFIX;
            Long doingCount = jedisXfsPool.scard(doingQueueName);
            if (doingCount == 0l) {
                continue;
            }

            String lockKey = doingQueueName + XfsConstant.REDIS_KEY_DOING_LIST_LOCK_SUFFIX;
            boolean lockFlag = getLock(lockKey);
            if (!lockFlag) {
                continue;
            }

            Set<String> doingReportIds = jedisXfsPool.smembers(doingQueueName);
            LOG.info("Doing reportId list " + doingReportIds);
            for (String doingReportId : doingReportIds) {
                jedisXfsPool.rpush(queueName, doingReportId);
                jedisXfsPool.srem(doingQueueName, doingReportId);
                LOG.info("Jedis remove reportId[" + doingReportId + "]");
            }

            jedisXfsPool.expire(lockKey, 0);
        }*/

        start();
    }

    private void start() {
        Timer timer = new Timer();
        timer.schedule(this, timerDelay, timerPeriod);
    }

    protected boolean getLock(String key, int expireTime) {
        Long lockFlag = commonJedis.setnx(key, System.currentTimeMillis() + "");
        if (lockFlag != null && lockFlag == 1l) {
            commonJedis.expire(key, expireTime);
            return true;
        }

        String millSeconds = commonJedis.get(key);
        if (millSeconds == null) {
            lockFlag = commonJedis.setnx(key, System.currentTimeMillis() + "");
            if (lockFlag != null && lockFlag == 1l) {
                commonJedis.expire(key, expireTime);
                return true;
            } else {
                return false;
            }
        }

        long currentMillSeconds = System.currentTimeMillis();
        if (currentMillSeconds - Long.valueOf(millSeconds) >= expireTime) {
            String redisLockTime = commonJedis.getSet(key, currentMillSeconds + "");
            if (redisLockTime == null || redisLockTime.equals(millSeconds)) {
                commonJedis.expire(key, expireTime);
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
