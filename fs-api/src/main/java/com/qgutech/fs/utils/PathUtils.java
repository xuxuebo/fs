package com.qgutech.fs.utils;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.FsServer;
import com.qgutech.fs.domain.ProcessorTypeEnum;
import com.qgutech.fs.domain.VideoTypeEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class PathUtils implements UriUtils, FsConstants {

    public static String getOriginFilePath(FsFile fsFile) {
        Map<String, String> batchOriginFilePathMap = getBatchOriginFilePathMap(Arrays.asList(fsFile));
        if (MapUtils.isEmpty(batchOriginFilePathMap)) {
            return null;
        }

        return batchOriginFilePathMap.get(fsFile.getId());
    }

    public static Map<String, String> getBatchOriginFilePathMap(List<FsFile> fsFiles) {
        Map<String, String> batchOriginFilePathMap = new HashMap<String, String>(fsFiles.size());
        StringBuilder builder = new StringBuilder();
        for (FsFile fsFile : fsFiles) {
            String fsFileId = fsFile.getId();
            builder.append(PATH_SEPARATOR).append(fsFile.getCorpCode()).append(PATH_SEPARATOR)
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
            batchOriginFilePathMap.put(fsFileId, builder.toString());
            builder.delete(0, builder.length());
        }

        return batchOriginFilePathMap;
    }

    public static String getOriginDownloadUrl(FsFile fsFile, FsServer fsServer
            , String httpProtocol, String session) {
        return getOriginUrl(fsFile, fsServer, DOWNLOAD_FILE_URI, httpProtocol, session);
    }

    public static String getOriginFileUrl(FsFile fsFile, FsServer fsServer
            , String httpProtocol, String session) {
        return getOriginUrl(fsFile, fsServer, GET_FILE_URI, httpProtocol, session);
    }

    public static String getOriginUrl(FsFile fsFile, FsServer fsServer
            , String action, String httpProtocol, String session) {
        List<FsFile> fsFiles = new ArrayList<FsFile>(1);
        fsFiles.add(fsFile);
        Map<String, FsServer> fileIdFsServerMap = new HashMap<String, FsServer>(1);
        fileIdFsServerMap.put(fsFile.getId(), fsServer);
        Map<String, String> batchOriginFileUrlMap = getBatchOriginFileUrlMap(fsFiles
                , fileIdFsServerMap, action, httpProtocol, session);
        if (MapUtils.isEmpty(batchOriginFileUrlMap)) {
            return null;
        }

        return batchOriginFileUrlMap.get(fsFile.getId());
    }

    public static Map<String, String> getBatchOriginFileUrlMap(List<FsFile> fsFiles, Map<String, FsServer> fileIdFsServerMap, String action, String httpProtocol, String session) {
        Map<String, String> batchOriginFilePathMap = getBatchOriginFilePathMap(fsFiles);
        if (MapUtils.isEmpty(batchOriginFilePathMap)) {
            return new HashMap<String, String>(0);
        }

        Map<String, String> batchOriginFileUrlMap = new HashMap<String, String>(batchOriginFilePathMap.size());
        for (FsFile fsFile : fsFiles) {
            String fsFileId = fsFile.getId();
            FsServer fsServer = fileIdFsServerMap.get(fsFileId);
            if (fsServer == null) {
                continue;
            }

            String originFilePath = batchOriginFilePathMap.get(fsFileId);
            if (StringUtils.isEmpty(originFilePath)) {
                continue;
            }

            String originFileUrl = httpProtocol + HTTP_COLON + fsServer.getHost()
                    + action + Signer.sign(fsServer, fsFile, session) + originFilePath;
            batchOriginFileUrlMap.put(fsFileId, originFileUrl);
        }

        return batchOriginFileUrlMap;
    }

    public static String getImagePath(FsFile fsFile) {
        Map<String, String> batchImagePathMap = getBatchImagePathMap(Arrays.asList(fsFile));
        if (MapUtils.isEmpty(batchImagePathMap)) {
            return null;
        }

        return batchImagePathMap.get(fsFile.getId());
    }

    public static Map<String, String> getBatchImagePathMap(List<FsFile> fsFiles) {
        Map<String, String> batchImagePathMap = new HashMap<String, String>(fsFiles.size());
        for (FsFile fsFile : fsFiles) {
            ProcessorTypeEnum processor = fsFile.getProcessor();
            if (!ProcessorTypeEnum.IMG.equals(processor)
                    && !ProcessorTypeEnum.ZIMG.equals(processor)
                    && !ProcessorTypeEnum.DOC.equals(processor)
                    && !ProcessorTypeEnum.ZDOC.equals(processor)) {
                continue;
            }

            String fsFileId = fsFile.getId();
            StringBuilder builder = new StringBuilder();
            builder.append(PATH_SEPARATOR).append(fsFile.getCorpCode()).append(PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(PATH_SEPARATOR).append(FILE_DIR_GEN)
                    .append(PATH_SEPARATOR).append(FILE_DIR_IMG).append(PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(PATH_SEPARATOR).append(fsFileId);
            if (ProcessorTypeEnum.IMG.equals(processor)) {
                builder.append(PATH_SEPARATOR).append(ORIGIN_IMAGE_NAME);
            } else if (ProcessorTypeEnum.DOC.equals(processor)
                    || ProcessorTypeEnum.ZIMG.equals(processor)) {
                builder.append(PATH_SEPARATOR).append(FIRST).append(ORIGIN_IMAGE_NAME);
            } else {
                builder.append(PATH_SEPARATOR).append(FIRST).append(PATH_SEPARATOR)
                        .append(FIRST).append(ORIGIN_IMAGE_NAME);
            }

            batchImagePathMap.put(fsFileId, builder.toString());
            builder.delete(0, builder.length());
        }

        return batchImagePathMap;
    }

    public static String getImageUrl(FsFile fsFile, FsServer fsServer
            , String httpProtocol, String session) {
        List<FsFile> fsFiles = new ArrayList<FsFile>(1);
        fsFiles.add(fsFile);
        Map<String, FsServer> fileIdFsServerMap = new HashMap<String, FsServer>(1);
        fileIdFsServerMap.put(fsFile.getId(), fsServer);
        Map<String, String> batchImageUrlMap = getBatchImageUrlMap(fsFiles
                , fileIdFsServerMap, httpProtocol, session);
        if (MapUtils.isEmpty(batchImageUrlMap)) {
            return null;
        }

        return batchImageUrlMap.get(fsFile.getId());
    }

    public static Map<String, String> getBatchImageUrlMap(List<FsFile> fsFiles
            , Map<String, FsServer> fileIdFsServerMap, String httpProtocol, String session) {
        Map<String, String> batchImagePathMap = getBatchImagePathMap(fsFiles);
        if (MapUtils.isEmpty(batchImagePathMap)) {
            return new HashMap<String, String>(0);
        }

        Map<String, String> batchImageUrlMap = new HashMap<String, String>(batchImagePathMap.size());
        for (FsFile fsFile : fsFiles) {
            String fsFileId = fsFile.getId();
            FsServer fsServer = fileIdFsServerMap.get(fsFileId);
            if (fsServer == null) {
                continue;
            }

            String imagePath = batchImagePathMap.get(fsFileId);
            if (StringUtils.isEmpty(imagePath)) {
                continue;
            }

            String imageUrl = httpProtocol + HTTP_COLON + fsServer.getHost()
                    + GET_FILE_URI + Signer.sign(fsServer, fsFile, session) + imagePath;
            batchImageUrlMap.put(fsFileId, imageUrl);
        }

        return batchImageUrlMap;
    }

    public static String getZipPath(FsFile fsFile) {
        Map<String, String> batchZipPathMap = getBatchZipPathMap(Arrays.asList(fsFile));
        if (MapUtils.isEmpty(batchZipPathMap)) {
            return null;
        }

        return batchZipPathMap.get(fsFile.getId());
    }

    public static Map<String, String> getBatchZipPathMap(List<FsFile> fsFiles) {
        Map<String, String> batchZipPathMap = new HashMap<String, String>(fsFiles.size());
        for (FsFile fsFile : fsFiles) {
            if (!ProcessorTypeEnum.ZIP.equals(fsFile.getProcessor())) {
                continue;
            }

            String fsFileId = fsFile.getId();
            StringBuilder builder = new StringBuilder();
            builder.append(PATH_SEPARATOR).append(fsFile.getCorpCode()).append(PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(PATH_SEPARATOR).append(FILE_DIR_GEN)
                    .append(PATH_SEPARATOR).append(FILE_DIR_UNZIP).append(PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime())).append(PATH_SEPARATOR)
                    .append(fsFileId).append(PATH_SEPARATOR).append(ZIP_INDEX_FILE);
            batchZipPathMap.put(fsFileId, builder.toString());
            builder.delete(0, builder.length());
        }

        return batchZipPathMap;
    }

    public static String getZipUrl(FsFile fsFile, FsServer fsServer
            , String httpProtocol, String session) {
        List<FsFile> fsFiles = new ArrayList<FsFile>(1);
        fsFiles.add(fsFile);
        Map<String, FsServer> fileIdFsServerMap = new HashMap<String, FsServer>(1);
        fileIdFsServerMap.put(fsFile.getId(), fsServer);
        Map<String, String> batchZipUrlMap = getBatchZipUrlMap(fsFiles
                , fileIdFsServerMap, httpProtocol, session);
        if (MapUtils.isEmpty(batchZipUrlMap)) {
            return null;
        }

        return batchZipUrlMap.get(fsFile.getId());
    }

    public static Map<String, String> getBatchZipUrlMap(List<FsFile> fsFiles , Map<String, FsServer> fileIdFsServerMap, String httpProtocol, String session) {
        Map<String, String> batchZipPathMap = getBatchZipPathMap(fsFiles);
        if (MapUtils.isEmpty(batchZipPathMap)) {
            return new HashMap<String, String>(0);
        }

        Map<String, String> batchZipUrlMap = new HashMap<String, String>(batchZipPathMap.size());
        for (FsFile fsFile : fsFiles) {
            String fsFileId = fsFile.getId();
            FsServer fsServer = fileIdFsServerMap.get(fsFileId);
            if (fsServer == null) {
                continue;
            }

            String zipPath = batchZipPathMap.get(fsFileId);
            if (StringUtils.isEmpty(zipPath)) {
                continue;
            }

            String zipUrl = httpProtocol + HTTP_COLON + fsServer.getHost() + GET_FILE_URI + Signer.sign(fsServer, fsFile, session) + zipPath;
            batchZipUrlMap.put(fsFileId, zipUrl);
        }

        return batchZipUrlMap;
    }

    public static Map<String, String> getVideoTypePathMap(FsFile fsFile) {
        List<Map<String, String>> videoPaths = getVideoPaths(fsFile);
        if (CollectionUtils.isEmpty(videoPaths)) {
            return new HashMap<String, String>(0);
        }

        return videoPaths.get(0);
    }

    public static List<Map<String, String>> getVideoPaths(FsFile fsFile) {
        return getBatchVideoPathsMap(Arrays.asList(fsFile)).get(fsFile.getId());
    }

    public static Map<String, List<Map<String, String>>> getBatchVideoPathsMap(List<FsFile> fsFiles) {
        Map<String, List<Map<String, String>>> batchVideoPathsMap =
                new HashMap<String, List<Map<String, String>>>(fsFiles.size());
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
            sb.append(PATH_SEPARATOR).append(fsFile.getCorpCode()).append(PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(PATH_SEPARATOR).append(FILE_DIR_GEN)
                    .append(PATH_SEPARATOR).append(DEFAULT_VIDEO_TYPE).append(PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(PATH_SEPARATOR).append(fsFileId);
            String[] vLevels = videoLevels.split(VERTICAL_LINE_REGEX);
            List<Map<String, String>> videoPaths = new ArrayList<Map<String, String>>(vLevels.length);
            for (int i = 0; i < vLevels.length; i++) {
                VideoTypeEnum videoTypeEnum = VideoTypeEnum.valueOf(vLevels[i]);
                VideoTypeEnum[] videoTypeEnums = VideoTypeEnum.getVideoTypeEnums(videoTypeEnum);
                Map<String, String> videoTypePathMap = new HashMap<String, String>(videoTypeEnums.length);
                videoPaths.add(videoTypePathMap);
                StringBuilder builder = new StringBuilder();
                for (VideoTypeEnum typeEnum : videoTypeEnums) {
                    if (ProcessorTypeEnum.ZVID.equals(processor)) {
                        builder.append(PATH_SEPARATOR).append(i + 1);
                    }

                    String videoType = typeEnum.name();
                    builder.append(PATH_SEPARATOR).append(videoType.toLowerCase()).append(PATH_SEPARATOR)
                            .append(videoType.toLowerCase()).append(DEFAULT_VIDEO_SUFFIX);
                    videoTypePathMap.put(videoType, sb.toString() + builder.toString());
                    builder.delete(0, builder.length());
                }
            }

            sb.delete(0, sb.length());
            batchVideoPathsMap.put(fsFileId, videoPaths);
        }

        return batchVideoPathsMap;
    }

    public static Map<String, String> getVideoTypeUrlMap(FsFile fsFile, FsServer fsServer
            , String httpProtocol, String session) {
        List<Map<String, String>> videoUrls = getVideoUrls(fsFile, fsServer, httpProtocol, session);
        if (CollectionUtils.isEmpty(videoUrls)) {
            return new HashMap<String, String>(0);
        }

        return videoUrls.get(0);
    }

    public static List<Map<String, String>> getVideoUrls(FsFile fsFile, FsServer fsServer
            , String httpProtocol, String session) {
        List<FsFile> fsFiles = new ArrayList<FsFile>(1);
        fsFiles.add(fsFile);
        Map<String, FsServer> fileIdFsServerMap = new HashMap<String, FsServer>(1);
        fileIdFsServerMap.put(fsFile.getId(), fsServer);
        Map<String, List<Map<String, String>>> batchVideoUrlsMap = getBatchVideoUrlsMap(fsFiles
                , fileIdFsServerMap, httpProtocol, session);
        if (MapUtils.isEmpty(batchVideoUrlsMap)) {
            return null;
        }

        return batchVideoUrlsMap.get(fsFile.getId());
    }

    public static Map<String, List<Map<String, String>>> getBatchVideoUrlsMap(List<FsFile> fsFiles
            , Map<String, FsServer> fileIdFsServerMap, String httpProtocol, String session) {
        Map<String, List<Map<String, String>>> batchVideoPathsMap = getBatchVideoPathsMap(fsFiles);
        if (MapUtils.isEmpty(batchVideoPathsMap)) {
            return new HashMap<String, List<Map<String, String>>>(0);
        }

        Map<String, List<Map<String, String>>> batchVideoUrlsMap =
                new HashMap<String, List<Map<String, String>>>(batchVideoPathsMap.size());
        for (FsFile fsFile : fsFiles) {
            String fsFileId = fsFile.getId();
            FsServer fsServer = fileIdFsServerMap.get(fsFileId);
            if (fsServer == null) {
                continue;
            }

            List<Map<String, String>> videoPaths = batchVideoPathsMap.get(fsFileId);
            if (CollectionUtils.isEmpty(videoPaths)) {
                continue;
            }

            List<Map<String, String>> videoUrls = new ArrayList<Map<String, String>>(videoPaths.size());
            for (Map<String, String> videoTypePathMap : videoPaths) {
                Map<String, String> videoTypeUrlMap = new HashMap<String, String>(videoTypePathMap.size());
                for (Map.Entry<String, String> entry : videoTypePathMap.entrySet()) {
                    String videoUrl = httpProtocol + HTTP_COLON + fsServer.getHost()
                            + GET_FILE_URI + Signer.sign(fsServer, fsFile, session) + entry.getValue();
                    videoTypeUrlMap.put(entry.getKey(), videoUrl);
                }

                videoUrls.add(videoTypeUrlMap);
            }

            batchVideoUrlsMap.put(fsFileId, videoUrls);
        }

        return batchVideoUrlsMap;
    }

    public static String getVideoCoverPath(FsFile fsFile) {
        List<String> videoCoverPaths = getVideoCoverPaths(fsFile);
        if (CollectionUtils.isEmpty(videoCoverPaths)) {
            return null;
        }

        return videoCoverPaths.get(0);
    }

    public static List<String> getVideoCoverPaths(FsFile fsFile) {
        return getBatchVideoCoverPathsMap(Arrays.asList(fsFile)).get(fsFile.getId());
    }

    public static Map<String, String> getBatchVideoCoverPathMap(List<FsFile> fsFiles) {
        Map<String, List<String>> batchVideoCoverPathsMap = getBatchVideoCoverPathsMap(fsFiles);
        if (MapUtils.isEmpty(batchVideoCoverPathsMap)) {
            return new HashMap<String, String>(0);
        }

        Map<String, String> batchVideoCoverPathMap =
                new HashMap<String, String>(batchVideoCoverPathsMap.size());
        for (Map.Entry<String, List<String>> entry : batchVideoCoverPathsMap.entrySet()) {
            List<String> covers = entry.getValue();
            if (CollectionUtils.isEmpty(covers)) {
                continue;
            }

            batchVideoCoverPathMap.put(entry.getKey(), covers.get(0));
        }

        return batchVideoCoverPathMap;
    }

    public static Map<String, List<String>> getBatchVideoCoverPathsMap(List<FsFile> fsFiles) {
        Map<String, List<String>> batchVideoCoverPathsMap = new HashMap<String, List<String>>(fsFiles.size());
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
            sb.append(PATH_SEPARATOR).append(fsFile.getCorpCode()).append(PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(PATH_SEPARATOR).append(FILE_DIR_GEN)
                    .append(PATH_SEPARATOR).append(DEFAULT_VIDEO_TYPE).append(PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(PATH_SEPARATOR).append(fsFileId);
            String[] vLevels = videoLevels.split(VERTICAL_LINE_REGEX);
            List<String> videoCoverPaths = new ArrayList<String>(vLevels.length);
            for (int i = 0; i < vLevels.length; i++) {
                StringBuilder builder = new StringBuilder();
                if (ProcessorTypeEnum.ZVID.equals(processor)) {
                    builder.append(PATH_SEPARATOR).append(i + 1);
                }

                builder.append(PATH_SEPARATOR).append(VIDEO_COVER);
                videoCoverPaths.add(sb.toString() + builder.toString());
                builder.delete(0, builder.length());
            }

            sb.delete(0, sb.length());
            batchVideoCoverPathsMap.put(fsFileId, videoCoverPaths);
        }

        return batchVideoCoverPathsMap;
    }

    public static String getVideoCoverUrl(FsFile fsFile
            , FsServer fsServer, String httpProtocol, String session) {
        List<String> videoCoverUrls = getVideoCoverUrls(fsFile, fsServer, httpProtocol, session);
        if (CollectionUtils.isEmpty(videoCoverUrls)) {
            return null;
        }

        return videoCoverUrls.get(0);
    }

    public static List<String> getVideoCoverUrls(FsFile fsFile
            , FsServer fsServer, String httpProtocol, String session) {
        List<FsFile> fsFiles = new ArrayList<FsFile>(1);
        fsFiles.add(fsFile);
        Map<String, FsServer> fileIdFsServerMap = new HashMap<String, FsServer>(1);
        fileIdFsServerMap.put(fsFile.getId(), fsServer);
        Map<String, List<String>> batchVideoCoverUrlsMap = getBatchVideoCoverUrlsMap(fsFiles
                , fileIdFsServerMap, httpProtocol, session);
        if (MapUtils.isEmpty(batchVideoCoverUrlsMap)) {
            return null;
        }

        return batchVideoCoverUrlsMap.get(fsFile.getId());
    }

    public static Map<String, String> getBatchVideoCoverUrlMap(List<FsFile> fsFiles
            , Map<String, FsServer> fileIdFsServerMap, String httpProtocol, String session) {
        Map<String, List<String>> batchVideoCoverUrlsMap = getBatchVideoCoverUrlsMap(fsFiles
                , fileIdFsServerMap, httpProtocol, session);
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

    public static Map<String, List<String>> getBatchVideoCoverUrlsMap(List<FsFile> fsFiles
            , Map<String, FsServer> fileIdFsServerMap, String httpProtocol, String session) {
        Map<String, List<String>> batchVideoCoverPathsMap = getBatchVideoCoverPathsMap(fsFiles);
        if (MapUtils.isEmpty(batchVideoCoverPathsMap)) {
            return new HashMap<String, List<String>>(0);
        }

        Map<String, List<String>> batchVideoCoverUrlsMap =
                new HashMap<String, List<String>>(batchVideoCoverPathsMap.size());
        for (FsFile fsFile : fsFiles) {
            String fsFileId = fsFile.getId();
            FsServer fsServer = fileIdFsServerMap.get(fsFileId);
            if (fsServer == null) {
                continue;
            }

            List<String> videoCoverPaths = batchVideoCoverPathsMap.get(fsFileId);
            if (CollectionUtils.isEmpty(videoCoverPaths)) {
                continue;
            }

            List<String> videoCoverUrls = new ArrayList<String>(videoCoverPaths.size());
            for (String videoCoverPath : videoCoverPaths) {
                String videoCoverUrl = httpProtocol + HTTP_COLON + fsServer.getHost()
                        + GET_FILE_URI + Signer.sign(fsServer, fsFile, session) + videoCoverPath;
                videoCoverUrls.add(videoCoverUrl);
            }

            batchVideoCoverUrlsMap.put(fsFileId, videoCoverUrls);
        }

        return batchVideoCoverUrlsMap;
    }

    public static String getAudioPath(FsFile fsFile) {
        List<String> audioPaths = getAudioPaths(fsFile);
        if (CollectionUtils.isEmpty(audioPaths)) {
            return null;
        }

        return audioPaths.get(0);
    }

    public static List<String> getAudioPaths(FsFile fsFile) {
        return getBatchAudioPathsMap(Arrays.asList(fsFile)).get(fsFile.getId());
    }

    public static Map<String, String> getBatchAudioPathMap(List<FsFile> fsFiles) {
        Map<String, List<String>> batchAudioPathsMap = getBatchAudioPathsMap(fsFiles);
        if (MapUtils.isEmpty(batchAudioPathsMap)) {
            return new HashMap<String, String>(0);
        }

        Map<String, String> batchAudioPathMap = new HashMap<String, String>(batchAudioPathsMap.size());
        for (Map.Entry<String, List<String>> entry : batchAudioPathsMap.entrySet()) {
            List<String> audioPaths = entry.getValue();
            if (CollectionUtils.isEmpty(audioPaths)) {
                continue;
            }

            batchAudioPathMap.put(entry.getKey(), audioPaths.get(0));
        }

        return batchAudioPathMap;
    }

    public static Map<String, List<String>> getBatchAudioPathsMap(List<FsFile> fsFiles) {
        Map<String, List<String>> batchAudioPathsMap = new HashMap<String, List<String>>(fsFiles.size());
        for (FsFile fsFile : fsFiles) {
            ProcessorTypeEnum processor = fsFile.getProcessor();
            if (!ProcessorTypeEnum.AUD.equals(processor)
                    && !ProcessorTypeEnum.ZAUD.equals(processor)) {
                continue;
            }

            StringBuilder sb = new StringBuilder();
            String fsFileId = fsFile.getId();
            sb.append(PATH_SEPARATOR).append(fsFile.getCorpCode()).append(PATH_SEPARATOR)
                    .append(fsFile.getAppCode()).append(PATH_SEPARATOR).append(FILE_DIR_GEN)
                    .append(PATH_SEPARATOR).append(DEFAULT_AUDIO_TYPE).append(PATH_SEPARATOR)
                    .append(FsUtils.formatDateToYYMM(fsFile.getCreateTime()))
                    .append(PATH_SEPARATOR).append(fsFileId);
            Integer subFileCount = fsFile.getSubFileCount();
            subFileCount = subFileCount == null || subFileCount <= 0 ? 1 : subFileCount;
            List<String> audioPaths = new ArrayList<String>(subFileCount);
            for (int i = 0; i < subFileCount; i++) {
                StringBuilder builder = new StringBuilder();
                if (ProcessorTypeEnum.ZAUD.equals(processor)) {
                    builder.append(PATH_SEPARATOR).append(i + 1);
                }

                builder.append(PATH_SEPARATOR).append(DEFAULT_AUDIO_NAME);
                audioPaths.add(sb.toString() + builder.toString());
                builder.delete(0, builder.length());
            }

            sb.delete(0, sb.length());
            batchAudioPathsMap.put(fsFileId, audioPaths);
        }

        return batchAudioPathsMap;
    }

    public static String getAudioUrl(FsFile fsFile, FsServer fsServer
            , String httpProtocol, String session) {
        List<String> audioUrls = getAudioUrls(fsFile, fsServer, httpProtocol, session);
        if (CollectionUtils.isEmpty(audioUrls)) {
            return null;
        }

        return audioUrls.get(0);
    }

    public static List<String> getAudioUrls(FsFile fsFile, FsServer fsServer
            , String httpProtocol, String session) {
        List<FsFile> fsFiles = new ArrayList<FsFile>(1);
        fsFiles.add(fsFile);
        Map<String, FsServer> fileIdFsServerMap = new HashMap<String, FsServer>(1);
        fileIdFsServerMap.put(fsFile.getId(), fsServer);

        return getBatchAudioUrlsMap(fsFiles, fileIdFsServerMap, httpProtocol, session).get(fsFile.getId());
    }

    public static Map<String, String> getBatchAudioUrlMap(List<FsFile> fsFiles
            , Map<String, FsServer> fileIdFsServerMap, String httpProtocol, String session) {
        Map<String, List<String>> batchAudioUrlsMap = getBatchAudioUrlsMap(fsFiles
                , fileIdFsServerMap, httpProtocol, session);
        if (MapUtils.isEmpty(batchAudioUrlsMap)) {
            return new HashMap<String, String>(0);
        }

        Map<String, String> batchAudioUrlMap = new HashMap<String, String>(batchAudioUrlsMap.size());
        for (Map.Entry<String, List<String>> entry : batchAudioUrlsMap.entrySet()) {
            List<String> audioUrls = entry.getValue();
            if (CollectionUtils.isEmpty(audioUrls)) {
                continue;
            }

            batchAudioUrlMap.put(entry.getKey(), audioUrls.get(0));
        }

        return batchAudioUrlMap;
    }

    public static Map<String, List<String>> getBatchAudioUrlsMap(List<FsFile> fsFiles
            , Map<String, FsServer> fileIdFsServerMap, String httpProtocol, String session) {
        Map<String, List<String>> batchAudioPathsMap = getBatchAudioPathsMap(fsFiles);
        if (MapUtils.isEmpty(batchAudioPathsMap)) {
            return new HashMap<String, List<String>>(0);
        }

        Map<String, List<String>> batchAudioUrlsMap =
                new HashMap<String, List<String>>(batchAudioPathsMap.size());
        for (FsFile fsFile : fsFiles) {
            String fsFileId = fsFile.getId();
            FsServer fsServer = fileIdFsServerMap.get(fsFileId);
            if (fsServer == null) {
                continue;
            }

            List<String> audioPaths = batchAudioPathsMap.get(fsFileId);
            if (CollectionUtils.isEmpty(audioPaths)) {
                continue;
            }

            List<String> audioUrls = new ArrayList<String>(audioPaths.size());
            for (String audioPath : audioPaths) {
                String audioUrl = httpProtocol + HTTP_COLON + fsServer.getHost()
                        + GET_FILE_URI + Signer.sign(fsServer, fsFile, session) + audioPath;
                audioUrls.add(audioUrl);
            }

            batchAudioUrlsMap.put(fsFileId, audioUrls);
        }

        return batchAudioUrlsMap;
    }
}
