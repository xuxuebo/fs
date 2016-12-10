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
    private static String getFileUrl;
    private static boolean upload;
    private static boolean download;
    private static boolean convert;
    private static String fileStoreDir;
    private static String imageType;
    private static String videoType;
    private static String audioType;
    private static String zipType;
    private static int cutImageTime;
    private static boolean docConvert;
    private static String asyncUrl;
    private static String backUri;
    private static String httpProtocol;
    private static boolean canOutputDocumentDomain;
    private static int getFileMaxExecuteCnt;
    private static int saveFileMaxExecuteCnt;
    private static int deleteFileMaxExecuteCnt;
    private static int updateFileMaxExecuteCnt;
    private static String checkSessionUrl;
    private static String checkSessionSecret;
    private static String checkSessionCorrectResult;
    private static int checkSessionCacheTime;
    private static String md5FileDir;
    private static String chunkFileDir;
    private static String officeTrustDir;

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

    public static String getGetFileUrl() {
        return getFileUrl;
    }

    public void setGetFileUrl(String getFileUrl) {
        PropertiesUtils.getFileUrl = getFileUrl;
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

    public static int getCutImageTime() {
        return cutImageTime;
    }

    public void setCutImageTime(int cutImageTime) {
        PropertiesUtils.cutImageTime = cutImageTime;
    }

    public static boolean isDocConvert() {
        return docConvert;
    }

    public void setDocConvert(boolean docConvert) {
        PropertiesUtils.docConvert = docConvert;
    }

    public static String getBackUri() {
        return backUri;
    }

    public void setBackUri(String backUri) {
        PropertiesUtils.backUri = backUri;
    }

    public static String getAsyncUrl() {
        return asyncUrl;
    }

    public void setAsyncUrl(String asyncUrl) {
        PropertiesUtils.asyncUrl = asyncUrl;
    }

    public static String getHttpProtocol() {
        return httpProtocol;
    }

    public void setHttpProtocol(String httpProtocol) {
        PropertiesUtils.httpProtocol = httpProtocol;
    }

    public static boolean isCanOutputDocumentDomain() {
        return canOutputDocumentDomain;
    }

    public void setCanOutputDocumentDomain(boolean canOutputDocumentDomain) {
        PropertiesUtils.canOutputDocumentDomain = canOutputDocumentDomain;
    }

    public static int getGetFileMaxExecuteCnt() {
        return getFileMaxExecuteCnt;
    }

    public void setGetFileMaxExecuteCnt(int getFileMaxExecuteCnt) {
        PropertiesUtils.getFileMaxExecuteCnt = getFileMaxExecuteCnt;
    }

    public static int getSaveFileMaxExecuteCnt() {
        return saveFileMaxExecuteCnt;
    }

    public void setSaveFileMaxExecuteCnt(int saveFileMaxExecuteCnt) {
        PropertiesUtils.saveFileMaxExecuteCnt = saveFileMaxExecuteCnt;
    }

    public static int getDeleteFileMaxExecuteCnt() {
        return deleteFileMaxExecuteCnt;
    }

    public void setDeleteFileMaxExecuteCnt(int deleteFileMaxExecuteCnt) {
        PropertiesUtils.deleteFileMaxExecuteCnt = deleteFileMaxExecuteCnt;
    }

    public static int getUpdateFileMaxExecuteCnt() {
        return updateFileMaxExecuteCnt;
    }

    public void setUpdateFileMaxExecuteCnt(int updateFileMaxExecuteCnt) {
        PropertiesUtils.updateFileMaxExecuteCnt = updateFileMaxExecuteCnt;
    }

    public static String getCheckSessionUrl() {
        return checkSessionUrl;
    }

    public void setCheckSessionUrl(String checkSessionUrl) {
        PropertiesUtils.checkSessionUrl = checkSessionUrl;
    }

    public static String getCheckSessionSecret() {
        return checkSessionSecret;
    }

    public void setCheckSessionSecret(String checkSessionSecret) {
        PropertiesUtils.checkSessionSecret = checkSessionSecret;
    }

    public static String getCheckSessionCorrectResult() {
        return checkSessionCorrectResult;
    }

    public void setCheckSessionCorrectResult(String checkSessionCorrectResult) {
        PropertiesUtils.checkSessionCorrectResult = checkSessionCorrectResult;
    }

    public static int getCheckSessionCacheTime() {
        return checkSessionCacheTime;
    }

    public void setCheckSessionCacheTime(int checkSessionCacheTime) {
        PropertiesUtils.checkSessionCacheTime = checkSessionCacheTime;
    }

    public static String getMd5FileDir() {
        return md5FileDir;
    }

    public void setMd5FileDir(String md5FileDir) {
        PropertiesUtils.md5FileDir = md5FileDir;
    }

    public static String getChunkFileDir() {
        return chunkFileDir;
    }

    public void setChunkFileDir(String chunkFileDir) {
        PropertiesUtils.chunkFileDir = chunkFileDir;
    }

    public static String getOfficeTrustDir() {
        return officeTrustDir;
    }

    public void setOfficeTrustDir(String officeTrustDir) {
        PropertiesUtils.officeTrustDir = officeTrustDir;
    }
}
