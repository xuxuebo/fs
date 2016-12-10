package com.qgutech.fs.convert;


import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.FsUtils;
import com.qgutech.fs.utils.PropertiesUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class AbstractDocToPdfConverter extends AbstractConverter {

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

    protected File windowsConvert(String inputFilePath, String targetFileDirPath) throws Exception {
        File beforeFile = beforeConvert(inputFilePath);
        try {
            return doWindowsConvert(beforeFile.getAbsolutePath(), targetFileDirPath);
        } finally {
            afterConvert(beforeFile);
        }
    }

    protected File doWindowsConvert(String inputFilePath, String targetFileDirPath) throws Exception {
        return super.windowsConvert(inputFilePath, targetFileDirPath);
    }

}
