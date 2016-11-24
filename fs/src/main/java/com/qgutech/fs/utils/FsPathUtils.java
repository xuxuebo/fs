package com.qgutech.fs.utils;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.FsServer;

public class FsPathUtils extends PathUtils {

    public static FsServer getFsServer() {
        FsServer fsServer = new FsServer();
        fsServer.setHost(PropertiesUtils.getServerHost());
        fsServer.setSecret(PropertiesUtils.getServerSecret());
        fsServer.setServerCode(PropertiesUtils.getServerCode());

        return fsServer;
    }

    public static String getOriginFileUrl(FsFile fsFile, String session) {
        return getOriginFileUrl(fsFile, getFsServer(), PropertiesUtils.getHttpProtocol(), session);
    }

    public static String getImageUrl(FsFile fsFile, String session) {
        return getImageUrl(fsFile, getFsServer(), PropertiesUtils.getHttpProtocol(), session);
    }


}
