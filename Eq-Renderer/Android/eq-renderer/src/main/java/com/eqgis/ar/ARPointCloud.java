package com.eqgis.ar;

import java.nio.FloatBuffer;

/**
 * AR点云对象
 */
public class ARPointCloud {
    com.google.ar.core.PointCloud corecloud = null;
    com.huawei.hiar.ARPointCloud hwcloud = null;

    ARPointCloud(com.google.ar.core.PointCloud coreobj,com.huawei.hiar.ARPointCloud hwobj){
        if (coreobj==null && hwobj==null){
            throw new IllegalArgumentException();
        }
        corecloud = coreobj;
        hwcloud = hwobj;
    }

    /**
     * 释放点云，使用完点云以后调用。
     */
    public void release() {
        if (corecloud!=null){
            corecloud.release();
        }
        if (hwcloud!=null){
            hwcloud.release();
        }
    }

    /**
     * 获取点云中所有点的坐标x，y，z，以及置信度。
     * <p>数据格式为[x0,y0,z0,c0,x1,y1,z1,c1]。其坐标值都在世界坐标系下，使用右手坐标系表示。</p>
     * @return 点云数据
     */
    public FloatBuffer getPoints() {
        if (corecloud!=null){
            return corecloud.getPoints();
        }else{
            return hwcloud.getPoints();
        }
    }

    /**
     * 获取当前帧的时间戳，以纳秒为单位，从开机时间开始计算。
     * @return 时间戳
     */
    public long getTimestampNs() {
        if (corecloud!=null){
            return corecloud.getTimestamp();
        }else{
            return hwcloud.getTimestampNs();
        }
    }



}
