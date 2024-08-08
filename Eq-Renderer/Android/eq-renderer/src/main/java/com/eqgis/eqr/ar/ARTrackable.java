package com.eqgis.eqr.ar;


/**
 * AR可跟踪对象
 * @author tanyx
 */
public class ARTrackable {
    protected  com.google.ar.core.Trackable coretrackable ;
    protected  com.huawei.hiar.ARTrackable hwtrackable ;

    ARTrackable( com.google.ar.core.Trackable coreobj,com.huawei.hiar.ARTrackable hwobj ){
        if (coreobj==null && hwobj==null){
            throw new IllegalArgumentException();
        }
        coretrackable = coreobj;
        hwtrackable = hwobj;
    }

    /**
     * 获取当前可跟踪对象的跟踪状态。
     * @return 跟踪状态
     */
    public TrackingState getTrackingState(){
        if (coretrackable!=null){
            return TrackingState.fromARCore( coretrackable.getTrackingState() );
        }else{
            return  TrackingState.fromHuawei( hwtrackable.getTrackingState() );
        }
    }

    /**
     * 根据ARPose创建锚点
     * @param pose AR位姿对象
     * @return 锚点对象
     */
    public ARAnchor createAnchor(ARPose pose){
        if (coretrackable!=null){
             com.google.ar.core.Anchor anchor = coretrackable.createAnchor(pose.corepose);
             if (anchor!=null){
                 return new ARAnchor(anchor,null);
             }else{
                 return null;
             }
        }else{
            com.huawei.hiar.ARAnchor anchor = hwtrackable.createAnchor(pose.hwpose);
            if (anchor!=null) {
                return new ARAnchor(null, anchor);
            }else{
                return null;
            }
        }
    }

//    Collection<ARAnchor> getAnchors(){
//        if (coretrackable!=null){
//            Collection<com.google.ar.core.Anchor> coreAnchors = coretrackable.getAnchors();
//            for (int i=0;i<coreAnchors.size();i++){
//
//            }
//
//            return new ARAnchor(coretrackable.createAnchor(var1),null);
//        }else{
//            return  new ARAnchor( null,hwtrackable.createAnchor(var1));
//        }
//
//    }


}
