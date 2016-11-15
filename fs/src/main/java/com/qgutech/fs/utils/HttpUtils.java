package com.qgutech.fs.utils;

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
import java.util.Map;

public class HttpUtils {

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
}
