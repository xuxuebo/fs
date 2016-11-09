package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;

import java.io.File;

public class FileProcessor extends AbstractProcessor {

    @Override
    protected boolean needAsync(FsFile fsFile) {
        return false;
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        try {
            afterProcess(fsFile);
        } finally {
            deleteFile(new File(fsFile.getTmpFilePath()).getParentFile());
        }
    }
}
