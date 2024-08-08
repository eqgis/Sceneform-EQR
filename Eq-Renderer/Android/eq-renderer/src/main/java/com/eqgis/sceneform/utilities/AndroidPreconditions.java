package com.eqgis.sceneform.utilities;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Looper;

import androidx.annotation.VisibleForTesting;

import com.eqgis.sceneform.rendering.EngineInstance;

/**
 * Helper class for common android specific preconditions used inside of RenderCore.
 *
 * @hide
 */
public class AndroidPreconditions {
  private static final boolean IS_ANDROID_API_AVAILABLE = checkAndroidApiAvailable();
  private static final boolean IS_MIN_ANDROID_API_LEVEL = isMinAndroidApiLevelImpl();
  private static boolean isUnderTesting = false;

  /**
   * 检查是否与引擎创建时的线程一致
   */
  public static void checkEngineThread() {
    if (!isAndroidApiAvailable() || isUnderTesting()) {
      return;
    }

//    boolean isOnUIThread = Thread.currentThread().getName().equals("WorkHandler");
    boolean isWorkHandler = Looper.myLooper() == EngineInstance.getHandler().getLooper();
//    boolean isOnUIThread = Looper.getMainLooper().getThread() == Thread.currentThread();
    Preconditions.checkState(isWorkHandler, "Must be called from the WorkHandler thread.");
  }

  /**
   * Enforce the minimum Android api level
   *
   * @throws IllegalStateException if the api level is not high enough
   */
  public static void checkMinAndroidApiLevel() {
    Preconditions.checkState(isMinAndroidApiLevel(), "Sceneform requires Android N or later");
  }

  /**
   * Returns true if the Android API is currently available. Useful for branching functionality to
   * make it testable via junit. The android API is available for Robolectric tests and android
   * emulator tests.
   */
  public static boolean isAndroidApiAvailable() {
    return IS_ANDROID_API_AVAILABLE;
  }

  public static boolean isUnderTesting() {
    return isUnderTesting;
  }

  /**
   * Returns true if the Android api level is above the minimum or if not on Android.
   *
   * <p>Also returns true if not on Android or in a test.
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
