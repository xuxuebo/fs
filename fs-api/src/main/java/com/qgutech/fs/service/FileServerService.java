package com.qgutech.fs.service;

import java.util.List;
import java.util.Map;

public interface FileServerService {

    String getOriginFileUrl(String corpCode, String appCode, String storedFileId);

    Map<String, String> getBatchOriginFileUrlMap(String corpCode, String appCode
            , List<String> storedFileIdList);

    String getHighVideoUrl(String corpCode, String appCode, String storedFileId);

    String getMiddleVideoUrl(String corpCode, String appCode, String storedFileId);

    String getLowVideoUrl(String corpCode, String appCode, String storedFileId);

    String getOriginVideoUrl(String corpCode, String appCode, String storedFileId);
}
