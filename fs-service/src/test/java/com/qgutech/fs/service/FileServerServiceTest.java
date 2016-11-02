package com.qgutech.fs.service;


import org.junit.Test;

import javax.annotation.Resource;
import java.util.UUID;

public class FileServerServiceTest extends BaseServiceTest {
    @Resource
    private FileServerService fileServerService;

    @Test
    public void test() {
        fileServerService.getFileUrl("corpCode", "appCode", UUID.randomUUID().toString());
    }


}
