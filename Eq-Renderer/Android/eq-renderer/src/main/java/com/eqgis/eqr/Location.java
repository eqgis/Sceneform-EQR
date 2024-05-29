package com.eqgis.eqr;

/**
 * 地理位置
 */
public class Location {
    private double x;
    private double y;
    private double z;

    /**
     * 构造函数
     */
    public Location() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    /**
     * 构造函数
     * @param source 位置
     */
    public Location(Location source){
        this.x = source.x;
        this.y = source.y;
        this.z = source.z;
    }

    /**
     * 构造函数
     * @param longitude
     * @param latitude
     * @param height
     */
    public Location(double longitude, double latitude, double height) {
        this.x = longitude;
        this.y = latitude;
        this.z = height;
    }

    /**
     * 获取经度
     * @return
     */
    public double getX() {
        return x;
    }

    /**
     * 设置经度
     * @param x
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * 获取纬度
     * @return
     */
    public double getY() {
        return y;
    }

    /**
     * 设置纬度
     * @param y
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * 获取高度
     * @return
     */
    public double getZ() {
        return z;
    }

    /**
     * 设置高度
     * @param z
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * 设置位置
     * @param source
     */
    public void setLocation(Location source){
        this.x = source.x;
        this.y = source.y;
        this.z = source.z;
    }

    @Override
    public String toString() {
        return "[" + x +"," + y +"," + z +']';
    }
}
