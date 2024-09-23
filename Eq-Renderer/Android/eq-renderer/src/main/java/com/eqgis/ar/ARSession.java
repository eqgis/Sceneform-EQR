package com.eqgis.ar;

import android.content.Context;

import com.google.sceneform.ARPlatForm;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.sceneform.rendering.CameraStream;
import com.eqgis.ar.exceptions.ARCameraException;
import com.eqgis.ar.exceptions.ARSessionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * ARSession对象
 */
public class ARSession {
    Session coreSession = null;
    com.huawei.hiar.ARSession hwSession = null;


    ARConfig arConfig = null;

    ARSession(Session coreobj, com.huawei.hiar.ARSession hwobj){
        if (coreobj==null && hwobj==null){
            throw new IllegalArgumentException();
        }
        coreSession = coreobj;
        hwSession = hwobj;
    }

    /**
     * 构造函数
     * @param context 上下文
     * @throws ARSessionException Session异常
     */

    public ARSession(Context context) throws ARSessionException {
        if (ARPlatForm.isArCore()){
            try {
                coreSession = new Session(context);
            } catch (UnavailableArcoreNotInstalledException | UnavailableApkTooOldException | UnavailableSdkTooOldException | UnavailableDeviceNotCompatibleException e) {
                throw new ARSessionException(e);
            }
        }else{
            hwSession = new com.huawei.hiar.ARSession(context);
        }
    }

    /**
     * 获取ARConfig对象
     * @return ARConfig对象
     */
    public ARConfig getArConfig() {
        return arConfig;
    }

    /**
     * 根据ARPose创建锚点
     * @param pose AR位姿数据
     * @return 锚点
     */
    public  ARAnchor createAnchor(ARPose pose) {
        if (coreSession!=null){
            Anchor anchor = coreSession.createAnchor(pose.corepose);
            if (anchor!=null){
                return new ARAnchor(anchor,null);
            }else{
                return null;
            }
        }else{
            com.huawei.hiar.ARAnchor anchor = hwSession.createAnchor(pose.hwpose);
            if (anchor!=null) {
                return new ARAnchor(null, anchor);
            }else{
                return null;
            }
        }
    }

    /**
     * 根据ARPose创建锚点
     * @param pose AR位姿数据
     * @return 锚点
     */
    public  ARAnchor addAnchor(ARPose pose) {
        return this.createAnchor(pose);
    }

