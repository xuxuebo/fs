package com.qgutech.fs.utils;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * Created by ldc.
 */
public class FsFileManagerUtil {

    private static Gson gson = new Gson();

    /**
     * 获取单个文件的文件路径
     * @param fileServer 文件服务器主机地址,hf.21tb.com or http://hf.21tb.com
     * @param fileId 文件id
     */
    public static String getFileUrl(String fileServer,String fileId, String session){
        Assert.notNull(fileId,"fileId is null");
        Assert.notNull(fileId,"session is null");
        Assert.notNull(fileId,"fileServer is null");
        String remoteUrl = "/fs-service/fileServer/remoteMethod";
        remoteUrl = getFileServer(fileServer, remoteUrl);
        String methodName = "getFileUrl";
        String parameterTypes = "java.lang.String,java.lang.String";
        List<String> fileIds = new ArrayList<String>(0);
        fileIds.add(fileId);
        Map<String,String> paramsMap = getParamMap(fileIds,methodName,parameterTypes,session);
        String content = HttpUtils.doPost(remoteUrl, paramsMap);
        Map resMap = gson.fromJson(content,Map.class);
        return (String) resMap.get("content");
    }

    /**
     * 批量获取文件的文件路径
     * @param fileServer 文件服务器主机地址,hf.21tb.com or http://hf.21tb.com
     * @param fileIdList 文件id集合
     */
    public static String getBatchFileUrl(String fileServer, List<String> fileIdList, String session){
        Assert.notNull(fileServer,"fileServer is null");
        Assert.notNull(fileIdList,"fileIdList is null");
        Assert.notNull(session,"session is null");
        String methodName = "getBatchFileUrlMap";
        String remoteUrl = "/fs-service/fileServer/remoteMethod";
        String parameterTypes = "java.util.List,java.lang.String";
        remoteUrl = getFileServer(fileServer, remoteUrl);
        Map<String,String> paramsMap = getParamMap(fileIdList,methodName,parameterTypes,session);
        String content = HttpUtils.doPost(remoteUrl, paramsMap);
        Map resMap = gson.fromJson(content,Map.class);
        return gson.toJson(resMap.get("content"));
    }

    private static String getFileServer(String fileServer, String postUrl) {
        if (!fileServer.contains("http")) {
            postUrl = "http://" + fileServer + postUrl;
        }else{
            postUrl = fileServer + postUrl;
        }
        return postUrl;
    }

    //拼接公共参数
    private static Map<String,String> getParamMap(
            List<String> fileIds,String methodName,
            String parameterTypes,String session){
        Map<String,String> paramsMap = new HashMap<String,String>();
        paramsMap.put("interfaceName","com.qgutech.fs.service.FileServerService");
        paramsMap.put("methodName",methodName);
        paramsMap.put("parameterTypes",parameterTypes);
        if(fileIds.size() == 1){
            paramsMap.put("arguments[0]",fileIds.get(0));
        }else{
            paramsMap.put("arguments[0]","[" + StringUtils.join(fileIds.toArray(),",")+"]");
        }

        paramsMap.put("arguments[1]", Signer.md5(session));
        paramsMap.put("timestamp", String.valueOf(new Date().getTime()));

        String signText = paramsMap.get("interfaceName") + "|" + paramsMap.get("methodName")
                + "|" + paramsMap.get("timestamp") + "|" + paramsMap.get("timestamp")
                + "|" + paramsMap.get("methodName") + "|" + paramsMap.get("interfaceName");
        String sign = Signer.md5(signText);
        paramsMap.put("sign",sign);
        return paramsMap;
    }

    /**
     * 文件上传
     * @param fileServer 文件服务器主机地址,hf.21tb.com or http://hf.21tb.com
     * @param paramValueMap 对应字符参数
     * @param multipartFile multipartFile文件
     */
    public static String uploadFile(String fileServer, Map<String, String> paramValueMap, MultipartFile multipartFile){
        Assert.notNull(fileServer,"fileServer can not be null!");
        Assert.notNull(multipartFile,"multipartFile can not be null!");
        String uploadFileUrl = "/fs/file/uploadFile";
        String uploadUrl = getFileServer(fileServer,uploadFileUrl);
        return HttpUtils.uploadFile(uploadUrl, paramValueMap, multipartFile);
    }

    public static void main(String[] args) {
        String fileUrl = getFileUrl("http://localhost","402881175b895a5e015b895c705d0001", "123456");
        List<String> fileIds = new ArrayList<String>();
        fileIds.add("402881175b895a5e015b895c705d0001");
        fileIds.add("402881175b895a5e015b895c705d0001");
        String content = getBatchFileUrl("http://localhost",fileIds, "123456");
        System.out.println(fileUrl);
        System.out.println(content);
    }
}
