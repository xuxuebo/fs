package com.qgutech.fs.domain;


import org.springframework.util.Assert;

public enum VideoTypeEnum {
    H(1280, 720, 1024),//超清
    M(640, 480, 512),//高清
    L(480, 360, 256),//流畅
    O(0, 0, 0); //原画质

    /**
     * 分辨率-宽度
     */
    private int width;

    /**
     * 分辨率-高度
     */
    private int height;

    /**
     * 码率（单位为kb/s）
     */
    private int bitRate;

    private VideoTypeEnum(int width, int height, int bitRate) {
        this.width = width;
        this.height = height;
        this.bitRate = bitRate;
    }

    public int getBitRate() {
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

    public static VideoTypeEnum getVideoTypeEnum(int bitRate) {
        if (bitRate >= H.getBitRate()) {
            return H;
        } else if (bitRate >= M.getBitRate()) {
            return M;
        } else if (bitRate >= L.getBitRate()) {
            return L;
        } else {
            return O;
        }
    }

}
