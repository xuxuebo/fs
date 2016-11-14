package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;

public class DocProcessor extends AbstractProcessor {

    protected boolean validateFile(FsFile fsFile) throws Exception {
        return validateDoc(fsFile.getSuffix());
    }

}
