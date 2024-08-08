package com.eqgis.eqr.ar;

import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Trackable;

/**
 * 碰撞检测结果类
 * @author tanyx
 */
public class ARHitResult {

    com.google.ar.core.HitResult coreResult = null;
    com.huawei.hiar.ARHitResult hwResult = null;

    ARHitResult( com.google.ar.core.HitResult coreobj,com.huawei.hiar.ARHitResult hwobj){
        if (coreobj==null && hwobj==null){
            throw new IllegalArgumentException();
        }
        coreResult = coreobj;
        hwResult = hwobj;
    }

    /**
     * 返回相机与碰撞点的距离，单位为米。
     * @return 距离
     */
    public float getDistance() {
        if (coreResult!=null){
            return coreResult.getDistance();
        }else {
            return hwResult.getDistance();
        }
    }

    /**
     * 获取交点位姿，其平移向量是交点在世界坐标系的坐标，其旋转分量根据碰撞点的不同类型（与平面的交点、与点云的交点）而有不同的定义。
     * @return AR位姿对象
     */
    public ARPose getHitPose() {
        if (coreResult!=null){
            com.google.ar.core.Pose pose = coreResult.getHitPose();
            if (pose!=null){
                return new ARPose(pose,null);
            }else{
                return null;
            }

        }else{
            com.huawei.hiar.ARPose pose = hwResult.getHitPose();
            if (pose!=null){
                return ARPose.updatePoseOnAREngine(new ARPose(null,pose));
            }else{
                return null;
            }
        }
    }

    /**
     * 在碰撞命中位置创建一个新的锚点。
     * @return 锚点对象
     */
    public  ARAnchor createAnchor() {
        if (coreResult!=null){
            com.google.ar.core.Anchor anchor = coreResult.createAnchor();
            if (anchor!=null){
                return new ARAnchor(anchor,null);
            }else{
                return null;
            }
        }else{
            com.huawei.hiar.ARAnchor anchor = hwResult.createAnchor();
            if (anchor!=null){
                return new ARAnchor( null,anchor);
            }else{
                return null;
            }
        }
    }

    /**
     * 返回被命中的可跟踪对象。
     * @return AR跟踪对象
     */
    public ARTrackable getTrackable() {
        if (coreResult!=null){
            Trackable trackable = coreResult.getTrackable();
            if (trackable==null)return null;
            if (trackable instanceof Plane){
                return new ARPlane((Plane)trackable,null);
            }else if(trackable instanceof Point){
                return new ARPoint((Point)trackable,null);
            }else{
                return new ARTrackable(trackable,null);
            }
        }else{
            com.huawei.hiar.ARTrackable trackable = hwResult.getTrackable();
            if (trackable==null)return null;
            if (trackable instanceof com.huawei.hiar.ARPlane){
                return new ARPlane(null,(com.huawei.hiar.ARPlane)trackable);
            }else if(trackable instanceof com.huawei.hiar.ARPoint){
                return new ARPoint(null,(com.huawei.hiar.ARPoint)trackable);
            }else {
                return new ARTrackable(null, trackable);
            }
        }
    }


}
