package com.qgutech.fs.utils;


import com.oreilly.servlet.multipart.FileRenamePolicy;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;

public class SpecifyFileRenamePolicy implements FileRenamePolicy {

    private String filename;

    public SpecifyFileRenamePolicy(String filename) {
        this.filename = filename;
    }

    @Override
    public File rename(File file) {
        if (file == null) {
            return null;
        }

        String extension = FilenameUtils.getExtension(filename);
        String filename;
        if (StringUtils.isEmpty(extension)) {
            filename = this.filename + FsConstants.POINT
                    + FilenameUtils.getExtension(file.getName());
        } else {
            filename = this.filename;
        }

        return new File(file.getParent(), filename);
    }
}
