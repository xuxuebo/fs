package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.domain.VideoTypeEnum;
import com.qgutech.fs.utils.*;
import redis.clients.jedis.JedisCommands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class VideoProcessor extends AbstractProcessor {

    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateVideo(fsFile.getSuffix());
    }

    @Override
    protected void submitToRedis(FsFile fsFile) {
        JedisCommands commonJedis = FsRedis.getCommonJedis();
        commonJedis.sadd(RedisKey.FS_QUEUE_NAME_LIST, RedisKey.FS_VIDEO_QUEUE_LIST);
        String fsFileId = fsFile.getId();
        //当重复提交时，防止处理音频重复
        //commonJedis.lrem(RedisKey.FS_VIDEO_QUEUE_LIST, 0, fsFileId);
        commonJedis.lpush(RedisKey.FS_VIDEO_QUEUE_LIST, fsFileId);
        commonJedis.set(RedisKey.FS_FILE_CONTENT_PREFIX + fsFileId, gson.toJson(fsFile));
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        final String genFilePath = getGenFilePath(fsFile);
        final String tmpFilePath = fsFile.getTmpFilePath();
        try {
            File genFile = new File(genFilePath);
            if (!genFile.exists() && !genFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + genFile.getAbsolutePath() + "] failed!");
            }

            final Video video = FsUtils.getVideo(tmpFilePath);
            VideoTypeEnum videoType = VideoTypeEnum.getVideoTypeEnum(video.getBitRate());
            VideoTypeEnum[] videoTypeEnums = VideoTypeEnum.getVideoTypeEnums(videoType);
            List<Future<String>> futures = new ArrayList<Future<String>>(videoTypeEnums.length + 1);
            for (VideoTypeEnum videoTypeEnum : videoTypeEnums) {
                final VideoTypeEnum vt = videoTypeEnum;
                futures.add(taskExecutor.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        List<String> commands = new ArrayList<String>(11);
                        commands.add(FsConstants.FFMPEG);
                        commands.add("-i");
                        commands.add(tmpFilePath);
                        commands.add("-b:v");
                        if (VideoTypeEnum.O.equals(vt)) {
                            commands.add(video.getBitRate() + "k");
                        } else {
                            commands.add(vt.getBitRate() + "k");
                        }

                        commands.add("-s");
                        if (VideoTypeEnum.O.equals(vt)) {
                            commands.add(video.getResolution());
                        } else {
                            commands.add(vt.getResolution());
                        }

                        commands.add("-hls_list_size");
                        commands.add("0");
                        commands.add("-y");
                        commands.add(genFilePath + File.separator + vt.name().toLowerCase()
                                + File.separator + vt.name().toLowerCase()
                                + FsConstants.DEFAULT_VIDEO_SUFFIX);
                        return FsUtils.executeCommand(commands.toArray(new String[commands.size()]));
                    }
                }));

            }

            futures.add(taskExecutor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    long duration = FsUtils.parseStringTimeToLong(video.getDuration());
                    int cutImageTime = PropertiesUtils.getCutImageTime();
                    if (cutImageTime * 1000 > duration) {
                        cutImageTime = 0;
                    }

                    return FsUtils.executeCommand(new String[]{FsConstants.FFMPEG, "-i"
                            , tmpFilePath, "-ss", cutImageTime + "", "-y"
                            , genFilePath + File.separator + FsConstants.VIDEO_COVER});
                }
            }));

            try {
                for (Future<String> future : futures) {
                    future.get();
                }
            } catch (Exception e) {
                for (Future<String> future : futures) {
                    future.cancel(true);
                }

                throw e;
            }

            fsFile.setVideoLevels(videoType.name());
            fsFile.setDurations(video.getDuration());
            afterProcess(fsFile);
        } catch (Exception e) {
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setCreateTime(null);
            deleteFile(genFilePath);
            updateFsFile(fsFile);

            throw e;
        } finally {
            deleteFile(new File(tmpFilePath).getParentFile());
        }
    }

}
