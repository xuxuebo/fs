package com.qgutech.fs.model;


import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

public class StoredFile {

    /**
     * 上传文件在文件系统中的主键
     */
    private String storedFileId;

    /**
     * 上传文件的文件名（源文件名称）
     */
    private String storedFileName;

    /**
     * 处理类型（上传时必须指定）
     *
     * @see com.qgutech.fs.model.ProcessorTypeEnum
     */
    private String processor;

    /**
     * 应用编号（上传时必须指定）
     */
    private String appCode;

    /**
     * 公司编号（上传时必须指定）
     */
    private String corpCode;

    /**
     * 子文件数
     */
    private Integer subFileCount;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 关联的业务主键（上传时必须指定）
     */
    private String businessId;

    /**
     * 关联的业务编号
     */
    private String businessCode;

    /**
     * 指定业务目录，指定时可以将一些指定业务的关联的文件放到该目录下
     */
    private String businessDir;

    /**
     * 文件尾缀
     */
    private String suffix;

    /**
     * 上传的文件对象
     */
    private MultipartFile file;

    /**
     * 创建时间
     */
    private Date createTime;


}
