package com.qgutech.fs.utils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpUtils {
    public static String doPost(String url, Map<String, String> paramMap) {
        Assert.hasText(url, "Url is empty!");
        HttpClient client = new HttpClient();
        PostMethod postMethod = new PostMethod(url);
        try {
            if (paramMap != null && paramMap.size() > 0) {
                List<NameValuePair> pairs = new ArrayList<NameValuePair>(paramMap.size());
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    NameValuePair nameValuePair = new NameValuePair(entry.getKey(), entry.getValue());
                    pairs.add(nameValuePair);
                }

                postMethod.setRequestBody(pairs.toArray(new NameValuePair[pairs.size()]));
            }


            client.executeMethod(postMethod);
            if (postMethod.getStatusCode() == HttpStatus.SC_OK) {
                return postMethod.getResponseBodyAsString();
            } else {
                throw new RuntimeException("Unexpected failure: " + postMethod.getStatusLine().toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred when send post request[url:" + url
                    + ",paramMap:" + paramMap + "]!", e);
        } finally {
            postMethod.releaseConnection();
        }
    }

    public static String doPost(String url, Map<String, String> paramMap
            , Map<String, String> filePathNameMap) {
        return null;
    }
}
