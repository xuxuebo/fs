package com.qgutech.fs.service;

import java.util.List;
import java.util.Map;

public interface FileServerService {

    String getOriginFileUrl(String corpCode, String appCode, String storedFileId);

    Map<String, String> getBatchOriginFileUrlMap(String corpCode, String appCode
            , List<String> storedFileIdList);

    String getVideoCoverUrl(String corpCode, String appCode, String storedFileId);

    String getHighVideoUrl(String corpCode, String appCode, String storedFileId);

    String getMiddleVideoUrl(String corpCode, String appCode, String storedFileId);

    String getLowVideoUrl(String corpCode, String appCode, String storedFileId);

    String getOriginVideoUrl(String corpCode, String appCode, String storedFileId);

    String getFileUrl(String corpCode, String appCode, String storedFileId);

    Map<String, String> getBatchFileUrlMap(String corpCode, String appCode
            , List<String> storedFileIdList);

    Integer getSubFileCount(String corpCode, String appCode, String storedFileId);

    List<Integer> getSubFileCountList(String corpCode, String appCode, String storedFileId);

    Map<String, Integer> getSubFileCountMap(String corpCode, String appCode
            , List<String> storedFileIdList);
}
