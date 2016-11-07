package com.qgutech.fs.service;


import com.qgutech.fs.domain.ProcessStatusEnum;
import com.qgutech.fs.domain.ProcessorTypeEnum;
import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.VideoTypeEnum;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import javax.annotation.Resource;
import java.util.UUID;

public class FsFileServiceTest extends BaseServiceTest {
    @Resource
    private FsFileService fsFileService;


    @Test
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
        fsFile.setServerCode("0000");
        fsFile.setSuffix("doc");

        fsFileService.save(fsFile);
    }

    @Test
    public void testSaveVideo() {
        FsFile fsFile = new FsFile();
        fsFile.setAppCode("els");
        fsFile.setCorpCode("zhaojie");
        fsFile.setBusinessCode("course001");
        fsFile.setBusinessDir("cour");
        fsFile.setBusinessId(UUID.randomUUID().toString().replace("-", ""));
        fsFile.setFileSize(10000l);
        fsFile.setProcessor(ProcessorTypeEnum.VID);
        fsFile.setStoredFileName("wodezuguo.flv");
        fsFile.setSuffix("flv");
        fsFile.setStatus(ProcessStatusEnum.SUCCESS);
        fsFile.setVideoLevels(VideoTypeEnum.H.name());
        fsFile.setDurations("00:10:10");
        fsFile.setServerCode("0000");
        fsFileService.save(fsFile);
    }

    @Test
    public void testSaveZVideo() {
        FsFile fsFile = new FsFile();
        fsFile.setAppCode("els");
        fsFile.setCorpCode("zhaojie");
        fsFile.setBusinessCode("course001");
        fsFile.setBusinessDir("cour");
        fsFile.setBusinessId(UUID.randomUUID().toString().replace("-", ""));
        fsFile.setFileSize(10000l);
        fsFile.setProcessor(ProcessorTypeEnum.VID);
        fsFile.setStoredFileName("wodezuguo.zip");
        fsFile.setSuffix("zip");
        fsFile.setSubFileCount(3);
        fsFile.setStatus(ProcessStatusEnum.SUCCESS);
        fsFile.setVideoLevels(VideoTypeEnum.H.name() + "|" + VideoTypeEnum.L.name()
                + "|" + VideoTypeEnum.M.name());
        fsFile.setDurations("00:10:10|00:10:10|00:10:10");
        fsFile.setServerCode("0000");
        fsFileService.save(fsFile);
    }

    @Test
    @Rollback(false)
    public void testSaveAudio() {
        FsFile fsFile = new FsFile();
        fsFile.setAppCode("els");
        fsFile.setCorpCode("zhaojie");
        fsFile.setBusinessCode("course001");
        fsFile.setBusinessDir("cour");
        fsFile.setBusinessId(UUID.randomUUID().toString().replace("-", ""));
        fsFile.setFileSize(10000l);
        fsFile.setProcessor(ProcessorTypeEnum.AUD);
        fsFile.setStoredFileName("wodezuguo.mp3");
        fsFile.setSuffix("mp3");
        fsFile.setStatus(ProcessStatusEnum.SUCCESS);
        fsFile.setDurations("00:10:10");
        fsFile.setServerCode("0000");
        fsFileService.save(fsFile);
    }

    @Test
    @Rollback(false)
    public void testSaveZAudio() {
        FsFile fsFile = new FsFile();
        fsFile.setAppCode("els");
        fsFile.setCorpCode("zhaojie");
        fsFile.setBusinessCode("course001");
        fsFile.setBusinessDir("cour");
        fsFile.setBusinessId(UUID.randomUUID().toString().replace("-", ""));
        fsFile.setFileSize(10000l);
        fsFile.setProcessor(ProcessorTypeEnum.ZAUD);
        fsFile.setStoredFileName("wodezuguo.zip");
        fsFile.setSuffix("zip");
        fsFile.setStatus(ProcessStatusEnum.SUCCESS);
        fsFile.setSubFileCount(3);
        fsFile.setDurations("00:10:10|00:10:10|00:10:10");
        fsFile.setServerCode("0000");
        fsFileService.save(fsFile);
    }


}
