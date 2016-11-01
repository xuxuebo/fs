package com.qgutech.fs.utils;

/**
 * 数据库连接对象
 */
public class Database {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Database database = (Database) o;

        if (driverClass != null ? !driverClass.equals(database.driverClass) : database.driverClass != null)
            return false;
        if (jdbcUrl != null ? !jdbcUrl.equals(database.jdbcUrl) : database.jdbcUrl != null) return false;
        if (password != null ? !password.equals(database.password) : database.password != null) return false;
        if (schema != null ? !schema.equals(database.schema) : database.schema != null) return false;
        if (userName != null ? !userName.equals(database.userName) : database.userName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = driverClass != null ? driverClass.hashCode() : 0;
        result = 31 * result + (jdbcUrl != null ? jdbcUrl.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (schema != null ? schema.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Database{" +
                "driverClass='" + driverClass + '\'' +
                ", jdbcUrl='" + jdbcUrl + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", schema='" + schema + '\'' +
                '}';
    }
}
