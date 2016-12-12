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
        String tmpDirPath = FsPathUtils.getImportTmpDirPath(tmp);
        File tmpDir = new File(tmpDirPath);
        if (!tmpDir.exists() && !tmpDir.mkdirs() && !tmpDir.exists()) {
            throw new IOException("Creating directory[" + tmpDirPath + "] failed!");
        }

        String tmpFilePath = tmpDirPath + File.separator
                + tmp + FsConstants.POINT + fsFile.getSuffix();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        String fsFileId = null;
        String originFilePath = null;
        boolean needAsync = true;
        try {
            saveTmpFile(fsFile, tmpFilePath);
            if (!validateFile(fsFile)) {
                LOG.error("Uploading file[" + fsFile + "] is illegally!");
                fsFile.setProcessMsg("File is illegally!");
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                return fsFile;
            }

            fsFile.setStatus(ProcessStatusEnum.PROCESSING);
            fsFileId = HttpUtils.saveFsFile(fsFile);
            fsFile.setId(fsFileId);
            inputStream = new FileInputStream(tmpFilePath);
            originFilePath = getOriginFilePath(fsFile);
            File originFile = new File(originFilePath);
            File parentFile = originFile.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs() && !parentFile.exists()) {
                throw new IOException("Creating directory[" + parentFile.getAbsolutePath() + "] failed!");
            }

            if (!originFile.exists() && !originFile.createNewFile() && !originFile.exists()) {
                throw new IOException("Creating file[" + originFilePath + "] failed!");
            }

            outputStream = new FileOutputStream(originFilePath);
            IOUtils.copy(inputStream, outputStream);
            IOUtils.closeQuietly(inputStream);//为了能够删除临时文件需要在这儿关闭流
            needAsync = needAsync(fsFile);
            if (needAsync) {
                submit(fsFile, 0);
            } else {
                process(fsFile);
            }
        } catch (Exception e) {
            needAsync = false;
            IOUtils.closeQuietly(outputStream);
            FsUtils.deleteFile(originFilePath);
            FsUtils.deleteFile(getGenFilePath(fsFile));
            HttpUtils.deleteFsFile(fsFileId);//todo redis
            throw e;
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
            if (!needAsync) {
                FsUtils.deleteFile(tmpDir);
            }
        }

        fsFile.setFileUrl(FsPathUtils.getOriginFileUrl(fsFile));
        return fsFile;
    }

    protected final boolean validateResumeParams(FsFile fsFile) throws Exception {
        String storedFileName = fsFile.getStoredFileName();
        if (StringUtils.isEmpty(storedFileName)) {
            LOG.error("Uploading file's originalFilename is empty!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("Param Error!");
            return false;
        }

        String extension = FilenameUtils.getExtension(storedFileName);
        fsFile.setSuffix(extension.toLowerCase());
        if (StringUtils.isEmpty(extension)) {
            LOG.error("Uploading file[fileName:" + storedFileName + "]'s extension is empty!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("Param Error!");
            return false;
        }

        Long fileSize = fsFile.getFileSize();
        if (fileSize == null || fileSize <= 0) {
            LOG.error("Uploading file[fileName:" + storedFileName + "]'s size[" + fileSize + "] is illegal!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("Param Error!");
            return false;
        }

        fsFile.setServerCode(PropertiesUtils.getServerCode());
        fsFile.setServerHost(PropertiesUtils.getServerHost());
        if (StringUtils.isEmpty(fsFile.getId())) {
            fsFile.setCreateTime(new Date());
        } else if (fsFile.getCreateTime() == null) {
            FsFile dbFsFile = HttpUtils.getFsFile(fsFile.getId());
            fsFile.setBeforeFsFileJson(gson.toJson(dbFsFile));
            fsFile.setCreateTime(dbFsFile == null ? new Date() : dbFsFile.getCreateTime());
        }

        return true;
    }

    protected final boolean validateParams(FsFile fsFile) throws Exception {
        if (fsFile == null) {
            LOG.error("The param fsFile is null!");
            return false;
        }

        if (!fsFile.validateUpload()) {
            LOG.error("The fields[appCode:" + fsFile.getAppCode() + ",corpCode:" + fsFile.getCorpCode()
                    + ",businessId:" + fsFile.getBusinessId() + ",processor:" + fsFile.getProcessor()
                    + "] of fsFile must be not empty!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("Param Error!");
            return false;
        }

        if (StringUtils.isNotEmpty(fsFile.getResumeType())
                && StringUtils.isNotEmpty(fsFile.getMd5())) {
            return validateResumeParams(fsFile);
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
                        file = entry.getValue();
                    }
                }
            }
        }

        if (file == null || file.isEmpty() || StringUtils.isEmpty(file.getOriginalFilename())) {
            LOG.error("Uploading file not exist or originalFilename is empty!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("Param Error!");
            return false;
        }

        String originalFilename = file.getOriginalFilename();
        fsFile.setStoredFileName(originalFilename);
        String extension = FilenameUtils.getExtension(originalFilename);
        if (StringUtils.isEmpty(extension)) {
            LOG.error("Uploading file[fileName:" + originalFilename + "]'s extension is empty!");
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            fsFile.setProcessMsg("Param Error!");
            return false;
        }

        fsFile.setSuffix(extension.toLowerCase());
        fsFile.setFileSize(file.getSize());
        fsFile.setServerCode(PropertiesUtils.getServerCode());
        fsFile.setServerHost(PropertiesUtils.getServerHost());
        if (StringUtils.isEmpty(fsFile.getId())) {
            fsFile.setCreateTime(new Date());
        } else if (fsFile.getCreateTime() == null) {
            FsFile dbFsFile = HttpUtils.getFsFile(fsFile.getId());
            fsFile.setBeforeFsFileJson(gson.toJson(dbFsFile));
            fsFile.setCreateTime(dbFsFile == null ? new Date() : dbFsFile.getCreateTime());
        }

        return true;
    }

    protected final void saveTmpFile(FsFile fsFile, String tmpFilePath) throws Exception {
        if (StringUtils.isNotEmpty(fsFile.getTmpFilePath())) {
            File srcFile = new File(fsFile.getTmpFilePath());
            if (srcFile.exists()) {
                FileUtils.copyFile(srcFile, new File(tmpFilePath));
                fsFile.setTmpFilePath(tmpFilePath);
                return;
            }
        }

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
            LOG.error("Exception occurred when submitting fsFile[" + fsFile
                    + "submitCount:" + (count + 1) + "] to redis!", e);
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

    @Override
    public String getGenFilePath(FsFile fsFile) {
        return PropertiesUtils.getFileStoreDir() + fsFile.getCorpCode()
                + File.separator + fsFile.getAppCode()
                + File.separator + FsConstants.FILE_DIR_GEN
                + File.separator + ProcessorTypeEnum.toGenDirectory(fsFile.getProcessor())
                + File.separator + FsUtils.formatDateToYYMM(fsFile.getCreateTime())
                + File.separator + fsFile.getId();
    }

    protected void afterProcess(FsFile fsFile) throws Exception {
        fsFile.setStatus(ProcessStatusEnum.SUCCESS);
        HttpUtils.updateFsFile(fsFile);

        String beforeFsFileJson = fsFile.getBeforeFsFileJson();
        if (StringUtils.isNotEmpty(beforeFsFileJson)) {
            FsFile beforeFsFile = gson.fromJson(beforeFsFileJson, FsFile.class);
            String beforeOriginFilePath = getOriginFilePath(beforeFsFile);
            String originFilePath = getOriginFilePath(fsFile);
            if (!beforeOriginFilePath.equals(originFilePath)) {
                FsUtils.deleteFile(beforeOriginFilePath);
            }

            if (!ProcessorTypeEnum.FILE.equals(fsFile.getProcessor())) {
                String beforeGenFilePath = getGenFilePath(beforeFsFile);
                String genFilePath = getGenFilePath(fsFile);
                if (!beforeGenFilePath.equals(genFilePath)) {
                    FsUtils.deleteFile(beforeGenFilePath);
                }
            }
        }
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

    @Override
    public void submitToReprocess(FsFile fsFile) throws Exception {
        String tmp = FsUtils.generateUUID();
        String tmpDirPath = FsPathUtils.getImportTmpDirPath(tmp);
        File tmpDir = new File(tmpDirPath);
        if (!tmpDir.exists() && !tmpDir.mkdirs()) {
            throw new IOException("Creating directory[" + tmpDirPath + "] failed!");
        }

        String tmpFilePath = tmpDirPath + File.separator
                + tmp + FsConstants.POINT + fsFile.getSuffix();
        fsFile.setTmpFilePath(tmpFilePath);
        boolean needAsync = true;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(getOriginFilePath(fsFile));
            outputStream = new FileOutputStream(fsFile.getTmpFilePath());
            IOUtils.copy(inputStream, outputStream);
            needAsync = needAsync(fsFile);
            if (needAsync) {
                fsFile.setStatus(ProcessStatusEnum.PROCESSING);
                submit(fsFile, 0);
            } else {
                process(fsFile);
            }

            fsFile.setStatus(ProcessStatusEnum.SUCCESS);
        } catch (Exception e) {
            FsUtils.deleteFile(tmpDir);
            needAsync = true;

            throw e;
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
            if (!needAsync) {
                FsUtils.deleteFile(tmpDir);
            }
        }
    }

    protected String getProcessQueueName() {
        return null;
    }

    protected void submitToRedis(FsFile fsFile) {
        fsFile.setFile(null);
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
