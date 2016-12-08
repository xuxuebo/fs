package com.qgutech.fs.convert;


import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.FsUtils;

import java.io.File;

public class Doc2PdfConverter extends AbstractConverter {

    protected Converter converter;

    @Override
    protected String[] getCommands(String srcFilePath, String targetFilePath) {
        return new String[]{convertToolPath, srcFilePath.replace("\\", "\\\\")
                , targetFilePath.replace("\\", "\\\\")};
    }

    @Override
    protected File windowsConvert(String inputFilePath, String targetFileDirPath) throws Exception {
        try {
            if (converter != null) {
                return converter.convert(inputFilePath, targetFileDirPath);
            }
        } catch (Exception e) {
            return super.windowsConvert(inputFilePath, targetFileDirPath);
        }

        return super.windowsConvert(inputFilePath, targetFileDirPath);
    }

    @Override
    protected File getTargetFile(String targetFilePath) {
        return new File(targetFilePath, FsConstants.PDF_PREFIX
                + FsUtils.generateUUID() + FsConstants.PDF_SUFFIX);
    }

    public Converter getConverter() {
        return converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }
}
