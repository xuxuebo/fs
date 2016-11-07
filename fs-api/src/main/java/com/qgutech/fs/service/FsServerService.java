package com.qgutech.fs.service;


import com.qgutech.fs.domain.FsServer;
import com.qgutech.fs.service.base.BaseService;

import java.util.List;

public interface FsServerService extends BaseService<FsServer> {

    List<FsServer> getUploadFsServerList(String corpCode);

    List<FsServer> getDownloadFsServerListByServerCode(String corpCode, String serverCode);

}
