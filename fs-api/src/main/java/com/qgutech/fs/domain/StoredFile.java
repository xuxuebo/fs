package com.qgutech.fs.domain;


import org.hibernate.annotations.GenericGenerator;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_fs_stored_file")
//@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "commonCache")
public class StoredFile {

    /**
     * 上传文件在文件系统中的主键
     */
    @Id
    @Column(nullable = false, length = 32)
    @GeneratedValue(generator = "uuidGenerator")
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    private String storedFileId;

    /**
     * 上传文件的文件名（源文件名称）
     */
    @Column(nullable = false, length = 200)
    private String storedFileName;

    /**
     * 处理类型（上传时必须指定）
     *
     * @see ProcessorTypeEnum
     */
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ProcessorTypeEnum processor;

    /**
     * 应用编号（上传时必须指定）
     */
    @Column(nullable = false, length = 50)
    private String appCode;

    /**
     * 公司编号（上传时必须指定）
     */
    @Column(nullable = false, length = 50)
    private String corpCode;

    /**
     * 子文件数
     */
    @Column
    private Integer subFileCount;

    /**
     * 表示zdoc转化为图片后，每个文档的页数，如zdoc包含三个文档，也数为4,5,6，形式如4|5|6
     */
    @Column(length = 100)
    private String subFileCounts;

    /**
     * 文件大小
     */
    @Column(nullable = false)
    private Long fileSize;

    /**
     * 关联的业务主键（上传时必须指定）
     */
    @Column(nullable = false, length = 50)
    private String businessId;

    /**
     * 关联的业务编号
     */
    @Column(length = 50)
    private String businessCode;

    /**
     * 指定业务目录，指定时可以将一些指定业务的关联的文件放到该目录下
     */
    @Column(length = 50)
    private String businessDir;

    /**
     * 文件尾缀
     */
    @Column(nullable = false, length = 10)
    private String suffix;

    /**
     * 视频的级别
     *
     * @see com.qgutech.fs.domain.VideoTypeEnum
     */
    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private VideoTypeEnum videoLevel;

    /**
     * 视频时长
     */
    @Column(length = 20)
    private String duration;

    /**
     * 上传的文件对象
     */
    @Transient
    private MultipartFile file;

    /**
     * 创建时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createTime;

    /**
     * 最后更新时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date updateTime;

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

    public ProcessorTypeEnum getProcessor() {
        return processor;
    }

    public void setProcessor(ProcessorTypeEnum processor) {
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

    public VideoTypeEnum getVideoLevel() {
        return videoLevel;
    }

    public void setVideoLevel(VideoTypeEnum videoLevel) {
        this.videoLevel = videoLevel;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
