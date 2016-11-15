package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.utils.*;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.JedisCommands;

import java.io.File;
import java.io.IOException;

public class ZipDocProcessor extends AbstractDocProcessor {

    @Override
    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateZip(fsFile.getSuffix());
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        String tmpFilePath = fsFile.getTmpFilePath();
        File parentFile = new File(tmpFilePath).getParentFile();
        try {
            if (!decompress(fsFile, new Validate() {
                @Override
                public boolean validate(String extension) {
                    return validateDoc(extension);
                }
            })) {
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                updateFsFile(fsFile);
                return;
            }

            File decompressDir = new File(parentFile, FsConstants.DECOMPRESS);
            File[] docFiles = decompressDir.listFiles();
            if (docFiles == null || docFiles.length == 0) {
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                updateFsFile(fsFile);
                return;
            }

            final String genFilePath = getGenFilePath(fsFile);
            File genFile = new File(genFilePath);
            if (!genFile.exists() && !genFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + genFilePath + "] failed!");
            }

            File imageTmpDirFile = new File(parentFile, FsUtils.generateUUID() + FsConstants.FILE_DIR_IMG);
            if (!imageTmpDirFile.exists() && !imageTmpDirFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + imageTmpDirFile.getAbsolutePath() + "] failed!");
            }

            String pdfTmpDirPath = parentFile.getAbsolutePath() + File.separator;
            StringBuilder subFileCounts = new StringBuilder();
            for (int i = 0; i < docFiles.length; i++) {
                File docFile = docFiles[i];
                String srcFilePath = docFile.getAbsolutePath();
                File nextImageTmpDirFile = new File(imageTmpDirFile, (i + 1) + "");
                String nextImageTmpDirPath = nextImageTmpDirFile.getAbsolutePath() + File.separator;
                if (!nextImageTmpDirFile.exists() && !nextImageTmpDirFile.mkdirs()) {
                    throw new IOException("Creating directory[path:" + nextImageTmpDirPath + "] failed!");
                }

                File nextGenFileDirFile = new File(genFilePath, (i + 1) + "");
                String nextGenFileDirPath = nextGenFileDirFile.getAbsolutePath() + File.separator;
                if (!nextGenFileDirFile.exists() && !nextGenFileDirFile.mkdirs()) {
                    throw new IOException("Creating directory[path:" + nextGenFileDirPath + "] failed!");
                }

                int docPage = processDoc(srcFilePath, nextImageTmpDirPath
                        , pdfTmpDirPath, nextGenFileDirPath);
                if (docPage <= 0) {
                    deleteFile(getGenFilePath(fsFile));
                    fsFile.setStatus(ProcessStatusEnum.FAILED);
                    updateFsFile(fsFile);
                    return;
                }

                subFileCounts.append(docPage).append(FsConstants.VERTICAL_LINE);
            }

            fsFile.setSubFileCount(docFiles.length);
            fsFile.setSubFileCounts(subFileCounts.delete(subFileCounts.length() - 1
                    , subFileCounts.length()).toString());
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

    @Override
    protected String getProcessQueueName() {
        return RedisKey.FS_ZIP_DOC_QUEUE_LIST;
    }
}
