package com.qgutech.fs.service;


import com.qgutech.fs.domain.ProcessorTypeEnum;
import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.VideoTypeEnum;
import com.qgutech.fs.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

@Service("fileServerService")
public class FileServerServiceImpl implements FileServerService {

    @Resource
    private FsFileService fsFileService;

    @Override
    public String getOriginFileUrl(String storedFileId) {
        Assert.hasText(storedFileId, "storedFileId is empty!");
        return getBatchOriginFileUrlMap(Arrays.asList(storedFileId)).get(storedFileId);
    }

    @Override
    public Map<String, String> getBatchOriginFileUrlMap(List<String> storedFileIdList) {
        Assert.notEmpty(storedFileIdList, "StoredFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(storedFileIdList);
        if (CollectionUtils.isEmpty(storedFileIdList)) {
            return new HashMap<String, String>(0);
        }

        //todo 获取服务器地址，权限验证字符串 http://hf.21tb.com/fs/权限验证字符串/corpCode/appCode/src/
        Map<String, String> batchOriginFileUrlMap = new HashMap<String, String>(fsFiles.size());
        StringBuilder builder = new StringBuilder();
        for (FsFile fsFile : fsFiles) {
            builder.append(fsFile.getCorpCode()).append(FsConstants.PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(FsConstants.PATH_SEPARATOR)
                    .append(FsConstants.FILE_DIR_SRC);
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

            String storedFileId = fsFile.getStoredFileId();
            builder.append(FsConstants.PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(FsConstants.PATH_SEPARATOR).append(fsFile.getBusinessId())
                    .append(FsConstants.PATH_SEPARATOR).append(storedFileId)
                    .append(FsConstants.POINT).append(fsFile.getSuffix());
            batchOriginFileUrlMap.put(storedFileId, builder.toString());
            builder.delete(0, builder.length());
        }

        return batchOriginFileUrlMap;
    }

    @Override
    public List<Map<String, String>> getVideoUrls(String storedFileId) {
        Assert.hasText(storedFileId, "storedFileId is empty!");
        return getBatchVideoUrlsMap(Arrays.asList(storedFileId)).get(storedFileId);
    }

    @Override
    public Map<String, String> getVideoTypeUrlMap(String storedFileId) {
        Assert.hasText(storedFileId, "storedFileId is empty!");
        List<Map<String, String>> videoUrls = getVideoUrls(storedFileId);
        if (CollectionUtils.isEmpty(videoUrls)) {
            return new HashMap<String, String>(0);
        }

        return videoUrls.get(0);
    }

    @Override
    public Map<String, List<Map<String, String>>> getBatchVideoUrlsMap(List<String> storedFileIdList) {
        Assert.notEmpty(storedFileIdList, "StoredFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(storedFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, List<Map<String, String>>>(0);
        }

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

            String storedFileId = fsFile.getStoredFileId();
            StringBuilder sb = new StringBuilder();
            sb.append(fsFile.getCorpCode()).append(FsConstants.PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(FsConstants.PATH_SEPARATOR)
                    .append(FsConstants.FILE_DIR_GEN).append(FsConstants.PATH_SEPARATOR)
                    .append(FsConstants.DEFAULT_VIDEO_TYPE).append(FsConstants.PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(FsConstants.PATH_SEPARATOR).append(storedFileId);
            String[] vLevels = videoLevels.split(FsConstants.VERTICAL_LINE_REGEX);
            List<Map<String, String>> videoUrls = new ArrayList<Map<String, String>>(vLevels.length);
            for (int i = 0; i < vLevels.length; i++) {
                VideoTypeEnum videoTypeEnum = VideoTypeEnum.valueOf(vLevels[i]);
                VideoTypeEnum[] videoTypeEnums = VideoTypeEnum.getVideoTypeEnums(videoTypeEnum);
                Map<String, String> videoTypeUrlMap = new HashMap<String, String>(videoTypeEnums.length);
                videoUrls.add(videoTypeUrlMap);
                StringBuilder builder = new StringBuilder();
                for (VideoTypeEnum typeEnum : videoTypeEnums) {
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

            sb.delete(0, sb.length());
            batchVideoUrlsMap.put(storedFileId, videoUrls);
        }

        return batchVideoUrlsMap;
    }

    @Override
    public List<String> getVideoCoverUrls(String storedFileId) {
        Assert.hasText(storedFileId, "StoredFileId is empty!");
        return getBatchVideoCoverUrlsMap(Arrays.asList(storedFileId)).get(storedFileId);
    }

    @Override
    public String getVideoCoverUrl(String storedFileId) {
        Assert.hasText(storedFileId, "StoredFileId is empty!");
        List<String> videoCoverUrls = getVideoCoverUrls(storedFileId);
        if (CollectionUtils.isEmpty(videoCoverUrls)) {
            return null;
        }

        return videoCoverUrls.get(0);
    }

    @Override
    public Map<String, String> getBatchVideoCoverUrlMap(List<String> storedFileIdList) {
        Assert.notEmpty(storedFileIdList, "StoredFileIdList is empty!");
        Map<String, List<String>> batchVideoCoverUrlsMap = getBatchVideoCoverUrlsMap(storedFileIdList);
        if (MapUtils.isEmpty(batchVideoCoverUrlsMap)) {
            return new HashMap<String, String>(0);
        }
        Map<String, String> batchVideoCoverUrlMap =
                new HashMap<String, String>(batchVideoCoverUrlsMap.size());
        for (Map.Entry<String, List<String>> entry : batchVideoCoverUrlsMap.entrySet()) {
            List<String> covers = entry.getValue();
            if (CollectionUtils.isEmpty(covers)) {
                continue;
            }

            batchVideoCoverUrlMap.put(entry.getKey(), covers.get(0));
        }

        return batchVideoCoverUrlMap;
    }

    @Override
    public Map<String, List<String>> getBatchVideoCoverUrlsMap(List<String> storedFileIdList) {
        Assert.notEmpty(storedFileIdList, "StoredFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(storedFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, List<String>>(0);
        }

        Map<String, List<String>> batchVideoCoverUrlsMap = new HashMap<String, List<String>>(fsFiles.size());
        for (FsFile fsFile : fsFiles) {
            ProcessorTypeEnum processor = fsFile.getProcessor();
            String videoLevels = fsFile.getVideoLevels();
            if ((!ProcessorTypeEnum.VID.equals(processor)
                    && !ProcessorTypeEnum.ZVID.equals(processor))
                    || StringUtils.isEmpty(videoLevels)) {
                continue;
            }

            String storedFileId = fsFile.getStoredFileId();
            StringBuilder sb = new StringBuilder();
            sb.append(fsFile.getCorpCode()).append(FsConstants.PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(FsConstants.PATH_SEPARATOR)
                    .append(FsConstants.FILE_DIR_GEN).append(FsConstants.PATH_SEPARATOR)
                    .append(FsConstants.DEFAULT_VIDEO_TYPE).append(FsConstants.PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(FsConstants.PATH_SEPARATOR).append(storedFileId);
            String[] vLevels = videoLevels.split(FsConstants.VERTICAL_LINE_REGEX);
            List<String> videoCoverUrls = new ArrayList<String>(vLevels.length);
            for (int i = 0; i < vLevels.length; i++) {
                StringBuilder builder = new StringBuilder();
                if (ProcessorTypeEnum.ZVID.equals(processor)) {
                    builder.append(FsConstants.PATH_SEPARATOR).append(i + 1);
                }

                builder.append(FsConstants.PATH_SEPARATOR).append(FsConstants.VIDEO_COVER);
                videoCoverUrls.add(sb.toString() + builder.toString());
                builder.delete(0, builder.length());
            }

            sb.delete(0, sb.length());
            batchVideoCoverUrlsMap.put(storedFileId, videoCoverUrls);
        }

        return batchVideoCoverUrlsMap;
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
