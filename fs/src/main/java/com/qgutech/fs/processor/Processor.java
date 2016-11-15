package com.qgutech.fs.processor;


import com.qgutech.fs.domain.FsFile;

public interface Processor {

    FsFile submit(FsFile fsFile) throws Exception;

    void process(FsFile fsFile) throws Exception;

    void afterProcess(FsFile fsFile) throws Exception;

}
