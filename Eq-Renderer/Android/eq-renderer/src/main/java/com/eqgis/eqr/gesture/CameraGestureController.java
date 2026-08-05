package com.eqgis.eqr.gesture;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import com.google.sceneform.Camera;
import com.google.sceneform.HitTestResult;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.ViewRenderable;
import com.google.sceneform.utilities.Preconditions;

/**
 * 场景相机手势控制器
 * <pre>
 *     单指拖动用于旋转相机，双指同步拖动用于在相机视图平面内平移相机，
 *     双指张开或捏合用于沿相机朝向前后移动。
 *     控制器不会消费触摸事件，节点点击和 ViewRenderable 内部控件仍可继续接收事件。
 * </pre>
 * @author tanyx
 */
public class CameraGestureController implements View.OnTouchListener {
    private static final int MODE_NONE = 0;
    private static final int MODE_ROTATE = 1;
    private static final int MODE_TRANSLATE = 2;
    private static final float DEFAULT_ROTATION_DEGREES_PER_PIXEL = 0.16f;
    private static final float DEFAULT_TRANSLATION_METERS_PER_PIXEL = 0.003f;
    private static final float DEFAULT_ZOOM_METERS_PER_PIXEL = 0.006f;
    private static final float DEFAULT_PITCH_LIMIT_DEGREES = 85.0f;

    private final Camera camera;
    private final Vector3 initialPosition;
    private final Quaternion initialRotation;

    private View touchView;
    private boolean enabled = true;
    private boolean blockSingleFingerRotation;
    private int gestureMode = MODE_NONE;
    private float lastX;
    private float lastY;
    private float lastFocusX;
    private float lastFocusY;
    private float lastSpan;
    private float accumulatedPitchDegrees;
    private float rotationDegreesPerPixel = DEFAULT_ROTATION_DEGREES_PER_PIXEL;
    private float translationMetersPerPixel = DEFAULT_TRANSLATION_METERS_PER_PIXEL;
    private float zoomMetersPerPixel = DEFAULT_ZOOM_METERS_PER_PIXEL;
    private float pitchLimitDegrees = DEFAULT_PITCH_LIMIT_DEGREES;

    /**
     * 创建相机手势控制器，调用线程必须是 Android UI 线程
     * @param camera {@link Camera} 需要控制的普通三维场景相机
     */
    public CameraGestureController(Camera camera) {
        this.camera = Preconditions.checkNotNull(camera, "Parameter \"camera\" was null.");
        initialPosition = new Vector3(camera.getWorldPosition());
        initialRotation = new Quaternion(camera.getWorldRotation());
    }

    /**
     * 将控制器绑定到触摸视图，重复绑定时会先解绑旧视图
     * @param view 接收相机手势的视图，通常为 SceneView
     * @return 当前控制器
     */
    @SuppressLint("ClickableViewAccessibility")
    public CameraGestureController attachTo(View view) {
        Preconditions.checkNotNull(view, "Parameter \"view\" was null.");
        detach();
        touchView = view;
        touchView.setOnTouchListener(this);
        return this;
    }

    /**
     * 解绑触摸视图并重置当前手势状态，应在宿主视图销毁前调用
     */
    public void detach() {
        if (touchView != null) {
            touchView.setOnTouchListener(null);
            touchView = null;
        }
        resetGestureState();
    }

