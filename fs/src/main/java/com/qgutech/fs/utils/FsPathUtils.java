package com.qgutech.fs.utils;


import com.qgutech.fs.domain.FsFile;
import com.qgutech.fs.domain.FsServer;
import com.qgutech.fs.domain.SignLevelEnum;
import org.apache.commons.lang.StringUtils;

public class FsPathUtils extends PathUtils {

    public static FsServer getFsServer() {
        FsServer fsServer = new FsServer();
        fsServer.setSignLevel(SignLevelEnum.valueOf(PropertiesUtils.getSignLevel()));
        fsServer.setHost(PropertiesUtils.getServerHost());
        fsServer.setSecret(PropertiesUtils.getServerSecret());
        fsServer.setServerCode(PropertiesUtils.getServerCode());

        return fsServer;
    }

    public static String getOriginFileUrl(FsFile fsFile) {
        return getOriginFileUrl(fsFile, getFsServer(), PropertiesUtils.getHttpProtocol(), fsFile.getSession());
    }

    public static String getImageUrl(FsFile fsFile) {
        return getImageUrl(fsFile, getFsServer(), PropertiesUtils.getHttpProtocol(), fsFile.getSession());
    }

    public static String getImageUrl(FsFile fsFile, String subFilePath) {
        String imageUrl = getImageUrl(fsFile, getFsServer(), PropertiesUtils.getHttpProtocol()
                , fsFile.getSession());
        if (StringUtils.isEmpty(imageUrl)) {
            return null;
        }

        int index = imageUrl.lastIndexOf(FsConstants.PATH_SEPARATOR);
        if (index < 0) {
            return imageUrl;
        }

        if (subFilePath.startsWith(FsConstants.PATH_SEPARATOR)) {
            return imageUrl.substring(0, index) + subFilePath;
        } else {
            return imageUrl.substring(0, index + 1) + subFilePath;
        }
    }


}
