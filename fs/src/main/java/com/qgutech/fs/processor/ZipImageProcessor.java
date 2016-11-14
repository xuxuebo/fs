package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ImageTypeEnum;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.utils.*;
import redis.clients.jedis.JedisCommands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

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
        String tmpFilePath = fsFile.getTmpFilePath();
        File parentFile = new File(tmpFilePath).getParentFile();
        try {
            if (!decompress(fsFile, new Validate() {
                @Override
                public boolean validate(String extension) {
                    return validateImage(extension);
                }
            })) {
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                updateFsFile(fsFile);
                return;
            }

            File decompressDir = new File(parentFile, FsConstants.DECOMPRESS);
            File[] imageFiles = decompressDir.listFiles();
            if (imageFiles == null || imageFiles.length == 0) {
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                updateFsFile(fsFile);
                return;
            }

            final String genFilePath = getGenFilePath(fsFile);
            File genFile = new File(genFilePath);
            if (!genFile.exists() && !genFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + genFile.getAbsolutePath() + "] failed!");
            }

            List<Future<String>> futures = new ArrayList<Future<String>>(imageFiles.length);
            final Semaphore semaphore = new Semaphore(getSemaphoreCnt());
            for (int i = 0; i < imageFiles.length; i++) {
                final int index = i + 1;
                final String imagePath = imageFiles[i].getAbsolutePath();
                semaphore.acquire();
                futures.add(taskExecutor.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        try {
                            String orgResolution = FsUtils.getImageResolution(imagePath);
                            int indexOf = orgResolution.indexOf("x");
                            int width = Integer.parseInt(orgResolution.substring(0, indexOf));
                            int height = Integer.parseInt(orgResolution.substring(indexOf + 1));
                            for (ImageTypeEnum value : ImageTypeEnum.values()) {
                                int w = value.getW();
                                int h = value.getH();
                                String resolution;
                                if (w > 0 && h > 0) {
                                    if (w > width) {
                                        w = width;
                                    }

                                    if (h > height) {
                                        h = height;
                                    }

                                    resolution = w + "*" + h;
                                } else {
                                    resolution = orgResolution;
                                }

                                String destFilePath = genFilePath + File.separator + index
                                        + value.name().toLowerCase() + FsConstants.DEFAULT_IMAGE_SUFFIX;
                                FsUtils.executeCommand(new String[]{FsConstants.FFMPEG, "-i", imagePath
                                        , "-s", resolution, "-y", destFilePath});
                            }

                            return null;
                        } finally {
                            semaphore.release();
                        }
                    }
                }));
            }

            getFutures(futures);
            afterProcess(fsFile);
        } catch (Exception e) {
            deleteFile(getGenFilePath(fsFile));
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            updateFsFile(fsFile);

            throw e;
        } finally {
            JedisCommands commonJedis = FsRedis.getCommonJedis();
            commonJedis.expire(RedisKey.FS_FILE_CONTENT_PREFIX + fsFile.getId(), 0);
            commonJedis.srem(RedisKey.FS_ZIP_IMAGE_QUEUE_LIST, fsFile.getId());
            deleteFile(parentFile);
        }
    }
}
