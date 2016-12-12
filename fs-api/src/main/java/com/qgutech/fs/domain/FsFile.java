package com.qgutech.fs.domain;


import com.qgutech.fs.domain.base.BaseEntity;
import com.qgutech.fs.utils.FsConstants;
import com.qgutech.fs.utils.ReflectUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Entity
@Table(name = "t_fs_file")
public class FsFile extends BaseEntity {
    public static final String _storedFileName = "storedFileName";
    public static final String _processor = "processor";
    public static final String _appCode = "appCode";
    public static final String _subFileCount = "subFileCount";
    public static final String _subFileCounts = "subFileCounts";
    public static final String _fileSize = "fileSize";
    public static final String _businessId = "businessId";
    public static final String _businessCode = "businessCode";
    public static final String _businessDir = "businessDir";
    public static final String _suffix = "suffix";
    public static final String _videoLevels = "videoLevels";
    public static final String _durations = "durations";
    public static final String _status = "status";
    public static final String _processMsg = "processMsg";
    public static final String _serverCode = "serverCode";
    public static final String _responseFormat = "responseFormat";
    public static final String _fileUrl = "fileUrl";
    public static final String _timestamp = "timestamp";
    public static final String _sign = "sign";
    public static final String _serverHost = "serverHost";
    public static final String _file = "file";
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

    @Column(length = 500)
    private String processMsg;

    /**
     * 文档上传的文档服务器所在的集群编号
     */
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

    /**
     * 用于临时保存文件的路径
     */
    @Transient
    private String tmpFilePath;

    /**
     * 文档处理完后，返回的url
     */
    @Transient
    private String backUrl;

    /**
     * 响应请求的格式
     */
    @Transient
    private String responseFormat = FsConstants.RESPONSE_FORMAT_JSON;

    /**
     * 文件的url
     */
    @Transient
    private String fileUrl;

    /**
     * 截图时指定的x轴坐标
     */
    @Transient
    private Integer x;

    /**
     * 截图时指定的y轴坐标
     */
    @Transient
    private Integer y;

    /**
     * 截图时指定的宽度
     */
    @Transient
    private Integer w;

    /**
     * 截图时指定的高度
     */
    @Transient
    private Integer h;

    /**
     * 登录的sessionId
     */
    @Transient
    private String session;

    /**
     * 文件的md5值
     */
    @Transient
    private String md5;

    /**
     * 断点续传的类型
     */
    @Transient
    private String resumeType;

    /**
     * 文件分块的每块大小
     */
    @Transient
    private Long chunkSize;

    /**
     * 文件当前分块的大小
     */
    @Transient
    private Long blockSize;

    /**
     * 文件分块的序号
     */
    @Transient
    private Long chunk;

    /**
     * 文件分块数量
     */
    @Transient
    private Long chunks;

    /**
     * 前一次上传的文件信息
     */
    @Transient
    private String beforeFsFileJson;

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

