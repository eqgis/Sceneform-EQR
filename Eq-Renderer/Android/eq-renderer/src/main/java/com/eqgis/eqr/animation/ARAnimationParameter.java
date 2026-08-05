package com.eqgis.eqr.animation;

import com.eqgis.eqr.Location;
import com.google.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * AR 动画参数
 * <pre>
 *     统一描述动画周期、重复方式、旋转轴、位移起终点以及曲线路径途经点。
 * </pre>
 */
public class ARAnimationParameter {
    private long duration = 6000L;//周期
    private int RepeatCount=-1;//循环次数，值为-1时一直循环
    private ARAnimationRepeatMode RepeatMode=ARAnimationRepeatMode.INFINITE;//循环方式
    private long StartDelay=0;//延迟播放时间
    private Vector3 rotationAxis = new Vector3(0,1,0);//旋转轴参数，初始为y轴
    private boolean isClockwise = false;//旋转方向是否为顺时针
    private Vector3 startVerctor=new Vector3(0,0,0);
    private Vector3 endVerctor=new Vector3(0,0,0);
    private boolean isLocationFlag = false;
    private Location startLocation;
    private Location endLocation;
    private List<Vector3> wayPoints=new ArrayList<Vector3>();

    /**
     * 设置周期
     * @param duration 周期，默认值：6000ms
     * */
    public void setDuration(Long duration){
        this.duration=duration;
    }

    /**
     * 获取AR动画的周期参数
     * */
    public long getDuration(){
        return duration;
    }

    /**
     * 设置动画播放的循环次数
     * @param count 次数
     *  -1代表一直循环
     */
    public void setRepeatCount(int count){
        RepeatCount=count;
    }

    /**
     * 获取AR动画参数的循环次数
     * */
    public int getRepeatCount(){
        return RepeatCount;
    }

    /**
     * 对于默认动画无效
     * @param Mode 1表示动画将从头开始重新启动；2表示每次迭代时都会反转方向；-1表示动画无限期
     */
    public void setRepeatMode(ARAnimationRepeatMode Mode){
        RepeatMode=Mode;
    }
    public ARAnimationRepeatMode getRepeatMode(){

        return RepeatMode;
    }

    /**
     * 启动延迟
     * @param delay
     */
    public void setStartDelay(long delay){
        StartDelay=delay;
    }
    public long getStartDelay(){
        return StartDelay;
    }


    /**
     * 内部使用，不用再转换，这是符合arcore的坐标系
     * @return
     */
    protected Vector3 getRotation(){
        return rotationAxis;
    }

    /**
     * 获取旋转轴
     * @return Sceneform 坐标系下的旋转轴
     */
    public Vector3 getRotationAxis() {
        return new Vector3(rotationAxis.x, rotationAxis.y, rotationAxis.z);
    }

    /**
     * 设置旋转轴
     * @param rotationAxis Sceneform 坐标系下的旋转轴
     */
    public void setRotationAxis(Vector3 rotationAxis) {
        if (rotationAxis == null) {
            throw new IllegalArgumentException("rotationAxis must not be null");
        }
        if (rotationAxis.lengthSquared() < 0.000001f) {
            throw new IllegalArgumentException("rotationAxis must not be zero");
        }
        this.rotationAxis = rotationAxis.normalized();
    }

    /**
     * 获取旋转动画旋转方向
     * 仅旋转动画使用
     * */
    protected boolean getClockwise() {
        return isClockwise;
    }

    /**
     * 设置旋转动画旋转方向（是否采用顺时针方向旋转，默认false）
     * 仅旋转动画使用
     * */
    public void setClockwise(boolean clockwise) {
        isClockwise = clockwise;
    }


    /**
     * 内部直接使用，符合ar坐标系
     * @return
     */
    Vector3 getStartVertor(){
        return startVerctor;
    }

    /**
     * 获取位移动画起点
     * @return Sceneform 坐标系下的起点
     */
    public Vector3 getStartVector() {
        return new Vector3(startVerctor.x, startVerctor.y, startVerctor.z);
    }

    /**
     * 设置位移动画起点
     * @param startVector Sceneform 坐标系下的起点
     */
    public void setStartVector(Vector3 startVector) {
        if (startVector == null) {
            throw new IllegalArgumentException("startVector must not be null");
        }
        startVerctor = new Vector3(startVector.x, startVector.y, startVector.z);
    }

    /**
     * 获取位移动画终点
     * @return Sceneform 坐标系下的终点
     */
    public Vector3 getEndVector() {
        return new Vector3(endVerctor.x, endVerctor.y, endVerctor.z);
    }

    /**
     * 设置位移动画终点
     * @param endVector Sceneform 坐标系下的终点
     */
    public void setEndVector(Vector3 endVector) {
        if (endVector == null) {
            throw new IllegalArgumentException("endVector must not be null");
        }
        endVerctor = new Vector3(endVector.x, endVector.y, endVector.z);
    }

    boolean isLocationFlag() {
        return isLocationFlag;
    }


    /**
     * 获取起点的地理位置
     * @return
     */
    public Location getStartLocation() {
        if (!isLocationFlag){
            return null;
        }
        return startLocation;
    }

    /**
     * 设置起点的地理位置
     * @param startLocation
     */
    public void setStartLocation(Location startLocation) {
        isLocationFlag = true;
        this.startLocation = startLocation;
    }

    /**
     * 获取终点的地理位置
     * @return
     */
    public Location getEndLocation() {
        if (!isLocationFlag){
            return null;
        }
        return endLocation;
    }

    /**
     * 设置位移动画终点的地理位置
     * @param endLocation
     */
    public void setEndLocation(Location endLocation) {
        isLocationFlag = true;
        this.endLocation = endLocation;
    }

    /**
     * 途经点
     * @param wayPoint 途经点
     */
    public void addWayPoint(Vector3 wayPoint){
        if (wayPoint == null) {
            throw new IllegalArgumentException("wayPoint must not be null");
        }
        wayPoints.add(new Vector3(wayPoint.x, wayPoint.y, wayPoint.z));
    }

    /**
     * 获取曲线路径途经控制点副本
     * @return Sceneform 坐标系下的控制点列表
     */
    public List<Vector3> getWayPoints(){
        List<Vector3> result = new ArrayList<>();
        for (Vector3 wayPoint : wayPoints) {
            result.add(new Vector3(wayPoint.x, wayPoint.y, wayPoint.z));
        }
        return result;
    }

    /**
     * 清空曲线路径途经点
     */
    public void clearWayPoints() {
        wayPoints.clear();
    }
}
