package com.qgutech.fs.convert;


import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.FsUtils;
import com.qgutech.fs.utils.PropertiesUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;

public class DocToPdfConverterChain extends AbstractConverter {

    private List<Converter> converters;

    protected File beforeConvert(String inputFilePath) throws Exception {
        File officeTrustDir = new File(PropertiesUtils.getOfficeTrustDir());
        if (!officeTrustDir.exists() && !officeTrustDir.mkdirs() && !officeTrustDir.exists()) {
            throw new RuntimeException("Creating office trust directory["
                    + officeTrustDir.getAbsolutePath() + "] failed!");
        }

        File trustFile = new File(officeTrustDir, FsUtils.generateUUID()
                + FsConstants.POINT + FilenameUtils.getExtension(inputFilePath));
        FileUtils.copyFile(new File(inputFilePath), trustFile);

        return trustFile;
    }

    protected void afterConvert(File beforeFile) throws Exception {
        FsUtils.deleteFile(beforeFile);
    }

    @Override
    protected File windowsConvert(String inputFilePath, String targetFileDirPath) throws Exception {
        Exception exception = null;
        File beforeFile = beforeConvert(inputFilePath);
        try {
            for (Converter converter : converters) {
                try {
                    return converter.convert(beforeFile.getAbsolutePath(), targetFileDirPath);
                } catch (Exception e) {
                    exception = e;
                }
            }
        } finally {
            afterConvert(beforeFile);
        }

        throw exception;
    }

    public List<Converter> getConverters() {
        return converters;
    }

    public void setConverters(List<Converter> converters) {
        this.converters = converters;
    }
}
