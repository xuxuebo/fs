package com.qgutech.fs.utils;

import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.Properties;

public class MimeUtils {

    private static final Properties properties = new Properties();
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    static {
        InputStream resourceAsStream = MimeUtils.class.getClassLoader()
                .getResourceAsStream("mime.properties");
        if (resourceAsStream == null) {
            throw new RuntimeException("The mime.properties in classpath not exist!");
        }

        try {
            properties.load(resourceAsStream);
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred when load content from mime.properties!", e);
        }
    }

    public static String getContentTypeByExt(String ext) {
        if (StringUtils.isEmpty(ext)) {
            return DEFAULT_CONTENT_TYPE;
        }

        return properties.getProperty(ext, DEFAULT_CONTENT_TYPE);
    }
}
