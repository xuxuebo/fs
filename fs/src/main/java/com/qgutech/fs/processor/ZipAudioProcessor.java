package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.utils.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import redis.clients.jedis.JedisCommands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public class ZipAudioProcessor extends AbstractProcessor {
    @Override
    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateZip(fsFile.getSuffix());
    }

    @Override
    protected void submitToRedis(FsFile fsFile) {
        JedisCommands commonJedis = FsRedis.getCommonJedis();
        commonJedis.sadd(RedisKey.FS_QUEUE_NAME_LIST, RedisKey.FS_ZIP_AUDIO_QUEUE_LIST);
        String fsFileId = fsFile.getId();
        //当重复提交时，防止重复处理
        //commonJedis.lrem(RedisKey.FS_ZIP_AUDIO_QUEUE_LIST, 0, fsFileId);
        commonJedis.lpush(RedisKey.FS_ZIP_AUDIO_QUEUE_LIST, fsFileId);
        commonJedis.set(RedisKey.FS_FILE_CONTENT_PREFIX + fsFileId, gson.toJson(fsFile));
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        String tmpFilePath = fsFile.getTmpFilePath();
        File parentFile = new File(tmpFilePath).getParentFile();
        try {
            if (!decompress(fsFile, new Validate() {
                @Override
                public boolean validate(String extension) {
                    return validateAudio(extension);
                }
            })) {
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                updateFsFile(fsFile);
                return;
            }

            File decompressDir = new File(parentFile, FsConstants.DECOMPRESS);
            File[] audioFiles = decompressDir.listFiles();
            if (audioFiles == null || audioFiles.length == 0) {
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                updateFsFile(fsFile);
                return;
            }

            final String genFilePath = getGenFilePath(fsFile);
            File genFile = new File(genFilePath);
            if (!genFile.exists() && !genFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + genFile.getAbsolutePath() + "] failed!");
            }

            StringBuilder durations = new StringBuilder();
            List<Future<String>> futures = new ArrayList<Future<String>>();
            final Semaphore semaphore = new Semaphore(getSemaphoreCnt());
            for (int i = 0; i < audioFiles.length; i++) {
                final int index = i + 1;
                File pFile = new File(genFilePath + File.separator + index);
                if (!pFile.exists() && !pFile.mkdirs()) {
                    throw new IOException("Creating directory[path:" + pFile.getAbsolutePath() + "] failed!");
                }

                File audioFile = audioFiles[i];
                final String audioPath = audioFile.getAbsolutePath();
                Audio audio = FsUtils.getAudio(audioPath);
                durations.append(audio.getDuration()).append(FsConstants.VERTICAL_LINE);
                String extension = FilenameUtils.getExtension(audioPath);
                if (!FsConstants.DEFAULT_AUDIO_TYPE.equals(extension)) {
                    semaphore.acquire();
                    futures.add(taskExecutor.submit(new Callable<String>() {
                        @Override
                        public String call() throws Exception {
                            try {
                                return FsUtils.executeCommand(new String[]{FsConstants.FFMPEG, "-i", audioPath
                                        , "-y", genFilePath + File.separator + index
                                        + File.separator + FsConstants.DEFAULT_AUDIO_NAME});
                            } finally {
                                semaphore.release();
                            }
                        }
                    }));
                } else {
                    File destFile = new File(pFile, FsConstants.DEFAULT_AUDIO_NAME);
                    FileUtils.copyFile(audioFile, destFile);
                }
            }

            getFutures(futures);
            fsFile.setDurations(durations.substring(0, durations.length() - 1));
            afterProcess(fsFile);
        } catch (Exception e) {
            deleteFile(getGenFilePath(fsFile));
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            updateFsFile(fsFile);

            throw e;
        } finally {
            JedisCommands commonJedis = FsRedis.getCommonJedis();
            commonJedis.expire(RedisKey.FS_FILE_CONTENT_PREFIX + fsFile.getId(), 0);
            commonJedis.srem(RedisKey.FS_ZIP_AUDIO_QUEUE_LIST + RedisKey.FS_DOING_LIST_SUFFIX, fsFile.getId());
            deleteFile(parentFile);
        }
    }
}
