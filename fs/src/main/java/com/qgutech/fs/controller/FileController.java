package com.qgutech.fs.controller;

import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.processor.Processor;
import com.qgutech.fs.processor.ProcessorFactory;
import com.qgutech.fs.utils.PropertiesUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/file/*")
public class FileController {

    private static final Log LOG = LogFactory.getLog(FileController.class);

    @Resource
    private ProcessorFactory processorFactory;

    @RequestMapping("/uploadFile")
    public void uploadFile(FsFile fsFile, HttpServletRequest request, HttpServletResponse response) {
        if (PropertiesUtils.isUpload()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            Processor processor = processorFactory.acquireProcessor(fsFile.getProcessor());
            fsFile = processor.submit(fsFile);
        } catch (Exception e) {
            LOG.error(e);
        }


    }

    @RequestMapping("/getFile/*")
    public String getFile(FsFile fsFile, HttpServletResponse response) {
        if (PropertiesUtils.isDownload()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        return null;
    }

    @RequestMapping("/downloadFile/*")
    public String downloadFile(FsFile fsFile, HttpServletResponse response) {
        if (PropertiesUtils.isDownload()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        return null;
    }

    @RequestMapping("/cutImage")
    public String cutImage(FsFile fsFile, HttpServletResponse response) {
        if (PropertiesUtils.isUpload()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        return null;
    }

    @RequestMapping("/reprocessFile")
    public String reprocessFile(FsFile fsFile) {
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
