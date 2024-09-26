package com.eqgis.eqr;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Created by John on 02/03/2018.
 */

 class DeviceOrientation implements SensorEventListener {

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float[] R = new float[9];
    private float[] I = new float[9];
    private Sensor gsensor;
    private Sensor msensor;

    public float pitch;
    public float roll;
    private WindowManager windowManager;
    private SensorManager mSensorManager;
    private float orientation = 0f;

    public DeviceOrientation(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * Gets the device orientation in degrees from the azimuth (clockwise)
     *
     * @return orientation [0-360] in degrees
     */
    public float getOrientation() {
        return orientation;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //1.
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
            case Sensor.TYPE_ROTATION_VECTOR:
                processSensorOrientation(event.values);
                break;
            default:
                Log.e("DeviceOrientation", "Sensor event type not supported");
                break;
        }


        //2.
//        final float alpha = 1.0f;//0.97f;
//
//        synchronized (this) {
//            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//
//
//                mGravity[0] = alpha * mGravity[0] + (1 - alpha)
//                        * event.values[0];
//                mGravity[1] = alpha * mGravity[1] + (1 - alpha)
//                        * event.values[1];
//                mGravity[2] = alpha * mGravity[2] + (1 - alpha)
//                        * event.values[2];
//
//                // mGravity = event.values;
//
//                // Log.e(TAG, Float.toString(mGravity[0]));
//            }
//
//            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//                // mGeomagnetic = event.values;
//
//                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha)
//                        * event.values[0];
//                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha)
//                        * event.values[1];
//                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha)
//                        * event.values[2];
//                // Log.e(TAG, Float.toString(event.values[0]));
//
//            }
//
//            boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
//                    mGeomagnetic);
//            if (success) {
//                float orientation[] = new float[3];
//                SensorManager.getOrientation(R, orientation);
//                // Log.d(TAG, "azimuth (rad): " + azimuth);
//                this.orientation = (float) Math.toDegrees(orientation[0]); // orientation
//              //  azimuth = (azimuth + azimuthFix + 360) % 360;
//                // Log.d(TAG, "azimuth (deg): " + azimuth);
//            }
//        }


//        //3.
//        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
//
//            SensorManager.getRotationMatrixFromVector(rMat,event.values);
//
//            this.orientation = (float) (Math.toDegrees(SensorManager.getOrientation(rMat,orientationVector)[0])+ 360);
//            System.out.println("ThisOrientation:"+this.orientation);
//        }

    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void processSensorOrientation(float[] rotation) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotation);
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

        // azimuth/pitch/roll
        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);


        this.orientation = ((float) Math.toDegrees(orientation[0]) + 360f) % 360f;



    }

    float [] orientationVector = new float [3];
    float [] rMat = new float [9];

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.w("DeviceOrientation", "Orientation compass unreliable");
        }
    }

    public void resume() {
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);

        //2
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_GAME);

    }

    public void pause() {
        mSensorManager.unregisterListener(this);
    }
}