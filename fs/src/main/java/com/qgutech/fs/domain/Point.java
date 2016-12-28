package com.qgutech.fs.domain;

/**
 * 坐标点
 */
public class Point {
    /**
     * 坐标-横坐标
     */
    private int x;
    /**
     * 坐标-纵坐标
     */
    private int y;

    public Point() {

    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
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
}
