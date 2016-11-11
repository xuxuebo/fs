package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;

public class FileProcessor extends AbstractProcessor {

    @Override
    protected boolean needAsync(FsFile fsFile) {
        return false;
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        afterProcess(fsFile);
    }
}
