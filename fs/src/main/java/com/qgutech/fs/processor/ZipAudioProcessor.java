package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.utils.PropertiesUtils;
import org.apache.commons.lang.StringUtils;

public class ZipAudioProcessor extends AbstractProcessor {
    @Override
    protected boolean validateFile(FsFile fsFile) throws Exception {
        /*String audioTypes = PropertiesUtils.getAudioType();
        if (StringUtils.isEmpty(audioTypes)) {
            return true;
        }

        for (String audioType : audioTypes.split(",")) {
            if (audioType.equalsIgnoreCase(fsFile.getSuffix())) {
                return true;
            }
        }*/
       /* String suffix = fsFile.getSuffix();
        if (!"zip".equals(suffix) && !"rar".equals(suffix)) {
            return false;
        }*/
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

        return true;
    }
}
