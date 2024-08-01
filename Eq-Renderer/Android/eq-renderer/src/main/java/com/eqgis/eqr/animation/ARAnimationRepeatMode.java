package com.eqgis.eqr.animation;

public class ARAnimationRepeatMode {
    private int value;
    /**
     * 构造函数
     * 派生类必须调用该函数以初始化枚举字段
     *
     * @param value    int
     */
    protected ARAnimationRepeatMode(int value) {
        this.value = value;
    }

    /**
     * 表示动画无限期
     */
    public static final ARAnimationRepeatMode INFINITE=new ARAnimationRepeatMode(-1);
    /**
     * 表示动画将从头开始重新启动
     */
    public static final ARAnimationRepeatMode RESTART=new ARAnimationRepeatMode(1);
    /**
     * 表示每次迭代时都会反转方向
     */
    public static final ARAnimationRepeatMode REVERSE=new ARAnimationRepeatMode(2);

    public int getValue(){
        return value;
    }
}
