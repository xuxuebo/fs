package com.qgutech.fs.convert;


import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.FsUtils;

import java.io.File;

public class Doc2PdfConverter extends AbstractConverter {

    @Override
    protected String[] getCommands(String srcFilePath, String targetFilePath) {
        return new String[]{convertToolPath, srcFilePath.replace("\\", "\\\\")
                , targetFilePath.replace("\\", "\\\\")};
    }

    @Override
    protected File getTargetFile(String targetFilePath) {
        return new File(targetFilePath, FsConstants.PDF_PREFIX
                + FsUtils.generateUUID() + FsConstants.PDF_SUFFIX);
    }

    @Override
    protected File windowsConvert(String inputFilePath, String targetFileDirPath) throws Exception {
        File targetFile = getTargetFile(targetFileDirPath);
        String[] commands = getCommands(inputFilePath, targetFile.getAbsolutePath());
        String result = FsUtils.executeCommand(commands);
        if (result.length() > 0) {
            if (targetFile.exists()) {
                LOG.info("Executing command[" + FsUtils.toString(commands)
                        + "] and result message is:" + result);
            } else {
                throw new RuntimeException("Executing command[" + FsUtils.toString(commands)
                        + "] failed and result message is:" + result);
            }
        }

        return targetFile;
    }
}
