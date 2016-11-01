package com.qgutech.fs.utils;

/**
 * 数据库连接对象
 */
public class DataBase {
    /**
     * 数据库驱动类
     */
    private String driverClass;
    /**
     * 驱动地址
     */
    private String jdbcUrl;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String password;

    /**
     * 数据库schema
     */
    private String schema;

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public int hashCode() {
        return jdbcUrl == null ? 7 : jdbcUrl.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (this == obj
                || ((obj instanceof DataBase)
                && jdbcUrl.equalsIgnoreCase(((DataBase) obj).getJdbcUrl())));
    }

    @Override
    public String toString() {
        return "DataBase{" +
                "driverClass='" + driverClass + '\'' +
                ", jdbcUrl='" + jdbcUrl + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", schema='" + schema + '\'' +
                '}';
    }
}
