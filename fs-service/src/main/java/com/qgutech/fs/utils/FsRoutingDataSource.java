package com.qgutech.fs.utils;

import com.google.gson.Gson;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FsRoutingDataSource extends AbstractRoutingDataSource {

    protected static long lastModifiedTime = 0l;
    protected static String path;
    protected static final Gson gson = new Gson();
    protected static final Log LOG = LogFactory.getLog(FsRoutingDataSource.class);
    protected static final String dataSourceFileName = "dataSource.properties";
    protected static final Map<String, Database> lookupDatabaseMap = new HashMap<String, Database>();

    @Override
    protected Object determineCurrentLookupKey() {
        return getDatabase();
    }

    @Override
    public void afterPropertiesSet() {
        if (this.defaultTargetDataSource != null) {
            this.resolvedDefaultDataSource = resolveSpecifiedDataSource(this.defaultTargetDataSource);
        }

        Properties properties = getDataSourceProperties();
        if (properties.size() == 0) {
            return;
        }

        this.resolvedDataSources = new HashMap<Object, DataSource>(properties.size());
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || value == null
                    || !(key instanceof String) || !(value instanceof String)) {
                continue;
            }

            Database database = gson.fromJson((String) value, Database.class);
            ComboPooledDataSource dataSource = new ComboPooledDataSource();
            try {
                dataSource.setDriverClass(database.getDriverClass());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            dataSource.setJdbcUrl(database.getJdbcUrl());
            dataSource.setUser(database.getUserName());
            dataSource.setPassword(database.getPassword());
            lookupDatabaseMap.put((String) key, database);
            resolvedDataSources.put(database, dataSource);
        }

        lastModifiedTime = getFileLastModifiedTime();
    }

    private long getFileLastModifiedTime() {
        if (path == null) {
            URL url = getClass().getClassLoader().getResource(dataSourceFileName);
            if (url == null) {
                throw new RuntimeException(dataSourceFileName + " in classpath not exist!");
            }

            try {
                path = java.net.URLDecoder.decode(url.getPath(), "UTF-8");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return new File(path).lastModified();
    }

    private Properties getDataSourceProperties() {
        if (path == null) {
            URL url = getClass().getClassLoader().getResource(dataSourceFileName);
            if (url == null) {
                throw new RuntimeException(dataSourceFileName + " in classpath not exist!");
            }

            try {
                path = java.net.URLDecoder.decode(url.getPath(), "UTF-8");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(path));
            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(dataSourceFileName + " in classpath not exist!", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return properties;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        switchSchema(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        switchSchema(connection);
        return connection;
    }

    private Database getDatabase() {
        String appCode = ExecutionContext.getAppCode();
        String corpCode = ExecutionContext.getCorpCode();
        Database database = lookupDatabaseMap.get(appCode + ";;" + corpCode);
        if (database != null) {
            return database;
        }

        database = lookupDatabaseMap.get("*;;" + corpCode);
        if (database != null) {
            return database;
        }

        return lookupDatabaseMap.get(appCode + ";;*");
    }

    private void switchSchema(Connection connection) {
        Database database = getDatabase();
        if (database == null) {
            return;
        }

        String schema = database.getSchema();
        if (schema == null || schema.trim().isEmpty()) {
            return;
        }

        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("SET search_path TO \"" + schema + "\",public;");
        } catch (Throwable e) {
            LOG.warn("SET search_path TO \"" + schema + "\",public failed!", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Throwable e) {
                    LOG.warn("Close statement failed!", e);
                }
            }
        }
    }

    protected DataSource determineTargetDataSource() {
        Assert.notNull(this.resolvedDataSources, "DataSource router not initialized");
        Object lookupKey = determineCurrentLookupKey();
        DataSource dataSource = this.resolvedDataSources.get(lookupKey);
        if (dataSource == null && (this.lenientFallback || lookupKey == null)) {
            dataSource = this.resolvedDefaultDataSource;
        }

        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
        }

        return dataSource;
    }
}
