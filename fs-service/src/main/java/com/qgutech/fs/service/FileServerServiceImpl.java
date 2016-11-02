package com.qgutech.fs.service;


import com.qgutech.fs.domain.ProcessorTypeEnum;
import com.qgutech.fs.domain.StoredFile;
import com.qgutech.fs.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@BeforeMethod(name = "setExecutionContext", parameters = {String.class, String.class})
@AfterMethod(name = "clearExecutionContext")
public class FileServerServiceImpl implements FileServerService {

    @Resource
    private SessionFactory sessionFactory;
    @Resource
    private StoredFileService storedFileService;

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
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public String getOriginFileUrl(String corpCode, String appCode, String storedFileId) {
        Assert.hasText(storedFileId, "storedFileId is empty!");
        return getBatchFileUrlMap(corpCode, appCode, Arrays.asList(storedFileId)).get(storedFileId);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public Map<String, String> getBatchOriginFileUrlMap(String corpCode, String appCode
            , List<String> storedFileIdList) {
        Assert.notEmpty(storedFileIdList, "StoredFileIds is empty!");
        List<StoredFile> storedFiles = storedFileService.listByIds(storedFileIdList);
        if (CollectionUtils.isEmpty(storedFileIdList)) {
            return new HashMap<String, String>(0);
        }

        Map<String, String> batchOriginFileUrlMap = new HashMap<String, String>(storedFiles.size());
        StringBuilder builder = new StringBuilder();
        for (StoredFile storedFile : storedFiles) {
            //todo 获取服务器地址，权限验证字符串 http://hf.21tb.com/fs/权限验证字符串/corpCode/appCode/src/
            builder.append(corpCode).append(FsConstants.PATH_SEPARATOR).append(appCode)
                    .append(FsConstants.PATH_SEPARATOR).append(FsConstants.FILE_DIR_SRC);
            String businessDir = storedFile.getBusinessDir();
            if (StringUtils.isNotEmpty(businessDir)) {
                builder.append(FsConstants.PATH_SEPARATOR).append(businessDir);
            }

            ProcessorTypeEnum processor = storedFile.getProcessor();
            if (ProcessorTypeEnum.DOC.equals(processor)
                    || ProcessorTypeEnum.AUD.equals(processor)
                    || ProcessorTypeEnum.VID.equals(processor)
                    || ProcessorTypeEnum.IMG.equals(processor)
                    || ProcessorTypeEnum.FILE.equals(processor)
                    || ProcessorTypeEnum.ZIP.equals(processor)) {
                builder.append(FsConstants.PATH_SEPARATOR).append(processor.name().toLowerCase());
            } else {
                builder.append(FsConstants.PATH_SEPARATOR).append(ProcessorTypeEnum.ZIP.name().toLowerCase());
            }

            builder.append(FsConstants.PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(storedFile.getCreateTime()))
                    .append(FsConstants.PATH_SEPARATOR).append(storedFile.getBusinessId())
                    .append(FsConstants.PATH_SEPARATOR).append(storedFile.getStoredFileId())
                    .append(FsConstants.POINT).append(storedFile.getSuffix());
            batchOriginFileUrlMap.put(storedFile.getStoredFileId(), builder.toString());
            builder.delete(0, builder.length());
        }

        return batchOriginFileUrlMap;
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
