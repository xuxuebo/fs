package com.qgutech.fs.service;


import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;


@ContextConfiguration(locations = {"classpath:/spring-config/spring-context.xml", "classpath:/ehcache.xml"})
public class BaseServiceTest  extends AbstractTransactionalJUnit4SpringContextTests {


}
