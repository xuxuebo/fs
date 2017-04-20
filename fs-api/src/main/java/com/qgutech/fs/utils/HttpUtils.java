package com.qgutech.fs.utils;

import com.google.gson.Gson;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpUtils {

    private static final Gson gson = new Gson();

    private static final String UTF_8 = "UTF-8";

    private static final String BOUNDARY = "---7d4a6d158c9";

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
                MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                        .setCharset(Consts.UTF_8);
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
                                , new StringBody(entry.getValue()
                                , ContentType.create("text/plain", Consts.UTF_8)));
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

    public String uploadFile(String httpUrl, Map<String, String> paramValueMap,MultipartFile storedFile) {
        FormFile formFile = new FormFile();
        formFile.setContentType("application/octet-stream");
        formFile.setFileName(storedFile.getName());
        formFile.setFormName("file");

        try {
            formFile.setData(storedFile.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get multipartFile inputStream", e);
        }

        byte[] resultBytes;
        String responseText = null;
        try {
            resultBytes = sendMultipartRequest(httpUrl, paramValueMap, formFile);
            if (resultBytes != null) {
                responseText  = new String(resultBytes, "UTF-8");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("向文档转换服务器提交的过程中出错");
        }

        return responseText;

    }

    public static byte[] sendMultipartRequest(String url,
                                              Map<String, String> parameters, FormFile formFile) throws Exception {
        if (formFile == null) {
            return send(url, parameters, null);
        } else {
            List<FormFile> files = new ArrayList<FormFile>();
            files.add(formFile);
            return send(url, parameters, files);
        }

    }

    /**
     * 直接通过HTTP协议提交数据到服务器,实现表单提交功能
     *
     * @param url
     *            上传路径
     * @param parameters
     *            请求参数 key为参数名,value为参数值
     * @param files
     *            上传文件
     * @throws Exception
     */

    public static byte[] send(String url, Map<String, String> parameters,
                              List<FormFile> files) throws Exception {
        if (url == null) {
            return null;
        }

        HttpURLConnection conn = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            conn = getHttpClient(url);

            outputStream = conn.getOutputStream();
            sendUploadParameters(outputStream, parameters);
            sendFilesData(outputStream, files);
            byte[] requestEnd = ("--" + BOUNDARY + "--\r\n").getBytes(UTF_8);// 数据结束标志
            outputStream.write(requestEnd);
            outputStream.flush();
            int httpCode = conn.getResponseCode();
            if (httpCode != 200) {
                throw new RuntimeException("Upload file failed, Http Response code: " + httpCode);
            }

            inputStream = conn.getInputStream();
            return getResultData(inputStream);
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
               e.printStackTrace();
            }finally {
                IOUtils.closeQuietly(outputStream);
                IOUtils.closeQuietly(inputStream);
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }

    private static byte[] getResultData(InputStream inputStream)
            throws IOException {
        int length = -1;
        ByteArrayOutputStream resultDataSteam = new ByteArrayOutputStream();
        byte[] buff = new byte[512];
        while ((length = inputStream.read(buff)) != -1) {
            resultDataSteam.write(buff, 0, length);
        }
        return resultDataSteam.toByteArray();
    }

    private static void sendFilesData(OutputStream outputStream,
                                      List<FormFile> files) throws Exception {
        if (files == null) {
            return;
        }

        for (FormFile file : files) {
            StringBuilder split = new StringBuilder();
            split.append("--");
            split.append(BOUNDARY);
            split.append("\r\n");
            split.append("Content-Disposition: form-data;name=\""
                    + file.formName + "\";filename=\"" + file.fileName
                    + "\"\r\n");
            split.append("Content-Type: " + file.contentType + "\r\n\r\n");
            outputStream.write(split.toString().getBytes(UTF_8));

            if (file.data instanceof byte[]) {
                byte[] byteData = (byte[]) file.data;
                outputStream.write(byteData, 0, byteData.length);
            } else if (file.data instanceof InputStream) {
                int length = -1;
                byte[] buff = new byte[512];
                InputStream streamData = (InputStream) file.data;
                while ((length = streamData.read(buff)) != -1) {
                    outputStream.write(buff, 0, length);
                }
            } else {
                String stringData = file.data.toString();
                byte[] byteData = stringData.getBytes(UTF_8);
                outputStream.write(byteData, 0, byteData.length);
            }

            outputStream.write("\r\n".getBytes());
        }
    }

    private static void sendUploadParameters(OutputStream outputStream,
                                             Map<String, String> params) throws Exception {
        if (params == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, String>> entrySet = params.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {// 构建表单字段内容
            sb.append("--");
            sb.append(BOUNDARY);
            sb.append("\r\n");
            sb.append("Content-Disposition: form-data; name=\""
                    + entry.getKey() + "\"\r\n\r\n");
            sb.append(entry.getValue());
            sb.append("\r\n");
        }

        String paramStrings = sb.toString();
        byte[] paramsData = paramStrings.getBytes(UTF_8);
        outputStream.write(paramsData);
    }

    private static HttpURLConnection getHttpClient(String actionUrl)
            throws Exception {
        URL url = new URL(actionUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Charset", UTF_8);
        connection.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + BOUNDARY);

        connection.setConnectTimeout(5000);
        return connection;
    }

    public static class FormFile {
        public static final String IMAGE_TYPE = "image/*";
        public static final String FILE_TYPE = "application/octet-stream";

        /* 上传文件的数据 */
        public Object data;
        /* 文件名称 */
        public String fileName;
        /* 表单字段名称 */
        public String formName;
        /* 内容类型 */
        public String contentType;

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFormName() {
            return formName;
        }

        public void setFormName(String formName) {
            this.formName = formName;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public static final FormFile getFileForm() {
            FormFile formFile = new FormFile();
            formFile.contentType = FILE_TYPE;
            return formFile;
        }

        public static final FormFile getImageForm() {
            FormFile formFile = new FormFile();
            formFile.contentType = IMAGE_TYPE;
            return formFile;
        }
    }
}
