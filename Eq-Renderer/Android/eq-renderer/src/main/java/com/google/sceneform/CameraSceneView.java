package com.google.sceneform;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.eqgis.eqr.utils.PoseUtils;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.rendering.CameraStream;
import com.google.sceneform.rendering.ExternalTexture;
import com.google.sceneform.rendering.Renderer;
import com.google.sceneform.utilities.Preconditions;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * 相机场景视图
 * <p>用于实现3Dof的AR场景</p>
 * @author tanyx 2024/9/25
 * @version 1.0
 * <br/>SampleCode:<br/>
 * <code>
 *
 * </code>
 **/
public class CameraSceneView extends SceneView implements SensorEventListener {
    @Nullable
    private CameraStream cameraStream;
    @Nullable
    private ExternalTexture externalTexture;
    private Renderer renderer;
    private com.google.sceneform.ExSceneView.BeginFrameListener beginFrameListener;
    private com.google.sceneform.ExSceneView.InitializeListener initializeListener;
    private boolean isInit = false;
    private int textureId = -1;


    private WindowManager windowManager;
    private SensorManager mSensorManager;

    //Camera2相关
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private Size videoSize;
    private int screenRotation = Surface.ROTATION_0;
//    public Node node;

    //记录初始参数（第一次通过传感器获取到的角度值）
    private boolean  rotationInitialized = false;
    private float[] rotation = new float[3];

