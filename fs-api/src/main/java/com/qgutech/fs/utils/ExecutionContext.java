package com.qgutech.fs.utils;


import java.util.HashMap;
import java.util.Map;

/**
 * 用于在应用内及应用间保存、传递执行上下文信息。
 */
public class ExecutionContext {
    /**
     * 用于保存线程相关信息
     */
    private transient static ThreadLocal<Map<String, String>> threadLocal =
            new ThreadLocal<Map<String, String>>();

    /**
     * 公司编号
     */
    public static final String CORP_CODE = "corp_code";

    /**
     * 应用编号
     */
    public static final String APP_CODE = "app_code";

    public static final String USER_ID = "user_id";

    public static Map<String, String> getContextMap() {
        return threadLocal.get();
    }

    public static void setContextMap(Map<String, String> contextMap) {
        threadLocal.set(contextMap);
    }

    public static String get(String key) {
        Map<String, String> contextMap = getContextMap();
        if (contextMap == null) {
            return null;
        }

        return contextMap.get(key);
    }

    public static String put(String key, String value) {
        Map<String, String> contextMap = getContextMap();
        if (contextMap == null) {
            contextMap = new HashMap<String, String>();
            setContextMap(contextMap);
        }

        return contextMap.put(key, value);
    }

    public static String getCorpCode() {
        return get(CORP_CODE);
    }

    public static void setCorpCode(String corpCode) {
        put(CORP_CODE, corpCode);
    }

    public static String getAppCode() {
        return get(APP_CODE);
    }

    public static void setAppCode(String appCode) {
        put(APP_CODE, appCode);
    }

    public static void setUserId(String userId) {
        put(USER_ID, userId);
    }

    public static String getUserId() {
        return get(USER_ID);
    }
}
