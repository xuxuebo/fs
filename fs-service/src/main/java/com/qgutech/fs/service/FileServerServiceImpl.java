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
public class FileServerServiceImpl implements FileServerService {

    @Resource
    private FsFileService fsFileService;
    @Resource
    private FsServerService fsServerService;

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
            String corpServerCodeKey = corpCode + FsConstants.VERTICAL_LINE + serverCode;
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
            String corpServerCodeKey = corpCode + FsConstants.VERTICAL_LINE + serverCode;
            List<FsServer> fsServers = corpServerCodeFsServersMap.get(corpServerCodeKey);
            if (CollectionUtils.isEmpty(fsServers)) {
                fsServers = corpServerCodeFsServersMap.get(FsConstants.DEFAULT_CORP_CODE
                        + FsConstants.VERTICAL_LINE + serverCode);
            }

            Assert.notEmpty(fsServers, "Both default and " + corpCode +
                    " hasn't server[serverCode:" + serverCode + "]!");
            fileIdFsServerMap.put(fsFile.getId(), fsServers.get((int) (Math.random() * fsServers.size())));
        }

        return fileIdFsServerMap;
    }

    @Override
    public String getOriginFileUrl(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        return getBatchOriginFileUrlMap(Arrays.asList(fsFileId)).get(fsFileId);
    }

    @Override
    public Map<String, String> getBatchOriginFileUrlMap(List<String> fsFileIdList) {
        Assert.notEmpty(fsFileIdList, "FsFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(fsFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, String>(0);
        }

        Map<String, FsServer> fileIdFsServerMap = getFileIdFsServerMap(fsFiles);
        return PathUtils.getBatchOriginFileUrlMap(fsFiles, fileIdFsServerMap
                , PropertiesUtils.getHttpProtocol(), ExecutionContext.getSession());
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

        Map<String, FsServer> fileIdFsServerMap = getFileIdFsServerMap(fsFiles);
        return PathUtils.getBatchVideoUrlsMap(fsFiles, fileIdFsServerMap
                , PropertiesUtils.getHttpProtocol(), ExecutionContext.getSession());
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
        return PathUtils.getBatchVideoCoverUrlsMap(fsFiles, fileIdFsServerMap
                , PropertiesUtils.getHttpProtocol(), ExecutionContext.getSession());
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
        for (String fsFileId : fsFileIdList) {
            List<String> audioUrls = batchAudioUrlsMap.get(fsFileId);
            if (CollectionUtils.isEmpty(audioUrls)) {
                continue;
            }

            batchAudioUrlMap.put(fsFileId, audioUrls.get(0));
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
        return PathUtils.getBatchAudioUrlsMap(fsFiles, fileIdFsServerMap
                , PropertiesUtils.getHttpProtocol(), ExecutionContext.getSession());
    }

    @Override
    public String getZipUrl(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        return getBatchZipUrlMap(Arrays.asList(fsFileId)).get(fsFileId);
    }

    @Override
    public Map<String, String> getBatchZipUrlMap(List<String> fsFileIdList) {
        Assert.notEmpty(fsFileIdList, "FsFileIdList is empty!");
        List<FsFile> fsFiles = fsFileService.listByIds(fsFileIdList);
        if (CollectionUtils.isEmpty(fsFiles)) {
            return new HashMap<String, String>(0);
        }

        Map<String, FsServer> fileIdFsServerMap = getFileIdFsServerMap(fsFiles);
        return PathUtils.getBatchZipUrlMap(fsFiles, fileIdFsServerMap
                , PropertiesUtils.getHttpProtocol(), ExecutionContext.getSession());
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
        return PathUtils.getBatchImageUrlMap(fsFiles, fileIdFsServerMap
                , PropertiesUtils.getHttpProtocol(), ExecutionContext.getSession());
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
        List<FsFile> files = new ArrayList<FsFile>();
        List<FsFile> zips = new ArrayList<FsFile>();
        List<FsFile> images = new ArrayList<FsFile>();
        List<FsFile> videos = new ArrayList<FsFile>();
        List<FsFile> audios = new ArrayList<FsFile>();
        for (FsFile fsFile : fsFiles) {
            switch (fsFile.getProcessor()) {
                case FILE:
                    files.add(fsFile);
                    break;
                case ZIP:
                    zips.add(fsFile);
                    break;
                case DOC:
                    images.add(fsFile);
                    break;
                case ZDOC:
                    images.add(fsFile);
                    break;
                case IMG:
                    images.add(fsFile);
                    break;
                case ZIMG:
                    images.add(fsFile);
                    break;
                case VID:
                    videos.add(fsFile);
                    break;
                case ZVID:
                    videos.add(fsFile);
                    break;
                case AUD:
                    audios.add(fsFile);
                    break;
                case ZAUD:
                    audios.add(fsFile);
                    break;
            }
        }

        String httpProtocol = PropertiesUtils.getHttpProtocol();
        String session = ExecutionContext.getSession();
        Map<String, FsServer> fileIdFsServerMap = getFileIdFsServerMap(fsFiles);
        if (CollectionUtils.isNotEmpty(files)) {
            Map<String, String> batchOriginFileUrlMap = PathUtils.getBatchOriginFileUrlMap(files
                    , fileIdFsServerMap, httpProtocol, session);
            if (MapUtils.isNotEmpty(batchOriginFileUrlMap)) {
                batchFileUrlMap.putAll(batchOriginFileUrlMap);
            }
        }

        if (CollectionUtils.isNotEmpty(zips)) {
            Map<String, String> batchZipUrlMap = PathUtils.getBatchZipUrlMap(zips
                    , fileIdFsServerMap, httpProtocol, session);
            if (MapUtils.isNotEmpty(batchZipUrlMap)) {
                batchFileUrlMap.putAll(batchZipUrlMap);
            }
        }

        if (CollectionUtils.isNotEmpty(images)) {
            Map<String, String> batchImageUrlMap = PathUtils.getBatchImageUrlMap(images
                    , fileIdFsServerMap, httpProtocol, session);
            if (MapUtils.isNotEmpty(batchImageUrlMap)) {
                batchFileUrlMap.putAll(batchImageUrlMap);
            }
        }

        if (CollectionUtils.isNotEmpty(videos)) {
            Map<String, List<Map<String, String>>> batchVideoUrlsMap = PathUtils.getBatchVideoUrlsMap(videos
                    , fileIdFsServerMap, httpProtocol, session);
            if (MapUtils.isNotEmpty(batchVideoUrlsMap)) {
                for (FsFile video : videos) {
                    List<Map<String, String>> videoUrls = batchVideoUrlsMap.get(video.getId());
                    if (CollectionUtils.isEmpty(videoUrls) || videoUrls.get(0) == null) {
                        continue;
                    }

                    String originVideoUrl = videoUrls.get(0).get(VideoTypeEnum.O.name());
                    if (StringUtils.isNotEmpty(originVideoUrl)) {
                        batchFileUrlMap.put(video.getId(), originVideoUrl);
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(audios)) {
            Map<String, String> batchAudioUrlMap = PathUtils.getBatchAudioUrlMap(audios
                    , fileIdFsServerMap, httpProtocol, session);
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

            String[] subFileCountList = subFileCounts.split(FsConstants.VERTICAL_LINE_REGEX);
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
