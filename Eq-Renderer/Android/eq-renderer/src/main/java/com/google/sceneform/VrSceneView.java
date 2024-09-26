package com.google.sceneform;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.eqgis.eqr.utils.PoseUtils;
import com.google.sceneform.math.Quaternion;

/**
 * VR场景视图
 * @author tanyx 2024/9/25
 * @version 1.0
 * <br/>SampleCode:<br/>
 **/
public class VrSceneView extends SceneView implements SensorEventListener {
    private WindowManager windowManager;
    private SensorManager mSensorManager;

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
                Log.e(VrSceneView.class.getSimpleName(), "Sensor event type not supported");
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    /**
     * 构造函数
     */
    public VrSceneView(Context context) {
        super(context);
        initBaseParameter(context);
    }

    /**
     * 构造函数
     */
    public VrSceneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBaseParameter(context);
    }

    private void initBaseParameter(Context context) {
        ARPlatForm.setType(ARPlatForm.Type.NONE);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public void resume() {
        super.resume();
        registerListener();
    }

    @Override
    public void pause() {
        unRegisterListener();
        super.pause();
    }

    @Override
    public void destroy() {
        super.destroy();
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
        getScene().getCamera().setWorldRotation(quaternion);
    }

    public void registerListener(){
        mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_GAME);
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
