package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.utils.*;

import java.io.File;
import java.io.IOException;

public class ZipDocProcessor extends AbstractDocProcessor {

    @Override
    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateZip(fsFile.getSuffix());
    }

    @Override
    protected void process(FsFile fsFile, File tmpDirFile) throws Exception {
        if (!decompress(fsFile, new Validate() {
            @Override
            public boolean validate(String extension) {
                return validateDoc(extension);
            }
        })) {
            LOG.error("Document collection[" + fsFile.getTmpFilePath()
                    + "] is empty or contains directory or contains not document file!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("文档集为空或者包含文件夹或者包含非文档文件");
            HttpUtils.updateFsFile(fsFile);
            return;
        }

        File decompressDir = new File(tmpDirFile, FsConstants.DECOMPRESS);
        File[] docFiles = decompressDir.listFiles();
        if (docFiles == null || docFiles.length == 0) {
            LOG.error("Document collection[" + fsFile.getTmpFilePath() + "] is empty!");
            fsFile.setProcessMsg("文档集为空");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            HttpUtils.updateFsFile(fsFile);
            return;
        }

        String genFilePath = getGenFilePath(fsFile);
        File genFile = new File(genFilePath);
        FsUtils.deleteFile(genFilePath);
        if (!genFile.exists() && !genFile.mkdirs() && !genFile.exists()) {
            throw new IOException("Creating directory[path:" + genFilePath + "] failed!");
        }

        File imageTmpDirFile = new File(tmpDirFile, FsUtils.generateUUID() + FsConstants.FILE_DIR_IMG);
        if (!imageTmpDirFile.exists() && !imageTmpDirFile.mkdirs() && !imageTmpDirFile.exists()) {
            throw new IOException("Creating directory[path:" + imageTmpDirFile.getAbsolutePath() + "] failed!");
        }

        String pdfTmpDirPath = tmpDirFile.getAbsolutePath() + File.separator;
        StringBuilder subFileCounts = new StringBuilder();
        for (int i = 0; i < docFiles.length; i++) {
            File docFile = docFiles[i];
            String srcFilePath = docFile.getAbsolutePath();
            File nextImageTmpDirFile = new File(imageTmpDirFile, (i + 1) + "");
            String nextImageTmpDirPath = nextImageTmpDirFile.getAbsolutePath();
            if (!nextImageTmpDirFile.exists() && !nextImageTmpDirFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + nextImageTmpDirPath + "] failed!");
            }

            File nextGenFileDirFile = new File(genFilePath, (i + 1) + "");
            String nextGenFileDirPath = nextGenFileDirFile.getAbsolutePath();
            if (!nextGenFileDirFile.exists() && !nextGenFileDirFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + nextGenFileDirPath + "] failed!");
            }

            int docPage = processDoc(srcFilePath, nextImageTmpDirPath
                    , pdfTmpDirPath, nextGenFileDirPath);
            if (docPage <= 0) {
                FsUtils.deleteFile(getGenFilePath(fsFile));
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                HttpUtils.updateFsFile(fsFile);
                return;
            }

            subFileCounts.append(docPage).append(FsConstants.VERTICAL_LINE);
        }

        fsFile.setSubFileCount(docFiles.length);
        fsFile.setSubFileCounts(subFileCounts.delete(subFileCounts.length() - 1
                , subFileCounts.length()).toString());
        afterProcess(fsFile);
    }

    @Override
    protected String getProcessQueueName() {
        return RedisKey.FS_ZIP_DOC_QUEUE_LIST;
    }
}
