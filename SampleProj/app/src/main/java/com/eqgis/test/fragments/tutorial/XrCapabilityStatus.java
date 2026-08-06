package com.eqgis.test.fragments.tutorial;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import androidx.core.content.ContextCompat;

import com.eqgis.ar.ARPlugin;
import com.google.ar.core.ArCoreApk;

/**
 * XR 教程运行能力检测结果
 * <pre>
 *     在创建 ARSceneLayout 前检查相机权限、相机硬件、方向传感器和厂商 AR 服务，
 *     避免不支持设备直接进入 AR Session 初始化流程。ARCore 应用以 optional 模式
 *     声明后，Availability 才能返回真实的设备兼容性结果。
 * </pre>
 * @author tanyx
 */
final class XrCapabilityStatus {
    private final String engineName;
    private final boolean arReady;
    private final boolean cameraReady;
    private final boolean rotationSensorReady;
    private final String arUnavailableReason;
    private final String cameraUnavailableReason;

    private XrCapabilityStatus(String engineName,
                               boolean arReady,
                               boolean cameraReady,
                               boolean rotationSensorReady,
                               String arUnavailableReason,
                               String cameraUnavailableReason) {
        this.engineName = engineName;
        this.arReady = arReady;
        this.cameraReady = cameraReady;
        this.rotationSensorReady = rotationSensorReady;
        this.arUnavailableReason = arUnavailableReason;
        this.cameraUnavailableReason = cameraUnavailableReason;
    }

    /**
     * 检查当前设备的 AR 与 Camera 3DoF 运行条件
     * @param context Android 上下文
     * @return 当前设备能力检测结果
     */
    static XrCapabilityStatus inspect(Context context) {
        PackageManager packageManager = context.getPackageManager();
        boolean hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        boolean hasCameraPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean cameraReady = hasCamera && hasCameraPermission;
        String cameraReason = "";
        if (!hasCamera) {
            cameraReason = "当前设备未提供可用相机硬件。";
        } else if (!hasCameraPermission) {
            cameraReason = "相机权限未授予，AR 与 Camera 3DoF 均无法启动。请在系统设置中允许相机权限。";
        }

        SensorManager sensorManager =
                (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        boolean rotationSensorReady = sensorManager != null
                && sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null;

        boolean huaweiEngine = ARPlugin.isHuawei();
        String engineName = huaweiEngine ? "AREngine" : "ARCore";
        if (!cameraReady) {
            return new XrCapabilityStatus(
                    engineName,
                    false,
                    false,
                    rotationSensorReady,
                    cameraReason,
                    cameraReason);
        }

        if (huaweiEngine) {
            try {
                boolean ready = ARPlugin.isARApkReady(context);
                return new XrCapabilityStatus(
                        engineName,
                        ready,
                        true,
                        rotationSensorReady,
                        ready ? "" : "AREngine 服务不可用：服务未安装、版本过低，或当前设备不在 AREngine 支持范围内。",
                        "");
            } catch (RuntimeException error) {
                return new XrCapabilityStatus(
                        engineName,
                        false,
                        true,
                        rotationSensorReady,
                        "AREngine 能力检查失败：" + readableMessage(error),
                        "");
            }
        }

        try {
            ArCoreApk.Availability availability =
                    ArCoreApk.getInstance().checkAvailability(context.getApplicationContext());
            switch (availability) {
                case SUPPORTED_INSTALLED:
                    boolean apkReady = ARPlugin.isARApkReady(context);
                    return new XrCapabilityStatus(
                            engineName,
                            apkReady,
                            true,
                            rotationSensorReady,
                            apkReady ? "" : "Google Play Services for AR 版本过低，无法创建 ARCore Session。",
                            "");
                case SUPPORTED_NOT_INSTALLED:
                    return unsupportedAr(
                            engineName,
                            rotationSensorReady,
                            "设备支持 ARCore，但尚未安装 Google Play Services for AR。");
                case SUPPORTED_APK_TOO_OLD:
                    return unsupportedAr(
                            engineName,
                            rotationSensorReady,
                            "设备支持 ARCore，但 Google Play Services for AR 版本过低。");
                case UNSUPPORTED_DEVICE_NOT_CAPABLE:
                    return unsupportedAr(
                            engineName,
                            rotationSensorReady,
                            "当前机型或 Android 版本不支持 ARCore。可用相机画面无法提供 6DoF 位置跟踪。当前仅能采用 Camera 3DoF 降级方案。");
                case UNKNOWN_CHECKING:
                    return unsupportedAr(
                            engineName,
                            rotationSensorReady,
                            "ARCore 正在检查设备兼容性，当前结果尚未返回。请检查网络或稍后重新进入页面。");
                case UNKNOWN_TIMED_OUT:
                    return unsupportedAr(
                            engineName,
                            rotationSensorReady,
                            "ARCore 兼容性查询超时，可能是网络或 Google 服务不可用。");
                case UNKNOWN_ERROR:
                default:
                    return unsupportedAr(
                            engineName,
                            rotationSensorReady,
                            "ARCore 兼容性检查发生错误，无法安全创建 AR Session。");
            }
        } catch (RuntimeException error) {
            return unsupportedAr(
                    engineName,
                    rotationSensorReady,
                    "ARCore 能力检查失败：" + readableMessage(error));
        }
    }

    private static XrCapabilityStatus unsupportedAr(String engineName,
                                                     boolean rotationSensorReady,
                                                     String reason) {
        return new XrCapabilityStatus(
                engineName,
                false,
                true,
                rotationSensorReady,
                reason,
                "");
    }

    private static String readableMessage(RuntimeException error) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty()
                ? error.getClass().getSimpleName()
                : message;
    }

    String getEngineName() {
        return engineName;
    }

    boolean isArReady() {
        return arReady;
    }

    boolean canUseCameraFallback() {
        return cameraReady;
    }

    boolean hasRotationSensor() {
        return rotationSensorReady;
    }

    String getArUnavailableReason() {
        return arUnavailableReason;
    }

    String getCameraUnavailableReason() {
        return cameraUnavailableReason;
    }
}
