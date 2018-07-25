package com.qgutech.fs.utils;

import org.springframework.beans.factory.FactoryBean;
import sun.misc.BASE64Decoder;

import java.util.Properties;

public class PropertiesEncryptFactoryBean implements FactoryBean {

    private Properties properties;

    @Override
    public Object getObject() throws Exception {
        return getProperties();
    }

    @Override
    public Class getObjectType() {
        return java.util.Properties.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties inProperties) {
        this.properties = inProperties;
        String originalUsername = properties.getProperty("user");
        String originalPassword = properties.getProperty("password");
        if (originalUsername != null) {
            properties.put("user", deEncryptUsername(originalUsername));
        }
        if (originalPassword != null) {
            properties.put("password", deEncryptPassword(originalPassword));
        }
    }

    private String deEncryptUsername(String originalUsername) {
        return deEncryptString(originalUsername);
    }

    private String deEncryptPassword(String originalPassword) {
        return deEncryptString(originalPassword);
    }

    private static String deEncryptString(String express) {
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] bytes = decoder.decodeBuffer(express);
            for (int i = 0; i < 5; i++) {
                bytes = decoder.decodeBuffer(new String(bytes, "UTF-8"));
            }
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}  