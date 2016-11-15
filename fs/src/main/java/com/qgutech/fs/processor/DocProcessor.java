package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.utils.*;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.JedisCommands;

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
    public void process(FsFile fsFile) throws Exception {
        String tmpFilePath = fsFile.getTmpFilePath();
        File parentFile = new File(tmpFilePath).getParentFile();
        try {
            String genFilePath = getGenFilePath(fsFile);
            File genFile = new File(genFilePath);
            if (!genFile.exists() && !genFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + genFilePath + "] failed!");
            }

            File imageTmpDirFile = new File(parentFile, FsUtils.generateUUID() + FsConstants.FILE_DIR_IMG);
            String imageTmpDirPath = imageTmpDirFile.getAbsolutePath() + File.separator;
            if (!imageTmpDirFile.exists() && !imageTmpDirFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + imageTmpDirPath + "] failed!");
            }

            String pdfTmpDirPath = parentFile.getAbsolutePath() + File.separator;
            int docPage = processDoc(tmpFilePath, imageTmpDirPath, pdfTmpDirPath, genFilePath);
            if (docPage <= 0) {
                deleteFile(getGenFilePath(fsFile));
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                updateFsFile(fsFile);
                return;
            }

            fsFile.setSubFileCount(docPage);
            afterProcess(fsFile);
        } catch (Exception e) {
            deleteFile(getGenFilePath(fsFile));
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            updateFsFile(fsFile);

            throw e;
        } finally {
            JedisCommands commonJedis = FsRedis.getCommonJedis();
            commonJedis.expire(RedisKey.FS_FILE_CONTENT_PREFIX + fsFile.getId(), 0);
            commonJedis.srem(getProcessQueueName() + RedisKey.FS_DOING_LIST_SUFFIX, fsFile.getId());
            deleteFile(parentFile);
            if (StringUtils.isNotEmpty(fsFile.getBackUrl())) {
                deleteFile(getGenFilePath(fsFile));
                deleteFile(getOriginFilePath(fsFile));
            }
        }
    }
}
