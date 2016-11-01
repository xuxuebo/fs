package com.qgutech.fs.domain;


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
     * @see com.qgutech.fs.domain.ProcessorTypeEnum
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
     * 表示zdoc转化为图片后，每个文档的页数，如zdoc包含三个文档，也数为4,5,6，形式如4|5|6
     */
    private String subFileCounts;

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

    /**
     * 视频的级别
     *
     * @see com.qgutech.fs.domain.VideoTypeEnum
     */
    private String videoLevel;

    public String getStoredFileId() {
        return storedFileId;
    }

    public void setStoredFileId(String storedFileId) {
        this.storedFileId = storedFileId;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getCorpCode() {
        return corpCode;
    }

    public void setCorpCode(String corpCode) {
        this.corpCode = corpCode;
    }

    public Integer getSubFileCount() {
        return subFileCount;
    }

    public void setSubFileCount(Integer subFileCount) {
        this.subFileCount = subFileCount;
    }

    public String getSubFileCounts() {
        return subFileCounts;
    }

    public void setSubFileCounts(String subFileCounts) {
        this.subFileCounts = subFileCounts;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public String getBusinessDir() {
        return businessDir;
    }

    public void setBusinessDir(String businessDir) {
        this.businessDir = businessDir;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getVideoLevel() {
        return videoLevel;
    }

    public void setVideoLevel(String videoLevel) {
        this.videoLevel = videoLevel;
    }
}
