package com.qgutech.fs.domain;


import com.qgutech.fs.domain.base.BaseEntity;
import com.qgutech.fs.utils.ReflectUtil;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.lang.reflect.Field;

@Entity
@Table(name = "t_fs_file")
public class FsFile extends BaseEntity {

    public static final String _serverCode = "serverCode";

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
     * 视频的级别，多个视频使用|分割，如H|H|L。
     *
     * @see com.qgutech.fs.domain.VideoTypeEnum
     */
    @Column(length = 50)
    private String videoLevels;

    /**
     * 视频或者音频的时长，比如00:20:20|01:10:10|00:00:50
     */
    @Column(length = 500)
    private String durations;

    /**
     * 文件状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProcessStatusEnum status;

    @Column(nullable = false, length = 20)
    private String serverCode;

    /**
     * 上传的文件对象
     */
    @Transient
    private MultipartFile file;

    /**
     * fs和fs-service通信时的时间戳
     */
    @Transient
    private Long timestamp;

    /**
     * fs和fs-service通信时的签名
     */
    @Transient
    private String sign;

    /**
     * fs和fs-service通信时的fs的域名或者ip（对外提供服务的域名）
     */
    @Transient
    private String serverHost;

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

    public String getVideoLevels() {
        return videoLevels;
    }

    public void setVideoLevels(String videoLevels) {
        this.videoLevels = videoLevels;
    }

    public String getDurations() {
        return durations;
    }

    public void setDurations(String durations) {
        this.durations = durations;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public ProcessStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ProcessStatusEnum status) {
        this.status = status;
    }

    public String getServerCode() {
        return serverCode;
    }

    public void setServerCode(String serverCode) {
        this.serverCode = serverCode;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public void merge(FsFile fsFile) {
        if (fsFile == null) {
            return;
        }

        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Column.class) == null) {
                continue;
            }

            Object fieldValue = ReflectUtil.getFieldValue(field, fsFile);
            if (fieldValue == null) {
                continue;
            }

            ReflectUtil.setFieldValue(field, this, fieldValue);
        }
    }

    @Override
    public String toString() {
        return "FsFile{" +
                "storedFileName='" + storedFileName + '\'' +
                ", processor=" + processor +
                ", appCode='" + appCode + '\'' +
                ", subFileCount=" + subFileCount +
                ", subFileCounts='" + subFileCounts + '\'' +
                ", fileSize=" + fileSize +
                ", businessId='" + businessId + '\'' +
                ", businessCode='" + businessCode + '\'' +
                ", businessDir='" + businessDir + '\'' +
                ", suffix='" + suffix + '\'' +
                ", videoLevels='" + videoLevels + '\'' +
                ", durations='" + durations + '\'' +
                ", file=" + file +
                ", status=" + status +
                ", serverCode='" + serverCode + '\'' +
                '}';
    }
}
