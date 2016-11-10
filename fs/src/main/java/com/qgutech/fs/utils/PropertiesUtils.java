package com.qgutech.fs.utils;


public class PropertiesUtils {
    private static String serverCode;
    private static String serverSecret;
    private static long urlExpireTime;
    private static String signLevel;
    private static String serverHost;
    private static String saveFileUrl;
    private static String updateFileUrl;
    private static String deleteFileUrl;
    private static boolean upload;
    private static boolean download;
    private static boolean convert;
    private static String fileStoreDir;
    private static String imageType;
    private static String videoType;
    private static String audioType;
    private static String zipType;

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

    public static long getUrlExpireTime() {
        return urlExpireTime;
    }

    public void setUrlExpireTime(long urlExpireTime) {
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

    public static boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        PropertiesUtils.upload = upload;
    }

    public static boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        PropertiesUtils.download = download;
    }

    public static boolean isConvert() {
        return convert;
    }

    public void setConvert(boolean convert) {
        PropertiesUtils.convert = convert;
    }

    public static String getFileStoreDir() {
        return fileStoreDir;
    }

    public void setFileStoreDir(String fileStoreDir) {
        PropertiesUtils.fileStoreDir = fileStoreDir;
    }

    public static String getDeleteFileUrl() {
        return deleteFileUrl;
    }

    public void setDeleteFileUrl(String deleteFileUrl) {
        PropertiesUtils.deleteFileUrl = deleteFileUrl;
    }

    public static String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        PropertiesUtils.imageType = imageType;
    }

    public static String getVideoType() {
        return videoType;
    }

    public void setVideoType(String videoType) {
        PropertiesUtils.videoType = videoType;
    }

    public static String getAudioType() {
        return audioType;
    }

    public void setAudioType(String audioType) {
        PropertiesUtils.audioType = audioType;
    }

    public static String getZipType() {
        return zipType;
    }

    public void setZipType(String zipType) {
        PropertiesUtils.zipType = zipType;
    }
}
