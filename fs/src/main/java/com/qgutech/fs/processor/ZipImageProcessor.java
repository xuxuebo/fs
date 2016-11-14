package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;

public class ZipImageProcessor extends AbstractProcessor {

    @Override
    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateZip(fsFile.getSuffix());
    }

    @Override
    protected void submitToRedis(FsFile fsFile) {
        super.submitToRedis(fsFile);
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        super.process(fsFile);
    }
}
