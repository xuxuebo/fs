package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ImageTypeEnum;
import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.FsUtils;
import com.qgutech.fs.utils.PropertiesUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class ImageProcessor extends AbstractProcessor {

    @Override
    protected boolean validateFile(FsFile fsFile) throws Exception {
        String imageTypes = PropertiesUtils.getImageType();
        if (StringUtils.isEmpty(imageTypes)) {
            return true;
        }

        for (String imageType : imageTypes.split(",")) {
            if (imageType.equalsIgnoreCase(fsFile.getSuffix())) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected boolean needAsync(FsFile fsFile) {
        return false;
    }

    @Override
    protected String getGenFilePath(FsFile fsFile) {
        return PropertiesUtils.getFileStoreDir() + fsFile.getCorpCode()
                + File.separator + fsFile.getAppCode()
                + File.separator + FsConstants.FILE_DIR_GEN
                + File.separator + FsConstants.FILE_DIR_IMG
                + File.separator + FsUtils.formatDateToYYMM(new Date())
                + File.separator + fsFile.getId();
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        String genFilePath = getGenFilePath(fsFile);
        try {
            File genFile = new File(genFilePath);
            if (!genFile.exists() && !genFile.mkdirs()) {
                throw new IOException("Creating directory[path:" + genFile.getAbsolutePath() + "] failed!");
            }

            ImageTypeEnum[] values = ImageTypeEnum.values();
            String tmpFilePath = fsFile.getTmpFilePath();
            String resolution = FsUtils.getImageResolution(tmpFilePath);
            int width = Integer.parseInt(resolution.substring(0, resolution.indexOf("x")));
            int height = Integer.parseInt(resolution.substring(resolution.indexOf("x") + 1));
            for (ImageTypeEnum value : values) {
                int w = value.getW();
                int h = value.getH();
                String command = "ffmpeg -i " + tmpFilePath;
                if (w > 0 && h > 0) {
                    if (w > width) {
                        w = width;
                    }

                    if (h > height) {
                        h = height;
                    }

                    command += " -s " + w + "*" + h;
                }

                command += " -y " + genFilePath + File.separator
                        + value.name().toLowerCase() + FsConstants.DEFAULT_IMAGE_SUFFIX;
                FsUtils.executeErrorCommand(command);
            }

            fsFile.setStatus(ProcessStatusEnum.SUCCESS);
            saveFile(fsFile);
        } catch (Exception e) {
            deleteFile(genFilePath);
            fsFile.setStatus(ProcessStatusEnum.FAILED);
            deleteFsFile(fsFile.getId());
            throw e;
        } finally {
            deleteFile(new File(fsFile.getTmpFilePath()).getParentFile());
        }
    }
}