    public ProcessStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ProcessStatusEnum status) {
        this.status = status;
    }

    public String getProcessMsg() {
        return processMsg;
    }

    public void setProcessMsg(String processMsg) {
        if (StringUtils.isEmpty(processMsg)) {
            return;
        }

        if (processMsg.length() > 500) {
            this.processMsg = processMsg.substring(0, 500);
        } else {
            this.processMsg = processMsg;
        }
    }

    public String getServerCode() {
        return serverCode;
    }

    public void setServerCode(String serverCode) {
        this.serverCode = serverCode;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
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

    public String getTmpFilePath() {
        return tmpFilePath;
    }

    public void setTmpFilePath(String tmpFilePath) {
        this.tmpFilePath = tmpFilePath;
    }

    public String getBackUrl() {
        return backUrl;
    }

    public void setBackUrl(String backUrl) {
        this.backUrl = backUrl;
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Integer getW() {
        return w;
    }

    public void setW(Integer w) {
        this.w = w;
    }

    public Integer getH() {
        return h;
    }

    public void setH(Integer h) {
        this.h = h;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getResumeType() {
        return resumeType;
    }

    public void setResumeType(String resumeType) {
        this.resumeType = resumeType;
    }

    public Long getChunk() {
        return chunk;
    }

    public void setChunk(Long chunk) {
        this.chunk = chunk;
    }

    public Long getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Long chunkSize) {
        this.chunkSize = chunkSize;
    }

    public Long getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(Long blockSize) {
        this.blockSize = blockSize;
    }

    public Long getChunks() {
        return chunks;
    }

    public void setChunks(Long chunks) {
        this.chunks = chunks;
    }

    public String getBeforeFsFileJson() {
        return beforeFsFileJson;
    }

    public void setBeforeFsFileJson(String beforeFsFileJson) {
        this.beforeFsFileJson = beforeFsFileJson;
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
                ", status=" + status +
                ", processMsg='" + processMsg + '\'' +
                ", serverCode='" + serverCode + '\'' +
                ", file=" + file +
                ", timestamp=" + timestamp +
                ", sign='" + sign + '\'' +
                ", serverHost='" + serverHost + '\'' +
                ", tmpFilePath='" + tmpFilePath + '\'' +
                ", backUrl='" + backUrl + '\'' +
                ", responseFormat='" + responseFormat + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                ", session='" + session + '\'' +
                ", md5='" + md5 + '\'' +
                ", resumeType='" + resumeType + '\'' +
                ", chunkSize=" + chunkSize +
                ", blockSize=" + blockSize +
                ", chunk=" + chunk +
                ", chunks=" + chunks +
                ", beforeFsFileJson='" + beforeFsFileJson + '\'' +
                '}';
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

    public boolean validateUpload() {
        return processor != null && StringUtils.isNotEmpty(appCode)
                && StringUtils.isNotEmpty(businessId) && StringUtils.isNotEmpty(getCorpCode());
    }

    public Map<String, String> toMap() {
        Map<String, String> resultMap = new HashMap<String, String>();
        if (StringUtils.isNotEmpty(getId())) {
            resultMap.put(_id, getId());
        }

        if (StringUtils.isNotEmpty(getCorpCode())) {
            resultMap.put(_corpCode, getCorpCode());
        }

        if (getCreateTime() != null) {
            resultMap.put(_createTime, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                    .format(getCreateTime()));
        }

        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Column.class) == null
                    && field.getAnnotation(Transient.class) == null
                    || field.getName().equals(_file)) {
                continue;
            }

            Object fieldValue = ReflectUtil.getFieldValue(field, this);
            if (fieldValue == null) {
                continue;
            }

            resultMap.put(field.getName(), fieldValue.toString());
        }

        return resultMap;
    }

    protected Map<String, String> toValueMap() {
        Map<String, String> valueMap = new TreeMap<String, String>();
        String id = getId();
        if (StringUtils.isNotEmpty(id)) {
            valueMap.put(_id, id);
        }

        String corpCode = getCorpCode();
        if (StringUtils.isNotEmpty(corpCode)) {
            valueMap.put(_corpCode, corpCode);
        }

        if (StringUtils.isNotEmpty(storedFileName)) {
            valueMap.put(_storedFileName, storedFileName);
        }

        if (processor != null) {
            valueMap.put(_processor, processor.name());
        }

        if (StringUtils.isNotEmpty(appCode)) {
            valueMap.put(_appCode, appCode);
        }

        if (subFileCount != null) {
            valueMap.put(_subFileCount, subFileCount.toString());
        }

        if (StringUtils.isNotEmpty(subFileCounts)) {
            valueMap.put(_subFileCounts, subFileCounts);
        }

        if (fileSize != null) {
            valueMap.put(_fileSize, fileSize.toString());
        }

        if (StringUtils.isNotEmpty(businessId)) {
            valueMap.put(_businessId, businessId);
        }

        if (StringUtils.isNotEmpty(businessCode)) {
            valueMap.put(_businessCode, businessCode);
        }

        if (StringUtils.isNotEmpty(businessDir)) {
            valueMap.put(_businessDir, businessDir);
        }

        if (StringUtils.isNotEmpty(suffix)) {
            valueMap.put(_suffix, suffix);
        }

        if (StringUtils.isNotEmpty(videoLevels)) {
            valueMap.put(_videoLevels, videoLevels);
        }

        if (StringUtils.isNotEmpty(durations)) {
            valueMap.put(_durations, durations);
        }

        if (status != null) {
            valueMap.put(_status, status.name());
        }

        if (StringUtils.isNotEmpty(processMsg)) {
            valueMap.put(_processMsg, processMsg);
        }

        if (StringUtils.isNotEmpty(serverCode)) {
            valueMap.put(_serverCode, serverCode);
        }

        if (StringUtils.isNotEmpty(responseFormat)) {
            valueMap.put(_responseFormat, responseFormat);
        }

        if (StringUtils.isNotEmpty(fileUrl)) {
            valueMap.put(_fileUrl, fileUrl);
        }

        return valueMap;
    }

    public String toJson() {
        Map<String, String> valueMap = toValueMap();
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            builder.append("\"");
            builder.append(entry.getKey());
            builder.append("\":");
            builder.append("\"");
            builder.append(entry.getValue());
            builder.append("\"");
            builder.append(",\n");
        }

        builder.delete(builder.length() - 2, builder.length());
        builder.append("\n}");

        return builder.toString();
    }

    public String toJsonp() {
        return "jsonp(" + toJson() + ")";
    }

    public String toHtml(String domain) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html>\n");
        builder.append(getHeader(domain));
        builder.append("<body>\n<div id='result'>\n");
        Map<String, String> valueMap = toValueMap();
        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            builder.append("<div id=\"");
            builder.append(entry.getKey());
            builder.append("\">");
            builder.append(entry.getValue());
            builder.append("</div>\n");
        }

        builder.append("</div>\n</body>\n</html>");
        return builder.toString();
    }

    protected String getHeader(String domain) {
        return "<head>\n"
                + (StringUtils.isNotEmpty(domain) ? "<script></script>\n"
                : "<script>document.domain='" + domain + "'</script>\n")
                + "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"/>\n"
                + "<meta http-equiv=\"pragma\" content=\"no-cache\"/>\n"
                + "<meta http-equiv=\"cache-control\" content=\"no-cache\"/>\n"
                + "<meta http-equiv=\"expires\" content=\"0\"/>\n"
                + "</head>\n";
    }

    public String toXml() {
        Map<String, String> valueMap = toValueMap();
        StringBuilder builder = new StringBuilder();
        builder.append("<result>\n");
        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            builder.append("<");
            builder.append(entry.getKey());
            builder.append(">");
            builder.append(entry.getValue());
            builder.append("</");
            builder.append(entry.getKey());
            builder.append(">\n");
        }

        builder.append("</result>");
        return builder.toString();
    }

}
