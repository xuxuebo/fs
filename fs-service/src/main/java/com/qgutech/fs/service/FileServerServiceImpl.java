package com.qgutech.fs.service;


import com.qgutech.fs.domain.FsServer;
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
public class FileServerServiceImpl implements FileServerService, FsConstants {

    @Resource
    private FsFileService fsFileService;
    @Resource
    private FsServerService fsServerService;

    @Override
    public String getOriginFileUrl(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        return getBatchOriginFileUrlMap(Arrays.asList(fsFileId)).get(fsFileId);
    }

    private Map<String, FsServer> getFileIdFsServerMap(List<FsFile> fsFiles) {
        Set<String> corpCodes = new HashSet<String>();
        Set<String> serverCodes = new HashSet<String>();
        for (FsFile fsFile : fsFiles) {
            corpCodes.add(fsFile.getCorpCode());
            serverCodes.add(fsFile.getServerCode());
        }

        List<FsServer> serverList = fsServerService.getDownloadFsServerList(corpCodes, serverCodes);
        if (CollectionUtils.isEmpty(serverList)) {
            return new HashMap<String, FsServer>(0);
        }

        Map<String, List<FsServer>> corpServerCodeFsServersMap = new HashMap<String, List<FsServer>>();
        for (FsServer fsServer : serverList) {
            String corpCode = fsServer.getCorpCode();
            String serverCode = fsServer.getServerCode();
            String corpServerCodeKey = corpCode + VERTICAL_LINE + serverCode;
            List<FsServer> fsServers = corpServerCodeFsServersMap.get(corpServerCodeKey);
            if (fsServers == null) {
                fsServers = new ArrayList<FsServer>();
                corpServerCodeFsServersMap.put(corpServerCodeKey, fsServers);
            }

            fsServers.add(fsServer);
        }

        Map<String, FsServer> fileIdFsServerMap = new HashMap<String, FsServer>(fsFiles.size());
        for (FsFile fsFile : fsFiles) {
            String corpCode = fsFile.getCorpCode();
            String serverCode = fsFile.getServerCode();
            String corpServerCodeKey = corpCode + VERTICAL_LINE + serverCode;
            List<FsServer> fsServers = corpServerCodeFsServersMap.get(corpServerCodeKey);
            if (CollectionUtils.isEmpty(fsServers)) {
                fsServers = corpServerCodeFsServersMap.get(DEFAULT_CORP_CODE + VERTICAL_LINE + serverCode);
            }

            Assert.notEmpty(fsServers, "Both default and " + corpCode +
                    " hasn't server[serverCode:" + serverCode + "]!");
            fileIdFsServerMap.put(fsFile.getId(), fsServers.get((int) (Math.random() * fsServers.size())));
        }

        return fileIdFsServerMap;
    }

