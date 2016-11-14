package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.utils.FsRedis;
import com.qgutech.fs.utils.RedisKey;
import redis.clients.jedis.JedisCommands;

public class ZipImageProcessor extends AbstractProcessor {

    @Override
    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateZip(fsFile.getSuffix());
    }

    @Override
    protected void submitToRedis(FsFile fsFile) {
        JedisCommands commonJedis = FsRedis.getCommonJedis();
        commonJedis.sadd(RedisKey.FS_QUEUE_NAME_LIST, RedisKey.FS_ZIP_IMAGE_QUEUE_LIST);
        String fsFileId = fsFile.getId();
        //当重复提交时，防止重复处理
        //commonJedis.lrem(RedisKey.FS_ZIP_IMAGE_QUEUE_LIST, 0, fsFileId);
        commonJedis.lpush(RedisKey.FS_ZIP_IMAGE_QUEUE_LIST, fsFileId);
        commonJedis.set(RedisKey.FS_FILE_CONTENT_PREFIX + fsFileId, gson.toJson(fsFile));
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        super.process(fsFile);
    }
}
