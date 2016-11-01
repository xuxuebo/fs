package com.qgutech.fs.domain;

public enum ImageTypeEnum {
    /**
     * 高分辨率图
     */
    H(0, 0, 1280, 720),
    /**
     * 中分辨率图
     */
    M(0, 0, 1280, 720),
    /**
     * 低分辨率图
     */
    L(0, 0, 1280, 720),
    /**
     * 原图
     */
    O(0, 0, 0, 0);

    /**
     * 图片的x轴坐标
     */
    private int x;
    /**
     * 图片的y轴坐标
     */
    private int y;
    /**
     * 图片的宽度
     */
    private int w;
    /**
     * 图片的高度
     */
    private int h;

    private ImageTypeEnum(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public String getImageName() {
        return x + "*" + y + "*" + w + "*" + h;
    }
}
