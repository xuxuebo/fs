package com.qgutech.fs.service;


import com.qgutech.fs.domain.ProcessorTypeEnum;
import com.qgutech.fs.domain.StoredFile;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import javax.annotation.Resource;
import java.util.UUID;

public class StoredFileServiceTest extends BaseServiceTest {
    @Resource
    private StoredFileService storedFileService;


    @Test
    @Rollback(false)
    public void testSave() {
        StoredFile storedFile = new StoredFile();
        storedFile.setAppCode("els");
        storedFile.setCorpCode("zhaojie");
        storedFile.setBusinessCode("course001");
        storedFile.setBusinessDir("cour");
        storedFile.setBusinessId(UUID.randomUUID().toString().replace("-", ""));
        storedFile.setFileSize(10000l);
        storedFile.setProcessor(ProcessorTypeEnum.DOC);
        storedFile.setStoredFileName("wodezuguo.doc");
        storedFile.setSubFileCount(10);
        storedFile.setSuffix("doc");

        storedFileService.save(storedFile);
    }


}
