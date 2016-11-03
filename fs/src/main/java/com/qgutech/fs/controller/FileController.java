package com.qgutech.fs.controller;

import com.qgutech.fs.domain.FsFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/file/*")
public class FileController {

    @RequestMapping("/uploadFile")
    public String uploadFile(FsFile fsFile) {
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

}
