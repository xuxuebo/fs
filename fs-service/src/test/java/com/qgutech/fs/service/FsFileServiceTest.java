package com.qgutech.fs.service;


import com.qgutech.fs.domain.ProcessorTypeEnum;
import com.qgutech.fs.domain.FsFile;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import javax.annotation.Resource;
import java.util.UUID;

public class FsFileServiceTest extends BaseServiceTest {
    @Resource
    private FsFileService fsFileService;


    @Test
    @Rollback(false)
    public void testSave() {
        FsFile fsFile = new FsFile();
        fsFile.setAppCode("els");
        fsFile.setCorpCode("zhaojie");
        fsFile.setBusinessCode("course001");
        fsFile.setBusinessDir("cour");
        fsFile.setBusinessId(UUID.randomUUID().toString().replace("-", ""));
        fsFile.setFileSize(10000l);
        fsFile.setProcessor(ProcessorTypeEnum.DOC);
        fsFile.setStoredFileName("wodezuguo.doc");
        fsFile.setSubFileCount(10);
        fsFile.setSuffix("doc");

        fsFileService.save(fsFile);
    }


}
