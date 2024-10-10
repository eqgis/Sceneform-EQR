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
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;
import android.view.Surface;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.eqgis.eqr.listener.BeginFrameListener;
import com.eqgis.eqr.listener.InitializeListener;
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
    private BeginFrameListener beginFrameListener;
    private InitializeListener initializeListener;
    private boolean isInit = false;
    private int textureId = -1;

    private WindowManager windowManager;
    private SensorManager mSensorManager;

    //Camera2相关
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private int screenRotation = Surface.ROTATION_0;
    private int cameraWidth,cameraHeight;

//    public Node node;

    //记录初始参数（第一次通过传感器获取到的角度值）
    private boolean  rotationInitialized = false;
    private float[] rotation = new float[3];
    //目标画面比例采用4:3
    private int desiredWidth = 1920;
    private int desiredHeight = 1440;
    private CameraCharacteristics characteristics;

    @Override
    public void onSensorChanged(SensorEvent event) {
        //1.
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
                Quaternion quaternion = processSensorOrientation(event.values);
                getScene().getCamera().setWorldRotation(quaternion);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            //数据精度不够
            Log.w(CameraSceneView.class.getSimpleName(), "Orientation compass unreliable");
        }
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
    public void setBeginFrameListener(BeginFrameListener beginFrameListener) {
        this.beginFrameListener = beginFrameListener;
    }

    private void initBaseParameter(Context context) {
        ARPlatForm.setType(ARPlatForm.Type.CAMERA);
        renderer = Preconditions.checkNotNull(getRenderer());
//        int orientation = context.getResources().getConfiguration().orientation;
//        Log.i("IKKYU", "initBaseParameter: context.getResources().getConfiguration()->>>>>>"+orientation);

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
        openCamera();
        registerListener();
        Log.i("IKKYU", "resume: ");
    }

    @Override
    public void pause() {
        unRegisterListener();
        closeCamera();
        super.pause();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        //更新FOV
        updateFov();
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
    public void setInitializeListener(InitializeListener initializeListener) {
        this.initializeListener = initializeListener;
    }

    /**
     * 关闭相机
     */
    private void closeCamera(){
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
//        getScene().getCamera().setFOV();
        CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            characteristics = manager.getCameraCharacteristics(cameraId);

            {
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                // 获取支持的输出尺寸
                Size[] outputSizes = map.getOutputSizes(MediaRecorder.class);
                // 选择合适的尺寸
                Size selectedSize = null;
                for (Size size : outputSizes) {
                    if (size.getWidth() == desiredWidth && size.getHeight() == desiredHeight) {
                        selectedSize = size;
                        break;
                    }
//                    Log.i("IKKYU", "openCamera: " + size.toString());
                }

                if (selectedSize != null) {
                    cameraWidth = selectedSize.getWidth();
                    cameraHeight = selectedSize.getHeight();
//                    Log.i(CameraSceneView.class.getSimpleName(), "IKKYU Selected video size: " + selectedSize.toString());
                } else {
                    // 处理未找到指定尺寸的情况
//                    Log.w(CameraSceneView.class.getSimpleName(), "IKKYU Desired size not found, using default.");
                    // 可以选择使用第一个支持的尺寸
                    cameraWidth = outputSizes[0].getWidth();
                    cameraHeight = outputSizes[0].getHeight();
                }
            }
//            Log.i(CameraSceneView.class.getSimpleName(), "openCamera: IKKYU: video size :cameraWidth:"+cameraWidth+" cameraHeight:"+cameraHeight);
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            Log.e(CameraSceneView.class.getSimpleName(), "openCamera: ", e);
        }
    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            startPreview();
            //Log.i(CameraSceneView.class.getSimpleName(), "onOpened: ");
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            cameraDevice = null;
            //Log.i(CameraSceneView.class.getSimpleName(), "onDisconnected: ");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
            //Log.i(CameraSceneView.class.getSimpleName(), "onError: ");
        }
    };

    private void startPreview() {
        try {
            SurfaceTexture texture = externalTexture.getSurfaceTexture();
            Surface surface = externalTexture.getSurface();
            //Log.i("IKKYU", "startPreview: texture纹理尺寸：w："+cameraWidth + "  h:"+cameraHeight);
            ViewGroup.LayoutParams layoutParams = getLayoutParams();

            //之所以这样做，是由于相机预览画面比例为4：3，为了避免画面被拉伸
            float scale = Math.max( Math.max(width,height) / (float)desiredWidth,Math.min(width,height) / (float)desiredHeight);
            if (width > height){
                //视图尺寸宽大于高，
                layoutParams.width = (int) (desiredWidth * scale);
                layoutParams.height = (int) (desiredHeight * scale);
            }else {
                layoutParams.width = (int) (desiredHeight * scale);
                layoutParams.height = (int) (desiredWidth * scale);
            }
            this.setLayoutParams(layoutParams);
            texture.setDefaultBufferSize(layoutParams.width,layoutParams.height);

            //Log.i(CameraSceneView.class.getSimpleName(), "Ikkyu startPreview: 控件尺寸 size:"+width + ","+height + " scale:"+scale);
            //Log.i(CameraSceneView.class.getSimpleName(), "Ikkyu startPreview: layoutParams.width:"+layoutParams.width + ","+height + " scale:"+scale);

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
    private Quaternion processSensorOrientation(float[] srcRotation) {
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
//            rotation[1] = (float) Math.toDegrees(orientation[1]);
//            rotation[2] = (float) Math.toDegrees(orientation[2]);
            rotationInitialized = true;
            return Quaternion.identity();
        }

        Quaternion quaternion = calculateRotation(
                (float) Math.toDegrees(orientation[0]) - rotation[0],
                (float) Math.toDegrees(orientation[1]) - rotation[1],
                (float) Math.toDegrees(orientation[2]) - rotation[2]);
//        Log.i("Pose", "processSensorOrientation2: "+df.format(Math.toDegrees(orientation[0]))
//                +"   v2:"+ df.format(Math.toDegrees(orientation[1]))
//                +"   v3:"+df.format(Math.toDegrees(orientation[2]))
//                +"   Q:"+quaternion.toString() + "fov: "+ getScene().getCamera().getVerticalFovDegrees());
//        node.setLocalRotation(quaternion);
        return quaternion;
    }

//    DecimalFormat df = new DecimalFormat("#.#");
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

    /**
     * 更新FOV
     */
    private void updateFov() {
        try {
            // 获取相机的焦距和传感器大小
            float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
//            for (int i = 0; i <focalLengths.length; i++) {
//                Log.d("CameraParams", "Ikkyu Focal Length: " + focalLengths[i]);
//            }
            float focalLength = focalLengths[0]; // 选择第一个焦距

            // 获取相机的图像传感器尺寸
            SizeF sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);

            float vFov = (float) Math.toDegrees(2 * Math.atan(sensorSize.getHeight() * 0.5f / focalLength));
            float hFov = (float) Math.toDegrees(2 * Math.atan(sensorSize.getWidth() * 0.5f / focalLength));
            float max = Math.max(vFov, hFov);
            float min = Math.min(vFov, hFov);
            //Log.w("IKKYU ", "openCamera: W:"+width + "  H:"+height);
            if (width > height){
                //横屏
                //Log.d("CameraParams", "Ikkyu x选择 FOV: " + min);
                getScene().getCamera().setVerticalFovDegrees(min);
            }else{
                //Log.d("CameraParams", "Ikkyu x选择 FOV: " + max);
                getScene().getCamera().setVerticalFovDegrees(max);
            }
            // 打印内参
            //Log.d("CameraParams", "Ikkyu x选择 Focal Length: " + focalLength);
            //Log.d("CameraParams", "Ikkyu Sensor Size: " + sensorSize);
            //Log.d("CameraParams", "Ikkyu Sensor Orientation: " + sensorOrientation);
            //Log.d("CameraParams", "Ikkyu FOV: vFov:" + vFov + "  hFov:"+hFov);

        }catch (RuntimeException e){
            //默认FOV
            getScene().getCamera().setVerticalFovDegrees(60);
        }
    }
}
