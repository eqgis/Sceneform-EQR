package com.google.ar.sceneform.rendering;

import android.view.View;

import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.utilities.Preconditions;

/**
 * Controls the size of a {@link ViewRenderable} in a {@link Scene} by
 * defining how many dp (density-independent pixels) there are per meter. This is recommended when
 * using an android layout that is built using dp.
 *
 * @see ViewRenderable.Builder#setSizer(ViewSizer)
 * @see ViewRenderable#setSizer(ViewSizer)
 */

public class DpToMetersViewSizer implements ViewSizer {
  private final int dpPerMeters;
  public static final int DEFAULT_DP_TO_METERS = 250;

  // Defaults to zero, Z value of the size doesn't currently have any semantic meaning,
  // but we may add that in later if we support ViewRenderables that have depth.
  private static final float DEFAULT_SIZE_Z = 0.0f;

  /**
   * Constructor for creating a sizer for controlling the size of a {@link ViewRenderable} by
   * defining how many dp there are per meter.
   *
   * @param dpPerMeters a number greater than zero representing the ratio of dp to meters
   */
  public DpToMetersViewSizer(int dpPerMeters) {
    if (dpPerMeters <= 0) {
      throw new IllegalArgumentException("dpPerMeters must be greater than zero.");
    }

    this.dpPerMeters = dpPerMeters;
  }

  /**
   * Returns the number of dp (density-independent pixels) there are per meter that is used for
   * controlling the size of a {@link ViewRenderable}.
   */
  public int getDpPerMeters() {
    return dpPerMeters;
  }

  @Override
  public Vector3 getSize(View view) {
    Preconditions.checkNotNull(view, "Parameter \"view\" was null.");

    float widthDp = ViewRenderableHelpers.convertPxToDp(view.getWidth());
    float heightDp = ViewRenderableHelpers.convertPxToDp(view.getHeight());

    return new Vector3(widthDp / dpPerMeters, heightDp / dpPerMeters, DEFAULT_SIZE_Z);
  }

  /**
   * 获取二维视图从米到像素的转换
   * @param width
   * @param height
   * @return
   */
  public Vector3 getViewPx(float width_meter,float height_meter){
    float widthDp=width_meter*dpPerMeters;
    float heightDp=height_meter*dpPerMeters;
    return new Vector3(ViewRenderableHelpers.convertDpToPx(widthDp),ViewRenderableHelpers.convertDpToPx(heightDp),DEFAULT_SIZE_Z);
  }

  /**
   * 米转像素
   * @param px
   * @return
   */
  public float convertPxToMeter(int px){
    float dp = ViewRenderableHelpers.convertPxToDp(px);
    return dp / dpPerMeters;
  }

  /**
   * 像素转米
   * @param meter
   * @return
   */
  public float convertMeterToPx(float meter){
    float dp=meter*dpPerMeters;
    return ViewRenderableHelpers.convertDpToPx(dp);
  }
}
