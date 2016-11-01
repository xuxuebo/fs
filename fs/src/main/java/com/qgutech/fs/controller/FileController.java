package com.qgutech.fs.controller;

import com.qgutech.fs.domain.StoredFile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/file/*")
public class FileController {

    @RequestMapping("/uploadFile")
    public String uploadFile(StoredFile storedFile) {
        return null;
    }

    @RequestMapping("/getFile/*")
    public String getFile(StoredFile storedFile) {
        return null;
    }

    @RequestMapping("/downloadFile/*")
    public String downloadFile(StoredFile storedFile) {
        return null;
    }

    @RequestMapping("/cutImage")
    public String cutImage(StoredFile storedFile) {
        return null;
    }

}
