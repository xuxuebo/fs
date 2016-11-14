package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.utils.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.JedisCommands;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ZipAudioProcessor extends AbstractProcessor {
    @Override
    protected boolean validateFile(FsFile fsFile) throws Exception {
        boolean valid = validateZip(fsFile.getSuffix());
        if (!valid) {
            return false;
        }

        String tmpFilePath = fsFile.getTmpFilePath();
        File parentFile = new File(tmpFilePath).getParentFile();
        File decompressDir = new File(parentFile, FsConstants.DECOMPRESS);
        FsUtils.decompress(tmpFilePath, decompressDir.getAbsolutePath());
        File[] files = decompressDir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                return false;
            }

            String extension = FilenameUtils.getExtension(file.getName());
            if (StringUtils.isEmpty(extension) || !validateAudio(extension)) {
                return false;
            }
        }

        fsFile.setSubFileCount(files.length);
        return true;
    }

    @Override
    protected boolean needAsync(FsFile fsFile) {
        File parentFile = new File(fsFile.getTmpFilePath()).getParentFile();
        File decompressDir = new File(parentFile, FsConstants.DECOMPRESS);
        File[] files = decompressDir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }

        for (File file : files) {
            String extension = FilenameUtils.getExtension(file.getName());
            if (!FsConstants.DEFAULT_AUDIO_TYPE.equalsIgnoreCase(extension)) {
                return true;
            }
        }

        return false;
    }

    protected boolean submit(FsFile fsFile, int count) throws Exception {
        String genFilePath = getGenFilePath(fsFile);
        File genFile = new File(genFilePath);
        if (!genFile.exists() && !genFile.mkdirs()) {
            throw new IOException("Creating directory[path:" + genFile.getAbsolutePath() + "] failed!");
        }

        File parentFile = new File(fsFile.getTmpFilePath()).getParentFile();
        File decompressDir = new File(parentFile, FsConstants.DECOMPRESS);
        File[] files = decompressDir.listFiles();
        if (files == null || files.length == 0) {
            return true;
        }

        Map<Integer, String> seqTmpFileMap = new HashMap<Integer, String>();
        for (int i = 0; i < files.length; i++) {
            File srcFile = files[i];
            String extension = FilenameUtils.getExtension(srcFile.getName());
            if (!FsConstants.DEFAULT_AUDIO_TYPE.equalsIgnoreCase(extension)) {
                seqTmpFileMap.put(i + 1, srcFile.getAbsolutePath());
                continue;
            }

            File parentDir = new File(genFilePath, (i + 1) + "");
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Creating directory[path:" + parentDir.getAbsolutePath() + "] failed!");
            }

            File destFile = new File(parentDir, FsConstants.DEFAULT_AUDIO_NAME);
            FileUtils.copyFile(srcFile, destFile);
        }

        if (seqTmpFileMap.size() == 0) {
            return true;
        }

        JedisCommands commonJedis = FsRedis.getCommonJedis();
        commonJedis.sadd(RedisKey.FS_QUEUE_NAME_LIST, RedisKey.FS_ZIP_AUDIO_QUEUE_LIST);
        Map<String, String> contentMap = new HashMap<String, String>(seqTmpFileMap.size() + 2);
        contentMap.put(FsConstants.FS_FILE, gson.toJson(fsFile));
        contentMap.put(FsConstants.WAIL_PROCESS_CNT, seqTmpFileMap.size() + StringUtils.EMPTY);
        String fsFileId = fsFile.getId();
        for (Map.Entry<Integer, String> entry : seqTmpFileMap.entrySet()) {
            Integer seq = entry.getKey();
            commonJedis.lpush(RedisKey.FS_ZIP_AUDIO_QUEUE_LIST, fsFileId
                    + FsConstants.UNDERLINE + seq);
            contentMap.put(seq.toString(), entry.getValue());
        }
        commonJedis.hmset(RedisKey.FS_FILE_CONTENT_PREFIX + fsFileId, contentMap);

        return true;
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        String fsFileId = fsFile.getId();
        int indexOf = fsFileId.indexOf(FsConstants.UNDERLINE);
        if (indexOf < 0) {
            submit(fsFile, 0);
            afterProcess(fsFile);
            return;
        }

        fsFileId = fsFileId.substring(0, indexOf);
        fsFile.setId(fsFileId);
        String genFilePath = getGenFilePath(fsFile) + File.separator + fsFileId.substring(indexOf + 1);
        File genFile = new File(genFilePath);
        if (!genFile.exists() && !genFile.mkdirs()) {
            throw new IOException("Creating directory[path:" + genFile.getAbsolutePath() + "] failed!");
        }

        FsUtils.executeCommand(new String[]{FsConstants.FFMPEG, "-i", fsFile.getTmpFilePath()
                , "-y", genFilePath + File.separator + FsConstants.DEFAULT_AUDIO_NAME});
    }
}
