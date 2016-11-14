package com.qgutech.fs.utils;


public class Video {
    /**
     * 时长，如00:10:10
     */
    private String duration;

    /**
     * 分辨率（宽度X高度）
     */
    private String resolution;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    @Override
    public String toString() {
        return "Video{" +
                "duration='" + duration + '\'' +
                ", resolution='" + resolution + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", bitRate='" + bitRate + '\'' +
                '}';
    }
}
