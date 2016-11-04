package com.qgutech.fs.service;


import com.qgutech.fs.utils.FsConstants;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;

public class FileServerServiceTest extends BaseServiceTest {
    @Resource
    private FileServerService fileServerService;

    @Test
    public void testGetOriginFileUrl() {
        fileServerService.getOriginFileUrl("402881d65828f018015828f01ae40000");
    }

    @Test
    public void testGetVideoUrls() {
        fileServerService.getVideoUrls("402881d65828f018015828f01ae40000");
    }

    @Test
    public void testGetVideoTypeUrlMap() {
        fileServerService.getVideoTypeUrlMap("402881d65828efe3015828efe5e90000");
    }

    @Test
    public void testGetBatchVideoUrlsMap() {
        fileServerService.getBatchVideoUrlsMap(Arrays.asList("402881d65828efe3015828efe5e90000"
                , "402881d65828f018015828f01ae40000"));
    }

    @Test
    public void testGetVideoCoverUrls() {
        fileServerService.getVideoCoverUrls("402881d65828f018015828f01ae40000");
    }

    @Test
    public void testGetVideoCoverUrl() {
        fileServerService.getVideoCoverUrl("402881d65828efe3015828efe5e90000");
    }

    @Test
    public void testGetBatchVideoCoverUrlsMap() {
        fileServerService.getBatchVideoCoverUrlsMap(Arrays.asList("402881d65828efe3015828efe5e90000"
                , "402881d65828f018015828f01ae40000"));
    }

    @Test
    public void testFetBatchAudioUrlsMap() {
        fileServerService.getBatchAudioUrlsMap(Arrays.asList("402881d6582de20b01582de20e3a0000"
                , "402881d6582de22e01582de2311d0000"));
    }


    @Test
    public void test() {
        String[] split = "a|b".split(FsConstants.VERTICAL_LINE_REGEX);
        for (String s : split) {
            System.out.println("------------");
            System.out.println(s);
            System.out.println("------------");
        }
    }


}
