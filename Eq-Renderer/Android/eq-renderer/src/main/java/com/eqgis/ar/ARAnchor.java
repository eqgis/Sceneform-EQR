package com.eqgis.ar;

/**
 * AR锚点
 * <pre>
 *     锚点是实际环境中一个固定的位置和指定的方向。当环境发生变化时，比如摄像头移动，为了维持这个物理空间的固定位置和方向，AR Engine会根据自己对于环境的检测结果，不断更新它的数值，以做到在该锚点上放置的物体固定不动的效果。在使用前，应使用getTrackingState()接口检查锚点的状态，锚点的状态为ARTrackable.TrackingState.TRACKING时，通过getPose()接口获取的数据才是可用的。
 * </pre>
 * @author tanyx
 */
public class ARAnchor {
    com.google.ar.core.Anchor coreAnchor = null;
    com.huawei.hiar.ARAnchor hwAnchor = null;

    /**
     * 构造函数
     * @param coreobj ARCore的锚点
     * @param hwobj 华为AREngine的锚点
     */
    public ARAnchor( com.google.ar.core.Anchor coreobj,com.huawei.hiar.ARAnchor hwobj ){
        if (coreobj==null && hwobj==null){
            throw new IllegalArgumentException();
        }
        coreAnchor = coreobj;
        hwAnchor = hwobj;
    }

    /**
     * 获取位姿
     * <p>获取锚点在世界坐标系下的位置和姿态信息</p>
     * @return
     */
    public ARPose getPose() {
        if (coreAnchor!=null){
            com.google.ar.core.Pose p = coreAnchor.getPose();
            if (p==null)return null;
            return new ARPose( p , null);
        }else{
            com.huawei.hiar.ARPose p = hwAnchor.getPose();
            if (p==null)return null;
            return ARPose.updatePoseOnAREngine(new ARPose( null, p));
//            return new ARPose( null, p);
        }
    }

    /**
     * 通知AR 引擎停止跟踪当前锚点
     */
    public void detach() {
        if (coreAnchor!=null){
            coreAnchor.detach();
        }
        if (hwAnchor!=null){
            hwAnchor.detach();
        }
    }

    /**
     * 获取锚点的跟踪状态
     * @return {@link TrackingState}跟踪状态
     */
    public TrackingState getTrackingState() {
        if (coreAnchor!=null){
            return TrackingState.fromARCore( coreAnchor.getTrackingState() );
        }else {
            return  TrackingState.fromHuawei( hwAnchor.getTrackingState() );
        }
    }
}
