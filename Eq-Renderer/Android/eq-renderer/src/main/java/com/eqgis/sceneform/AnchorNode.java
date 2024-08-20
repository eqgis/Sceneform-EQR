package com.eqgis.sceneform;

import android.util.Log;

import androidx.annotation.Nullable;

import com.eqgis.eqr.node.RootNode;
import com.eqgis.sceneform.math.MathHelper;
import com.eqgis.sceneform.math.Quaternion;
import com.eqgis.sceneform.math.Vector3;
import com.eqgis.ar.ARAnchor;
import com.eqgis.ar.ARPose;
import com.eqgis.ar.TrackingState;

/**
 * 锚节点
 * <p>结合了AR中的Anchor，使其具备空间中的锚定能力</p>
 */
public class AnchorNode extends Node {
  private static final String TAG = AnchorNode.class.getSimpleName();

  private RootNode rootNode;

  // The anchor that the node is following.
  @Nullable private ARAnchor anchor;

  // Determines if the movement between the node's current position and the anchor position should
  // be smoothed over time or immediate.
  private boolean isSmoothed = true;

  private boolean wasTracking;

  private static final float SMOOTH_FACTOR = 12.0f;

  /** Create an AnchorNode with no anchor. */
  public AnchorNode() {}

  /**
   * 构造函数
   * @param anchor AR中的Anchor对象
   */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public AnchorNode(ARAnchor anchor) {
    setAnchor(anchor);
  }

  /**
   * 设置Anchor对象
   * @param anchor AR中的Anchor对象
   */
  public void setAnchor(@Nullable ARAnchor anchor) {
    this.anchor = anchor;
    if (this.anchor != null) {
      // Force the anchored position to be updated immediately.
      updateTrackedPose(0.0f, true);
    }

    // Make sure children are enabled based on the initial state of the anchor.
    // This is particularly important for Hosted Anchors, which aren't tracking when created.
    wasTracking = isTracking();
    setChildrenEnabled(wasTracking || anchor == null);
  }

  /** 获取Anchor对象 */
  @Nullable
  public ARAnchor getAnchor() {
    return anchor;
  }

  /**
   * 设置为true将平滑节点当前位置和锚点位置之间的过渡。
   * 设置false立即应用转换。平滑默认为true。
   * @param smoothed 是否内插。
   */
  public void setSmoothed(boolean smoothed) {
    this.isSmoothed = smoothed;
  }

  /**
   * 如果插值了转换则返回true，如果立即应用转换则返回false。
   */
  public boolean isSmoothed() {
    return isSmoothed;
  }

  /** 判断是否处于跟踪状态 */
  //CHECK
  public boolean isTracking() {
    if (anchor == null || anchor.getTrackingState() !=  TrackingState.TRACKING) {
      return false;
    }

    return true;
  }

  /**
   * AnchorNode重新了它来更新方法，以匹配ARCore锚点位置同步更新。
   * @param frameTime 时间信息
   */
  @Override
  public void onUpdate(FrameTime frameTime) {
    super.onUpdate(frameTime);
    updateTrackedPose(frameTime.getDeltaSeconds(), false);
  }

  /**
   * 设置本地坐标系下的位置
   * @param position 位置信息
   */
  @Override
  public void setLocalPosition(Vector3 position) {
    if (anchor != null) {
      Log.w(TAG, "Cannot call setLocalPosition on AnchorNode while it is anchored.");
      return;
    }

    super.setLocalPosition(position);
  }

  /**
   * 设置本地坐标系下的旋转四元数
   * @param rotation 旋转四元数
   */
  @Override
  public void setLocalRotation(Quaternion rotation) {
    if (anchor != null) {
      Log.w(TAG, "Cannot call setLocalRotation on AnchorNode while it is anchored.");
      return;
    }

    super.setLocalRotation(rotation);
  }

  private void updateTrackedPose(float deltaSeconds, boolean forceImmediate) {
    boolean isTracking = isTracking();

    //如果锚当前没有跟踪，则隐藏子节点。
    if (isTracking != wasTracking) {
      // 如果没有锚点，子节点应该被启用，即使在这种情况下我们没有跟踪。
      setChildrenEnabled(isTracking || anchor == null);
    }

    //isTracking已经检查锚是否为空
    if (anchor == null || !isTracking) {
      wasTracking = isTracking;
      return;
    }

    ARPose pose = anchor.getPose();
    Vector3 desiredPosition = ArHelpers.extractPositionFromPose(pose);
    Quaternion desiredRotation = ArHelpers.extractRotationFromPose(pose);

    if (isSmoothed && !forceImmediate) {
      Vector3 position = getWorldPosition();
      float lerpFactor = MathHelper.clamp(deltaSeconds * SMOOTH_FACTOR, 0, 1);
      position.set(Vector3.lerp(position, desiredPosition, lerpFactor));
      super.setWorldPosition(position);

      Quaternion rotation = Quaternion.slerp(getWorldRotation(), desiredRotation, lerpFactor);
      super.setWorldRotation(rotation);
    } else {
      super.setWorldPosition(desiredPosition);
      super.setWorldRotation(desiredRotation);
    }

    wasTracking = isTracking;
  }


  @Override
  public void setParent(@Nullable NodeParent parent) {
    if (parent instanceof RootNode){
      //父节点是根节点，记录根节点
      rootNode = (RootNode) parent;
    }

    if (parent instanceof AnchorNode){
      //父节点包含根节点信息，记录根节点
      rootNode = ((AnchorNode) parent).rootNode;
    }
    super.setParent(parent);
  }

//  @Override
//  public void setWorldScale(Vector3 scale) {
//    super.setWorldScale(scale);
//  }

  /**
   * 设置世界坐标系下的位置
   * @param position 位置
   */
  @Override
  public void setWorldPosition(Vector3 position) {
    if (rootNode != null){
      super.setWorldPosition(Vector3.add(position,rootNode.getWorldPosition()));
      return;
    }
    if (anchor != null) {
      Log.w(TAG, "Cannot call setWorldPosition on AnchorNode while it is anchored.");
      return;
    }

    super.setWorldPosition(position);
  }


  /**
   * 设置世界坐标系下的旋转四元数
   *
   * @param rotation The rotation to apply.
   */
  @Override
  public void setWorldRotation(Quaternion rotation) {
    if (rootNode != null){
      super.setWorldRotation(Quaternion.multiply(rotation,rootNode.getWorldRotation()));
      return;
    }

    if (anchor != null) {
      Log.w(TAG, "Cannot call setWorldRotation on AnchorNode while it is anchored.");
      return;
    }

    super.setWorldRotation(rotation);
  }

  @Override
  public void setWorldScale(Vector3 scale) {
    if (rootNode != null){
      Vector3 rootScale = rootNode.getWorldScale();
      new Vector3(rootScale.x * scale.x, rootScale.y * scale.y, rootScale.z * scale.z);
      super.setWorldScale(scale);
      return;
    }
    super.setWorldScale(scale);
  }
}
