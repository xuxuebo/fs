package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.FsUtils;
import com.qgutech.fs.utils.PropertiesUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;

public class ZipAudioProcessor extends AbstractProcessor {
    @Override
    protected boolean validateFile(FsFile fsFile) throws Exception {
        String zipTypes = PropertiesUtils.getZipType();
        if (StringUtils.isEmpty(zipTypes)) {
            return false;
        }

        boolean valid = false;
        String suffix = fsFile.getSuffix();
        for (String zipType : zipTypes.split(",")) {
            if (zipType.equalsIgnoreCase(suffix)) {
                valid = true;
            }
        }

        if (!valid) {
            return false;
        }

        String tmpFilePath = fsFile.getTmpFilePath();
        File parentFile = new File(tmpFilePath).getParentFile();
        File decompressDir = new File(parentFile, FsConstants.DECOMPRESS);
        FsUtils.decompress(tmpFilePath, decompressDir.getAbsolutePath());


        return true;
    }
}
