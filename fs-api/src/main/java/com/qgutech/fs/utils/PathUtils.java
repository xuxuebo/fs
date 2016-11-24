package com.qgutech.fs.utils;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.ProcessorTypeEnum;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

}
