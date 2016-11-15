package com.qgutech.fs.processor;


import com.qgutech.fs.convert.Converter;
import com.qgutech.fs.domain.DocTypeEnum;
import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ImageTypeEnum;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.utils.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.JedisCommands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public class DocProcessor extends AbstractProcessor {

    protected Converter docToPdfConverter;
    protected Converter pdfToImageConverter;

    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateDoc(fsFile.getSuffix());
    }

    @Override
    protected void submitToRedis(FsFile fsFile) {
        if (PropertiesUtils.isDocConvert()) {
            JedisCommands commonJedis = FsRedis.getCommonJedis();
            commonJedis.sadd(RedisKey.FS_QUEUE_NAME_LIST, RedisKey.FS_DOC_QUEUE_LIST);
            String fsFileId = fsFile.getId();
            //当重复提交时，防止处理音频重复
            //commonJedis.lrem(RedisKey.FS_DOC_QUEUE_LIST, 0, fsFileId);
            commonJedis.lpush(RedisKey.FS_DOC_QUEUE_LIST, fsFileId);
            commonJedis.set(RedisKey.FS_FILE_CONTENT_PREFIX + fsFileId, gson.toJson(fsFile));
        } else {
            String backUrl = PropertiesUtils.getHttpProtocol() + FsConstants.HTTP_COLON
                    + PropertiesUtils.getServerHost() + PropertiesUtils.getBackUri();
            fsFile.setBackUrl(backUrl);
            HttpUtils.doPost(PropertiesUtils.getAsyncUrl(), fsFile.toMap()
                    , fsFile.getTmpFilePath(), fsFile.getStoredFileName());
        }
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        String tmpFilePath = fsFile.getTmpFilePath();
        File parentFile = new File(tmpFilePath).getParentFile();
        try {
            final String genFilePath = getGenFilePath(fsFile);
            File genFile = new File(genFilePath);
            if (!genFile.exists() && !genFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + genFile.getAbsolutePath() + "] failed!");
            }

            File imageDirFile = new File(parentFile, FsUtils.generateUUID() + FsConstants.FILE_DIR_IMG);
            if (!imageDirFile.exists() && !imageDirFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + imageDirFile.getAbsolutePath() + "] failed!");
            }

            String targetFileDirPath = imageDirFile.getAbsolutePath() + File.separator;
            String extension = FilenameUtils.getExtension(tmpFilePath);
            boolean convert = true;
            if (DocTypeEnum.PPT.name().equalsIgnoreCase(extension)
                    || DocTypeEnum.PPTX.name().equalsIgnoreCase(extension)) {
                try {
                    ConvertUtils.pptToPng(tmpFilePath, targetFileDirPath);
                    convert = false;
                } catch (Exception e) {
                    LOG.error("Exception occurred when ppt straight turn to png!", e);
                }
            }

            if (convert) {
                File pdfFile = docToPdfConverter.convert(tmpFilePath
                        , parentFile.getAbsolutePath() + File.separator);
                pdfToImageConverter.convert(pdfFile.getAbsolutePath(), targetFileDirPath);
            }

            File[] imageFiles = imageDirFile.listFiles();
            if (imageFiles == null || imageFiles.length == 0) {
                fsFile.setStatus(ProcessStatusEnum.FAILED);
                updateFsFile(fsFile);
                return;
            }

            List<Future<String>> futures = new ArrayList<Future<String>>(imageFiles.length);
            final Semaphore semaphore = new Semaphore(getSemaphoreCnt());
            for (int i = 0; i < imageFiles.length; i++) {
                final int index = i + 1;
                final String imagePath = imageFiles[i].getAbsolutePath();
                semaphore.acquire();
                futures.add(taskExecutor.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        try {
                            String orgResolution = FsUtils.getImageResolution(imagePath);
                            int indexOf = orgResolution.indexOf("x");
                            int width = Integer.parseInt(orgResolution.substring(0, indexOf));
                            int height = Integer.parseInt(orgResolution.substring(indexOf + 1));
                            for (ImageTypeEnum value : ImageTypeEnum.values()) {
                                int w = value.getW();
                                int h = value.getH();
                                String resolution;
                                if (w > 0 && h > 0) {
                                    if (w > width) {
                                        w = width;
                                    }

                                    if (h > height) {
                                        h = height;
                                    }

                                    resolution = w + "*" + h;
                                } else {
                                    resolution = orgResolution;
                                }

                                String destFilePath = genFilePath + File.separator + index
                                        + value.name().toLowerCase() + FsConstants.DEFAULT_IMAGE_SUFFIX;
                                FsUtils.executeCommand(new String[]{FsConstants.FFMPEG, "-i", imagePath
                                        , "-s", resolution, "-y", destFilePath});
                            }

                            return null;
                        } finally {
                            semaphore.release();
                        }
                    }
                }));
            }

            getFutures(futures);
            fsFile.setSubFileCount(imageFiles.length);
            afterProcess(fsFile);
        } catch (Exception e) {
            deleteFile(getGenFilePath(fsFile));
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            updateFsFile(fsFile);

            throw e;
        } finally {
            JedisCommands commonJedis = FsRedis.getCommonJedis();
            commonJedis.expire(RedisKey.FS_DOC_QUEUE_LIST + fsFile.getId(), 0);
            commonJedis.srem(RedisKey.FS_ZIP_VIDEO_QUEUE_LIST + RedisKey.FS_DOING_LIST_SUFFIX, fsFile.getId());
            deleteFile(parentFile);
            if (StringUtils.isNotEmpty(fsFile.getBackUrl())) {
                deleteFile(getGenFilePath(fsFile));
                deleteFile(getOriginFilePath(fsFile));
            }
        }
    }

    @Override
    public void afterProcess(FsFile fsFile) throws Exception {
        String backUrl = fsFile.getBackUrl();
        if (StringUtils.isNotEmpty(backUrl)) {
            String genFilePath = getGenFilePath(fsFile);
            File parentFile = new File(fsFile.getTmpFilePath()).getParentFile();
            File compressFile = new File(parentFile, FsUtils.generateUUID()
                    + FsConstants.POINT + FsConstants.COMPRESS_FILE_SUFFIX_ZIP);
            FsUtils.compress(genFilePath, compressFile.getAbsolutePath());
            HttpUtils.doPost(backUrl, fsFile.toMap(), compressFile.getAbsolutePath(), null);//todo 容错
        }

        fsFile.setStatus(ProcessStatusEnum.SUCCESS);
        updateFsFile(fsFile);
    }

    public Converter getDocToPdfConverter() {
        return docToPdfConverter;
    }

    public void setDocToPdfConverter(Converter docToPdfConverter) {
        this.docToPdfConverter = docToPdfConverter;
    }

    public Converter getPdfToImageConverter() {
        return pdfToImageConverter;
    }

    public void setPdfToImageConverter(Converter pdfToImageConverter) {
        this.pdfToImageConverter = pdfToImageConverter;
    }
}
