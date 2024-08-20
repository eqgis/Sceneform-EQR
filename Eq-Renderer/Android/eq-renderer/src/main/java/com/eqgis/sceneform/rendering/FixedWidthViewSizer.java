package com.eqgis.sceneform.rendering;

import android.view.View;

import com.eqgis.sceneform.math.Vector3;
import com.eqgis.sceneform.Scene;
import com.eqgis.sceneform.utilities.Preconditions;

/**
 * View宽度修改工具
 * <p>
 *     控制{@link Scene} by中{@link ViewRenderable}的大小
 *     定义它的宽度，以米为单位。宽度将改变以匹配视图的长宽比。
 * </p>
 *
 * @see ViewRenderable.Builder#setSizer(ViewSizer)
 * @see ViewRenderable#setSizer(ViewSizer)
 */

public class FixedWidthViewSizer implements ViewSizer {
  private final float widthMeters;

  // Defaults to zero, Z value of the size doesn't currently have any semantic meaning,
  // but we may add that in later if we support ViewRenderables that have depth.
  private static final float DEFAULT_SIZE_Z = 0.0f;

  /**
   * 构造函数
   *
   * @param widthMeters 宽度，单位：米
   */
  public FixedWidthViewSizer(float widthMeters) {
    if (widthMeters <= 0) {
      throw new IllegalArgumentException("widthMeters must be greater than zero.");
    }

    this.widthMeters = widthMeters;
  }

  /** 获取宽度值 */
  public float getWidth() {
    return widthMeters;
  }

  @Override
  public Vector3 getSize(View view) {
    Preconditions.checkNotNull(view, "Parameter \"view\" was null.");

    float aspectRatio = ViewRenderableHelpers.getAspectRatio(view);

    if (aspectRatio == 0.0f) {
      return Vector3.zero();
    }

    return new Vector3(widthMeters, widthMeters / aspectRatio, DEFAULT_SIZE_Z);
  }
}
