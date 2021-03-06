package com.qgutech.fs.domain;

import com.qgutech.fs.utils.FsConstants;

public enum ImageTypeEnum {
    /**
     * 高分辨率图
     */
    H(0, 0, 1366, 1366),
    /**
     * 中分辨率图
     */
    M(0, 0, 800, 800),
    /**
     * 低分辨率图
     */
    L(0, 0, 300, 300),
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
        return x + FsConstants.UNDERLINE + y + FsConstants.UNDERLINE + w + FsConstants.UNDERLINE + h;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }
}
