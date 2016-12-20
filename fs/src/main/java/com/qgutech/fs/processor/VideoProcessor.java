package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.domain.VideoTypeEnum;
import com.qgutech.fs.utils.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public class VideoProcessor extends AbstractProcessor {

    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateVideo(fsFile.getSuffix());
    }

    @Override
    protected String getProcessQueueName() {
        return RedisKey.FS_VIDEO_QUEUE_LIST;
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        final String genFilePath = getGenFilePath(fsFile);
        final String tmpFilePath = fsFile.getTmpFilePath();
        try {
            File genFile = new File(genFilePath);
            FsUtils.deleteFile(genFilePath);
            if (!genFile.exists() && !genFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + genFile.getAbsolutePath() + "] failed!");
            }

            final Video video = FsUtils.getVideo(tmpFilePath);
            VideoTypeEnum videoType = VideoTypeEnum.getVideoTypeEnum(video.getBitRate());
            VideoTypeEnum[] videoTypeEnums = VideoTypeEnum.getVideoTypeEnums(videoType);
            List<Future<String>> futures = new ArrayList<Future<String>>(videoTypeEnums.length + 1);
            final Semaphore semaphore = new Semaphore(getSemaphoreCnt());
            for (VideoTypeEnum videoTypeEnum : videoTypeEnums) {
                final VideoTypeEnum vt = videoTypeEnum;
                semaphore.acquire();
                futures.add(taskExecutor.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        try {
                            String bitRate = (VideoTypeEnum.O.equals(vt) ? video.getBitRate()
                                    : vt.getBitRate()) + "k";
                            String resolution = VideoTypeEnum.O.equals(vt) ? video.getResolution()
                                    : vt.getResolution();
                            File parentFile = new File(genFilePath + File.separator + vt.name().toLowerCase());
                            if (!parentFile.exists() && !parentFile.mkdirs()) {
                                throw new IOException("Creating directory[path:"
                                        + parentFile.getAbsolutePath() + "] failed!");
                            }

                            String destFilePath = genFilePath + File.separator + vt.name().toLowerCase()
                                    + File.separator + vt.name().toLowerCase()
                                    + FsConstants.DEFAULT_VIDEO_SUFFIX;
                            return FsUtils.executeCommand(new String[]{FsConstants.FFMPEG, "-i"
                                    , tmpFilePath, "-b:v", bitRate, "-s", resolution
                                    , "-hls_list_size", "0", "-y", destFilePath});
                        } finally {
                            semaphore.release();
                        }
                    }
                }));
            }

            semaphore.acquire();
            futures.add(taskExecutor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    try {
                        long duration = FsUtils.parseStringTimeToLong(video.getDuration());
                        int cutImageTime = PropertiesUtils.getCutImageTime();
                        if (cutImageTime * 1000 > duration) {
                            cutImageTime = 0;
                        }

                        return FsUtils.executeCommand(new String[]{FsConstants.FFMPEG, "-i"
                                , tmpFilePath, "-ss", cutImageTime + "", "-y"
                                , genFilePath + File.separator + FsConstants.VIDEO_COVER});
                    } finally {
                        semaphore.release();
                    }
                }
            }));

            getFutures(futures);
            fsFile.setVideoLevels(videoType.name());
            fsFile.setDurations(video.getDuration());
            afterProcess(fsFile);
        } catch (Throwable e) {
            FsUtils.deleteFile(genFilePath);
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg(e.getMessage());
            HttpUtils.updateFsFile(fsFile);

            throw new Exception(e);
        } finally {
            FsUtils.deleteFile(new File(tmpFilePath).getParentFile());
        }
    }
}
