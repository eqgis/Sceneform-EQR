package com.eqgis.sceneform.rendering;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;


/** Helper class for utility functions for a view rendered in world space. */

public class ViewRenderableHelpers {
  static boolean USE_PIXEL = true;

  /** Returns the aspect ratio of a view (width / height). */
  static float getAspectRatio(View view) {
    float viewWidth = (float) view.getWidth();
    float viewHeight = (float) view.getHeight();

    if (viewWidth == 0.0f || viewHeight == 0.0f) {
      return 0.0f;
    }

    return viewWidth / viewHeight;
  }

  /**
   * Returns the number of density independent pixels that a given number of pixels is equal to on
   * this device.
   */
  public static float convertPxToDp(int px) {
    if (USE_PIXEL)return px / 2.0f;//1m = 500px
    DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
    return px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT);
  }
  public static int convertDpToPx(float dp) {
    if (USE_PIXEL)return (int) (dp * 2.0f);//1m = 500px
    DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
    return (int) (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
  }
}
