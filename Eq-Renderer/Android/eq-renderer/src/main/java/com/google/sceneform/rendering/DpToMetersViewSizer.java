package com.google.sceneform.rendering;

import android.view.View;

import com.google.sceneform.math.Vector3;
import com.google.sceneform.Scene;
import com.google.sceneform.utilities.Preconditions;

/**
 * View尺寸转换工具
 * <pre>
 *     控制{@link Scene} by中{@link ViewRenderable}的大小
 *     定义每米有多少dp(与密度无关的像素)。当使用使用dp构建的android布局时，建议这样做。
 * </pre>
 *
 * @see ViewRenderable.Builder#setSizer(ViewSizer)
 * @see ViewRenderable#setSizer(ViewSizer)
 */

public class DpToMetersViewSizer implements ViewSizer {
  private final int dpPerMeters;

  //默认值转换比例，250dp = 1米
  public static final int DEFAULT_DP_TO_METERS = 250;

  // Defaults to zero, Z value of the size doesn't currently have any semantic meaning,
  // but we may add that in later if we support ViewRenderables that have depth.
  private static final float DEFAULT_SIZE_Z = 0.0f;

  /**
   * 构造函数
   * @param dpPerMeters 转换比例（1米表示多少dp值）
   */
  public DpToMetersViewSizer(int dpPerMeters) {
    if (dpPerMeters <= 0) {
      throw new IllegalArgumentException("dpPerMeters must be greater than zero.");
    }

    this.dpPerMeters = dpPerMeters;
  }

  /**
   * 获取转换系数
   * <p>
   *     世界坐标系下的1米表示的dp值
   * </p>
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
   * @param width_meter 宽度，单位：米
   * @param height_meter 高度，单位：米
   * @return
   */
  public Vector3 getViewPx(float width_meter,float height_meter){
    float widthDp=width_meter*dpPerMeters;
    float heightDp=height_meter*dpPerMeters;
    return new Vector3(ViewRenderableHelpers.convertDpToPx(widthDp),ViewRenderableHelpers.convertDpToPx(heightDp),DEFAULT_SIZE_Z);
  }

  /**
   * 米转像素
   * @param px 像素值
   * @return
   */
  public float convertPxToMeter(int px){
    float dp = ViewRenderableHelpers.convertPxToDp(px);
    return dp / dpPerMeters;
  }

  /**
   * 像素转米
   * @param meter 世界坐标系下的长度值
   * @return
   */
  public float convertMeterToPx(float meter){
    float dp=meter*dpPerMeters;
    return ViewRenderableHelpers.convertDpToPx(dp);
  }
}
