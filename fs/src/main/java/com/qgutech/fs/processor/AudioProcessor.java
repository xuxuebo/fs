package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.utils.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class AudioProcessor extends AbstractProcessor {

    @Override
    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateAudio(fsFile.getSuffix());
    }

    protected boolean needAsync(FsFile fsFile) {
        return !FsConstants.DEFAULT_AUDIO_TYPE.equals(fsFile.getSuffix());
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        String genFilePath = getGenFilePath(fsFile);
        boolean needAsync = needAsync(fsFile);
        String tmpFilePath = fsFile.getTmpFilePath();
        try {
            File genFile = new File(genFilePath);
            if (!genFile.exists() && !genFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + genFile.getAbsolutePath() + "] failed!");
            }

            if (needAsync) {
                FsUtils.executeCommand(new String[]{FsConstants.FFMPEG, "-i", tmpFilePath
                        , "-y", genFilePath + File.separator + FsConstants.DEFAULT_AUDIO_NAME});
            } else {
                File srcFile = new File(tmpFilePath);
                File destFile = new File(genFilePath, FsConstants.DEFAULT_AUDIO_NAME);
                FileUtils.copyFile(srcFile, destFile);
            }

            Audio audio = FsUtils.getAudio(tmpFilePath);
            fsFile.setDurations(audio.getDuration());

            afterProcess(fsFile);
        } catch (Exception e) {
            if (needAsync) {
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                fsFile.setCreateTime(null);
                deleteFile(genFilePath);
                HttpUtils.updateFsFile(fsFile);
            }

            throw e;
        } finally {
            deleteFile(new File(tmpFilePath).getParentFile());
        }
    }

    @Override
    protected String getProcessQueueName() {
        return RedisKey.FS_AUDIO_QUEUE_LIST;
    }
}
