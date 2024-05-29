package com.eqgis.ar;

import android.media.Image;
import android.view.MotionEvent;

import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.huawei.hiar.ARTrackable;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * AR帧对象
 * <p>仅可通过{@link ARSession#update()}创建</p>
 * @author tanyx
 */
public class ARFrame {
    Frame coreFrame = null;
    com.huawei.hiar.ARFrame hwFrame = null;

    ARFrame(Frame coreobj, com.huawei.hiar.ARFrame hwobj){
        if (coreobj==null && hwobj==null){
            throw new IllegalArgumentException();
        }
        coreFrame = coreobj;
        hwFrame = hwobj;
    }

    /**
     * 获取ARCore的帧对象
     * @return 帧对象
     */
    public Frame getCoreFrame() {
        return coreFrame;
    }

    /**
     * 获取华为AREngine的帧对象
     * @return 帧对象
     */
    public com.huawei.hiar.ARFrame getHwFrame() {
        return hwFrame;
    }

    //    public com.google.ar.core.Frame getFrame(){
//        return coreFrame;
//    }

    /**
     * 获取AR相机
     * @return
     */
    public ARCamera getCamera() {
        if (coreFrame!=null){
            com.google.ar.core.Camera c = coreFrame.getCamera();
            if (c==null)return null;
            return new ARCamera( c,null);
        }else{
            com.huawei.hiar.ARCamera c = hwFrame.getCamera();
            if (c==null)return null;
            return new ARCamera(null,c);
        }


    }

    /**
     * 获取AR场景的光照估计
     * @return 光照估计。
     */
    public ARLightEstimate getLightEstimate() {
        if (coreFrame!=null){
            com.google.ar.core.LightEstimate c = coreFrame.getLightEstimate();
            if (c==null)return null;
            return new ARLightEstimate(c,null);
        }else{
            com.huawei.hiar.ARLightEstimate c = hwFrame.getLightEstimate();
            if (c==null)return null;
            return new ARLightEstimate(null,c);
        }
    }

    /**
     * 获取图像的Metadata信息。
     * @return 相机的Metadata
     * @throws NotYetAvailableException
     */
    public ARImageMetadata getImageMetadata() throws NotYetAvailableException {
        if (coreFrame!=null){
            com.google.ar.core.ImageMetadata c = coreFrame.getImageMetadata();
            if (c==null)return null;
            return new ARImageMetadata(c,null);
        }else{
            com.huawei.hiar.ARImageMetadata c = hwFrame.getImageMetadata();
            if (c==null)return null;
            return new ARImageMetadata(null,c);
        }
    }

    /**
     * 返回当前帧的点云。
     * @return 点云对象
     */
    public  ARPointCloud acquirePointCloud() {
        if (coreFrame!=null){
            com.google.ar.core.PointCloud c = coreFrame.acquirePointCloud();
            if (c==null)return null;
            return new ARPointCloud(c,null);
        }else{
            com.huawei.hiar.ARPointCloud c = hwFrame.acquirePointCloud();
            if (c==null)return null;
            return new ARPointCloud(null,c);
        }
    }

    /**
     * 获取当前帧的点云对象
     * @return 点云对象
     */
    public ARPointCloud getPointCloud() {
        return this.acquirePointCloud();
    }

    /**
     * 获取当前点云的位姿
     * @return
     */
    public  ARPose getPointCloudPose() {
        if (coreFrame!=null){
            return  new ARPose(Pose.IDENTITY,null);
        }else{
            return  new ARPose(null,com.huawei.hiar.ARPose.IDENTITY);
        }
    }

//    public ARPose getPose() {
//        if (coreFrame!=null){
//            return new ARPointCloud(coreFrame.getAndroidSensorPose(),null);
//        }else{
//            return new ARPointCloud(null,hwFrame.getPose());
//        }
//    }

    /**
     * 获取当前帧的时间戳
     * @return 当前帧的时间戳，以纳秒为单位，从开机时间开始计算。
     */
    public long getTimestampNs() {
        if (coreFrame!=null){
            return  coreFrame.getTimestamp();
        }else{
            return hwFrame.getTimestampNs();
        }
    }

    /**
     * 获取在两次ARSession.update()之间更新过的锚点。
     * @return 被ARSession.update()更新过的锚点。
     */
    public Collection<ARAnchor> getUpdatedAnchors() {
        if (coreFrame!=null){
            Collection<com.google.ar.core.Anchor> anchors = coreFrame.getUpdatedAnchors();
            ArrayList<ARAnchor> results = new ArrayList<>();
            Iterator<com.google.ar.core.Anchor> iter = anchors.iterator();
            while (iter.hasNext()){
                com.google.ar.core.Anchor anchor = iter.next();
                results.add(new ARAnchor(anchor,null));
            }

//            for (int i=0;i<anchors.size();i++){
//                results.add(new ARAnchor(anchors[i],null) );
//            }
            return results;
        }else{
            Collection<com.huawei.hiar.ARAnchor> anchors = hwFrame.getUpdatedAnchors();
            ArrayList<ARAnchor> results = new ArrayList<>();
            Iterator<com.huawei.hiar.ARAnchor> iter = anchors.iterator();
            while (iter.hasNext()){
                com.huawei.hiar.ARAnchor anchor = iter.next();
                results.add(new ARAnchor(null,anchor));
            }
//            for (int i=0;i<anchors.size();i++){
//                results.add(new ARAnchor(null,anchors[i]) );
//            }
            return results;
        }
    }

    //使用这个方法，后续无法获取平面的父平面（getSubsumedBy()）
    @Deprecated
    ArrayList<ARPlane> results =null;
    public Collection<ARPlane> getUpdatedPlanes() {
        Collection<ARPlane> result = new ArrayList<>();
        if (coreFrame!=null){
            for (Plane plane : coreFrame.getUpdatedTrackables(Plane.class)) {
                result.add(new ARPlane(plane,null));
            }
        }else{
            for (com.huawei.hiar.ARPlane plane : hwFrame.getUpdatedPlanes()) {
                result.add(new ARPlane(null,plane));
            }
        }
        return result;
    }

    /**
     * 获取AR平面的位姿
     * @return
     */
    public ARPose getARPlanePose(){
        ARPose result = null;
        if (coreFrame!=null){
            for (Plane plane : coreFrame.getUpdatedTrackables(Plane.class)) {
                if (plane.getTrackingState() == TrackingState.TRACKING) {
                    result = new ARPose(plane.getCenterPose(),null);
                    break;
                }
            }
        }else{
            for (com.huawei.hiar.ARPlane plane : hwFrame.getUpdatedPlanes()) {
                if (plane.getTrackingState() == ARTrackable.TrackingState.TRACKING) {
                    result = ARPose.updatePoseOnAREngine(new ARPose(null,plane.getCenterPose()));
//                    result = new ARPose(null,plane.getCenterPose());
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 获取本次session.update()更新过的所有AR增强图像
     * @return
     */
    public Collection<ARAugmentedImage> getUpdatedAugmentedImage() {
        if (coreFrame!=null){
            Collection<com.google.ar.core.AugmentedImage> trackables = coreFrame.getUpdatedTrackables(com.google.ar.core.AugmentedImage.class);
            ArrayList<ARAugmentedImage> results = new ArrayList<>();
//            Iterator<com.google.ar.core.AugmentedImage> iter = trackables.iterator();
//            while (iter.hasNext()){
//                com.google.ar.core.AugmentedImage image = iter.next();
//                results.add(new ARAugmentedImage(image,null));
//            }
            for (com.google.ar.core.AugmentedImage image : trackables){
                results.add(new ARAugmentedImage(image,null));
            }

//            for (int i=0;i<anchors.size();i++){
//                results.add(new ARAnchor(anchors[i],null) );
//            }
            return results;
        }else{
            Collection<com.huawei.hiar.ARAugmentedImage> trackables = hwFrame.getUpdatedTrackables(com.huawei.hiar.ARAugmentedImage.class);
            ArrayList<ARAugmentedImage> results = new ArrayList<>();
//            Iterator<com.huawei.hiar.ARAugmentedImage> iter = trackables.iterator();
//            while (iter.hasNext()){
//                com.huawei.hiar.ARAugmentedImage image = iter.next();
//                results.add(new ARAugmentedImage(null,image));
//            }
            for (com.huawei.hiar.ARAugmentedImage image : trackables){
                results.add(new ARAugmentedImage(null,image));
            }


//            for (int i=0;i<anchors.size();i++){
//                results.add(new ARAnchor(null,anchors[i]) );
//            }
            return results;
        }
    }

//    public <T extends ARTrackable> Collection<T> getUpdatedTrackables(Class<T> filterType) {
//        coreFrame.getUpdatedTrackables();
//    }

//    public Collection<ARPlane> getUpdatedPlanes() {
//        coreFrame.get
//    }

//    public void getViewMatrix(float[] viewMatrix, int offset) {
//        if (viewMatrix != null && offset >= 0 && viewMatrix.length >= offset + 16) {
//            this.mCamera.getViewMatrix(viewMatrix, offset);
//        } else {
//            Log.e(TAG, "getViewMatrix: illegal argument");
//            throw new IllegalArgumentException("length of mViewMatrix is illegal.");
//        }
//    }

    /**
     * 从摄像头发射一条射线，该射线的方向由屏幕上的点(axisX, axisY)确定。射线与系统跟踪的平面或者是点云中的点碰撞（点云正常识别），从而产生交点，形成碰撞结果。按照交点与设备的距离从近到远进行排序，存放在链表中。(axisX, axisY)是像素在屏幕上坐标。
     * @param motionEvent
     * @return
     */
    public List<ARHitResult> hitTest(MotionEvent motionEvent) {
        return hitTest(motionEvent.getX(), motionEvent.getY());
    }

    /**
     * 从摄像头发射一条射线，该射线的方向由屏幕上的点(axisX, axisY)确定。射线与系统跟踪的平面或者是点云中的点碰撞（点云正常识别），从而产生交点，形成碰撞结果。按照交点与设备的距离从近到远进行排序，存放在链表中。(axisX, axisY)是像素在屏幕上坐标。
     * @param axisX x轴坐标
     * @param axisY y轴坐标
     * @return
     */
    public List<ARHitResult> hitTest(float axisX, float axisY) {
        if (coreFrame!=null){
            List<com.google.ar.core.HitResult> points = coreFrame.hitTest(axisX,axisY);
            List<ARHitResult> results = new ArrayList<>();
            for (int i=0;i<points.size();i++){
                results.add( new ARHitResult(points.get(i) , null) );
            }
            return results;
        }else{
            List<com.huawei.hiar.ARHitResult> points = hwFrame.hitTest(axisX,axisY);
            List<ARHitResult> results = new ArrayList<>();
            for (int i=0;i<points.size();i++){
                results.add( new ARHitResult( null,points.get(i) ) );
            }
            return results;
        }


    }

//    public List<ARHitResult> hitTestArea(float[] input2dPoints) {
//        if (input2dPoints != null && input2dPoints.length != 0) {
//            long var2 = this.mSession.mNativeHandle;
//            long var4 = this.mNativeHandle;
//            long[] input2dPoints1;
//            if ((input2dPoints1 = this.nativeHitTestArea(var2, var4, input2dPoints)) != null && input2dPoints1.length != 0) {
//                return this.getArHitResultsByIds(input2dPoints1);
//            } else {
//                Log.w(TAG, "hitTest: hitResult null!");
//                return null;
//            }
//        } else {
//            Log.e(TAG, "hitTest: illegal argument");
//            throw new IllegalArgumentException();
//        }
//    }

    @Deprecated
    public boolean isDisplayRotationChanged() {
        return this.hasDisplayGeometryChanged();
    }

    /**
     * 显示（长宽和旋转）是否发生变化。如果发生变化，需要重新调用transformDisplayUvCoords()获取正确的纹理贴图坐标。
     * @return
     */
    public boolean hasDisplayGeometryChanged() {
        if (coreFrame!=null){
            return  coreFrame.hasDisplayGeometryChanged();
        }else{
            return hwFrame.hasDisplayGeometryChanged();
        }
    }

    /**
     * 调整纹理映射坐标，以便可以正确地显示相机捕捉到的背景图片。
     * @param uvCoords 原始输入uv坐标值。
     * @param outUvCoords 调整后的uv坐标值
     */
    public void transformDisplayUvCoords(FloatBuffer uvCoords, FloatBuffer outUvCoords) {
        if (coreFrame!=null){
            coreFrame.transformDisplayUvCoords(uvCoords,outUvCoords);
        }else{
            hwFrame.transformDisplayUvCoords(uvCoords,outUvCoords);
        }
    }

//    public ARSceneMesh acquireSceneMesh() {
//        coreFrame.acquireCameraImage()
//    }

    /**
     * 接收相机图像
     * @return Image对象
     * @throws NotYetAvailableException
     */
    public Image acquireCameraImage() throws NotYetAvailableException {
        if (coreFrame!=null){
            return  coreFrame.acquireCameraImage();
        }else{
            return hwFrame.acquireCameraImage();
        }
    }

    public Image acquireDepthImage() throws NotYetAvailableException {
        if (coreFrame!=null){
            Image result = coreFrame.acquireDepthImage16Bits();
//            Image result = coreFrame.acquireDepthImage();
            return result;
        }else{
            return hwFrame.acquireDepthImage();
        }
    }

    /**
     * 获取原生深度图
     * <p>
     *     当前仅ARCore支持
     * </p>
     * @return
     * @throws NotYetAvailableException
     */
    public Image acquireRawDepthImage() throws NotYetAvailableException {
        //added by ikkyu
        if (coreFrame!=null){
            Image result = coreFrame.acquireDepthImage16Bits();
//            Image result = coreFrame.acquireRawDepthImage();
            return result;
        }else{
            return hwFrame.acquireDepthImage();
        }
    }

//    public Image acquirePreviewImage() {
//        if (coreFrame!=null){
//            return  coreFrame.ac;
//        }else{
//            return hwFrame.acquirePreviewImage();
//        }
//    }

}
