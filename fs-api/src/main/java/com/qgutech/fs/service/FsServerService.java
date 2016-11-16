package com.qgutech.fs.service;


import com.qgutech.fs.domain.FsServer;
import com.qgutech.fs.service.base.BaseService;

import java.util.List;
import java.util.Set;

public interface FsServerService extends BaseService<FsServer> {

    List<FsServer> getUploadFsServerList(String corpCode);

    List<FsServer> getDownloadFsServerListByServerCode(String corpCode, String serverCode);

    List<FsServer> getDownloadFsServerList(Set<String> corpCodes, Set<String> serverCodes);

    FsServer getFsServerByServerHostAndServerCode(String serverHost, String serverCode);
}