    /**
     * 获取所有锚点，包括TrackingState为PAUSED，TRACKING和STOPPED。应用处理时需要仅绘制TRACKING状态的锚点，删除STOPPED状态的锚点。
     * @return 所有锚点的集合。
     */
    public Collection< ARAnchor > getAllAnchors() {
        if (coreSession!=null){
            Collection<Anchor> anchors = coreSession.getAllAnchors();
            ArrayList<ARAnchor> results = new ArrayList<>();
            Iterator<Anchor> iter = anchors.iterator();
            while (iter.hasNext()){
                Anchor anchor = iter.next();
                results.add(new ARAnchor(anchor,null));
            }

//            for (int i=0;i<anchors.size();i++){
//                results.add(new ARAnchor(anchors[i],null) );
//            }
            return results;
        }else{
            Collection<com.huawei.hiar.ARAnchor> anchors = hwSession.getAllAnchors();
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

    /**
     * 获取所有检测到的平面，如果ARConfigBase.PlaneFindingMode为DISABLED，则返回空。
     * <p>AREngine平台下，当前方法已过时，需用getAllTrackables()替代</p>
     * @return
     */
    public Collection< ARPlane > getAllPlanes() {
        if (coreSession!=null){
            Collection<com.google.ar.core.Plane> anchors = coreSession.getAllTrackables(com.google.ar.core.Plane.class);
            ArrayList<ARPlane> results = new ArrayList<>();
            Iterator<com.google.ar.core.Plane> iter = anchors.iterator();
            while (iter.hasNext()){
                com.google.ar.core.Plane plane = iter.next();
                results.add(new ARPlane(plane,null));
            }

//            for (int i=0;i<anchors.size();i++){
//                results.add(new ARAnchor(anchors[i],null) );
//            }
            return results;
        }else{
            Collection<com.huawei.hiar.ARPlane> anchors = hwSession.getAllPlanes();
            ArrayList<ARPlane> results = new ArrayList<>();
            Iterator<com.huawei.hiar.ARPlane> iter = anchors.iterator();
            while (iter.hasNext()){
                com.huawei.hiar.ARPlane plane = iter.next();
                results.add(new ARPlane(null,plane));
            }
//            for (int i=0;i<anchors.size();i++){
//                results.add(new ARAnchor(null,anchors[i]) );
//            }
            return results;
        }
    }

//    public Collection< ARPlane > getAllPlanes() {
//        coreSession.getAllTrackables();
//        hwSession.getAllTrackables()
//
//        return this.getAllTrackables(com.huawei.hiar.ARPlane.class);
//    }

//    public void getProjectionMatrix(float[] dest, int offset, float near, float far) {
//        Object var5;
//        Object var10000 = var5 = this.syncObject;
//        com.huawei.hiar.ARSession var10001 = this;
//        synchronized(var5) {
//            var10001.mLastFrame.mCamera.getProjectionMatrix(dest, offset, near, far);
//        }
//    }

    /**
     * 判断是否支持，ARCore和AREngine平台下，当前方法都已过时
     * @param config ARConfig
     * @return 状态值
     */
    @Deprecated
    public boolean isSupported(ARConfig config) {
        if (coreSession!=null){
            return coreSession.isSupported(config.coreConfig);
        }else{
            return hwSession.isSupported(config.hwConfig);
        }
    }

    /**
     * 暂停Session
     * <p>停止相机预览流，不清除平面和锚点数据，释放相机（否则其他应用无法使用相机服务），不会断开与服务端的连接。调用后需要使用resume()恢复。</p>
     */
    public void pause() {
        if (coreSession!=null){
            coreSession.pause();
        }else{
            hwSession.pause();
        }
        ARCamera.offsetAngle = null;
    }

    /**
     * 停止Session
     * <p>停止ARSession，停止相机预览流，清除平面和锚点数据，并释放相机，终止本次会话。调用后，如果要再次启动，需要新建ARSession。</p>
     */
    public void stop() {
        if (coreSession!=null){
            coreSession.close();
        }else{
            hwSession.stop();
        }
        ARCamera.offsetAngle = null;
    }

    /**
     * 移除集合中的锚点对象
     * <p>内部使用{@link ARAnchor#detach()}方法</p>
     * @param anchors 锚点集合
     */
    public void removeAnchors(Collection< ARAnchor > anchors) {
        if (anchors != null) {
            Iterator<ARAnchor> iter = anchors.iterator();

            while(iter.hasNext()) {
                ARAnchor anchor = iter.next();
                anchor.detach();
            }
        }
    }

    /**
     * 开始运行ARSession，或者在调用pause()以后恢复ARSession的运行状态。
     * @throws ARCameraException
     */
    public void resume() throws ARCameraException {
        if (coreSession!=null){
            try {
                coreSession.resume();
            } catch (CameraNotAvailableException e) {
                throw new ARCameraException(e);
            }
        }else{
            hwSession.resume();
        }
    }

    /**
     * 根据config恢复session。
     * <p>有两种使用场景：启动服务和服务被暂停后重启。注意：在AREngine平台下，调用了ARSession.stop()之后，该方法不能恢复。</p>
     * @param config
     * @throws ARCameraException
     */
    public void resume(ARConfig  config) throws ARCameraException {
        if (coreSession!=null){
            coreSession.configure(config.coreConfig);
            try {
                coreSession.resume();
            } catch (CameraNotAvailableException e) {
                throw new ARCameraException(e);
            }
        }else{
            hwSession.resume(config.hwConfig);
        }
        this.arConfig = config;
    }

    /**
     * 更新计算结果
     * <p>应用在需要获取最新的数据时调用此接口，如相机发生移动以后，使用此接口可以获取新的锚点坐标、平面坐标、相机获取的图像帧等。如果{@link com.eqgis.ar.ARConfig.UpdateMode}为BLOCKING，那么该函数会阻塞至有新的帧可用。</p>
     * @return
     * @throws ARCameraException
     */
    public ARFrame update() throws ARCameraException {
        if (coreSession!=null){
            try {
                return new ARFrame(coreSession.update(),null);
            } catch (CameraNotAvailableException e) {
                throw new ARCameraException(e);
            }
        }else{
            return new ARFrame(null,hwSession.update());
        }
    }

    /**
     * 设置可用于存储相机预览流数据的openGL textureId
     * <p>应用调用ARSession.update()后，AR Engine会更新相机预览到纹理textureId中。textureId使用时需要指定为GL_TEXTURE_EXTERNAL_OES，如：GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId)。</p>
     * @param textureId 相机预览数据流的openGL textureId。
     */
    public void setCameraTextureNames(int[] textureId) {
        if (coreSession!=null){
            //Passing multiple textures allows for a multithreaded rendering pipeline, unlike {@link #setCameraTextureName}.
            coreSession.setCameraTextureNames(textureId);
        }else{
            hwSession.setCameraTextureName(textureId[0]);
        }
    }

    @Deprecated
    public void setDisplayGeometry(float width, float height) {
        if (coreSession!=null){
            coreSession.setDisplayGeometry(0,(int)width,(int)height);
        }else{
            hwSession.setDisplayGeometry(width,height);
        }
    }

    /**
     * 设置显示界面的旋转角度、宽和高。
     * @param displayRotation 旋转角度
     * @param widthPx 宽
     * @param heightPx 高
     */
    public void setDisplayGeometry(int displayRotation, int widthPx, int heightPx) {
        if (coreSession!=null){
            coreSession.setDisplayGeometry(displayRotation,widthPx,heightPx);
        }else{
            hwSession.setDisplayGeometry(displayRotation,widthPx,heightPx);
        }
    }

    /**
     * 配置Session
     * @param config AR配置对象
     */
    public void configure(ARConfig config) {
        if (coreSession!=null){
//            config.coreConfig.setLightEstimationMode(Config.LightEstimationMode.AMBIENT_INTENSITY);//Ĭ�ϲ��ô�����
            coreSession.configure(config.coreConfig);
        }else{
//            config.hwConfig.setLightingMode(ARConfigBase.LIGHT_MODE_AMBIENT_INTENSITY);//Ĭ�ϲ��ô�����
            hwSession.configure(config.hwConfig);
        }
        this.arConfig = config;
    }

//    public <T extends  ARTrackable> Collection<T> getAllTrackables(Class<T> filterType) {
//        if (coreSession!=null){
//            Collections<com.google.ar.core.Trackable> trackables = coreSession.getAllTrackables();
//            Iterator<com.google.ar.core.Trackable> iter = trackables.
//
//
//        }else{
//
//        }
//
//    }

    /**
     * @return
     */
    public boolean isHDR() {
        if (arConfig.coreConfig != null){
            return arConfig.coreConfig.getLightEstimationMode() == Config.LightEstimationMode.ENVIRONMENTAL_HDR;
        }else {
            return false;
        }
    }

    /**
     * 获取相机配置对象
     * @return AR相机配置对象
     */
    public ARCameraConfig getCameraConfig() {
        if (coreSession!=null){
            com.google.ar.core.CameraConfig c = coreSession.getCameraConfig();
            if (c==null)return null;
            return new ARCameraConfig(c,null);
        }else{
            com.huawei.hiar.ARCameraConfig c = hwSession.getCameraConfig();
            if (c==null)return null;
            return new ARCameraConfig(null,c);
        }
    }

    /**
     * 获取设备支持的深度数据类型
     * @return 支持的深度数据类型
     */
    public CameraStream.DepthMode checkIfDepthIsEnabled() {
        CameraStream.DepthMode depthMode = CameraStream.DepthMode.NO_DEPTH;

        if (coreSession != null){
            if (coreSession.isDepthModeSupported(Config.DepthMode.AUTOMATIC))
                if (coreSession.getConfig().getDepthMode() == Config.DepthMode.AUTOMATIC) {
                    depthMode = CameraStream.DepthMode.DEPTH;
                }

            if (coreSession.isDepthModeSupported(Config.DepthMode.RAW_DEPTH_ONLY))
                if (coreSession.getConfig().getDepthMode() == Config.DepthMode.RAW_DEPTH_ONLY) {
                    depthMode = CameraStream.DepthMode.RAW_DEPTH;
                }
        }else {
            //华为设备默认都支持
            depthMode = CameraStream.DepthMode.DEPTH;
        }
        return depthMode;
    }

    /**
     * 判断设备是否支持深度API
     * @return
     */
    public boolean isDepthModeSupported() {
        if (coreSession != null){
            if (coreSession.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                return true;
            }
        }else {
            //环境Mesh支持
            //
            //arenginesdk
            //
            //2.0.0.5
            //
            //EMUI 9.1及以上、HarmonyOS 2.0及以上
            //
            //深度相机
            //
            //P30 Pro、荣耀V20、Mate 30 Pro、P40 Pro、P40 Pro+、Mate 40 Pro+
            return true;
        }
        return false;
    }

    /**
     * 关闭Session
     */
    public void close() {
        stop();
//        if (coreSession!=null){
//            coreSession.close();
//        }else {
//            hwSession.stop();
//        }
    }

//    public int getSupportedSemanticMode() {
//        if (coreSession!=null){
//            return coreSession.getSupportedCameraConfigs();
//        }else{
//            return new ARCameraConfig(null,hwSession.getCameraConfig());
//        }
//    }

//    public void setCloudServiceAuthInfo(String authInfo) {
//        coreSession.setCameraTextureName();
//    }

//    public void addServiceListener(ServiceListener listener) {
//        coreSession.add
//    }


//    public void setEnvironmentTextureProbe(float[] arBoundBox) {
//        coreSession.setE
//    }

//    public void setEnvironmentTextureUpdateMode(com.huawei.hiar.ARSession.EnvironmentTextureUpdateMode textureUpdateMode) {
//        com.huawei.hiar.ARSession var10000 = this;
//        long var3 = this.mNativeHandle;
//        int var2 = textureUpdateMode.mNativeCode;
//        var10000.nativeSetEnvironmentTextureUpdateMode(var3, var2);
//    }

//    public static enum EnvironmentTextureUpdateMode {
//        UNKNOWN(-1),
//        AUTO(0),
//        UPDATE_ONE_TIME;
//
//        final int mNativeCode;
//
//        private EnvironmentTextureUpdateMode(int nativeCode) {
//            this.mNativeCode = nativeCode;
//        }
//
//        static com.huawei.hiar.ARSession.EnvironmentTextureUpdateMode forNumber(int nativeCode) {
//            com.huawei.hiar.ARSession.EnvironmentTextureUpdateMode[] var1;
//            int var2 = (var1 = values()).length;
//
//            for(int var3 = 0; var3 < var2; ++var3) {
//                com.huawei.hiar.ARSession.EnvironmentTextureUpdateMode var4;
//                if ((var4 = var1[var3]).mNativeCode == nativeCode) {
//                    return var4;
//                }
//            }
//
//            return UNKNOWN;
//        }
//
//        static {
//            com.huawei.hiar.ARSession.EnvironmentTextureUpdateMode var0;
//            com.huawei.hiar.ARSession.EnvironmentTextureUpdateMode var10000 = var0 = new com.huawei.hiar.ARSession.EnvironmentTextureUpdateMode;
//            var10000.<init>(1);
//            UPDATE_ONE_TIME = var10000;
//        }
//    }

}
