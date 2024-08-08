package com.eqgis.eqr.ar;


import com.google.ar.core.AugmentedImage;

/**
 * AR增强图像
 * @author tanyx
 */
public class ARAugmentedImage extends ARTrackable{

    ARAugmentedImage(com.google.ar.core.AugmentedImage coreobj,com.huawei.hiar.ARAugmentedImage hwobj){
        super(coreobj,hwobj);
    }

//    public Collection<ARAnchor> getAnchors() {
//        return super.getAnchors();
//    }

    /**
     * 获取以图像中心为坐标原点，在X轴上评估的识别出来物理图片宽度（以米为单位）。
     * @return 宽度
     */
    public float getExtentX() {
        if (coretrackable!=null){
            com.google.ar.core.AugmentedImage coreImage = (com.google.ar.core.AugmentedImage)coretrackable;
            return coreImage.getExtentX();
        }else{
            com.huawei.hiar.ARAugmentedImage hwImage = (com.huawei.hiar.ARAugmentedImage)hwtrackable;
            return hwImage.getExtentX();
        }
    }

    /**
     * 获取以图像中心为坐标原点，在Z轴上评估的识别出来物理图片高度（以米为单位）。
     * @return 高度
     */
    public float getExtentZ() {
        if (coretrackable!=null){
            com.google.ar.core.AugmentedImage coreImage = (com.google.ar.core.AugmentedImage)coretrackable;
            return coreImage.getExtentZ();
        }else{
            com.huawei.hiar.ARAugmentedImage hwImage = (com.huawei.hiar.ARAugmentedImage)hwtrackable;
            return hwImage.getExtentZ();
        }
    }

    /**
     * 获取识别的图片在配置到ARAugmentedImageDatabase的Index（从0开始，调用ARAugmentedImageDatabase.addImage()配置图片时顺序增加）。
     * @return
     */
    public int getIndex() {
        if (coretrackable!=null){
            com.google.ar.core.AugmentedImage coreImage = (com.google.ar.core.AugmentedImage)coretrackable;
            return coreImage.getIndex();
        }else{
            com.huawei.hiar.ARAugmentedImage hwImage = (com.huawei.hiar.ARAugmentedImage)hwtrackable;
            return hwImage.getIndex();
        }
    }

    public ARPose getCenterPose() {
        if (coretrackable!=null){
            com.google.ar.core.AugmentedImage coreImage = (com.google.ar.core.AugmentedImage)coretrackable;
            com.google.ar.core.Pose p = coreImage.getCenterPose();
            if (p==null)return null;
            return new ARPose(p,null);
        }else{
            com.huawei.hiar.ARAugmentedImage hwImage = (com.huawei.hiar.ARAugmentedImage)hwtrackable;
            com.huawei.hiar.ARPose p = hwImage.getCenterPose();
            if (p==null)return null;
            return new ARPose(null,p);
//            return ARPose.updatePoseOnAREngine(new ARPose(null,p));
        }
    }


    public String getName() {
        if (coretrackable!=null){
            com.google.ar.core.AugmentedImage coreImage = (com.google.ar.core.AugmentedImage)coretrackable;
            return coreImage.getName();
        }else{
            com.huawei.hiar.ARAugmentedImage hwImage = (com.huawei.hiar.ARAugmentedImage)hwtrackable;
            return hwImage.getName();
        }
    }

    public  TrackingState getTrackingState() {
        if (coretrackable!=null){
            com.google.ar.core.AugmentedImage coreImage = (com.google.ar.core.AugmentedImage)coretrackable;
            return TrackingState.fromARCore( coreImage.getTrackingState() );
        }else{
            com.huawei.hiar.ARAugmentedImage hwImage = (com.huawei.hiar.ARAugmentedImage)hwtrackable;
            return TrackingState.fromHuawei( hwImage.getTrackingState() );
        }
    }

    public Boolean isImageInCamera(){
        if (coretrackable!=null){
            com.google.ar.core.AugmentedImage coreImage = (com.google.ar.core.AugmentedImage)coretrackable;
            return coreImage.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING;
        }else{
            com.huawei.hiar.ARAugmentedImage hwImage = (com.huawei.hiar.ARAugmentedImage)hwtrackable;
            return hwImage.getTrackingState() == com.huawei.hiar.ARTrackable.TrackingState.TRACKING;
        }
    }
}
