package com.eqgis.sceneform.utilities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

/**
 * 用于检测和处理Ar Core版本的实用程序。
 * <p>
 *     这是早期scenefrom中判断ARCore Version的工具类。
 *     现已不再使用，使用{@link com.eqgis.ar.ARPlugin}替代
 * </p>
 */
@Deprecated
class ArCoreVersion {
  public static final int VERSION_CODE_1_3 = 180604036;

  private static final String METADATA_KEY_MIN_APK_VERSION = "com.google.ar.core.min_apk_version";

  public static int getMinArCoreVersionCode(Context context) {
    PackageManager packageManager = context.getPackageManager();
    String packageName = context.getPackageName();

    Bundle metadata;
    try {
      metadata =
          packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData;
    } catch (PackageManager.NameNotFoundException e) {
      throw new IllegalStateException("Could not load application package metadata.", e);
    }

    if (metadata.containsKey(METADATA_KEY_MIN_APK_VERSION)) {
      return metadata.getInt(METADATA_KEY_MIN_APK_VERSION);
    } else {
      throw new IllegalStateException(
          "Application manifest must contain meta-data." + METADATA_KEY_MIN_APK_VERSION);
    }
  }
}
