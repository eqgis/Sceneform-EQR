package com.eqgis.eqr.ar;

/**
 * AR相机内参对象
 * @author tanyx
 */
public class ARCameraIntrinsics {
    com.google.ar.core.CameraIntrinsics coreIntrinsics = null;
    com.huawei.hiar.ARCameraIntrinsics hwIntrinsics = null;

    ARCameraIntrinsics(com.google.ar.core.CameraIntrinsics coreobj ,com.huawei.hiar.ARCameraIntrinsics hwobj){
        if (coreobj==null && hwobj==null){
            throw new IllegalArgumentException();
        }
        coreIntrinsics = coreobj;
        hwIntrinsics = hwobj;
    }

//    public float[] getDistortions() {
//        if (coreIntrinsics!=null){
//            return coreIntrinsics.getImageDimensions();
//        }else{
//            return hwIntrinsics.getDistortions();
//        }
//    }

    /**
     * 获取相机预览流图像的尺寸，包括宽度和高度。
     * @return 相机图像的尺寸。返回数组大小为2的图像尺寸，int[0]对应width，int[1]对应height。
     */
    public int[] getImageDimensions() {
        if (coreIntrinsics!=null){
            return coreIntrinsics.getImageDimensions();
        }else{
            return hwIntrinsics.getImageDimensions();
        }
    }

    /**
     * 获取相机的主轴点，主轴点位置以像素为单位表示。
     * @return 相机的主轴点。返回的数组大小为2，float[0]代表主轴点的x坐标值，float[1]代表主轴点的y坐标值。
     */
    public float[] getPrincipalPoint() {
        if (coreIntrinsics!=null){
            return coreIntrinsics.getPrincipalPoint();
        }else{
            return hwIntrinsics.getPrincipalPoint();
        }
    }

    /**
     * 获取相机的焦距（定焦焦距）。
     * @return 相机的焦距（定焦焦距）。返回的数组大小为2，float[0]代表相机内参矩阵x(u)方向的像素焦距，float[1]代表相机内参矩阵y(u)方向的像素焦距。
     */
    public float[] getFocalLength() {
        if (coreIntrinsics!=null){
            return coreIntrinsics.getFocalLength();
        }else{
            return hwIntrinsics.getFocalLength();
        }
    }

}
