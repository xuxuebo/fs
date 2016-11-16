package com.qgutech.fs;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.utils.HttpUtils;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import javax.sql.DataSource;


@ContextConfiguration(locations = {"classpath:/spring-config/spring-context.xml"})
public class BaseServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Override
    public void setDataSource(DataSource dataSource) {
        //np
    }

    @Test
    public void test() {
        FsFile fsFile = HttpUtils.getFsFile("402881d65828f018015828f01ae40000");
    }

}
