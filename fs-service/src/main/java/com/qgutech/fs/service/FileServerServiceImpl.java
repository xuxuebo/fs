package com.qgutech.fs.service;


import com.qgutech.fs.domain.ProcessorTypeEnum;
import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.VideoTypeEnum;
import com.qgutech.fs.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

@BeforeMethod(name = "setExecutionContext", parameters = {String.class, String.class})
@AfterMethod(name = "clearExecutionContext")
public class FileServerServiceImpl implements FileServerService {

    @Resource
    private SessionFactory sessionFactory;
    @Resource
    private FsFileService fsFileService;

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
        return getBatchOriginFileUrlMap(corpCode, appCode, Arrays.asList(storedFileId)).get(storedFileId);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public Map<String, String> getBatchOriginFileUrlMap(String corpCode, String appCode
            , List<String> storedFileIdList) {
        Assert.notEmpty(storedFileIdList, "StoredFileIds is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(storedFileIdList);
        if (CollectionUtils.isEmpty(storedFileIdList)) {
            return new HashMap<String, String>(0);
        }

        Map<String, String> batchOriginFileUrlMap = new HashMap<String, String>(fsFiles.size());
        StringBuilder builder = new StringBuilder();
        for (FsFile fsFile : fsFiles) {
            //todo 获取服务器地址，权限验证字符串 http://hf.21tb.com/fs/权限验证字符串/corpCode/appCode/src/
            builder.append(corpCode).append(FsConstants.PATH_SEPARATOR).append(appCode)
                    .append(FsConstants.PATH_SEPARATOR).append(FsConstants.FILE_DIR_SRC);
            String businessDir = fsFile.getBusinessDir();
            if (StringUtils.isNotEmpty(businessDir)) {
                builder.append(FsConstants.PATH_SEPARATOR).append(businessDir);
            }

            ProcessorTypeEnum processor = fsFile.getProcessor();
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
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(FsConstants.PATH_SEPARATOR).append(fsFile.getBusinessId())
                    .append(FsConstants.PATH_SEPARATOR).append(fsFile.getStoredFileId())
                    .append(FsConstants.POINT).append(fsFile.getSuffix());
            batchOriginFileUrlMap.put(fsFile.getStoredFileId(), builder.toString());
            builder.delete(0, builder.length());
        }

        return batchOriginFileUrlMap;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public List<Map<String, String>> getVideoUrls(String corpCode, String appCode, String storedFileId) {
        Assert.hasText(storedFileId, "storedFileId is empty!");
        FsFile fsFile = fsFileService.get(storedFileId);
        if (fsFile == null) {
            return new ArrayList<Map<String, String>>(0);
        }

        return getBatchVideoUrlsMap(corpCode, appCode, Arrays.asList(storedFileId)).get(storedFileId);
    }

    @Override
    public Map<String, String> getVideoTypeUrlMap(String corpCode, String appCode, String storedFileId) {
        Assert.hasText(storedFileId, "storedFileId is empty!");
        List<Map<String, String>> videoUrls = getVideoUrls(corpCode, appCode, storedFileId);
        if (CollectionUtils.isEmpty(videoUrls)) {
            return new HashMap<String, String>(0);
        }

        return videoUrls.get(0);
    }

    @Override
    public Map<String, List<Map<String, String>>> getBatchVideoUrlsMap(String corpCode
            , String appCode, List<String> storedFileIdList) {
        Assert.notEmpty(storedFileIdList, "StoredFileIds is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(storedFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, List<Map<String, String>>>(0);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(corpCode).append(FsConstants.PATH_SEPARATOR).append(appCode)
                .append(FsConstants.PATH_SEPARATOR).append(FsConstants.FILE_DIR_GEN)
                .append(FsConstants.PATH_SEPARATOR).append(FsConstants.DEFAULT_VIDEO_TYPE)
                .append(FsConstants.PATH_SEPARATOR);
        Map<String, List<Map<String, String>>> batchVideoUrlsMap =
                new HashMap<String, List<Map<String, String>>>(fsFiles.size());
        for (FsFile fsFile : fsFiles) {
            ProcessorTypeEnum processor = fsFile.getProcessor();
            String videoLevels = fsFile.getVideoLevels();
            if ((!ProcessorTypeEnum.VID.equals(processor)
                    && !ProcessorTypeEnum.ZVID.equals(processor))
                    || StringUtils.isEmpty(videoLevels)) {
                continue;
            }

            String dateFormat = FsUtils.formatDateToYYMM(fsFile.getCreateTime());
            String storedFileId = fsFile.getStoredFileId();
            String[] vLevels = videoLevels.split(FsConstants.VERTICAL_LINE_REGEX);
            List<Map<String, String>> videoUrls = new ArrayList<Map<String, String>>(vLevels.length);
            for (int i = 0; i < vLevels.length; i++) {
                VideoTypeEnum videoTypeEnum = VideoTypeEnum.valueOf(vLevels[i]);
                VideoTypeEnum[] videoTypeEnums = VideoTypeEnum.getVideoTypeEnums(videoTypeEnum);
                Map<String, String> videoTypeUrlMap = new HashMap<String, String>(videoTypeEnums.length);
                videoUrls.add(videoTypeUrlMap);
                StringBuilder builder = new StringBuilder();
                for (VideoTypeEnum typeEnum : videoTypeEnums) {
                    builder.append(dateFormat).append(FsConstants.PATH_SEPARATOR).append(storedFileId);
                    if (ProcessorTypeEnum.ZVID.equals(processor)) {
                        builder.append(FsConstants.PATH_SEPARATOR).append(i + 1);
                    }

                    String videoType = typeEnum.name();
                    builder.append(FsConstants.PATH_SEPARATOR).append(videoType.toLowerCase())
                            .append(FsConstants.PATH_SEPARATOR).append(videoType.toLowerCase())
                            .append(FsConstants.DEFAULT_VIDEO_SUFFIX);
                    videoTypeUrlMap.put(videoType, sb.toString() + builder.toString());
                    builder.delete(0, builder.length());
                }
            }

            batchVideoUrlsMap.put(storedFileId, videoUrls);
        }

        return batchVideoUrlsMap;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public List<String> getVideoCoverUrls(String corpCode, String appCode, String storedFileId) {
        Assert.hasText(storedFileId, "storedFileId is empty!");
        return null;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public String getVideoCoverUrl(String corpCode, String appCode, String storedFileId) {
        Assert.hasText(storedFileId, "storedFileId is empty!");
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
