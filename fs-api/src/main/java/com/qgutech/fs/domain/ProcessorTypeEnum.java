package com.qgutech.fs.domain;

import com.qgutech.fs.utils.FsConstants;

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
    ZDOC;

    public static String toDirectory(ProcessorTypeEnum processorTypeEnum) {
        if (VID.equals(processorTypeEnum)
                || AUD.equals(processorTypeEnum)
                || DOC.equals(processorTypeEnum)
                || IMG.equals(processorTypeEnum)
                || FILE.equals(processorTypeEnum)) {
            return processorTypeEnum.name().toLowerCase();
        }

        return ZIP.name().toLowerCase();
    }

    public static String toGenDirectory(ProcessorTypeEnum processorTypeEnum) {
        if (AUD.equals(processorTypeEnum) || ZAUD.equals(processorTypeEnum)) {
            return FsConstants.DEFAULT_AUDIO_TYPE;
        } else if (VID.equals(processorTypeEnum) || ZVID.equals(processorTypeEnum)) {
            return FsConstants.DEFAULT_VIDEO_TYPE;
        } else if (IMG.equals(processorTypeEnum) || ZIMG.equals(processorTypeEnum)
                || DOC.equals(processorTypeEnum) || ZDOC.equals(processorTypeEnum)) {
            return FsConstants.FILE_DIR_IMG;
        } else if (ZIP.equals(processorTypeEnum)) {
            return FsConstants.FILE_DIR_UNZIP;
        } else {
            throw new RuntimeException("processorTypeEnum[" + processorTypeEnum + "] is not support!");
        }
    }
}