    @Override
    public void onSensorChanged(SensorEvent event) {
        //1.
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
                processSensorOrientation(event.values);
                break;
            default:
                Log.e("DeviceOrientation", "Sensor event type not supported");
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.w("DeviceOrientation", "Orientation compass unreliable");
        }
    }

    public interface BeginFrameListener{
        void onBeginFrame(long frameTimeNanos);
    }

    /**
     * 纹理初始化监听事件
     */
    public interface InitializeListener{
        /**
         * 当纹理初始化成功是触发回调
         * @param externalTexture 扩展纹理
         */
        void initializeTexture(ExternalTexture externalTexture);
    }

    /**
     * 构造函数
     *
     * @param context 安卓上下文
     */
    public CameraSceneView(Context context) {
        super(context);
        initBaseParameter(context);
    }

    /**
     * 构造函数
     *
     * @param context 安卓上下文
     * @see #CameraSceneView(Context, AttributeSet)
     */
    public CameraSceneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBaseParameter(context);
    }

    /**
     * set BeginFrameListener
     * @param beginFrameListener
     */
    public void setBeginFrameListener(com.google.sceneform.ExSceneView.BeginFrameListener beginFrameListener) {
        this.beginFrameListener = beginFrameListener;
    }

    private void initBaseParameter(Context context) {
        ARPlatForm.setType(ARPlatForm.Type.CAMERA);
        renderer = Preconditions.checkNotNull(getRenderer());
        int orientation = context.getResources().getConfiguration().orientation;

        Log.i("IKKYU", "initBaseParameter: context.getResources().getConfiguration()->>>>>>"+orientation);

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screenRotation = windowManager.getDefaultDisplay().getRotation();
        float[] vertex;
        //根据屏幕显示方向，构造vertices
        switch (screenRotation) {
            case Surface.ROTATION_90:
                vertex = new float[] {
                        -1.0f, 1.0f, 1.0f,//0
                        -1.0f, -1.0f,1.0f,//1
                        1.0f, 1.0f,1.0f,//2
                        1.0f, -1.0f,1.0f//3
                };
                break;
            case Surface.ROTATION_180:
                vertex = new float[] {
                        -1.0f, -1.0f,1.0f,//1
                        1.0f, -1.0f,1.0f,//3
                        -1.0f, 1.0f, 1.0f,//0
                        1.0f, 1.0f,1.0f//2
                };
                break;
            case Surface.ROTATION_270:
                vertex = new float[] {
                        1.0f, -1.0f,1.0f,//3
                        1.0f, 1.0f,1.0f,//2
                        -1.0f, -1.0f,1.0f,//1
                        -1.0f, 1.0f, 1.0f,//0
                };
                break;
            default:
                vertex = new float[] {
                        1.0f, 1.0f,1.0f,//2
                        -1.0f, 1.0f, 1.0f,//0
                        1.0f, -1.0f,1.0f,//3
                        -1.0f, -1.0f,1.0f//1
                };
                break;
        }
        // 背景平面初始化
        cameraStream = new CameraStream(new int[]{textureId},renderer,vertex, new float[] {
                0.0f,0.0f,
                0.0f,1.0f,
                1.0f,0.0f,
                1.0f,1.0f});
        externalTexture = new ExternalTexture();
    }

    @Override
    public void resume() {
        super.resume();
//        openCamera();
        registerListener();
    }

    @Override
    public void pause() {
        unRegisterListener();
//        closeCamera();
        super.pause();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    /**
     * 每帧渲染前触发
     *
     * @return 如果场景在渲染前需要更新，则为True。
     * @hide
     */
    @Override
    public boolean onBeginFrame(long frameTimeNanos) {
        if (externalTexture == null)return true;

        // 初始化默认的纹理
        if (!cameraStream.isTextureInitialized()) {
            cameraStream.initializeTexture(textureId,externalTexture);
            if (!isInit && initializeListener != null){
                initializeListener.initializeTexture(externalTexture);
                isInit = true;
            }
        }

        //更新深度图
        if (ARPlatForm.OCCLUSION_MODE == ARPlatForm.OcclusionMode.OCCLUSION_ENABLED && super.customDepthImage != null){
            cameraStream.recalculateOcclusion(customDepthImage);//use
        }

//        float[] transformMatrix = getTransformMatrix(90, 0);
//        int matrixHandle = GLES30.glGetUniformLocation(program, "uMatrix");
//        GLES30.glUniformMatrix4fv(matrixHandle, 1, false, transformMatrix, 0);
//         绑定相机纹理并绘制
//        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        if (beginFrameListener!=null){
            beginFrameListener.onBeginFrame(frameTimeNanos);
        }
        return true;
    }

    /**
     * 获取拓展纹理
     * @return {@link ExternalTexture}
     */
    @Nullable
    public ExternalTexture getExternalTexture() {
        return externalTexture;
    }

    /**
     * 设置纹理初始化监听事件
     * @param initializeListener 监听事件
     */
    public void setInitializeListener(com.google.sceneform.ExSceneView.InitializeListener initializeListener) {
        this.initializeListener = initializeListener;
    }

    private void closeCamera(){
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            videoSize = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(SurfaceTexture.class)[0];

            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            startPreview();
            Log.i(CameraSceneView.class.getSimpleName(), "onOpened: ");
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            cameraDevice = null;
            Log.i(CameraSceneView.class.getSimpleName(), "onDisconnected: ");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
            Log.i(CameraSceneView.class.getSimpleName(), "onError: ");
        }
    };

    private void startPreview() {
        try {
            //todo，需要做尺寸校正和裁剪
            SurfaceTexture texture = externalTexture.getSurfaceTexture();
            texture.setDefaultBufferSize(videoSize.getWidth(),videoSize.getHeight());
            Surface surface = externalTexture.getSurface();

            final CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        captureSession.setRepeatingRequest(builder.build(), null, null);
                    } catch (CameraAccessException e) {
                        Log.e(CameraSceneView.class.getSimpleName(), "onConfigured: ", e);
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    // Handle failure
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(CameraSceneView.class.getSimpleName(), "startPreview: ", e);
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void processSensorOrientation(float[] srcRotation) {
        float[] rotationMatrix = new float[9];
//        float[] rotation = {-srcRotation[1],srcRotation[2],srcRotation[0]};
        SensorManager.getRotationMatrixFromVector(rotationMatrix, srcRotation);
        final int worldAxisX;
        final int worldAxisY;

        switch (windowManager.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_90:
                worldAxisX = SensorManager.AXIS_Z;
                worldAxisY = SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                worldAxisX = SensorManager.AXIS_MINUS_X;
                worldAxisY = SensorManager.AXIS_MINUS_Z;
                break;
            case Surface.ROTATION_270:
                worldAxisX = SensorManager.AXIS_MINUS_Z;
                worldAxisY = SensorManager.AXIS_X;
                break;
            case Surface.ROTATION_0:
            default:
                worldAxisX = SensorManager.AXIS_X;
                worldAxisY = SensorManager.AXIS_Z;
                break;
        }
        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX,
                worldAxisY, adjustedRotationMatrix);

        // yaw/pitch/roll
        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);


        if (!rotationInitialized){
            rotation[0] = (float) Math.toDegrees(orientation[0]);
            rotation[1] = 0;
            rotation[2] = 0;
            rotationInitialized = true;
            return;
        }

        Quaternion quaternion = calculateRotation(
                (float) Math.toDegrees(orientation[0]) - rotation[0],
                (float) Math.toDegrees(orientation[1]) - rotation[1],
                (float) Math.toDegrees(orientation[2]) - rotation[2]);
//        Log.i("Pose", "processSensorOrientation2: "+df.format(Math.toDegrees(orientation[0]))
//                +"   v2:"+ df.format(Math.toDegrees(orientation[1]))
//                +"   v3:"+df.format(Math.toDegrees(orientation[2]))
//        +"   Q:"+quaternion.toString() + "fov: "+ getScene().getCamera().getVerticalFovDegrees());
//        node.setLocalRotation(quaternion);
        getScene().getCamera().setWorldRotation(quaternion);
    }

    DecimalFormat df = new DecimalFormat("#.#");
    public void registerListener(){
        mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_GAME);

        //2
//        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//                SensorManager.SENSOR_DELAY_UI);
//        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
//                SensorManager.SENSOR_DELAY_UI);
//        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
//                SensorManager.SENSOR_DELAY_UI);
    }

    public void unRegisterListener(){
        mSensorManager.unregisterListener(this);
    }

    /**
     * 计算旋转四元数
     * @return ENU坐标系的姿态//东北天坐标系
     */
    private Quaternion calculateRotation(float yaw,float pitch,float roll){
        return PoseUtils.toQuaternion(-pitch, -yaw, -roll);
    }

}
