package com.eqgis.sceneform.utilities;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Looper;

import androidx.annotation.VisibleForTesting;

/**
 * 安卓条件判断工具类
 * <p>内部使用</p>
 * @hide
 */
public class AndroidPreconditions {
  private static final boolean IS_ANDROID_API_AVAILABLE = checkAndroidApiAvailable();
  private static final boolean IS_MIN_ANDROID_API_LEVEL = isMinAndroidApiLevelImpl();
  private static boolean isUnderTesting = false;

  /**
   * 检查是否处于UI线程
   * <pre>
   *     注意：Engine是在SceneView初始化期间创建，而这是在UI线程中操作的。
   *     后续诸如资源加载等一切涉及调用filament的Engine的时候，需要确保线程统一
   *     当然，Engine创建也可以在子线程中操作，这样后续资源加载等操作就可以在子线程操作，
   *     这能有效的避免阻塞UI线程。但现开源的社区版本暂不提供。
   *     若需要请联系我（https://github.com/eqgis/Sceneform-EQR）
   * </pre>
   */
  public static void checkUiThread() {
    if (!isAndroidApiAvailable() || isUnderTesting()) {
      return;
    }

    boolean isOnUIThread = Looper.getMainLooper().getThread() == Thread.currentThread();
    Preconditions.checkState(isOnUIThread, "Must be called from the UI thread.");
  }

  /**
   * 检查所需的最低安卓API
   *
   * @throws IllegalStateException if the api level is not high enough
   */
  public static void checkMinAndroidApiLevel() {
    Preconditions.checkState(isMinAndroidApiLevel(), "Sceneform-EQR requires Android N or later");
  }

  /**
   * 判断当前安卓API是否可用
   */
  public static boolean isAndroidApiAvailable() {
    return IS_ANDROID_API_AVAILABLE;
  }

  /**
   * 判断是否处于测试
   * <p>内部开发使用，方便调试的</p>
   */
  public static boolean isUnderTesting() {
    return isUnderTesting;
  }

  /**
   * 判断安卓API是否高于所需的最低API
   */
  public static boolean isMinAndroidApiLevel() {
    return isUnderTesting() || IS_MIN_ANDROID_API_LEVEL;
  }

  @VisibleForTesting
  public static void setUnderTesting(boolean isUnderTesting) {
    AndroidPreconditions.isUnderTesting = isUnderTesting;
  }

  private static boolean isMinAndroidApiLevelImpl() {
    return !isAndroidApiAvailable() || (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP);
  }

  private static boolean checkAndroidApiAvailable() {
    try {
      Class.forName("android.app.Activity");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