    /**
     * 设置控制器启用状态
     * @param enabled true 表示响应相机手势
     * @return 当前控制器
     */
    public CameraGestureController setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            resetGestureState();
        }
        return this;
    }

    /**
     * 获取控制器启用状态
     * @return true 表示响应相机手势
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置单指旋转灵敏度
     * @param degreesPerPixel 每移动一个像素对应的旋转角度，必须大于 0
     * @return 当前控制器
     */
    public CameraGestureController setRotationSensitivity(float degreesPerPixel) {
        if (degreesPerPixel <= 0) {
            throw new IllegalArgumentException("degreesPerPixel must be greater than 0.");
        }
        rotationDegreesPerPixel = degreesPerPixel;
        return this;
    }

    /**
     * 设置双指平移灵敏度
     * @param metersPerPixel 每移动一个像素对应的世界坐标距离，必须大于 0
     * @return 当前控制器
     */
    public CameraGestureController setTranslationSensitivity(float metersPerPixel) {
        if (metersPerPixel <= 0) {
            throw new IllegalArgumentException("metersPerPixel must be greater than 0.");
        }
        translationMetersPerPixel = metersPerPixel;
        return this;
    }

    /**
     * 设置双指缩放灵敏度
     * @param metersPerPixel 双指间距每变化一个像素时，相机前后移动的世界坐标距离，必须大于 0
     * @return 当前控制器
     */
    public CameraGestureController setZoomSensitivity(float metersPerPixel) {
        if (metersPerPixel <= 0) {
            throw new IllegalArgumentException("metersPerPixel must be greater than 0.");
        }
        zoomMetersPerPixel = metersPerPixel;
        return this;
    }

    /**
     * 设置相对初始姿态的俯仰限制
     * @param degrees 最大俯仰角，取值范围为 (0, 90]
     * @return 当前控制器
     */
    public CameraGestureController setPitchLimit(float degrees) {
        if (degrees <= 0 || degrees > 90) {
            throw new IllegalArgumentException("degrees must be in the range (0, 90].");
        }
        pitchLimitDegrees = degrees;
        accumulatedPitchDegrees = clamp(
                accumulatedPitchDegrees,
                -pitchLimitDegrees,
                pitchLimitDegrees);
        return this;
    }

    /**
     * 将相机恢复到创建控制器时的位置和姿态
     */
    public void resetCamera() {
        camera.setWorldPosition(new Vector3(initialPosition));
        camera.setWorldRotation(new Quaternion(initialRotation));
        accumulatedPitchDegrees = 0;
        resetGestureState();
    }

    /**
     * 处理触摸事件，但不消费事件，以保留场景节点与 ViewRenderable 的原有交互
     * @param view 当前触摸视图
     * @param event 触摸事件
     * @return 始终返回 false
     */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (!enabled || event == null) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                beginSingleFingerGesture(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() >= 2) {
                    beginTwoFingerGesture(event, -1);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() >= 2) {
                    transformCamera(event);
                } else if (event.getPointerCount() == 1) {
                    rotateCamera(event);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                handlePointerUp(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetGestureState();
                break;
            default:
                break;
        }
        return false;
    }

    private void beginSingleFingerGesture(MotionEvent event) {
        gestureMode = MODE_ROTATE;
        lastX = event.getX(0);
        lastY = event.getY(0);
        if (camera.getScene() == null) {
            blockSingleFingerRotation = false;
            return;
        }
        HitTestResult hitTestResult = camera.getScene().hitTest(event);
        blockSingleFingerRotation = hitTestResult.getNode() != null
                && hitTestResult.getNode().getRenderable() instanceof ViewRenderable;
    }

    private void beginTwoFingerGesture(MotionEvent event, int excludedPointerIndex) {
        gestureMode = MODE_TRANSLATE;
        lastFocusX = calculateFocus(event, excludedPointerIndex, true);
        lastFocusY = calculateFocus(event, excludedPointerIndex, false);
        lastSpan = calculateSpan(event, excludedPointerIndex);
        blockSingleFingerRotation = true;
    }

    private void rotateCamera(MotionEvent event) {
        float currentX = event.getX(0);
        float currentY = event.getY(0);
        if (gestureMode != MODE_ROTATE) {
            gestureMode = MODE_ROTATE;
            lastX = currentX;
            lastY = currentY;
            return;
        }
        float deltaX = currentX - lastX;
        float deltaY = currentY - lastY;
        lastX = currentX;
        lastY = currentY;
        if (blockSingleFingerRotation) {
            return;
        }

        float yawDegrees = -deltaX * rotationDegreesPerPixel;
        float targetPitch = clamp(
                accumulatedPitchDegrees - deltaY * rotationDegreesPerPixel,
                -pitchLimitDegrees,
                pitchLimitDegrees);
        float pitchDelta = targetPitch - accumulatedPitchDegrees;

        Quaternion currentRotation = camera.getWorldRotation();
        Quaternion yawRotation = Quaternion.axisAngle(Vector3.up(), yawDegrees);
        Quaternion yawedRotation = Quaternion.multiply(yawRotation, currentRotation);
        Vector3 pitchAxis = Quaternion.rotateVector(yawedRotation, Vector3.right()).normalized();
        Quaternion pitchRotation = Quaternion.axisAngle(pitchAxis, pitchDelta);
        //desc- yaw 使用世界上方向，pitch 使用旋转后的相机右方向，避免连续拖动产生滚转。
        camera.setWorldRotation(Quaternion.multiply(pitchRotation, yawedRotation).normalized());
        accumulatedPitchDegrees = targetPitch;
    }

    private void transformCamera(MotionEvent event) {
        float focusX = calculateFocus(event, -1, true);
        float focusY = calculateFocus(event, -1, false);
        float span = calculateSpan(event, -1);
        if (gestureMode != MODE_TRANSLATE) {
            gestureMode = MODE_TRANSLATE;
            lastFocusX = focusX;
            lastFocusY = focusY;
            lastSpan = span;
            blockSingleFingerRotation = true;
            return;
        }
        float deltaX = focusX - lastFocusX;
        float deltaY = focusY - lastFocusY;
        float deltaSpan = span - lastSpan;
        lastFocusX = focusX;
        lastFocusY = focusY;
        lastSpan = span;

        Vector3 horizontalOffset = camera.getRight().scaled(-deltaX * translationMetersPerPixel);
        Vector3 verticalOffset = camera.getUp().scaled(deltaY * translationMetersPerPixel);
        Vector3 zoomOffset = camera.getForward().scaled(deltaSpan * zoomMetersPerPixel);
        Vector3 offset = Vector3.add(Vector3.add(horizontalOffset, verticalOffset), zoomOffset);
        //desc- 双指质心控制视图平移，指间距控制相机前后移动，两种变化可在同一帧叠加。
        camera.setWorldPosition(Vector3.add(camera.getWorldPosition(), offset));
    }

    private void handlePointerUp(MotionEvent event) {
        int liftedPointerIndex = event.getActionIndex();
        int remainingPointerCount = event.getPointerCount() - 1;
        if (remainingPointerCount >= 2) {
            beginTwoFingerGesture(event, liftedPointerIndex);
        } else if (remainingPointerCount == 1) {
            int remainingIndex = liftedPointerIndex == 0 ? 1 : 0;
            gestureMode = MODE_ROTATE;
            lastX = event.getX(remainingIndex);
            lastY = event.getY(remainingIndex);
            //desc- 双指平移结束后必须重新落下一根手指才启动旋转，避免切换模式时相机跳动。
            blockSingleFingerRotation = true;
        } else {
            resetGestureState();
        }
    }

    private float calculateFocus(MotionEvent event, int excludedPointerIndex, boolean horizontal) {
        float total = 0;
        int count = 0;
        for (int i = 0; i < event.getPointerCount(); i++) {
            if (i == excludedPointerIndex) {
                continue;
            }
            total += horizontal ? event.getX(i) : event.getY(i);
            count++;
        }
        return count == 0 ? 0 : total / count;
    }

    private float calculateSpan(MotionEvent event, int excludedPointerIndex) {
        int firstPointerIndex = -1;
        int secondPointerIndex = -1;
        for (int i = 0; i < event.getPointerCount(); i++) {
            if (i == excludedPointerIndex) {
                continue;
            }
            if (firstPointerIndex < 0) {
                firstPointerIndex = i;
            } else {
                secondPointerIndex = i;
                break;
            }
        }
        if (secondPointerIndex < 0) {
            return 0;
        }
        float deltaX = event.getX(secondPointerIndex) - event.getX(firstPointerIndex);
        float deltaY = event.getY(secondPointerIndex) - event.getY(firstPointerIndex);
        return (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    private void resetGestureState() {
        gestureMode = MODE_NONE;
        blockSingleFingerRotation = false;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
