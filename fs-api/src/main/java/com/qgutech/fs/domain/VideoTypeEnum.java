package com.qgutech.fs.domain;


import org.springframework.util.Assert;

public enum VideoTypeEnum {
    H(1280, 720, "1024k"),//超清
    M(640, 480, "512k"),//高清
    L(480, 360, "256k"),//流畅
    O(0, 0, null); //原画质

    /**
     * 分辨率-宽度
     */
    private int width;

    /**
     * 分辨率-高度
     */
    private int height;

    /**
     * 码率（如512k表示512k bps）
     */
    private String bitRate;

    private VideoTypeEnum(int width, int height, String bitRate) {
        this.width = width;
        this.height = height;
        this.bitRate = bitRate;
    }

    public String getBitRate() {
        return bitRate;
    }

    public String getResolution() {
        return width + "*" + height;
    }

    public static VideoTypeEnum[] getVideoTypeEnums(VideoTypeEnum videoTypeEnum) {
        Assert.notNull(videoTypeEnum, "VideoTypeEnum is null!");
        switch (videoTypeEnum) {
            case H:
                return new VideoTypeEnum[]{H, M, L, O};
            case M:
                return new VideoTypeEnum[]{M, L, O};
            case L:
                return new VideoTypeEnum[]{L, O};
            case O:
                return new VideoTypeEnum[]{O};
            default:
                throw new RuntimeException("VideoTypeEnum[" + videoTypeEnum + "] is invalid!");
        }
    }

}
