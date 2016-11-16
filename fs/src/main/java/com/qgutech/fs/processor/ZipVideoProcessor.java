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

public class ZipVideoProcessor extends AbstractProcessor {

    @Override
    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateZip(fsFile.getSuffix());
    }

    @Override
    protected String getProcessQueueName() {
        return RedisKey.FS_ZIP_VIDEO_QUEUE_LIST;
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        String tmpFilePath = fsFile.getTmpFilePath();
        File parentFile = new File(tmpFilePath).getParentFile();
        try {
            if (!decompress(fsFile, new Validate() {
                @Override
                public boolean validate(String extension) {
                    return validateVideo(extension);
                }
            })) {
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                updateFsFile(fsFile);
                return;
            }

            File decompressDir = new File(parentFile, FsConstants.DECOMPRESS);
            File[] videoFiles = decompressDir.listFiles();
            if (videoFiles == null || videoFiles.length == 0) {
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
            StringBuilder videoLevels = new StringBuilder();
            List<Future<String>> futures = new ArrayList<Future<String>>();
            final Semaphore semaphore = new Semaphore(getSemaphoreCnt());
            for (int i = 0; i < videoFiles.length; i++) {
                final int index = i + 1;
                File pFile = new File(genFilePath + File.separator + index);
                if (!pFile.exists() && !pFile.mkdirs()) {
                    throw new IOException("Creating directory[path:" + pFile.getAbsolutePath() + "] failed!");
                }

                File videoFile = videoFiles[i];
                final String videoPath = videoFile.getAbsolutePath();
                final Video video = FsUtils.getVideo(videoPath);
                durations.append(video.getDuration()).append(FsConstants.VERTICAL_LINE);
                VideoTypeEnum videoType = VideoTypeEnum.getVideoTypeEnum(video.getBitRate());
                videoLevels.append(videoType.name()).append(FsConstants.VERTICAL_LINE);
                VideoTypeEnum[] videoTypeEnums = VideoTypeEnum.getVideoTypeEnums(videoType);
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
                                File parentFile = new File(genFilePath + File.separator + index
                                        + File.separator + vt.name().toLowerCase());
                                if (!parentFile.exists() && !parentFile.mkdirs()) {
                                    throw new IOException("Creating directory[path:"
                                            + parentFile.getAbsolutePath() + "] failed!");
                                }

                                String destFilePath = genFilePath + File.separator + index
                                        + File.separator + vt.name().toLowerCase()
                                        + File.separator + vt.name().toLowerCase()
                                        + FsConstants.DEFAULT_VIDEO_SUFFIX;
                                return FsUtils.executeCommand(new String[]{FsConstants.FFMPEG, "-i"
                                        , videoPath, "-b:v", bitRate, "-s", resolution
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
                                    , videoPath, "-ss", cutImageTime + "", "-y"
                                    , genFilePath + File.separator + index
                                    + File.separator + FsConstants.VIDEO_COVER});
                        } finally {
                            semaphore.release();
                        }
                    }
                }));
            }

            getFutures(futures);
            fsFile.setDurations(durations.substring(0, durations.length() - 1));
            fsFile.setVideoLevels(videoLevels.substring(0, videoLevels.length() - 1));
            afterProcess(fsFile);
        } catch (Exception e) {
            deleteFile(getGenFilePath(fsFile));
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            updateFsFile(fsFile);

            throw e;
        } finally {
            deleteFile(parentFile);
        }
    }
}
