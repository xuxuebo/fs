package com.qgutech.fs.service;


import com.qgutech.fs.utils.FsConstants;
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

    @Test
    public void testGetVideoUrls() {
        //402881d65828efe3015828efe5e90000
        //402881d65828f018015828f01ae40000
        fileServerService.getVideoUrls("zhaojie", "els", "402881d65828f018015828f01ae40000");
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
