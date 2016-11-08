package com.qgutech.fs.utils;


public class PropertiesUtils {
    private static String httpProtocol;
    private static String serverName;
    private static long maxWaitForRequest;

    public static String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        PropertiesUtils.serverName = serverName;
    }

    public static String getHttpProtocol() {
        return httpProtocol;
    }

    public void setHttpProtocol(String httpProtocol) {
        PropertiesUtils.httpProtocol = httpProtocol;
    }

    public static long getMaxWaitForRequest() {
        return maxWaitForRequest;
    }

    public void setMaxWaitForRequest(long maxWaitForRequest) {
        PropertiesUtils.maxWaitForRequest = maxWaitForRequest;
    }
}
