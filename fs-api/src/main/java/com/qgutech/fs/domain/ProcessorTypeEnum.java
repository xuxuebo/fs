package com.qgutech.fs.domain;

public enum ProcessorTypeEnum {
    /**
     * 视频
     */
    VID,
    /**
     * 音频
     */
    AUD,
    /**
     * 图片
     */
    IMG,
    /**
     * 文档
     */
    DOC,
    /**
     * 普通上传文件，不做处理
     */
    FILE,
    /**
     * 压缩文件，只做加压处理
     */
    ZIP,
    /**
     * 压缩文件，里面的都是视频，做视频处理
     */
    ZVID,
    /**
     * 压缩文件，里面的都是音频，做音频处理
     */
    ZAUD,
    /**
     * 压缩文件，里面的都是图片，做图片处理
     */
    ZIMG,
    /**
     * 压缩文件，里面的都是文档，做文档处理
     */
    ZDOC
}
