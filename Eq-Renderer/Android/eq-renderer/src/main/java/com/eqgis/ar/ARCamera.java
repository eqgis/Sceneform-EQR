package com.eqgis.ar;



import android.view.Surface;

import com.google.sceneform.math.Quaternion;

/**
 * AR相机
 * <p>提供当前帧对应的相机信息，ARSession启动以后，引擎内部维护一个伴随ARSession生命周期的ARCamera对象，每次调用ARSession.update()时，该对象的值均会被更新。</p>
 * @author tanyx
 */
public class ARCamera {
    com.google.ar.core.Camera coreCamera = null;
    com.huawei.hiar.ARCamera hwCamera = null;

    private static InitCallback initCallback;

    /**
     * 初始化回调
     */
    public interface InitCallback{
        /**
         * AR相机初始化成功后回调
         * @param offsetAngle
         */
        void complete(float offsetAngle);
    }

    /**
     * 设置初始化回调
     * @param callback
     */
    public static void setInitCallback(InitCallback callback) {
        initCallback = callback;
    }

    /**
     * 水平面上的初始偏移角度
     * <p>用于校正AREngine的坐标系，使其与ARCore的一致</p>
     * x轴正方向为0度，顺时针为正
     * added by Ikkyu 2022/10/25
     */
    static Float offsetAngle;
    static int surfaceRotation;

    /**
     * <p>仅供ArSceneView调用</p>
     */
    public static void setSurfaceRotation(int surfaceRotation) {
        ARCamera.surfaceRotation = surfaceRotation;
    }

    /**
     * 获取偏移角度
     * @return 角度值，单位：度
     */
    public static float getOffsetAngle() {
        if (offsetAngle == null){
            return 0.0f;
        }
        return offsetAngle;
    }

    /**
     * 构造函数
     * @param coreobj ARCore的相机对象
     * @param hwobj 华为AREngine的相机对象
     */
    public ARCamera(com.google.ar.core.Camera coreobj ,
                    com.huawei.hiar.ARCamera hwobj){
        if (coreobj==null && hwobj==null){
            throw new IllegalArgumentException();
        }
        coreCamera = coreobj;
        hwCamera = hwobj;
    }

    /**
     * 获取ARCore的相机对象
     * @return ARCore的相机对象
     */
    public com.google.ar.core.Camera getCoreCamera(){
        return coreCamera;
    }

    /**
     * 获取华为AREngine的相机对象
     * @return 华为AREngine的相机对象
     */
    public com.huawei.hiar.ARCamera getHwCamera(){
        return hwCamera;
    }

    /**
     * 返回用户设备在世界坐标系中的位置和姿态，返回值的位置和方向跟随设备的物理相机变化（不受屏幕旋转影响）。
     * @return 用户设备在世界坐标系中的位姿。
     */
    public  ARPose getPose() {
        if (coreCamera!=null){
            com.google.ar.core.Pose p = coreCamera.getPose();
            if (p==null)return null;
            return new ARPose( p , null);
        }else{
            com.huawei.hiar.ARPose p = hwCamera.getPose();
            if (p==null)return null;
            return ARPose.updatePoseOnAREngine(new ARPose( null, p));
        }
    }

    /**
     * 获取相机位姿的当前跟踪状态。
     * @return {@link TrackingState}位姿跟踪状态
     */
    public  TrackingState getTrackingState() {
        if (coreCamera!=null){
            return TrackingState.fromARCore( coreCamera.getTrackingState() );
        }else{
            return TrackingState.fromHuawei( hwCamera.getTrackingState() );
        }
    }

    /**
     * 获取当前帧对应相机的视图矩阵
     * <p>可用于世界坐标系到相机坐标系的变化</p>
     * @param viewMatrix 存储至少16个浮点数，以列为顺序表示4x4矩阵。
     * @param offset 开始写入viewMatrix的开始偏移地址。
     */
    public void getViewMatrix(float[] viewMatrix, int offset) {
        if (coreCamera!=null){
            coreCamera.getViewMatrix(viewMatrix,offset);
        }else{
            hwCamera.getViewMatrix(viewMatrix,offset);
        }
    }

