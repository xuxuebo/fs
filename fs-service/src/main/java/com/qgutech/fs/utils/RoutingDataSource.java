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
import java.util.*;

public class RoutingDataSource extends AbstractRoutingDataSource {

    private static long lastModifiedTime = 0l;
    private static String path;
    private static final Gson gson = new Gson();
    private static final Log LOG = LogFactory.getLog(RoutingDataSource.class);
    private static final String dataSourceFileName = "dataSource.properties";
    private static final Map<String, Database> lookupDatabaseMap = new HashMap<String, Database>();
    private static final Map<String, Long> databaseKeyCntMap = new HashMap<String, Long>();
    private static final Map<DataSource, Long> waitToCloseMap = new HashMap<DataSource, Long>();
    private static final long DEFAULT_EXPIRE_TIME = 3 * 60 * 1000l;
    private static final long DEFAULT_EXECUTE_TIME = 30 * 60 * 1000l;

    private boolean enableAutoUpdate = true;
    private long expireTime = DEFAULT_EXPIRE_TIME;
    private long executeTime = DEFAULT_EXECUTE_TIME;

    public void init() {
        if (!isEnableAutoUpdate()) {
            return;
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (waitToCloseMap.size() == 0) {
                    return;
                }

                for (Map.Entry<DataSource, Long> entry : waitToCloseMap.entrySet()) {
                    if (System.currentTimeMillis() - entry.getValue() > getExpireTime()) {
                        DataSource dataSource = entry.getKey();
                        if (dataSource instanceof ComboPooledDataSource) {
                            ((ComboPooledDataSource) dataSource).close();
                        }

                        waitToCloseMap.remove(dataSource);
                    }
                }
            }
        }, getExecuteTime(), getExecuteTime());
    }

    @Override
    protected Object determineCurrentLookupKey() {
        Database database = getDatabase();
        if (database == null) {
            return null;
        }

        return createKeyFromDatabase(database);
    }

    @Override
    public void afterPropertiesSet() {
        if (this.defaultTargetDataSource != null) {
            this.resolvedDefaultDataSource = resolveSpecifiedDataSource(this.defaultTargetDataSource);
        }

        lastModifiedTime = getFileLastModifiedTime();
        Properties properties = getDataSourceProperties();
        this.resolvedDataSources = new HashMap<Object, DataSource>();
        if (properties.size() == 0) {
            return;
        }

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || value == null
                    || !(key instanceof String)
                    || !(value instanceof String)) {
                continue;
            }

            Database database = gson.fromJson((String) value, Database.class);
            String keyFromDatabase = createKeyFromDatabase(database);
            DataSource dataSource = this.resolvedDataSources.get(keyFromDatabase);
            Long count = databaseKeyCntMap.get(keyFromDatabase);
            if (dataSource == null && count == null) {
                databaseKeyCntMap.put(keyFromDatabase, 1l);
                this.resolvedDataSources.put(keyFromDatabase, toDataSource(database));
            } else {
                databaseKeyCntMap.put(keyFromDatabase, ++count);
            }

            lookupDatabaseMap.put((String) key, database);
        }
    }

    private String createKeyFromDatabase(Database database) {
        return database.getDriverClass() + ";;" + database.getJdbcUrl() + ";;"
                + database.getUserName() + ";;" + database.getPassword();
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

    private void updateDataSources() {
        Properties properties = getDataSourceProperties();
        if (properties.size() == 0) {
            return;
        }

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || value == null
                    || !(key instanceof String) || !(value instanceof String)) {
                continue;
            }

            Database database = gson.fromJson((String) value, Database.class);
            Database oldDatabase = lookupDatabaseMap.get(key);
            String keyFromDatabase = createKeyFromDatabase(database);
            if (oldDatabase == null) {
                DataSource dataSource = this.resolvedDataSources.get(keyFromDatabase);
                Long count = databaseKeyCntMap.get(keyFromDatabase);
                if (dataSource == null && count == null) {
                    databaseKeyCntMap.put(keyFromDatabase, 1l);
                    this.resolvedDataSources.put(keyFromDatabase, toDataSource(database));
                } else {
                    databaseKeyCntMap.put(keyFromDatabase, ++count);
                }

                lookupDatabaseMap.put((String) key, database);
                continue;
            }

            if (database.equals(oldDatabase)) {
                continue;
            }

            String oldKeyFromDatabase = createKeyFromDatabase(oldDatabase);
            if (keyFromDatabase.equals(oldKeyFromDatabase)) {
                lookupDatabaseMap.put((String) key, database);
                continue;
            }

            DataSource dataSource = this.resolvedDataSources.get(keyFromDatabase);
            Long count = databaseKeyCntMap.get(keyFromDatabase);
            if (dataSource == null && count == null) {
                databaseKeyCntMap.put(keyFromDatabase, 1l);
                this.resolvedDataSources.put(keyFromDatabase, toDataSource(database));
            } else {
                databaseKeyCntMap.put(keyFromDatabase, ++count);
            }

            Long oldCount = databaseKeyCntMap.get(oldKeyFromDatabase);
            if ((oldCount == null || oldCount <= 1)) {
                DataSource oldDataSource = this.resolvedDataSources.get(oldKeyFromDatabase);
                this.resolvedDataSources.remove(oldKeyFromDatabase);
                waitToCloseMap.put(oldDataSource, System.currentTimeMillis());
            } else {
                databaseKeyCntMap.put(oldKeyFromDatabase, --oldCount);
            }

            lookupDatabaseMap.put((String) key, database);
        }
    }

    private DataSource toDataSource(Database database) {
        try {
            ComboPooledDataSource dataSource = new ComboPooledDataSource();
            dataSource.setDriverClass(database.getDriverClass());
            dataSource.setJdbcUrl(database.getJdbcUrl());
            dataSource.setUser(database.getUserName());
            dataSource.setPassword(database.getPassword());
            return dataSource;
        } catch (Exception e) {
            throw new RuntimeException("Create ComboPooledDataSource by Database["
                    + database + "] failed!", e);
        }
    }

    protected DataSource determineTargetDataSource() {
        Assert.notNull(this.resolvedDataSources, "DataSource router not initialized");
        if (isEnableAutoUpdate()) {
            synchronized (lookupDatabaseMap) {
                long fileLastModifiedTime = getFileLastModifiedTime();
                if (fileLastModifiedTime != lastModifiedTime) {
                    updateDataSources();
                    lastModifiedTime = fileLastModifiedTime;
                }
            }
        }

        Object lookupKey = determineCurrentLookupKey();
        DataSource dataSource = this.resolvedDataSources.get(lookupKey);
        if (dataSource == null && (this.lenientFallback || lookupKey == null)) {
            dataSource = this.resolvedDefaultDataSource;
        }

        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine target");
        }

        return dataSource;
    }

    public boolean isEnableAutoUpdate() {
        return enableAutoUpdate;
    }

    public void setEnableAutoUpdate(boolean enableAutoUpdate) {
        this.enableAutoUpdate = enableAutoUpdate;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public long getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(long executeTime) {
        this.executeTime = executeTime;
    }

    public static void main(String[] args) {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.afterPropertiesSet();
        routingDataSource.determineTargetDataSource();
    }
}
