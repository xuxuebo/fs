package com.qgutech.fs.processor;


import com.google.gson.Gson;
import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.domain.ProcessorTypeEnum;
import com.qgutech.fs.utils.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractProcessor implements Processor {

    protected static final int MAX_EXECUTE_CNT = 10;
    protected static final int DEFAULT_WAIT_TIME = 1000;

    protected final Log LOG = LogFactory.getLog(getClass());
    protected final Gson gson = new Gson();

    @Override
    public FsFile beforeProcess(FsFile fsFile) throws Exception {
        if (!validateParams(fsFile)) {
            return fsFile;
        }

        String tmp = FsUtils.generateUUID();
        String tmpDirPath = PropertiesUtils.getFileStoreDir() + FsConstants.FILE_DIR_TMP
                + File.separator + FsConstants.FILE_DIR_IMPT + File.separator + tmp;
        File tmpDir = new File(tmpDirPath);
        if (!tmpDir.exists() && !tmpDir.mkdirs()) {
            throw new IOException("Creating directory[" + tmpDirPath + "] failed!");
        }

        String tmpFilePath = tmpDirPath + File.separator
                + tmp + FsConstants.POINT + fsFile.getSuffix();
        fsFile.setTmpFilePath(tmpFilePath);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        String fsFileId = null;
        String originFilePath = null;
        try {
            saveTmpFile(fsFile);
            if (!validateFile(fsFile)) {
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                return fsFile;
            }

            fsFile.setStatus(ProcessStatusEnum.PROCESSING);
            fsFileId = saveFsFile(fsFile);
            fsFile.setId(fsFileId);
            inputStream = new FileInputStream(tmpFilePath);
            originFilePath = getOriginFilePath(fsFile);
            outputStream = new FileOutputStream(originFilePath);
            IOUtils.copy(inputStream, outputStream);

            if (needAsync(fsFile)) {
                submit(fsFile, 0);
            } else {
                process(fsFile);
            }
        } catch (Exception e) {
            deleteFile(originFilePath);
            deleteFile(tmpDir);
            deleteFsFile(fsFileId);
            throw e;
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }

        return fsFile;
    }

    protected final void deleteFile(String filePath) {
        if (StringUtils.isNotEmpty(filePath)) {
            deleteFile(new File(filePath));
        }
    }

    protected final void deleteFile(File file) {
        try {
            if (file != null && file.exists()) {
                FileUtils.forceDelete(file);
            }
        } catch (Exception e) {
            //not need process
        }
    }

    protected final boolean validateParams(FsFile fsFile) throws Exception {
        if (fsFile == null) {
            return false;
        }

        if (!fsFile.validateUpload()) {
            LOG.error("The fields[appCode:" + fsFile.getAppCode() + ",corpCode:" + fsFile.getCorpCode()
                    + ",businessId:" + fsFile.getBusinessId() + ",processor:" + fsFile.getProcessor()
                    + "] of fsFile must be not empty!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            return false;
        }

        MultipartFile file = fsFile.getFile();
        if (file == null || StringUtils.isEmpty(file.getOriginalFilename())
                || file.getInputStream() == null) {
            LOG.error("Upload file not exist or originalFilename is empty!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            return false;
        }

        String originalFilename = file.getOriginalFilename();
        fsFile.setStoredFileName(originalFilename);
        String extension = FilenameUtils.getExtension(originalFilename);
        if (StringUtils.isEmpty(extension)) {
            LOG.error("Upload file[fileName:" + originalFilename + "]'s extension is empty!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            return false;
        }

        fsFile.setSuffix(extension.toLowerCase());
        fsFile.setFileSize(file.getSize());
        fsFile.setServerCode(PropertiesUtils.getServerCode());
        fsFile.setServerHost(PropertiesUtils.getServerHost());
        fsFile.setCreateTime(new Date());

        return true;
    }

    protected final void saveTmpFile(FsFile fsFile) throws Exception {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = fsFile.getFile().getInputStream();
            outputStream = new FileOutputStream(fsFile.getTmpFilePath());
            IOUtils.copy(inputStream, outputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
            fsFile.setFile(null);
        }
    }

    protected boolean validateFile(FsFile fsFile) throws Exception {
        return true;
    }

    protected String unZip(FsFile fsFile) {
        return null;
    }

    protected boolean submit(FsFile fsFile, int count) {
        try {
            submitToRedis(fsFile);
        } catch (Exception e) {
            try {
                Thread.sleep(DEFAULT_WAIT_TIME);
            } catch (Exception ex) {
                //NP
            }

            if (count < MAX_EXECUTE_CNT) {
                submit(fsFile, ++count);
            } else {
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                return false;
            }
        }

        return true;
    }

    protected void submitToRedis(FsFile fsFile) {

    }

    protected boolean needAsync(FsFile fsFile) {
        return true;
    }

    protected final String deleteFsFile(String fsFileId) {
        if (StringUtils.isEmpty(fsFileId)) {
            return null;
        }

        long timestamp = System.currentTimeMillis();
        String serverHost = PropertiesUtils.getServerHost();
        String sign = Signer.sign(fsFileId, serverHost, PropertiesUtils.getServerSecret(), timestamp);
        Map<String, String> paramMap = new HashMap<String, String>(4);
        paramMap.put(FsFile._id, fsFileId);
        paramMap.put(FsFile._timestamp, timestamp + "");
        paramMap.put(FsFile._sign, sign);
        paramMap.put(FsFile._serverHost, serverHost);

        return HttpUtils.doPost(PropertiesUtils.getDeleteFileUrl(), paramMap);
    }

    protected final String saveFsFile(FsFile fsFile) {
        long timestamp = System.currentTimeMillis();
        fsFile.setTimestamp(timestamp);
        fsFile.setSign(Signer.sign(PropertiesUtils.getServerHost()
                , PropertiesUtils.getServerSecret(), timestamp));
        return HttpUtils.doPost(PropertiesUtils.getSaveFileUrl(), fsFile.toMap());
    }

    protected final String updateFsFile(FsFile fsFile) {
        long timestamp = System.currentTimeMillis();
        fsFile.setTimestamp(timestamp);
        fsFile.setSign(Signer.sign(fsFile.getId(), PropertiesUtils.getServerHost()
                , PropertiesUtils.getServerSecret(), timestamp));
        return HttpUtils.doPost(PropertiesUtils.getUpdateFileUrl(), fsFile.toMap());
    }

    protected String getOriginFilePath(FsFile fsFile) {
        StringBuilder builder = new StringBuilder();
        builder.append(PropertiesUtils.getFileStoreDir()).append(fsFile.getCorpCode())
                .append(File.separator).append(fsFile.getAppCode())
                .append(File.separator).append(FsConstants.FILE_DIR_SRC);
        String businessDir = fsFile.getBusinessDir();
        if (StringUtils.isNotEmpty(businessDir)) {
            builder.append(File.separator).append(businessDir);
        }

        builder.append(File.separator).append(ProcessorTypeEnum.toDirectory(fsFile.getProcessor()))
                .append(File.separator).append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                .append(File.separator).append(fsFile.getBusinessId())
                .append(File.separator).append(fsFile.getId())
                .append(FsConstants.POINT).append(fsFile.getSuffix());

        return builder.toString();
    }

    protected String getGenFilePath(FsFile fsFile) {
        throw new UnsupportedOperationException("This method should be override by ths subClass!");
    }

    @Override
    public void process(FsFile fsFile) throws Exception {

    }

    @Override
    public void afterProcess(FsFile fsFile) throws Exception {
        fsFile.setStatus(ProcessStatusEnum.SUCCESS);
        updateFsFile(fsFile);
    }
}
