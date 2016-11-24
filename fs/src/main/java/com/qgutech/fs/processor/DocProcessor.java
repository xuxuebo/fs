package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.utils.*;

import java.io.File;
import java.io.IOException;

public class DocProcessor extends AbstractDocProcessor {

    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateDoc(fsFile.getSuffix());
    }

    @Override
    protected String getProcessQueueName() {
        return RedisKey.FS_DOC_QUEUE_LIST;
    }

    @Override
    protected void process(FsFile fsFile, File tmpDirFile) throws Exception {
        String genFilePath = getGenFilePath(fsFile);
        File genFile = new File(genFilePath);
        if (!genFile.exists() && !genFile.mkdirs()) {
            throw new IOException("Creating directory[path:" + genFilePath + "] failed!");
        }

        File imageTmpDirFile = new File(tmpDirFile, FsUtils.generateUUID() + FsConstants.FILE_DIR_IMG);
        String imageTmpDirPath = imageTmpDirFile.getAbsolutePath() + File.separator;
        if (!imageTmpDirFile.exists() && !imageTmpDirFile.mkdirs()) {
            throw new IOException("Creating directory[path:" + imageTmpDirPath + "] failed!");
        }

        String pdfTmpDirPath = tmpDirFile.getAbsolutePath() + File.separator;
        int docPage = processDoc(fsFile.getTmpFilePath(), imageTmpDirPath, pdfTmpDirPath, genFilePath);
        if (docPage <= 0) {
            FsUtils.deleteFile(getGenFilePath(fsFile));
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            HttpUtils.updateFsFile(fsFile);
        } else {
            fsFile.setSubFileCount(docPage);
            afterProcess(fsFile);
        }
    }
}
