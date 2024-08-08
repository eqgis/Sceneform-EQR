package com.eqgis.sceneform;

import android.util.Log;

import androidx.annotation.Nullable;

import com.eqgis.eqr.node.RootNode;
import com.eqgis.sceneform.math.MathHelper;
import com.eqgis.sceneform.math.Quaternion;
import com.eqgis.sceneform.math.Vector3;
import com.eqgis.eqr.ar.ARAnchor;
import com.eqgis.eqr.ar.ARPose;
import com.eqgis.eqr.ar.TrackingState;

/**
 * Node that is automatically positioned in world space based on an ARCore Anchor.
 *
 * <p>When the Anchor isn't tracking, all children of this node are disabled.
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
   * Create an AnchorNode with the specified anchor.
   *
   * @param anchor the ARCore anchor that this node will automatically position itself to.
   */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public AnchorNode(ARAnchor anchor) {
    setAnchor(anchor);
  }

  /**
   * Set an ARCore anchor and force the position of this node to be updated immediately.
   *
   * @param anchor the ARCore anchor that this node will automatically position itself to.
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

  /** Returns the ARCore anchor if it exists or null otherwise. */
  @Nullable
  public ARAnchor getAnchor() {
    return anchor;
  }

  /**
   * Set true to smooth the transition between the node’s current position and the anchor position.
   * Set false to apply transformations immediately. Smoothing is true by default.
   *
   * @param smoothed Whether the transformations are interpolated.
   */
  public void setSmoothed(boolean smoothed) {
    this.isSmoothed = smoothed;
  }

  /**
   * Returns true if the transformations are interpolated or false if they are applied immediately.
   */
  public boolean isSmoothed() {
    return isSmoothed;
  }

  /** Returns true if the ARCore anchor’s tracking state is TRACKING. */
  //CHECK
  public boolean isTracking() {
    if (anchor == null || anchor.getTrackingState() !=  TrackingState.TRACKING) {
      return false;
    }

    return true;
  }

  /**
   * AnchorNode overrides this to update the node's position to match the ARCore Anchor's position.
   *
   * @param frameTime provides time information for the current frame
   */
  @Override
  public void onUpdate(FrameTime frameTime) {
    super.onUpdate(frameTime);
    updateTrackedPose(frameTime.getDeltaSeconds(), false);
  }

  /**
   * Set the local-space position of this node if it is not anchored. If the node is anchored, this
   * call does nothing.
   *
   * @param position The position to apply.
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
   * Set the local-space rotation of this node if it is not anchored. If the node is anchored, this
   * call does nothing.
   *
   * @param rotation The rotation to apply.
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

    // Hide the children if the anchor isn't currently tracking.
    if (isTracking != wasTracking) {
      // The children should be enabled if there is no anchor, even though we aren't tracking in
      // that case.
      setChildrenEnabled(isTracking || anchor == null);
    }

    // isTracking already checks if the anchor is null, but we need the anchor null check for
    // static analysis.
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
   * Set the world-space position of this node if it is not anchored. If the node is anchored, this
   * call does nothing.
   *
   * @param position The position to apply.
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
   * Set the world-space rotation of this node if it is not anchored. If the node is anchored, this
   * call does nothing.
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
