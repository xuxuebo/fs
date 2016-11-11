package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.FsUtils;
import com.qgutech.fs.utils.PropertiesUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        String tmpFilePath = fsFile.getTmpFilePath();
        if (validateAudio(FilenameUtils.getExtension(tmpFilePath))) {
            return true;
        }

        File parentFile = new File(tmpFilePath).getParentFile();
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

        doTransfer(fsFile);
        return false;
    }

    protected void doTransfer(FsFile fsFile) {
        String genFilePath = getGenFilePath(fsFile);
        File parentFile = new File(fsFile.getTmpFilePath()).getParentFile();
        try {
            File genFile = new File(genFilePath);
            if (!genFile.exists() && !genFile.mkdirs()) {
                throw new IOException("Creating directory[path:"
                        + genFile.getAbsolutePath() + "] failed!");
            }

            File decompressDir = new File(parentFile, FsConstants.DECOMPRESS);
            File[] files = decompressDir.listFiles();
            if (files == null || files.length == 0) {
                return;
            }

            for (int i = 0; i < files.length; i++) {
                File srcFile = files[i];
                File parentDir = new File(genFilePath, (i + 1) + "");
                if (!parentDir.exists() && !parentDir.mkdirs()) {
                    throw new IOException("Creating directory[path:"
                            + parentDir.getAbsolutePath() + "] failed!");
                }

                File destFile = new File(parentDir, FsConstants.DEFAULT_AUDIO_NAME);
                FileUtils.copyFile(srcFile, destFile);
            }

            fsFile.setStatus(ProcessStatusEnum.SUCCESS);
            updateFsFile(fsFile);//todo 需要保存在redis中
        } catch (Exception e) {
            deleteFile(genFilePath);
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setCreateTime(null);
            deleteFsFile(fsFile.getId());//todo delete originFile

            throw new RuntimeException(e);
        } finally {
            deleteFile(parentFile);
        }
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
        String fsFileId = fsFile.getId();
        int indexOf = fsFileId.indexOf(FsConstants.UNDERLINE);
        if (indexOf < 0) {
            return;
        }

        fsFileId = fsFileId.substring(0, indexOf);
        fsFile.setId(fsFileId);
        String genFilePath = getGenFilePath(fsFile) + File.separator + fsFileId.substring(indexOf + 1);
        File genFile = new File(genFilePath);
        if (!genFile.exists() && !genFile.mkdirs()) {
            throw new IOException("Creating directory[path:"
                    + genFile.getAbsolutePath() + "] failed!");
        }

        List<String> commands = new ArrayList<String>(5);
        commands.add("ffmpeg");
        commands.add("-i");
        commands.add(fsFile.getTmpFilePath());
        commands.add("-y");
        commands.add(genFilePath + File.separator + FsConstants.DEFAULT_AUDIO_NAME);
        FsUtils.executeCommand(commands.toArray(new String[commands.size()]));
    }
}