    @Override
    public Map<String, String> getBatchOriginFileUrlMap(List<String> fsFileIdList) {
        Assert.notEmpty(fsFileIdList, "FsFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(fsFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, String>(0);
        }

        Map<String, FsServer> fileIdFsServerMap = getFileIdFsServerMap(fsFiles);
        Map<String, String> batchOriginFileUrlMap = new HashMap<String, String>(fsFiles.size());
        StringBuilder builder = new StringBuilder();
        for (FsFile fsFile : fsFiles) {
            String fsFileId = fsFile.getId();
            FsServer fsServer = fileIdFsServerMap.get(fsFileId);
            builder.append(PropertiesUtils.getHttpProtocol()).append(HTTP_COLON).append(fsServer.getHost())
                    .append(PATH_SEPARATOR).append(PropertiesUtils.getServerName()).append(FILE_URL_GET_FILE)
                    .append(Signer.sign(fsServer, fsFile, ExecutionContext.getSession()))
                    .append(PATH_SEPARATOR).append(fsFile.getCorpCode()).append(PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(PATH_SEPARATOR).append(FILE_DIR_SRC);
            String businessDir = fsFile.getBusinessDir();
            if (StringUtils.isNotEmpty(businessDir)) {
                builder.append(PATH_SEPARATOR).append(businessDir);
            }

            ProcessorTypeEnum processor = fsFile.getProcessor();
            if (ProcessorTypeEnum.DOC.equals(processor)
                    || ProcessorTypeEnum.AUD.equals(processor)
                    || ProcessorTypeEnum.VID.equals(processor)
                    || ProcessorTypeEnum.IMG.equals(processor)
                    || ProcessorTypeEnum.FILE.equals(processor)
                    || ProcessorTypeEnum.ZIP.equals(processor)) {
                builder.append(PATH_SEPARATOR).append(processor.name().toLowerCase());
            } else {
                builder.append(PATH_SEPARATOR).append(ProcessorTypeEnum.ZIP.name().toLowerCase());
            }

            builder.append(PATH_SEPARATOR).append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(PATH_SEPARATOR).append(fsFile.getBusinessId())
                    .append(PATH_SEPARATOR).append(fsFileId).append(POINT).append(fsFile.getSuffix());
            batchOriginFileUrlMap.put(fsFileId, builder.toString());
            builder.delete(0, builder.length());
        }

        return batchOriginFileUrlMap;
    }

    @Override
    public List<Map<String, String>> getVideoUrls(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        return getBatchVideoUrlsMap(Arrays.asList(fsFileId)).get(fsFileId);
    }

    @Override
    public Map<String, String> getVideoTypeUrlMap(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        List<Map<String, String>> videoUrls = getVideoUrls(fsFileId);
        if (CollectionUtils.isEmpty(videoUrls)) {
            return new HashMap<String, String>(0);
        }

        return videoUrls.get(0);
    }

    @Override
    public Map<String, List<Map<String, String>>> getBatchVideoUrlsMap(List<String> fsFileIdList) {
        Assert.notEmpty(fsFileIdList, "FsFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(fsFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, List<Map<String, String>>>(0);
        }

        Map<String, List<Map<String, String>>> batchVideoUrlsMap =
                new HashMap<String, List<Map<String, String>>>(fsFiles.size());
        Map<String, FsServer> fileIdFsServerMap = getFileIdFsServerMap(fsFiles);
        for (FsFile fsFile : fsFiles) {
            ProcessorTypeEnum processor = fsFile.getProcessor();
            String videoLevels = fsFile.getVideoLevels();
            if ((!ProcessorTypeEnum.VID.equals(processor)
                    && !ProcessorTypeEnum.ZVID.equals(processor))
                    || StringUtils.isEmpty(videoLevels)) {
                continue;
            }

            String fsFileId = fsFile.getId();
            StringBuilder sb = new StringBuilder();
            FsServer fsServer = fileIdFsServerMap.get(fsFileId);
            sb.append(PropertiesUtils.getHttpProtocol()).append(HTTP_COLON).append(fsServer.getHost())
                    .append(PATH_SEPARATOR).append(PropertiesUtils.getServerName()).append(FILE_URL_GET_FILE)
                    .append(Signer.sign(fsServer, fsFile, ExecutionContext.getSession()))
                    .append(PATH_SEPARATOR).append(fsFile.getCorpCode()).append(PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(PATH_SEPARATOR).append(FILE_DIR_GEN)
                    .append(PATH_SEPARATOR).append(DEFAULT_VIDEO_TYPE).append(PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(PATH_SEPARATOR).append(fsFileId);
            String[] vLevels = videoLevels.split(VERTICAL_LINE_REGEX);
            List<Map<String, String>> videoUrls = new ArrayList<Map<String, String>>(vLevels.length);
            for (int i = 0; i < vLevels.length; i++) {
                VideoTypeEnum videoTypeEnum = VideoTypeEnum.valueOf(vLevels[i]);
                VideoTypeEnum[] videoTypeEnums = VideoTypeEnum.getVideoTypeEnums(videoTypeEnum);
                Map<String, String> videoTypeUrlMap = new HashMap<String, String>(videoTypeEnums.length);
                videoUrls.add(videoTypeUrlMap);
                StringBuilder builder = new StringBuilder();
                for (VideoTypeEnum typeEnum : videoTypeEnums) {
                    if (ProcessorTypeEnum.ZVID.equals(processor)) {
                        builder.append(PATH_SEPARATOR).append(i + 1);
                    }

                    String videoType = typeEnum.name();
                    builder.append(PATH_SEPARATOR).append(videoType.toLowerCase()).append(PATH_SEPARATOR)
                            .append(videoType.toLowerCase()).append(DEFAULT_VIDEO_SUFFIX);
                    videoTypeUrlMap.put(videoType, sb.toString() + builder.toString());
                    builder.delete(0, builder.length());
                }
            }

            sb.delete(0, sb.length());
            batchVideoUrlsMap.put(fsFileId, videoUrls);
        }

        return batchVideoUrlsMap;
    }

    @Override
    public List<String> getVideoCoverUrls(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        return getBatchVideoCoverUrlsMap(Arrays.asList(fsFileId)).get(fsFileId);
    }

    @Override
    public String getVideoCoverUrl(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        List<String> videoCoverUrls = getVideoCoverUrls(fsFileId);
        if (CollectionUtils.isEmpty(videoCoverUrls)) {
            return null;
        }

        return videoCoverUrls.get(0);
    }

    @Override
    public Map<String, String> getBatchVideoCoverUrlMap(List<String> fsFileIdList) {
        Assert.notEmpty(fsFileIdList, "FsFileIdList is empty!");
        Map<String, List<String>> batchVideoCoverUrlsMap = getBatchVideoCoverUrlsMap(fsFileIdList);
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
    public Map<String, List<String>> getBatchVideoCoverUrlsMap(List<String> fsFileIdList) {
        Assert.notEmpty(fsFileIdList, "FsFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(fsFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, List<String>>(0);
        }

        Map<String, FsServer> fileIdFsServerMap = getFileIdFsServerMap(fsFiles);
        Map<String, List<String>> batchVideoCoverUrlsMap = new HashMap<String, List<String>>(fsFiles.size());
        for (FsFile fsFile : fsFiles) {
            ProcessorTypeEnum processor = fsFile.getProcessor();
            String videoLevels = fsFile.getVideoLevels();
            if ((!ProcessorTypeEnum.VID.equals(processor)
                    && !ProcessorTypeEnum.ZVID.equals(processor))
                    || StringUtils.isEmpty(videoLevels)) {
                continue;
            }

            String fsFileId = fsFile.getId();
            StringBuilder sb = new StringBuilder();
            FsServer fsServer = fileIdFsServerMap.get(fsFileId);
            sb.append(PropertiesUtils.getHttpProtocol()).append(HTTP_COLON).append(fsServer.getHost())
                    .append(PATH_SEPARATOR).append(PropertiesUtils.getServerName()).append(FILE_URL_GET_FILE)
                    .append(Signer.sign(fsServer, fsFile, ExecutionContext.getSession()))
                    .append(PATH_SEPARATOR).append(fsFile.getCorpCode()).append(PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(PATH_SEPARATOR).append(FILE_DIR_GEN)
                    .append(PATH_SEPARATOR).append(DEFAULT_VIDEO_TYPE).append(PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(PATH_SEPARATOR).append(fsFileId);
            String[] vLevels = videoLevels.split(VERTICAL_LINE_REGEX);
            List<String> videoCoverUrls = new ArrayList<String>(vLevels.length);
            for (int i = 0; i < vLevels.length; i++) {
                StringBuilder builder = new StringBuilder();
                if (ProcessorTypeEnum.ZVID.equals(processor)) {
                    builder.append(PATH_SEPARATOR).append(i + 1);
                }

                builder.append(PATH_SEPARATOR).append(VIDEO_COVER);
                videoCoverUrls.add(sb.toString() + builder.toString());
                builder.delete(0, builder.length());
            }

            sb.delete(0, sb.length());
            batchVideoCoverUrlsMap.put(fsFileId, videoCoverUrls);
        }

        return batchVideoCoverUrlsMap;
    }

    @Override
    public String getAudioUrl(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        List<String> audioUrls = getAudioUrls(fsFileId);
        if (CollectionUtils.isEmpty(audioUrls)) {
            return null;
        }

        return audioUrls.get(0);
    }

    @Override
    public List<String> getAudioUrls(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        return getBatchAudioUrlsMap(Arrays.asList(fsFileId)).get(fsFileId);
    }

    @Override
    public Map<String, String> getBatchAudioUrlMap(List<String> fsFileIdList) {
        Assert.notEmpty(fsFileIdList, "FsFileIdList is empty!");
        Map<String, List<String>> batchAudioUrlsMap = getBatchAudioUrlsMap(fsFileIdList);
        if (MapUtils.isEmpty(batchAudioUrlsMap)) {
            return new HashMap<String, String>(0);
        }

        Map<String, String> batchAudioUrlMap = new HashMap<String, String>(batchAudioUrlsMap.size());
        for (String storedFileId : fsFileIdList) {
            List<String> audioUrls = batchAudioUrlsMap.get(storedFileId);
            if (CollectionUtils.isEmpty(audioUrls)) {
                continue;
            }

            batchAudioUrlMap.put(storedFileId, audioUrls.get(0));
        }

        return batchAudioUrlMap;
    }

    @Override
    public Map<String, List<String>> getBatchAudioUrlsMap(List<String> fsFileIdList) {
        Assert.notEmpty(fsFileIdList, "FsFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(fsFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, List<String>>(0);
        }

        Map<String, FsServer> fileIdFsServerMap = getFileIdFsServerMap(fsFiles);
        Map<String, List<String>> batchAudioUrlsMap = new HashMap<String, List<String>>(fsFiles.size());
        for (FsFile fsFile : fsFiles) {
            ProcessorTypeEnum processor = fsFile.getProcessor();
            if (!ProcessorTypeEnum.AUD.equals(processor)
                    && !ProcessorTypeEnum.ZAUD.equals(processor)) {
                continue;
            }

            StringBuilder sb = new StringBuilder();
            String fsFileId = fsFile.getId();
            FsServer fsServer = fileIdFsServerMap.get(fsFileId);
            sb.append(PropertiesUtils.getHttpProtocol()).append(HTTP_COLON).append(fsServer.getHost())
                    .append(PATH_SEPARATOR).append(PropertiesUtils.getServerName()).append(FILE_URL_GET_FILE)
                    .append(Signer.sign(fsServer, fsFile, ExecutionContext.getSession()))
                    .append(PATH_SEPARATOR).append(fsFile.getCorpCode()).append(PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(PATH_SEPARATOR).append(FILE_DIR_GEN)
                    .append(PATH_SEPARATOR).append(DEFAULT_AUDIO_TYPE).append(PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(PATH_SEPARATOR).append(fsFileId);
            Integer subFileCount = fsFile.getSubFileCount();
            subFileCount = subFileCount == null || subFileCount <= 0 ? 1 : subFileCount;
            List<String> audioUrls = new ArrayList<String>(subFileCount);
            for (int i = 0; i < subFileCount; i++) {
                StringBuilder builder = new StringBuilder();
                if (ProcessorTypeEnum.ZAUD.equals(processor)) {
                    builder.append(PATH_SEPARATOR).append(i + 1);
                }

                builder.append(PATH_SEPARATOR).append(DEFAULT_AUDIO_NAME);
                audioUrls.add(sb.toString() + builder.toString());
                builder.delete(0, builder.length());
            }

            sb.delete(0, sb.length());
            batchAudioUrlsMap.put(fsFileId, audioUrls);
        }

        return batchAudioUrlsMap;
    }

    @Override
    public String getZipUrl(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        return getBatchZipUrlMap(Arrays.asList(fsFileId)).get(fsFileId);
    }

    @Override
    public String getImageUrl(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        return getBatchImageUrlMap(Arrays.asList(fsFileId)).get(fsFileId);
    }

    @Override
    public Map<String, String> getBatchImageUrlMap(List<String> fsFileIdList) {
        Assert.notEmpty(fsFileIdList, "FsFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(fsFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, String>(0);
        }

        Map<String, FsServer> fileIdFsServerMap = getFileIdFsServerMap(fsFiles);
        Map<String, String> batchImageUrlMap = new HashMap<String, String>(fsFiles.size());
        for (FsFile fsFile : fsFiles) {
            ProcessorTypeEnum processor = fsFile.getProcessor();
            if (!ProcessorTypeEnum.IMG.equals(processor)
                    && !ProcessorTypeEnum.ZIMG.equals(processor)
                    && !ProcessorTypeEnum.DOC.equals(processor)
                    && !ProcessorTypeEnum.ZDOC.equals(processor)) {
                continue;
            }

            String fsFileId = fsFile.getId();
            StringBuilder sb = new StringBuilder();
            FsServer fsServer = fileIdFsServerMap.get(fsFileId);
            sb.append(PropertiesUtils.getHttpProtocol()).append(HTTP_COLON).append(fsServer.getHost())
                    .append(PATH_SEPARATOR).append(PropertiesUtils.getServerName()).append(FILE_URL_GET_FILE)
                    .append(Signer.sign(fsServer, fsFile, ExecutionContext.getSession()))
                    .append(PATH_SEPARATOR).append(fsFile.getCorpCode()).append(PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(PATH_SEPARATOR).append(FILE_DIR_GEN)
                    .append(PATH_SEPARATOR).append(FILE_DIR_IMG).append(PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(PATH_SEPARATOR).append(fsFileId);
            if (ProcessorTypeEnum.IMG.equals(processor)) {
                sb.append(PATH_SEPARATOR).append(ORIGIN_IMAGE_NAME);
            } else if (ProcessorTypeEnum.DOC.equals(processor)
                    || ProcessorTypeEnum.ZIMG.equals(processor)) {
                sb.append(PATH_SEPARATOR).append(FIRST).append(ORIGIN_IMAGE_NAME);
            } else {
                sb.append(PATH_SEPARATOR).append(FIRST).append(PATH_SEPARATOR)
                        .append(FIRST).append(ORIGIN_IMAGE_NAME);
            }

            batchImageUrlMap.put(fsFileId, sb.toString());
            sb.delete(0, sb.length());
        }

        return batchImageUrlMap;
    }

    @Override
    public Map<String, String> getBatchZipUrlMap(List<String> fsFileIdList) {
        Assert.notEmpty(fsFileIdList, "FsFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(fsFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, String>(0);
        }

        Map<String, FsServer> fileIdFsServerMap = getFileIdFsServerMap(fsFiles);
        Map<String, String> batchZipUrlMap = new HashMap<String, String>(fsFiles.size());
        for (FsFile fsFile : fsFiles) {
            if (!ProcessorTypeEnum.ZIP.equals(fsFile.getProcessor())) {
                continue;
            }

            String fsFileId = fsFile.getId();
            StringBuilder builder = new StringBuilder();
            FsServer fsServer = fileIdFsServerMap.get(fsFileId);
            builder.append(PropertiesUtils.getHttpProtocol()).append(HTTP_COLON).append(fsServer.getHost())
                    .append(PATH_SEPARATOR).append(PropertiesUtils.getServerName()).append(FILE_URL_GET_FILE)
                    .append(Signer.sign(fsServer, fsFile, ExecutionContext.getSession()))
                    .append(PATH_SEPARATOR).append(fsFile.getCorpCode()).append(PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(PATH_SEPARATOR).append(FILE_DIR_GEN)
                    .append(PATH_SEPARATOR).append(FILE_DIR_UNZIP).append(PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime())).append(PATH_SEPARATOR)
                    .append(fsFileId).append(PATH_SEPARATOR).append(ZIP_INDEX_FILE);
            batchZipUrlMap.put(fsFileId, builder.toString());
            builder.delete(0, builder.length());
        }

        return batchZipUrlMap;
    }

    @Override
    public String getFileUrl(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        return getBatchFileUrlMap(Arrays.asList(fsFileId)).get(fsFileId);
    }

    @Override
    public Map<String, String> getBatchFileUrlMap(List<String> fsFileIdList) {
        Assert.notEmpty(fsFileIdList, "FsFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(fsFileIdList);
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
            String storedFileId = fsFile.getId();
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
    public Long getSubFileCount(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        return getBatchSubFileCountMap(Arrays.asList(fsFileId)).get(fsFileId);
    }

    @Override
    public Map<String, Long> getBatchSubFileCountMap(List<String> fsFileIdList) {
        Assert.notEmpty(fsFileIdList, "FsFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(fsFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, Long>(0);
        }

        Map<String, Long> batchSubFileCountMap = new HashMap<String, Long>(fsFiles.size());
        for (FsFile fsFile : fsFiles) {
            String storedFileId = fsFile.getId();
            ProcessorTypeEnum processor = fsFile.getProcessor();
            if (ProcessorTypeEnum.DOC.equals(processor)
                    || ProcessorTypeEnum.ZDOC.equals(processor)
                    || ProcessorTypeEnum.ZIMG.equals(processor)
                    || ProcessorTypeEnum.ZAUD.equals(processor)
                    || ProcessorTypeEnum.ZVID.equals(processor)) {
                Integer subFileCount = fsFile.getSubFileCount();
                subFileCount = subFileCount == null || subFileCount <= 0 ? 1 : subFileCount;
                batchSubFileCountMap.put(storedFileId, subFileCount.longValue());
            } else {
                batchSubFileCountMap.put(storedFileId, 0l);
            }
        }

        return batchSubFileCountMap;
    }

    @Override
    public List<Long> getSubFileCounts(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        return getBatchSubFileCountsMap(Arrays.asList(fsFileId)).get(fsFileId);
    }

    @Override
    public Map<String, List<Long>> getBatchSubFileCountsMap(List<String> fsFileIdList) {
        Assert.notEmpty(fsFileIdList, "FsFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(fsFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, List<Long>>(0);
        }

        Map<String, List<Long>> batchSubFileCountsMap = new HashMap<String, List<Long>>(fsFiles.size());
        for (FsFile fsFile : fsFiles) {
            String subFileCounts = fsFile.getSubFileCounts();
            if (!ProcessorTypeEnum.ZDOC.equals(fsFile.getProcessor())
                    || StringUtils.isEmpty(subFileCounts)) {
                continue;
            }

            String[] subFileCountList = subFileCounts.split(VERTICAL_LINE_REGEX);
            List<Long> subCounts = new ArrayList<Long>(subFileCountList.length);
            for (String subCount : subFileCountList) {
                subCounts.add(Long.parseLong(subCount));
            }

            batchSubFileCountsMap.put(fsFile.getId(), subCounts);
        }

        return batchSubFileCountsMap;
    }

    @Override
    public List<FsServer> getUploadFsServerList(String corpCode) {
        Assert.hasText(corpCode, "CorpCode is empty!");
        return fsServerService.getUploadFsServerList(corpCode);
    }

    @Override
    public List<FsServer> getDownloadFsServerList(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        FsFile fsFile = fsFileService.get(fsFileId, FsFile._id, FsFile._serverCode, FsFile._corpCode);
        if (fsFile == null) {
            return new ArrayList<FsServer>(0);
        }

        return getDownloadFsServerListByServerCode(fsFile.getCorpCode(), fsFile.getServerCode());
    }

    @Override
    public List<FsServer> getDownloadFsServerListByServerCode(String corpCode, String serverCode) {
        Assert.hasText(corpCode, "CorpCode is empty!");
        Assert.hasText(serverCode, "ServerCode is empty!");
        return fsServerService.getDownloadFsServerListByServerCode(corpCode, serverCode);
    }
}
