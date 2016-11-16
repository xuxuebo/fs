package com.qgutech.fs.controller;

import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.processor.Processor;
import com.qgutech.fs.processor.ProcessorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@Controller
@RequestMapping("/file/*")
public class FileController {

    private static final Log LOG = LogFactory.getLog(FileController.class);

    @Resource
    private ProcessorFactory processorFactory;

    @RequestMapping("/uploadFile")
    public String uploadFile(FsFile fsFile) {
        try {
            Processor processor = processorFactory.acquireProcessor(fsFile.getProcessor());
            fsFile = processor.submit(fsFile);
        } catch (Exception e) {
            LOG.error(e);
        }

        return null;
    }

    @RequestMapping("/getFile/*")
    public String getFile(FsFile fsFile) {
        return null;
    }

    @RequestMapping("/downloadFile/*")
    public String downloadFile(FsFile fsFile) {
        return null;
    }

    @RequestMapping("/cutImage")
    public String cutImage(FsFile fsFile) {
        return null;
    }

    @RequestMapping("/asyncProcess")
    public String asyncProcess(FsFile fsFile) {
        return null;
    }

    @RequestMapping("/backUploadFile")
    public String backUploadFile(FsFile fsFile) {
        return null;
    }


}
