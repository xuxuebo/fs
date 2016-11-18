package com.qgutech.fs.utils;

import com.google.gson.Gson;
import com.qgutech.fs.domain.FsFile;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {

    public static final Gson gson = new Gson();

    public static String doPost(String url) {
        return doPost(url, null, null, null);
    }

    public static String doPost(String url, Map<String, String> paramMap) {
        return doPost(url, paramMap, null, null);
    }

    public static String doPost(String url, String filePath, String fileName) {
        return doPost(url, null, filePath, fileName);
    }

    public static String doPost(String url, Map<String, String> paramMap, String filePath, String fileName) {
        Assert.hasText(url, "Url is empty!");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(url);
            if (MapUtils.isNotEmpty(paramMap) || StringUtils.isNotEmpty(filePath)) {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                if (StringUtils.isNotEmpty(filePath)) {
                    FileBody fileBody;
                    File file = new File(filePath);
                    if (StringUtils.isNotEmpty(fileName)) {
                        fileBody = new FileBody(file, ContentType.DEFAULT_BINARY, fileName);
                    } else {
                        fileBody = new FileBody(file);
                    }

                    builder.addPart("file", fileBody);
                }

                if (MapUtils.isNotEmpty(paramMap)) {
                    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                        builder.addPart(entry.getKey()
                                , new StringBody(entry.getValue(), ContentType.TEXT_PLAIN));
                    }
                }

                httpPost.setEntity(builder.build());
            }

            CloseableHttpResponse response = httpClient.execute(httpPost);
            String result;
            try {
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                    throw new RuntimeException("Unexpected failure: " + statusLine.toString());
                }

                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, Charset.forName("UTF-8"));
                    EntityUtils.consume(resEntity);
                } else {
                    result = null;
                }
            } finally {
                response.close();
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred when send post request[url:" + url
                    + ",paramMap:" + paramMap + ",filePath:" + filePath + ",fileName:" + fileName + "]!", e);
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                //np
            }
        }
    }

    public static FsFile getFsFile(String fsFileId) {
        Assert.hasText(fsFileId, "FsFileId is empty!");
        return getFsFile(fsFileId, 1);
    }

    private static FsFile getFsFile(String fsFileId, int executeCnt) {
        long timestamp = System.currentTimeMillis();
        String serverHost = PropertiesUtils.getServerHost();
        String serverCode = PropertiesUtils.getServerCode();
        String serverSecret = PropertiesUtils.getServerSecret();
        String sign = Signer.sign(fsFileId, serverHost, serverCode, serverSecret, timestamp);
        Map<String, String> paramMap = new HashMap<String, String>(5);
        paramMap.put(FsFile._id, fsFileId);
        paramMap.put(FsFile._serverHost, serverHost);
        paramMap.put(FsFile._serverCode, serverCode);
        paramMap.put(FsFile._timestamp, timestamp + StringUtils.EMPTY);
        paramMap.put(FsFile._sign, sign);

        String fsFileJson;
        try {
            fsFileJson = HttpUtils.doPost(PropertiesUtils.getGetFileUrl(), paramMap);
        } catch (Exception e) {
            if (executeCnt >= PropertiesUtils.getGetFileMaxExecuteCnt()) {
                throw (RuntimeException) e;
            }

            return getFsFile(fsFileId, ++executeCnt);
        }

        if (StringUtils.isEmpty(fsFileJson)) {
            return null;
        }

        if (FsConstants.RESPONSE_RESULT_ERROR.equals(fsFileJson)) {
            if (executeCnt >= PropertiesUtils.getGetFileMaxExecuteCnt()) {
                throw new RuntimeException("Exception occurred when getting fsFile[id:"
                        + fsFileId + "] by post request[url:" + PropertiesUtils.getGetFileUrl()
                        + ",executeCnt:" + executeCnt + "]!");
            }

            return getFsFile(fsFileId, ++executeCnt);
        }

        return gson.fromJson(fsFileJson, FsFile.class);
    }

    public static String saveFsFile(FsFile fsFile) {
        Assert.notNull(fsFile, "FsFile is null!");
        String fsFileId = fsFile.getId();
        if (StringUtils.isNotEmpty(fsFileId)) {
            return fsFileId;
        }

        return saveFsFile(fsFile, 1);
    }

    private static String saveFsFile(FsFile fsFile, int executeCnt) {
        long timestamp = System.currentTimeMillis();
        fsFile.setTimestamp(timestamp);
        String serverHost = PropertiesUtils.getServerHost();
        fsFile.setServerHost(serverHost);
        String serverCode = PropertiesUtils.getServerCode();
        fsFile.setServerCode(serverCode);
        fsFile.setSign(Signer.sign(serverHost, serverCode, PropertiesUtils.getServerSecret(), timestamp));

        String fsFileId;
        try {
            fsFileId = HttpUtils.doPost(PropertiesUtils.getSaveFileUrl(), fsFile.toMap());
        } catch (Exception e) {
            if (executeCnt >= PropertiesUtils.getSaveFileMaxExecuteCnt()) {
                throw (RuntimeException) e;
            }

            return saveFsFile(fsFile, ++executeCnt);
        }

        if (StringUtils.isEmpty(fsFileId) || FsConstants.RESPONSE_RESULT_ERROR.equals(fsFileId)) {
            if (executeCnt >= PropertiesUtils.getSaveFileMaxExecuteCnt()) {
                throw new RuntimeException("Exception occurred when saving fsFile["
                        + fsFile + "] by post request[url:" + PropertiesUtils.getSaveFileUrl()
                        + ",executeCnt:" + executeCnt + "]!");
            }

            return saveFsFile(fsFile, ++executeCnt);
        }

        return fsFileId;
    }

    public static void deleteFsFile(String fsFileId) {
        if (StringUtils.isEmpty(fsFileId)) {
            return;
        }

        deleteFsFile(fsFileId, 1);
    }

    private static void deleteFsFile(String fsFileId, int executeCnt) {
        long timestamp = System.currentTimeMillis();
        String serverHost = PropertiesUtils.getServerHost();
        String serverCode = PropertiesUtils.getServerCode();
        String sign = Signer.sign(fsFileId, serverHost, serverCode
                , PropertiesUtils.getServerSecret(), timestamp);
        Map<String, String> paramMap = new HashMap<String, String>(5);
        paramMap.put(FsFile._id, fsFileId);
        paramMap.put(FsFile._serverHost, serverHost);
        paramMap.put(FsFile._serverCode, serverCode);
        paramMap.put(FsFile._timestamp, timestamp + StringUtils.EMPTY);
        paramMap.put(FsFile._sign, sign);

        String receive;
        try {
            receive = HttpUtils.doPost(PropertiesUtils.getDeleteFileUrl(), paramMap);
        } catch (Exception e) {
            if (executeCnt >= PropertiesUtils.getDeleteFileMaxExecuteCnt()) {
                throw (RuntimeException) e;
            }

            deleteFsFile(fsFileId, ++executeCnt);
            return;
        }

        if (FsConstants.RESPONSE_RESULT_ERROR.equals(receive)) {
            if (executeCnt >= PropertiesUtils.getDeleteFileMaxExecuteCnt()) {
                throw new RuntimeException("Exception occurred when deleting fsFile["
                        + paramMap + "] by post request[url:" + PropertiesUtils.getDeleteFileUrl()
                        + ",executeCnt:" + executeCnt + "]!");
            }

            deleteFsFile(fsFileId, ++executeCnt);
        }
    }

    public static void updateFsFile(FsFile fsFile) {
        Assert.notNull(fsFile, "FsFile is null!");
        Assert.hasText(fsFile.getId(), "FsFile's id is empty!");
        updateFsFile(fsFile, 1);
    }

    private static void updateFsFile(FsFile fsFile, int executeCnt) {
        long timestamp = System.currentTimeMillis();
        fsFile.setTimestamp(timestamp);
        String serverHost = PropertiesUtils.getServerHost();
        fsFile.setServerHost(serverHost);
        String serverCode = PropertiesUtils.getServerCode();
        fsFile.setServerCode(serverCode);
        fsFile.setSign(Signer.sign(fsFile.getId(), serverHost, serverCode
                , PropertiesUtils.getServerSecret(), timestamp));
        String receive;
        try {
            receive = HttpUtils.doPost(PropertiesUtils.getUpdateFileUrl(), fsFile.toMap());
        } catch (Exception e) {
            if (executeCnt >= PropertiesUtils.getUpdateFileMaxExecuteCnt()) {
                throw (RuntimeException) e;
            }

            updateFsFile(fsFile, ++executeCnt);
            return;
        }

        if (FsConstants.RESPONSE_RESULT_ERROR.equals(receive)) {
            if (executeCnt >= PropertiesUtils.getUpdateFileMaxExecuteCnt()) {
                throw new RuntimeException("Exception occurred when updating fsFile["
                        + fsFile + "] by post request[url:" + PropertiesUtils.getUpdateFileUrl()
                        + ",executeCnt:" + executeCnt + "]!");
            }

            updateFsFile(fsFile, ++executeCnt);
        }
    }
}
