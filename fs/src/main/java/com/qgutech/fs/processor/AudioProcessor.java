package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.utils.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.JedisCommands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioProcessor extends AbstractProcessor {

    @Override
    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateAudio(fsFile.getSuffix());
    }

    @Override
    protected void submitToRedis(FsFile fsFile) {
        JedisCommands commonJedis = FsRedis.getCommonJedis();
        commonJedis.sadd(RedisKey.FS_QUEUE_NAME_LIST, RedisKey.FS_AUDIO_QUEUE_LIST);
        commonJedis.lpush(RedisKey.FS_AUDIO_QUEUE_LIST, fsFile.getId());
        commonJedis.set(RedisKey.FS_FILE_CONTENT_PREFIX + fsFile.getId(), gson.toJson(fsFile));
    }

    protected boolean needAsync(FsFile fsFile) {
        return !FsConstants.DEFAULT_AUDIO_TYPE.equals(fsFile.getSuffix());
    }

    @Override
    protected String getGenFilePath(FsFile fsFile) {
        return PropertiesUtils.getFileStoreDir() + fsFile.getCorpCode()
                + File.separator + fsFile.getAppCode()
                + File.separator + FsConstants.FILE_DIR_GEN
                + File.separator + FsConstants.DEFAULT_AUDIO_TYPE
                + File.separator + FsUtils.formatDateToYYMM(fsFile.getCreateTime())
                + File.separator + fsFile.getId();
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        String genFilePath = getGenFilePath(fsFile);
        boolean async = needAsync(fsFile);
        try {
            File genFile = new File(genFilePath);
            if (!genFile.exists() && !genFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + genFile.getAbsolutePath() + "] failed!");
            }

            if (async) {
                List<String> commands = new ArrayList<String>(5);
                commands.add("ffmpeg");
                commands.add("-i");
                commands.add(fsFile.getTmpFilePath());
                commands.add("-y");
                commands.add(genFilePath + File.separator + FsConstants.DEFAULT_AUDIO_NAME);
                FsUtils.executeCommand(commands.toArray(new String[commands.size()]));
            } else {
                File srcFile = new File(fsFile.getTmpFilePath());
                File destFile = new File(genFilePath, FsConstants.DEFAULT_AUDIO_NAME);
                FileUtils.copyFile(srcFile, destFile);
            }

            afterProcess(fsFile);
        } catch (Exception e) {
            deleteFile(genFilePath);
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setCreateTime(null);
            if (async) {
                updateFsFile(fsFile);
            } else {
                deleteFsFile(fsFile.getId());//todo delete originFile
            }

            throw e;
        } finally {
            deleteFile(new File(fsFile.getTmpFilePath()).getParentFile());
        }
    }
}
