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
    private static final Map<String, Database1> lookupDatabaseMap = new HashMap<String, Database1>();
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
        Database1 database1 = getDatabase();
        if (database1 == null) {
            return null;
        }

        return createKeyFromDatabase(database1);
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

            Database1 database1 = gson.fromJson((String) value, Database1.class);
            String keyFromDatabase = createKeyFromDatabase(database1);
            DataSource dataSource = this.resolvedDataSources.get(keyFromDatabase);
            Long count = databaseKeyCntMap.get(keyFromDatabase);
            if (dataSource == null && count == null) {
                databaseKeyCntMap.put(keyFromDatabase, 1l);
                this.resolvedDataSources.put(keyFromDatabase, toDataSource(database1));
            } else {
                databaseKeyCntMap.put(keyFromDatabase, ++count);
            }

            lookupDatabaseMap.put((String) key, database1);
        }
    }

    private String createKeyFromDatabase(Database1 database1) {
        return database1.getDriverClass() + ";;" + database1.getJdbcUrl() + ";;"
                + database1.getUserName() + ";;" + database1.getPassword();
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

    private Database1 getDatabase() {
        String appCode = ExecutionContext.getAppCode();
        String corpCode = ExecutionContext.getCorpCode();
        Database1 database1 = lookupDatabaseMap.get(appCode + ";;" + corpCode);
        if (database1 != null) {
            return database1;
        }

        database1 = lookupDatabaseMap.get("*;;" + corpCode);
        if (database1 != null) {
            return database1;
        }

        return lookupDatabaseMap.get(appCode + ";;*");
    }

    private void switchSchema(Connection connection) {
        Database1 database1 = getDatabase();
        if (database1 == null) {
            return;
        }

        String schema = database1.getSchema();
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

            Database1 database1 = gson.fromJson((String) value, Database1.class);
            Database1 oldDatabase1 = lookupDatabaseMap.get(key);
            String keyFromDatabase = createKeyFromDatabase(database1);
            if (oldDatabase1 == null) {
                DataSource dataSource = this.resolvedDataSources.get(keyFromDatabase);
                Long count = databaseKeyCntMap.get(keyFromDatabase);
                if (dataSource == null && count == null) {
                    databaseKeyCntMap.put(keyFromDatabase, 1l);
                    this.resolvedDataSources.put(keyFromDatabase, toDataSource(database1));
                } else {
                    databaseKeyCntMap.put(keyFromDatabase, ++count);
                }

                lookupDatabaseMap.put((String) key, database1);
                continue;
            }

            if (database1.equals(oldDatabase1)) {
                continue;
            }

            String oldKeyFromDatabase = createKeyFromDatabase(oldDatabase1);
            if (keyFromDatabase.equals(oldKeyFromDatabase)) {
                lookupDatabaseMap.put((String) key, database1);
                continue;
            }

            DataSource dataSource = this.resolvedDataSources.get(keyFromDatabase);
            Long count = databaseKeyCntMap.get(keyFromDatabase);
            if (dataSource == null && count == null) {
                databaseKeyCntMap.put(keyFromDatabase, 1l);
                this.resolvedDataSources.put(keyFromDatabase, toDataSource(database1));
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

            lookupDatabaseMap.put((String) key, database1);
        }
    }

    private DataSource toDataSource(Database1 database1) {
        try {
            ComboPooledDataSource dataSource = new ComboPooledDataSource();
            dataSource.setDriverClass(database1.getDriverClass());
            dataSource.setJdbcUrl(database1.getJdbcUrl());
            dataSource.setUser(database1.getUserName());
            dataSource.setPassword(database1.getPassword());
            return dataSource;
        } catch (Exception e) {
            throw new RuntimeException("Create ComboPooledDataSource by Database1["
                    + database1 + "] failed!", e);
        }
    }

    protected DataSource determineTargetDataSource() {
        Assert.notNull(this.resolvedDataSources, "DataSource router not initialized");
        if (isEnableAutoUpdate()) {
            long fileLastModifiedTime = getFileLastModifiedTime();
            if (fileLastModifiedTime != lastModifiedTime) {
                synchronized (lookupDatabaseMap) {
                    fileLastModifiedTime = getFileLastModifiedTime();
                    if (fileLastModifiedTime != lastModifiedTime) {
                        updateDataSources();
                        lastModifiedTime = fileLastModifiedTime;
                    }
                }
            }
        }

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
