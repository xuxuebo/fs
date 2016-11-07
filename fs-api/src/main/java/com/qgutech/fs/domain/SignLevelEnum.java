package com.qgutech.fs.domain;


public enum SignLevelEnum {
    /**
     * 文档url不校验 NONE
     */
    nn,
    /**
     * 文档url校验文档服务器的秘钥 SECRET
     */
    st,
    /**
     * 文档url校验过期时间和文档服务器的秘钥 SECRET_TIME
     */
    stt,
    /**
     * 文档url校验文档服务器的登录session SESSION
     */
    sn,
    /**
     * 文档url校验过期时间，文档服务器的秘钥和登录session SECRET_TIME_SESSION
     */
    sts
}
