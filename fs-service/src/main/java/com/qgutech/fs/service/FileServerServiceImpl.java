package com.qgutech.fs.service;


import com.qgutech.fs.utils.AfterMethod;
import com.qgutech.fs.utils.BeforeMethod;
import com.qgutech.fs.utils.ExecutionContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@BeforeMethod(name = "setExecutionContext", parameters = {String.class, String.class})
@AfterMethod(name = "clearExecutionContext")
public class FileServerServiceImpl implements FileServerService {

    @Resource
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    public void setExecutionContext(String corpCode, String appCode) {
        Assert.hasText(corpCode, "CorpCode is empty!");
        Assert.hasText(appCode, "AppCode is empty!");
        ExecutionContext.setCorpCode(corpCode);
        ExecutionContext.setAppCode(appCode);
    }

    public void clearExecutionContext() {
        ExecutionContext.setContextMap(null);
    }

    @Override
    public String getOriginFileUrl(String corpCode, String appCode, String storedFileId) {
        return null;
    }

    @Override
    public Map<String, String> getBatchOriginFileUrlMap(String corpCode, String appCode, List<String> storedFileIdList) {
        return null;
    }

    @Override
    public String getVideoCoverUrl(String corpCode, String appCode, String storedFileId) {
        return null;
    }

    @Override
    public String getHighVideoUrl(String corpCode, String appCode, String storedFileId) {
        return null;
    }

    @Override
    public String getMiddleVideoUrl(String corpCode, String appCode, String storedFileId) {
        return null;
    }

    @Override
    public String getLowVideoUrl(String corpCode, String appCode, String storedFileId) {
        return null;
    }

    @Override
    public String getOriginVideoUrl(String corpCode, String appCode, String storedFileId) {
        return null;
    }

    @Override
    public String getFileUrl(String corpCode, String appCode, String storedFileId) {
        return null;
    }

    @Override
    public Map<String, String> getBatchFileUrlMap(String corpCode, String appCode, List<String> storedFileIdList) {
        return null;
    }

    @Override
    public Integer getSubFileCount(String corpCode, String appCode, String storedFileId) {
        return null;
    }

    @Override
    public List<Integer> getSubFileCountList(String corpCode, String appCode, String storedFileId) {
        return null;
    }

    @Override
    public Map<String, Integer> getSubFileCountMap(String corpCode, String appCode, List<String> storedFileIdList) {
        return null;
    }
}
