package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.utils.*;
import org.apache.commons.io.FileUtils;
import redis.clients.jedis.JedisCommands;

import java.io.File;
import java.io.IOException;

public class AudioProcessor extends AbstractProcessor {

    @Override
    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateAudio(fsFile.getSuffix());
    }

    @Override
    protected void submitToRedis(FsFile fsFile) {
        JedisCommands commonJedis = FsRedis.getCommonJedis();
        commonJedis.sadd(RedisKey.FS_QUEUE_NAME_LIST, RedisKey.FS_AUDIO_QUEUE_LIST);
        String fsFileId = fsFile.getId();
        //当重复提交时，防止处理音频重复
        //commonJedis.lrem(RedisKey.FS_AUDIO_QUEUE_LIST, 0, fsFileId);
        commonJedis.lpush(RedisKey.FS_AUDIO_QUEUE_LIST, fsFileId);
        commonJedis.set(RedisKey.FS_FILE_CONTENT_PREFIX + fsFileId, gson.toJson(fsFile));
    }

    protected boolean needAsync(FsFile fsFile) {
        return !FsConstants.DEFAULT_AUDIO_TYPE.equals(fsFile.getSuffix());
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        String genFilePath = getGenFilePath(fsFile);
        boolean needAsync = needAsync(fsFile);
        try {
            File genFile = new File(genFilePath);
            if (!genFile.exists() && !genFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + genFile.getAbsolutePath() + "] failed!");
            }

            if (needAsync) {
                FsUtils.executeCommand(new String[]{FsConstants.FFMPEG, "-i", fsFile.getTmpFilePath()
                        , "-y", genFilePath + File.separator + FsConstants.DEFAULT_AUDIO_NAME});
            } else {
                File srcFile = new File(fsFile.getTmpFilePath());
                File destFile = new File(genFilePath, FsConstants.DEFAULT_AUDIO_NAME);
                FileUtils.copyFile(srcFile, destFile);
            }

            Audio audio = FsUtils.getAudio(fsFile.getTmpFilePath());
            fsFile.setDurations(audio.getDuration());

            afterProcess(fsFile);
        } catch (Exception e) {
            if (needAsync) {
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                fsFile.setCreateTime(null);
                deleteFile(genFilePath);
                updateFsFile(fsFile);
            }

            throw e;
        } finally {
            deleteFile(new File(fsFile.getTmpFilePath()).getParentFile());
        }
    }
}
