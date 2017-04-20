package com.qgutech.fs.utils;

import com.google.gson.Gson;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ldc.
 */
public class FsFileManagerUtil {

    static Gson gson = new Gson();
    public static String getFileUrl(String fileId, String session,String fileServer){
        Assert.notNull(fileId,"fileId is null");
        Assert.notNull(fileId,"session is null");
        Assert.notNull(fileId,"fileServer is null");
        String postUrl = "";
        String remoteUrl = "/fs-service/fileServer/remoteMethod";
        if (!fileServer.contains("http")) {
            postUrl = "http://" + fileServer;
        }else{
            postUrl += fileServer;
        }

        postUrl += remoteUrl;
        Map<String,String> paramsMap = getParamMap(fileId, session);
        String content = HttpUtils.doPost(postUrl, paramsMap);
        Map resMap = gson.fromJson(content,Map.class);
        return (String) resMap.get("content");
    }

    //拼接公共参数
    private static Map<String,String> getParamMap(String fileId, String session){
        Map<String,String> paramsMap = new HashMap<String,String>();
        paramsMap.put("interfaceName","com.qgutech.fs.service.FileServerService");
        paramsMap.put("methodName","getFileUrl");
        paramsMap.put("parameterTypes","java.lang.String,java.lang.String");
        paramsMap.put("arguments[0]",fileId);
        paramsMap.put("arguments[1]", Signer.md5(session));
        paramsMap.put("timestamp", String.valueOf(new Date().getTime()));

        String signText = paramsMap.get("interfaceName") + "|" + paramsMap.get("methodName")
                + "|" + paramsMap.get("timestamp") + "|" + paramsMap.get("timestamp")
                + "|" + paramsMap.get("methodName") + "|" + paramsMap.get("interfaceName");
        String sign = Signer.md5(signText);
        paramsMap.put("sign",sign);
        return paramsMap;
    }

    public static void main(String[] args) {
        String fileUrl = getFileUrl("402881175b895a5e015b895c705d0001", "123456","http://localhost");
        System.out.println(fileUrl);
    }
}
