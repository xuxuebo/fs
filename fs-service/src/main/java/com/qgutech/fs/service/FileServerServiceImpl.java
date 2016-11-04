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
    public String getAudioUrl(String storedFileId) {
        Assert.hasText(storedFileId, "StoredFileId is empty!");
        List<String> audioUrls = getAudioUrls(storedFileId);
        if (CollectionUtils.isEmpty(audioUrls)) {
            return null;
        }

        return audioUrls.get(0);
    }

    @Override
    public List<String> getAudioUrls(String storedFileId) {
        Assert.hasText(storedFileId, "StoredFileId is empty!");
        return getBatchAudioUrlsMap(Arrays.asList(storedFileId)).get(storedFileId);
    }

    @Override
    public Map<String, String> getBatchAudioUrlMap(List<String> storedFileIdList) {
        Assert.notEmpty(storedFileIdList, "storedFileIdList is empty!");
        Map<String, List<String>> batchAudioUrlsMap = getBatchAudioUrlsMap(storedFileIdList);
        if (MapUtils.isEmpty(batchAudioUrlsMap)) {
            return new HashMap<String, String>(0);
        }

        Map<String, String> batchAudioUrlMap = new HashMap<String, String>(batchAudioUrlsMap.size());
        for (String storedFileId : storedFileIdList) {
            List<String> audioUrls = batchAudioUrlsMap.get(storedFileId);
            if (CollectionUtils.isEmpty(audioUrls)) {
                continue;
            }

            batchAudioUrlMap.put(storedFileId, audioUrls.get(0));
        }

        return batchAudioUrlMap;
    }

    @Override
    public Map<String, List<String>> getBatchAudioUrlsMap(List<String> storedFileIdList) {
        Assert.notEmpty(storedFileIdList, "storedFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(storedFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, List<String>>(0);
        }

        Map<String, List<String>> batchAudioUrlsMap = new HashMap<String, List<String>>(fsFiles.size());
        for (FsFile fsFile : fsFiles) {
            ProcessorTypeEnum processor = fsFile.getProcessor();
            if (!ProcessorTypeEnum.AUD.equals(processor)
                    && !ProcessorTypeEnum.ZAUD.equals(processor)) {
                continue;
            }

            String storedFileId = fsFile.getStoredFileId();
            StringBuilder sb = new StringBuilder();
            sb.append(fsFile.getCorpCode()).append(FsConstants.PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(FsConstants.PATH_SEPARATOR)
                    .append(FsConstants.FILE_DIR_GEN).append(FsConstants.PATH_SEPARATOR)
                    .append(FsConstants.DEFAULT_AUDIO_TYPE).append(FsConstants.PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(FsConstants.PATH_SEPARATOR).append(storedFileId);
            Integer subFileCount = fsFile.getSubFileCount();
            subFileCount = subFileCount == null || subFileCount <= 0 ? 1 : subFileCount;
            List<String> audioUrls = new ArrayList<String>(subFileCount);
            for (int i = 0; i < subFileCount; i++) {
                StringBuilder builder = new StringBuilder();
                if (ProcessorTypeEnum.ZAUD.equals(processor)) {
                    builder.append(FsConstants.PATH_SEPARATOR).append(i + 1);
                }

                builder.append(FsConstants.PATH_SEPARATOR).append(FsConstants.DEFAULT_AUDIO_NAME);
                audioUrls.add(sb.toString() + builder.toString());
                builder.delete(0, builder.length());
            }

            sb.delete(0, sb.length());
            batchAudioUrlsMap.put(storedFileId, audioUrls);
        }

        return batchAudioUrlsMap;
    }

    @Override
    public String getZipUrl(String storedFileId) {
        Assert.hasText(storedFileId, "StoredFileId is empty!");
        return getBatchZipUrlMap(Arrays.asList(storedFileId)).get(storedFileId);
    }

    @Override
    public String getImageUrl(String storedFileId) {
        Assert.hasText(storedFileId, "StoredFileId is empty!");
        return getBatchImageUrlMap(Arrays.asList(storedFileId)).get(storedFileId);
    }

    @Override
    public Map<String, String> getBatchImageUrlMap(List<String> storedFileIdList) {
        Assert.notEmpty(storedFileIdList, "storedFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(storedFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, String>(0);
        }

        Map<String, String> batchImageUrlMap = new HashMap<String, String>(fsFiles.size());
        for (FsFile fsFile : fsFiles) {
            ProcessorTypeEnum processor = fsFile.getProcessor();
            if (!ProcessorTypeEnum.IMG.equals(processor)
                    && !ProcessorTypeEnum.ZIMG.equals(processor)
                    && !ProcessorTypeEnum.DOC.equals(processor)
                    && !ProcessorTypeEnum.ZDOC.equals(processor)) {
                continue;
            }

            String storedFileId = fsFile.getStoredFileId();
            StringBuilder sb = new StringBuilder();
            sb.append(fsFile.getCorpCode()).append(FsConstants.PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(FsConstants.PATH_SEPARATOR)
                    .append(FsConstants.FILE_DIR_GEN).append(FsConstants.PATH_SEPARATOR)
                    .append(FsConstants.FILE_DIR_IMG).append(FsConstants.PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(FsConstants.PATH_SEPARATOR).append(storedFileId);
            if (ProcessorTypeEnum.IMG.equals(processor)) {
                sb.append(FsConstants.PATH_SEPARATOR).append(FsConstants.ORIGIN_IMAGE_NAME);
            } else if (ProcessorTypeEnum.DOC.equals(processor)
                    || ProcessorTypeEnum.ZIMG.equals(processor)) {
                sb.append(FsConstants.PATH_SEPARATOR).append(FsConstants.FIRST)
                        .append(FsConstants.ORIGIN_IMAGE_NAME);
            } else {
                sb.append(FsConstants.PATH_SEPARATOR).append(FsConstants.FIRST)
                        .append(FsConstants.PATH_SEPARATOR).append(FsConstants.FIRST)
                        .append(FsConstants.ORIGIN_IMAGE_NAME);
            }

            batchImageUrlMap.put(storedFileId, sb.toString());
            sb.delete(0, sb.length());
        }

        return batchImageUrlMap;
    }

    @Override
    public Map<String, String> getBatchZipUrlMap(List<String> storedFileIdList) {
        Assert.notEmpty(storedFileIdList, "storedFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(storedFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, String>(0);
        }

        Map<String, String> batchZipUrlMap = new HashMap<String, String>(fsFiles.size());
        for (FsFile fsFile : fsFiles) {
            if (!ProcessorTypeEnum.ZIP.equals(fsFile.getProcessor())) {
                continue;
            }

            String storedFileId = fsFile.getStoredFileId();
            StringBuilder builder = new StringBuilder();
            builder.append(fsFile.getCorpCode()).append(FsConstants.PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(FsConstants.PATH_SEPARATOR)
                    .append(FsConstants.FILE_DIR_GEN).append(FsConstants.PATH_SEPARATOR)
                    .append(FsConstants.FILE_DIR_UNZIP).append(FsConstants.PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(FsConstants.PATH_SEPARATOR).append(storedFileId)
                    .append(FsConstants.PATH_SEPARATOR).append(FsConstants.ZIP_INDEX_FILE);
            batchZipUrlMap.put(storedFileId, builder.toString());
            builder.delete(0, builder.length());
        }

        return batchZipUrlMap;
    }

    @Override
    public String getFileUrl(String storedFileId) {
        Assert.hasText(storedFileId, "StoredFileId is empty!");
        return getBatchFileUrlMap(Arrays.asList(storedFileId)).get(storedFileId);
    }

    @Override
    public Map<String, String> getBatchFileUrlMap(List<String> storedFileIdList) {
        Assert.notEmpty(storedFileIdList, "storedFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(storedFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, String>(0);
        }

        Map<String, String> batchFileUrlMap = new HashMap<String, String>(fsFiles.size());
        List<String> files = new ArrayList<String>();
        List<String> zips = new ArrayList<String>();
        List<String> images = new ArrayList<String>();
        List<String> videos = new ArrayList<String>();
        List<String> audios = new ArrayList<String>();
        for (FsFile fsFile : fsFiles) {
            String storedFileId = fsFile.getStoredFileId();
            switch (fsFile.getProcessor()) {
                case FILE:
                    files.add(storedFileId);
                    break;
                case ZIP:
                    zips.add(storedFileId);
                    break;
                case DOC:
                    images.add(storedFileId);
                    break;
                case ZDOC:
                    images.add(storedFileId);
                    break;
                case IMG:
                    images.add(storedFileId);
                    break;
                case ZIMG:
                    images.add(storedFileId);
                    break;
                case VID:
                    videos.add(storedFileId);
                    break;
                case ZVID:
                    videos.add(storedFileId);
                    break;
                case AUD:
                    audios.add(storedFileId);
                    break;
                case ZAUD:
                    audios.add(storedFileId);
                    break;
            }
        }

        if (CollectionUtils.isNotEmpty(files)) {
            Map<String, String> batchOriginFileUrlMap = getBatchOriginFileUrlMap(files);
            if (MapUtils.isNotEmpty(batchOriginFileUrlMap)) {
                batchFileUrlMap.putAll(batchOriginFileUrlMap);
            }
        }

        if (CollectionUtils.isNotEmpty(zips)) {
            Map<String, String> batchZipUrlMap = getBatchZipUrlMap(zips);
            if (MapUtils.isNotEmpty(batchZipUrlMap)) {
                batchFileUrlMap.putAll(batchZipUrlMap);
            }
        }

        if (CollectionUtils.isNotEmpty(images)) {
            Map<String, String> batchImageUrlMap = getBatchImageUrlMap(images);
            if (MapUtils.isNotEmpty(batchImageUrlMap)) {
                batchFileUrlMap.putAll(batchImageUrlMap);
            }
        }

        if (CollectionUtils.isNotEmpty(videos)) {
            Map<String, List<Map<String, String>>> batchVideoUrlsMap = getBatchVideoUrlsMap(videos);
            if (MapUtils.isNotEmpty(batchVideoUrlsMap)) {
                for (String video : videos) {
                    List<Map<String, String>> videoUrls = batchVideoUrlsMap.get(video);
                    if (CollectionUtils.isEmpty(videoUrls) || videoUrls.get(0) == null) {
                        continue;
                    }

                    String originVideoUrl = videoUrls.get(0).get(VideoTypeEnum.O.name());
                    if (StringUtils.isNotEmpty(originVideoUrl)) {
                        batchFileUrlMap.put(video, originVideoUrl);
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(audios)) {
            Map<String, String> batchAudioUrlMap = getBatchAudioUrlMap(audios);
            if (MapUtils.isNotEmpty(batchAudioUrlMap)) {
                batchFileUrlMap.putAll(batchAudioUrlMap);
            }
        }

        return batchFileUrlMap;
    }

    @Override
    public Integer getSubFileCount(String storedFileId) {
        return null;
    }

    @Override
    public List<Integer> getSubFileCountList(String storedFileId) {
        return null;
    }

    @Override
    public Map<String, Integer> getSubFileCountMap(List<String> storedFileIdList) {
        return null;
    }
}
