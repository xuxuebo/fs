package com.qgutech.fs.service;


import com.qgutech.fs.utils.ExecutionContext;
import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;


@ContextConfiguration(locations = {"classpath:/spring-config/spring-context.xml"})
public class BaseServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Before
    public void init(){
        ExecutionContext.setCorpCode("default");
        ExecutionContext.setAppCode("km");
        ExecutionContext.setSession("session");
    }

}
