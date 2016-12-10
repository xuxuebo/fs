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
}
