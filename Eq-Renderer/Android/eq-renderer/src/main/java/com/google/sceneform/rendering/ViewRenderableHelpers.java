package com.google.sceneform.rendering;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;


/**
 * View渲染对象工具类
 * <p>
 *     用于在世界空间中呈现视图的实用程序函数的Helper类。
 * </p>
 * */
public class ViewRenderableHelpers {
  static boolean USE_PIXEL = true;

  /** 获取View的宽高比 */
  static float getAspectRatio(View view) {
    float viewWidth = (float) view.getWidth();
    float viewHeight = (float) view.getHeight();

    if (viewWidth == 0.0f || viewHeight == 0.0f) {
      return 0.0f;
    }

    return viewWidth / viewHeight;
  }

  /**
   * 将px转换为dp
   * @param px px值
   * @return dp值
   */
  public static float convertPxToDp(int px) {
    if (USE_PIXEL)return px / 2.0f;//1m = 500px
    DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
    return px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT);
  }

  /**
   * 将dp转换为px
   * @param dp dp值
   * @return px值
   */
  public static int convertDpToPx(float dp) {
    if (USE_PIXEL)return (int) (dp * 2.0f);//1m = 500px
    DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
    return (int) (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
  }
}
