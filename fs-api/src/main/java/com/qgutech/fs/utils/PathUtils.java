package com.qgutech.fs.utils;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.FsServer;
import com.qgutech.fs.domain.ProcessorTypeEnum;
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

    public static String getOriginFileUrl(FsFile fsFile, FsServer fsServer
            , String httpProtocol, String session) {
        List<FsFile> fsFiles = new ArrayList<FsFile>(1);
        fsFiles.add(fsFile);
        Map<String, FsServer> fileIdFsServerMap = new HashMap<String, FsServer>(1);
        fileIdFsServerMap.put(fsFile.getId(), fsServer);
        Map<String, String> batchOriginFileUrlMap = getBatchOriginFileUrlMap(fsFiles
                , fileIdFsServerMap, httpProtocol, session);
        if (MapUtils.isEmpty(batchOriginFileUrlMap)) {
            return null;
        }

        return batchOriginFileUrlMap.get(fsFile.getId());
    }

    public static Map<String, String> getBatchOriginFileUrlMap(List<FsFile> fsFiles
            , Map<String, FsServer> fileIdFsServerMap, String httpProtocol, String session) {
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
                    + GET_FILE_URI + Signer.sign(fsServer, fsFile, session) + originFilePath;
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

}