    /**
     * 返回用户设备在世界坐标系中的位置和姿态
     * <p>返回值的位置位于设备的相机处，而方向大致与显示器的方向一致（注意：使用华为AREngine，屏幕旋转时方向会发生改变）。</p>
     * @return 用户设备在世界坐标系中的位姿
     */
    public ARPose getDisplayOrientedPose() {
        if (coreCamera!=null){
            com.google.ar.core.Pose p = coreCamera.getDisplayOrientedPose();
            if (p==null)return null;
            if (offsetAngle == null){
                offsetAngle = 0.0f;
                if (initCallback != null){
                    initCallback.complete(offsetAngle);
                }
            }
            return new ARPose(  p, null);
        }else{
            //返回用户设备在世界坐标系中的位置和姿态，返回值的位置位于设备的相机处，而方向大致与显示器的方向一致（与ARCore不同的是，屏幕旋转时方向会发生改变）。
            //参阅：https://developer.huawei.com/consumer/cn/doc/development/graphics-References/camera-0000001050121437#section417792449
            com.huawei.hiar.ARPose p = hwCamera.getDisplayOrientedPose();
            if (offsetAngle == null){
                float[] qu2 = new float[4];
                p.getRotationQuaternion(qu2,0);
                Quaternion q2 = new Quaternion(qu2[0],-qu2[2],qu2[1], qu2[3]);
                float x = (float) Math.toDegrees(Math.atan2(2*(q2.w*q2.x + q2.y*q2.z), 1-2*(q2.x * q2.x + q2.y*q2.y)));
                float y = (float) Math.toDegrees(Math.asin( 2 * (q2.w * q2.y - q2.x * q2.z)));
                float z = (float) Math.toDegrees(Math.atan2(2*(q2.w*q2.z + q2.x*q2.y), 1-2*(q2.y * q2.y + q2.z*q2.z)));
                float degree = (90.0f - y);
                if (x!=0.0f && y!=0.0f && z!=0.0f){
                    switch (surfaceRotation){
                        case Surface.ROTATION_0:
                            //注意：横竖屏的输入，有所区别
                            degree += 0;
                            break;
                        case Surface.ROTATION_90:
                            degree += 90;
                            break;
                        case Surface.ROTATION_180:
                            degree += 180;
                            break;
                        case Surface.ROTATION_270:
                            degree += 270;
                            break;
                    }
                    offsetAngle = degree;
                    if (initCallback != null){
                        initCallback.complete(offsetAngle);
                    }
                }
            }
            if (p==null)return null;
            return ARPose.updatePoseOnAREngine(new ARPose( null, p ));
        }
    }

    /**
     * 获取当前相机的投影矩阵
     * @param dest 存储至少16个浮点数，以列为顺序表示4x4矩阵。
     * @param offset 开始写入dest的开始偏移地址。
     * @param near 近裁剪平面的距离，单位：米
     * @param far 远裁剪平面的距离，单位：米
     */
    public void getProjectionMatrix(float[] dest, int offset, float near, float far) {
        if (coreCamera!=null){
            coreCamera.getProjectionMatrix(dest,offset,near,far);
        }else{
            hwCamera.getProjectionMatrix(dest,offset,near,far);
        }
    }

    /**
     * 获取物理相机离线内参的对象，可通过该对象获取相机的焦距、图像尺寸、主轴点和畸变参数。
     * @return 物理相机离线内参的对象。
     */
    public ARCameraIntrinsics getCameraImageIntrinsics() {
        if (coreCamera!=null){
            com.google.ar.core.CameraIntrinsics p = coreCamera.getTextureIntrinsics();
            if (p==null)return null;
            return new ARCameraIntrinsics(  p, null);
        }else{
            com.huawei.hiar.ARCameraIntrinsics p = hwCamera.getCameraImageIntrinsics();
            if (p==null)return null;
            return new ARCameraIntrinsics( null, p);
        }
    }

}
