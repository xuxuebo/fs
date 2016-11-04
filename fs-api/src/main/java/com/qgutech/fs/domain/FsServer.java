package com.qgutech.fs.domain;


import com.qgutech.fs.domain.base.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_fs_server")
public class FsServer extends BaseEntity {

    public static final String _serverName = "serverName";
    public static final String _serverCode = "serverCode";
    public static final String _host = "host";

    @Column(nullable = false, length = 100)
    private String serverName;

    @Column(nullable = false, length = 20)
    private String serverCode;

    @Column(nullable = false, length = 50)
    private String host;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getServerCode() {
        return serverCode;
    }

    public void setServerCode(String serverCode) {
        this.serverCode = serverCode;
    }
}
