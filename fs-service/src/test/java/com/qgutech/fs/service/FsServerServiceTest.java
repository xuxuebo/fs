package com.qgutech.fs.service;


import com.qgutech.fs.domain.FsServer;
import com.qgutech.fs.domain.SignLevelEnum;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import javax.annotation.Resource;

public class FsServerServiceTest extends BaseServiceTest {

    @Resource
    private FsServerService fsServerService;

    @Test
    @Rollback(false)
    public void testSave() {
        FsServer fsServer = new FsServer();
        fsServer.setServerCode("0000");
        fsServer.setDownload(true);
        fsServer.setHost("hf.21tb.com");
        fsServer.setSecret("serverSecret");
        fsServer.setSignLevel(SignLevelEnum.sts);
        fsServer.setUpload(true);
        fsServer.setVbox(false);
        fsServer.setServerName("sdgh");
        fsServer.setCorpCode("default");
        fsServerService.save(fsServer);
    }

}
