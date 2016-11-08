package com.qgutech.fs.utils;


public class PropertiesUtils {
    private static String serverCode;
    private static String serverSecret;
    private static String urlExpireTime;
    private static String signLevel;
    private static String serverHost;
    private static String saveFileUrl;
    private static String updateFileUrl;

    public static String getServerCode() {
        return serverCode;
    }

    public void setServerCode(String serverCode) {
        PropertiesUtils.serverCode = serverCode;
    }

    public static String getUpdateFileUrl() {
        return updateFileUrl;
    }

    public void setUpdateFileUrl(String updateFileUrl) {
        PropertiesUtils.updateFileUrl = updateFileUrl;
    }

    public static String getServerSecret() {
        return serverSecret;
    }

    public void setServerSecret(String serverSecret) {
        PropertiesUtils.serverSecret = serverSecret;
    }

    public static String getUrlExpireTime() {
        return urlExpireTime;
    }

    public void setUrlExpireTime(String urlExpireTime) {
        PropertiesUtils.urlExpireTime = urlExpireTime;
    }

    public static String getSignLevel() {
        return signLevel;
    }

    public void setSignLevel(String signLevel) {
        PropertiesUtils.signLevel = signLevel;
    }

    public static String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        PropertiesUtils.serverHost = serverHost;
    }

    public static String getSaveFileUrl() {
        return saveFileUrl;
    }

    public void setSaveFileUrl(String saveFileUrl) {
        PropertiesUtils.saveFileUrl = saveFileUrl;
    }
}
