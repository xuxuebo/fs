package com.qgutech.fs.processor;


import com.google.gson.Gson;
import com.qgutech.fs.domain.DocTypeEnum;
import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.domain.ProcessorTypeEnum;
import com.qgutech.fs.utils.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import redis.clients.jedis.JedisCommands;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public abstract class AbstractProcessor implements Processor {

    protected static final int MAX_SUBMIT_CNT = 10;
    protected static final int DEFAULT_WAIT_TIME = 1000;
    protected static final int DEFAULT_SEMAPHORE_CNT = Runtime.getRuntime().availableProcessors() / 2 + 1;

    protected final Log LOG = LogFactory.getLog(getClass());
    protected final Gson gson = new Gson();
    protected ThreadPoolTaskExecutor taskExecutor;
    protected JedisCommands commonJedis;
    protected int semaphoreCnt = DEFAULT_SEMAPHORE_CNT;
    protected int maxSubmitCnt = MAX_SUBMIT_CNT;
    protected int submitFailedWaitTime = DEFAULT_WAIT_TIME;

    @Override
    public FsFile submit(FsFile fsFile) throws Exception {
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
        boolean needAsync = true;
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

            needAsync = needAsync(fsFile);
            if (needAsync) {
                submit(fsFile, 0);
            } else {
                process(fsFile);
            }
        } catch (Exception e) {
            needAsync = true;
            deleteFile(originFilePath);
            deleteFile(tmpDir);
            deleteFile(getGenFilePath(fsFile));
            deleteFsFile(fsFileId);//todo redis
            throw e;
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
            if (!needAsync) {
                deleteFile(tmpDir);
            }
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
        if (file == null) {
            HttpServletRequest request = ((ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes()).getRequest();
            if (request instanceof MultipartRequest) {
                MultipartRequest multipartRequest = (MultipartRequest) request;
                Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
                if (MapUtils.isNotEmpty(fileMap)) {
                    for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                        fsFile.setFile(entry.getValue());
                    }
                }
            }
        }

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
        if (StringUtils.isEmpty(fsFile.getId())) {
            fsFile.setCreateTime(new Date());
        }

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

    protected final boolean validateAudio(String extension) {
        String audioTypes = PropertiesUtils.getAudioType();
        if (StringUtils.isEmpty(audioTypes)) {
            return false;
        }

        for (String audioType : audioTypes.split(",")) {
            if (audioType.equalsIgnoreCase(extension)) {
                return true;
            }
        }

        return false;
    }

    protected final boolean validateZip(String extension) {
        String zipTypes = PropertiesUtils.getZipType();
        if (StringUtils.isEmpty(zipTypes)) {
            return false;
        }

        for (String zipType : zipTypes.split(",")) {
            if (zipType.equalsIgnoreCase(extension)) {
                return true;
            }
        }

        return false;
    }

    protected final boolean validateVideo(String extension) {
        String videoTypes = PropertiesUtils.getVideoType();
        if (StringUtils.isEmpty(videoTypes)) {
            return false;
        }

        for (String videoType : videoTypes.split(",")) {
            if (videoType.equalsIgnoreCase(extension)) {
                return true;
            }
        }

        return false;
    }

    protected final boolean validateDoc(String extension) {
        return DocTypeEnum.isDoc(extension);
    }

    protected final boolean validateImage(String extension) {
        String imageTypes = PropertiesUtils.getImageType();
        if (StringUtils.isEmpty(imageTypes)) {
            return false;
        }

        for (String imageType : imageTypes.split(",")) {
            if (imageType.equalsIgnoreCase(extension)) {
                return true;
            }
        }

        return false;
    }

    protected boolean decompress(FsFile fsFile, Validate validate) throws Exception {
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
            if (StringUtils.isEmpty(extension) || !validate.validate(extension)) {
                return false;
            }
        }

        fsFile.setSubFileCount(files.length);
        return true;
    }

    protected interface Validate {
        boolean validate(String extension);
    }

    protected boolean submit(FsFile fsFile, int count) throws Exception {
        try {
            submitToRedis(fsFile);
        } catch (Exception e) {
            try {
                Thread.sleep(submitFailedWaitTime);
            } catch (Exception ex) {
                //NP
            }

            if (count < maxSubmitCnt) {
                submit(fsFile, ++count);
            } else {
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                return false;
            }
        }

        return true;
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
        String fsFileId = fsFile.getId();
        if (StringUtils.isNotEmpty(fsFileId)) {
            return fsFileId;
        }

        long timestamp = System.currentTimeMillis();
        fsFile.setTimestamp(timestamp);
        String serverHost = PropertiesUtils.getServerHost();
        fsFile.setServerHost(serverHost);
        fsFile.setSign(Signer.sign(serverHost, PropertiesUtils.getServerSecret(), timestamp));
        return HttpUtils.doPost(PropertiesUtils.getSaveFileUrl(), fsFile.toMap());
    }

    protected final String updateFsFile(FsFile fsFile) {
        long timestamp = System.currentTimeMillis();
        fsFile.setTimestamp(timestamp);
        String serverHost = PropertiesUtils.getServerHost();
        fsFile.setServerHost(serverHost);
        fsFile.setSign(Signer.sign(fsFile.getId(), serverHost
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
        return PropertiesUtils.getFileStoreDir() + fsFile.getCorpCode()
                + File.separator + fsFile.getAppCode()
                + File.separator + FsConstants.FILE_DIR_GEN
                + File.separator + ProcessorTypeEnum.toGenDirectory(fsFile.getProcessor())
                + File.separator + FsUtils.formatDateToYYMM(fsFile.getCreateTime())
                + File.separator + fsFile.getId();
    }

    protected void afterProcess(FsFile fsFile) throws Exception {
        fsFile.setStatus(ProcessStatusEnum.SUCCESS);
        updateFsFile(fsFile);
    }

    protected final <T> void getFutures(List<Future<T>> futures) throws Exception {
        try {
            for (Future<T> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            for (Future<T> future : futures) {
                future.cancel(true);
            }

            throw e;
        }
    }

    protected String getProcessQueueName() {
        return null;
    }

    protected void submitToRedis(FsFile fsFile) {
        commonJedis.sadd(RedisKey.FS_QUEUE_NAME_LIST, getProcessQueueName());
        String fsFileId = fsFile.getId();
        //当重复提交时，防止重复处理
        //commonJedis.lrem(getProcessQueueName(), 0, fsFileId);
        commonJedis.lpush(getProcessQueueName(), fsFileId);
        commonJedis.set(RedisKey.FS_FILE_CONTENT_PREFIX + fsFileId, gson.toJson(fsFile));
    }

    public int getSemaphoreCnt() {
        return semaphoreCnt;
    }

    public void setSemaphoreCnt(int semaphoreCnt) {
        this.semaphoreCnt = semaphoreCnt;
    }

    public ThreadPoolTaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public int getMaxSubmitCnt() {
        return maxSubmitCnt;
    }

    public void setMaxSubmitCnt(int maxSubmitCnt) {
        this.maxSubmitCnt = maxSubmitCnt;
    }

    public int getSubmitFailedWaitTime() {
        return submitFailedWaitTime;
    }

    public void setSubmitFailedWaitTime(int submitFailedWaitTime) {
        this.submitFailedWaitTime = submitFailedWaitTime;
    }

    public JedisCommands getCommonJedis() {
        return commonJedis;
    }

    public void setCommonJedis(JedisCommands commonJedis) {
        this.commonJedis = commonJedis;
    }
}
