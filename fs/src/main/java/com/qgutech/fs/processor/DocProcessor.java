package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;

public class DocProcessor extends AbstractProcessor {

    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateDoc(fsFile.getSuffix());
    }

    @Override
    protected void submitToRedis(FsFile fsFile) {
        //todo
    }

    @Override
    protected String getGenFilePath(FsFile fsFile) {
        //todo
        return null;
    }

    @Override
    public void process(FsFile fsFile) throws Exception {
        //todo
        afterProcess(fsFile);
    }
}
