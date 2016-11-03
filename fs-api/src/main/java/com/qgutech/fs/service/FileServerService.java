package com.qgutech.fs.service;

import java.util.List;
import java.util.Map;

public interface FileServerService {

    String getOriginFileUrl(String corpCode, String appCode, String storedFileId);

    Map<String, String> getBatchOriginFileUrlMap(String corpCode, String appCode
            , List<String> storedFileIdList);

    /**
     * 根据公司编号，应用编号，文件在文件系统中的主键获取清晰度（由码率和分辨率决定）和视频url的映射的列表。
     * 此方法仅适用于单个视频文件以及视频包。
     *
     * @param corpCode     公司编号
     * @param appCode      应用编号
     * @param storedFileId 文件在文件系统中的主键
     * @return 清晰度和视频url的映射
     * @see com.qgutech.fs.domain.VideoTypeEnum 清晰度
     */
    List<Map<String, String>> getVideoUrls(String corpCode, String appCode, String storedFileId);

    /**
     * 根据公司编号，应用编号，文件在文件系统中的主键获取清晰度（由码率和分辨率决定）和视频url的映射。
     * <b>注意：此方法仅适用于单个视频文件。</b>
     *
     * @param corpCode     公司编号
     * @param appCode      应用编号
     * @param storedFileId 文件在文件系统中的主键
     * @return 清晰度和视频url的映射
     * @see com.qgutech.fs.domain.VideoTypeEnum 清晰度
     */
    Map<String, String> getVideoTypeUrlMap(String corpCode, String appCode, String storedFileId);

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
