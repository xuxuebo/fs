package com.qgutech.fs.service;


import org.junit.Test;

import javax.annotation.Resource;
import java.util.UUID;

public class FileServerServiceTest extends BaseServiceTest {
    @Resource
    private FileServerService fileServerService;

    @Test
    public void testGetOriginFileUrl() {
        fileServerService.getOriginFileUrl("zhaojie", "els", "402881d6582476410158247644bf0000");
    }


}
