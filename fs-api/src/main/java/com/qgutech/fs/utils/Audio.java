package com.qgutech.fs.utils;


public class Audio {
    /**
     * 时长，如00:10:10
     */
    private String duration;

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

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    @Override
    public String toString() {
        return "Audio{" +
                "duration='" + duration + '\'' +
                ", bitRate=" + bitRate +
                '}';
    }
}
