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
    public static final String _vbox = "vbox";
    public static final String _upload = "upload";
    public static final String _download = "download";
    public static final String _secret = "secret";
    /**
     * 文档服务器的名称
     */
    @Column(nullable = false, length = 100)
    private String serverName;

    /**
     * 文档服务器的编号，一个分布式文档服务器集群用一个编号
     * ，如果是内部文档服务器则单独使用一个。
     */
    @Column(nullable = false, length = 20)
    private String serverCode;

    /**
     * 文档服务器的域名或者ip
     */
    @Column(nullable = false, length = 50)
    private String host;

    /**
     * 文档服务器是否是vBox，默认是false
     */
    @Column
    private Boolean vbox;

    /**
     * 文档服务器是否可以上传，默认是true
     */
    @Column
    private Boolean upload;

    /**
     * 文档服务器是否可以下载，默认是true
     */
    @Column
    private Boolean download;

    /**
     * 文档服务器的秘钥
     */
    @Column(length = 200)
    private String secret;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerCode() {
        return serverCode;
    }

    public void setServerCode(String serverCode) {
        this.serverCode = serverCode;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Boolean getVbox() {
        return vbox;
    }

    public void setVbox(Boolean vbox) {
        this.vbox = vbox;
    }

    public Boolean getUpload() {
        return upload;
    }

    public void setUpload(Boolean upload) {
        this.upload = upload;
    }

    public Boolean getDownload() {
        return download;
    }

    public void setDownload(Boolean download) {
        this.download = download;
    }
}
