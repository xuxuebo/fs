package com.qgutech.fs.processor;


import com.qgutech.fs.convert.Converter;
import com.qgutech.fs.domain.DocTypeEnum;
import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ImageTypeEnum;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.utils.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public abstract class AbstractDocProcessor extends AbstractProcessor {

    protected Converter docToPdfConverter;
    protected Converter pdfToImageConverter;

    @Override
    protected void submitToRedis(FsFile fsFile) {
        if (PropertiesUtils.isDocConvert()) {
            super.submitToRedis(fsFile);
        } else {
            String backUrl = PropertiesUtils.getHttpProtocol() + FsConstants.HTTP_COLON
                    + PropertiesUtils.getServerHost() + PropertiesUtils.getBackUri();
            fsFile.setBackUrl(backUrl);
            HttpUtils.doPost(PropertiesUtils.getAsyncUrl(), fsFile.toMap()
                    , fsFile.getTmpFilePath(), fsFile.getStoredFileName());
            //转发到文档转化服务器后，删除临时文件
            FsUtils.deleteFile(new File(fsFile.getTmpFilePath()).getParentFile());
        }
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        File tmpDirFile = new File(fsFile.getTmpFilePath()).getParentFile();
        try {
            process(fsFile, tmpDirFile);
        } catch (Throwable e) {
            FsUtils.deleteFile(getGenFilePath(fsFile));
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            HttpUtils.updateFsFile(fsFile);

            throw new Exception(e);
        } finally {
            FsUtils.deleteFile(tmpDirFile);
            if (StringUtils.isNotEmpty(fsFile.getBackUrl())) {
                FsUtils.deleteFile(getGenFilePath(fsFile));
                FsUtils.deleteFile(getOriginFilePath(fsFile));
            }
        }
    }

    protected abstract void process(FsFile fsFile, File tmpDirFile) throws Exception;

    protected int processDoc(String srcFilePath, String imageTmpDirPath
            , String pdfTmpDirPath, final String genFilePath) throws Exception {
        String extension = FilenameUtils.getExtension(srcFilePath);
        boolean convert = true;
        if (DocTypeEnum.PPT.name().equalsIgnoreCase(extension)
                || DocTypeEnum.PPTX.name().equalsIgnoreCase(extension)) {
            try {
                ConvertUtils.pptToPng(srcFilePath, imageTmpDirPath);
                convert = false;
            } catch (Exception e) {
                LOG.error("Exception occurred when ppt[" + srcFilePath
                        + "] straight turn to png[pngDir:" + imageTmpDirPath + "]!", e);
            }
        }

        if (convert) {
            File pdfFile = docToPdfConverter.convert(srcFilePath, pdfTmpDirPath);
            pdfToImageConverter.convert(pdfFile.getAbsolutePath(), imageTmpDirPath);
        }

        File[] imageFiles = new File(imageTmpDirPath).listFiles();
        if (imageFiles == null || imageFiles.length == 0) {
            LOG.error("The images[DirPath:" + imageTmpDirPath
                    + "] which converting from doc[path:" + srcFilePath + "] is empty!");
            return -1;
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
        return imageFiles.length;
    }

    @Override
    protected void afterProcess(FsFile fsFile) throws Exception {
        String backUrl = fsFile.getBackUrl();
        if (StringUtils.isNotEmpty(backUrl)) {
            String genFilePath = getGenFilePath(fsFile);
            File parentFile = new File(fsFile.getTmpFilePath()).getParentFile();
            File compressFile = new File(parentFile, FsUtils.generateUUID()
                    + FsConstants.POINT + FsConstants.COMPRESS_FILE_SUFFIX_ZIP);
            FsUtils.compress(genFilePath, compressFile.getAbsolutePath());
            fsFile.setSign(Signer.sign(fsFile));
            HttpUtils.doPost(backUrl, fsFile.toMap(), compressFile.getAbsolutePath(), null);//todo 容错
        }

        fsFile.setStatus(ProcessStatusEnum.SUCCESS);
        HttpUtils.updateFsFile(fsFile);
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
