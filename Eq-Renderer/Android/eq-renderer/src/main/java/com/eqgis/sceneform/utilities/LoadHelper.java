package com.eqgis.sceneform.utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 加载助手
 * <p>
 *     主要用于解析Uri
 * </p>
 *
 * @hide
 */
public class LoadHelper {
  private static final String TAG = LoadHelper.class.getName();
  // From https://developer.android.com/reference/android/content/res/Resources
  // The value 0 is an invalid identifier.
  public static final int INVALID_RESOURCE_IDENTIFIER = 0;
  private static final String RAW_RESOURCE_TYPE = "raw";
  private static final String DRAWABLE_RESOURCE_TYPE = "drawable";

  private static final String LAYOUT_RESOURCE_TYPE = "layout";
  private static final char SLASH_DELIMETER = '/';
  private static final String ANDROID_ASSET = SLASH_DELIMETER + "android_asset" + SLASH_DELIMETER;
  // Default cache size of 512MB.
  private static final long DEFAULT_CACHE_SIZE_BYTES = 512 << 20;

  /** 静态工具类 */
  private LoadHelper() {}

  /** 如果Uri是Android资源则为True，如果是其他Uri则为false。 */
  public static Boolean isAndroidResource(Uri sourceUri) {
    Preconditions.checkNotNull(sourceUri, "Parameter \"sourceUri\" was null.");
    return TextUtils.equals(ContentResolver.SCHEME_ANDROID_RESOURCE, sourceUri.getScheme());
  }

  /** 如果Uri是文件名，则为True，如果是远程路径，则为false。 */
  public static Boolean isFileAsset(Uri sourceUri) {
    Preconditions.checkNotNull(sourceUri, "Parameter \"sourceUri\" was null.");
    @Nullable String scheme = sourceUri.getScheme();
    return TextUtils.isEmpty(scheme) || Objects.equals(ContentResolver.SCHEME_FILE, scheme);
  }

  /**
   * 基于引用Uri规范化Uri。这个函数只是为了方便，因为Uri类也可以这样做。
   */
  public static Uri resolveUri(Uri unresolvedUri, @Nullable Uri parentUri) {

    if (parentUri == null) {
      return unresolvedUri;
    } else {
      return resolve(parentUri, unresolvedUri);
    }
  }

  /**
   * 从Android资源ID创建InputStream。
   * @throws IllegalArgumentException 对于无法加载的资源。
   */
  public static Callable<InputStream> fromResource(Context context, int resId) {
    Preconditions.checkNotNull(context, "Parameter \"context\" was null.");

    String resourceType = context.getResources().getResourceTypeName(resId);
    if (resourceType.equals(RAW_RESOURCE_TYPE) || resourceType.equals(DRAWABLE_RESOURCE_TYPE)) {
      return () -> context.getResources().openRawResource(resId);
    } else {
      throw new IllegalArgumentException(
          "Unknown resource resourceType '"
              + resourceType
              + "' in resId '"
              + context.getResources().getResourceName(resId)
              + "'. Resource will not be loaded");
    }
  }

  /**
   * 根据Uri的内容创建不同的InputStreams
   *
   * @throws IllegalArgumentException 对于无法加载的Uri。
   */
  public static Callable<InputStream> fromUri(Context context, Uri sourceUri) {
    return fromUri(context, sourceUri, null);
  }

  /**
   * 根据Uri的内容创建不同的InputStreams。
   *
   * @param requestProperty 向创建的输入流添加连接属性。
   * @throws IllegalArgumentException 对于无法加载的Uri。
   */
  public static Callable<InputStream> fromUri(
      Context context, Uri sourceUri, @Nullable Map<String, String> requestProperty) {
    Preconditions.checkNotNull(sourceUri, "Parameter \"sourceUri\" was null.");
    Preconditions.checkNotNull(context, "Parameter \"context\" was null.");
    if (isFileAsset(sourceUri)) {
      return fileUriToInputStreamCreator(context, sourceUri);
    } else if (isAndroidResource(sourceUri)) {
      // Note: Prefer creating InputStreams directly from resources.
      // By converting to URIs first, we can't load library resources from a dynamic module.
      return androidResourceUriToInputStreamCreator(context, sourceUri);
    } else if (isGltfDataUri(sourceUri)) {
      return dataUriInputStreamCreator(sourceUri);
    }
    return remoteUriToInputStreamCreator(sourceUri, requestProperty);
  }

  /**
   * 从Android资源生成一个Uri。
   *
   * @throws Resources.NotFoundException
   */
  public static Uri resourceToUri(Context context, int resID) {
    Resources resources = context.getResources();
    return new Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(resources.getResourcePackageName(resID))
        .appendPath(resources.getResourceTypeName(resID))
        .appendPath(resources.getResourceEntryName(resID))
        .build();
  }

  /** 返回raw路径下指定资源名的整数资源id。 */
  public static int rawResourceNameToIdentifier(Context context, String name) {
    return context.getResources().getIdentifier(name, RAW_RESOURCE_TYPE, context.getPackageName());
  }

  /** 返回drawable路径下指定资源名的整数资源id。 */
  public static int drawableResourceNameToIdentifier(Context context, String name) {
    return context
        .getResources()
        .getIdentifier(name, DRAWABLE_RESOURCE_TYPE, context.getPackageName());
  }

  /** 返回layout路径下指定资源名的整数资源id。 */
  public static int layoutResourceNameToIdentifier(Context context, String name) {
    return context
            .getResources()
            .getIdentifier(name, DRAWABLE_RESOURCE_TYPE, context.getPackageName());
  }

  /**
   * 启用HTTP缓存默认设置，远程Uri请求响应缓存到
   * cacheBaseDir / cacheFolderName
   */
  public static void enableCaching(Context context) {
    enableCaching(DEFAULT_CACHE_SIZE_BYTES, context.getCacheDir(), "http_cache");
  }

  /**
   * 启用HTTP缓存，远程Uri请求响应缓存到cacheBaseDir/cacheFolderName
   */
  public static void enableCaching(long cacheByteSize, File cacheBaseDir, String cacheFolderName) {
    // Define the default response cache if it has been previously defined.
    if (HttpResponseCache.getInstalled() == null) {
      try {
        File httpCacheDir = new File(cacheBaseDir, cacheFolderName);
        if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
          HttpResponseCache.install(httpCacheDir, cacheByteSize);
        }
      } catch (IOException e) {
        Log.i(TAG, "HTTP response cache installation failed:" + e);
      }
    }
  }

  public static void flushHttpCache() {
    HttpResponseCache cache = HttpResponseCache.getInstalled();
    if (cache != null) {
      cache.flush();
    }
  }

  /** 创建一个从assests中读取文件的inputStream*/
  // TODO: Fix nullness violation: dereference of possibly-null reference
  // sourceUri.getPath()
  @SuppressWarnings("nullness:dereference.of.nullable")
  private static Callable<InputStream> fileUriToInputStreamCreator(Context context, Uri sourceUri) {
    AssetManager assetManager = context.getAssets();
    String filename;
    if (sourceUri.getAuthority() == null) {
      filename = sourceUri.getPath();
    } else if (sourceUri.getPath().isEmpty()) {
      filename = sourceUri.getAuthority();
    } else {
      filename = sourceUri.getAuthority() + sourceUri.getPath();
    }

    // 移除 "android_asset/" from URI paths like "file:///android_asset/...".
    // TODO: Fix nullness violation: incompatible types in argument.
    @SuppressWarnings("nullness:argument.type.incompatible")
    String scrubbedFilename = removeAndroidAssetPath(filename);

    return () -> {
      if (assetExists(assetManager, scrubbedFilename)) {
        // Open Android Asset if an Asset was found
        return assetManager.open(scrubbedFilename);
      } else {
        // Open file from storage or other non asset location.
        return new FileInputStream(new File(filename));
      }
    };
  }

  private static String removeAndroidAssetPath(String filename) {
    // Remove "android_asset/" from URI paths like "file:///android_asset/...".
    String scrubbedFilename = filename;
    if (filename.startsWith(ANDROID_ASSET)) {
      scrubbedFilename = filename.substring(ANDROID_ASSET.length());
    }
    return scrubbedFilename;
  }

  /**
   * 创建InputStream
   *
   * @throws IllegalArgumentException 对于无法加载的Uri。
   */
  // TODO: incompatible types in return.
  @SuppressWarnings("nullness:return.type.incompatible")
  private static Callable<InputStream> androidResourceUriToInputStreamCreator(
      Context context, Uri sourceUri) {
    String sourceUriPath = sourceUri.getPath();
    // TODO: Fix nullness violation: dereference of possibly-null reference
    // sourceUriPath
    @SuppressWarnings("nullness:dereference.of.nullable")
    int lastSlashIndex = sourceUriPath.lastIndexOf(SLASH_DELIMETER);
    String resourceType = sourceUriPath.substring(1, lastSlashIndex);

    if (resourceType.equals(RAW_RESOURCE_TYPE) || resourceType.equals(DRAWABLE_RESOURCE_TYPE)) {
      return () -> context.getContentResolver().openInputStream(sourceUri);
    } else {
      throw new IllegalArgumentException(
          "Unknown resource resourceType '"
              + resourceType
              + "' in uri '"
              + sourceUri
              + "'. Resource will not be loaded");
    }
  }

  /**
   * 创建InputStream
   *
   * @throws IllegalArgumentException 对于无法加载的Uri。
   */
  private static Callable<InputStream> remoteUriToInputStreamCreator(
      Uri sourceUri, @Nullable Map<String, String> requestProperty) {
    try {
      URL sourceURL = new URL(sourceUri.toString());
      URLConnection conn = sourceURL.openConnection();
      // Apply properties to the connection if they are available.
      if (requestProperty != null) {
        for (Map.Entry<String, String> entry : requestProperty.entrySet()) {
          conn.addRequestProperty(entry.getKey(), entry.getValue());
        }
      }
      return () -> conn.getInputStream();
    } catch (MalformedURLException ex) {
      // This is rare. Most bad URL's get filtered out when the URL class is constructed.
      throw new IllegalArgumentException("Unable to parse url: \'" + sourceUri + "'", ex);
    } catch (IOException e) {
      throw new AssertionError("Error opening url connection: '" + sourceUri + "'", e);
    }
  }

  private static Uri resolve(Uri parent, Uri child) {
    try {
      URI javaParentUri = new URI(parent.toString());
      URI javaChildUri = new URI(child.toString());
      URI resolvedUri = javaParentUri.resolve(javaChildUri);
      return Uri.parse(resolvedUri.toString());
    } catch (URISyntaxException ex) {
      throw new IllegalArgumentException("Unable to parse Uri.", ex);
    }
  }

  private static boolean assetExists(AssetManager assetManager, String assetRelativePath)
      throws IOException {
    String targetAssetName;
    String[] assetsInSameDirectory;
    int lastSlashIndex = assetRelativePath.lastIndexOf(SLASH_DELIMETER);

    if (lastSlashIndex != -1) {
      targetAssetName = assetRelativePath.substring(lastSlashIndex + 1);
      assetsInSameDirectory = assetManager.list(assetRelativePath.substring(0, lastSlashIndex));
    } else {
      targetAssetName = assetRelativePath;
      assetsInSameDirectory = assetManager.list("");
    }

    if (assetsInSameDirectory != null) {
      // Search for Android Asset in given directory.
      for (String assetName : assetsInSameDirectory) {
        if (targetAssetName.equals(assetName)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isDataUri(Uri uri) {
    String scheme = uri.getScheme();
    return scheme != null && scheme.equals("data");
  }

  public static boolean isGltfDataUri(Uri uri) {
    if (!isDataUri(uri)) {
      return false;
    } else {
      return getGltfExtensionFromSchemeSpecificPart(uri.getSchemeSpecificPart()) != null;
    }
  }

  @Nullable
  private static String getGltfExtensionFromSchemeSpecificPart(String schemeSpecificPart) {
    if (schemeSpecificPart.startsWith("model/gltf-binary")) {
      return "glb";
    }
    if (schemeSpecificPart.startsWith("model/gltf+json")) {
      return "gltf";
    }
    return null;
  }

  /**
   * 创建InputStream
   *
   * @throws IllegalArgumentException 对于无法加载的Uri。
   */
  private static Callable<InputStream> dataUriInputStreamCreator(Uri uri) {
    String data = uri.getSchemeSpecificPart();
    int commaIndex = data.indexOf(',');
    if (commaIndex < 0) {
      throw new IllegalArgumentException("Malformed data uri - does not contain a ','");
    }
    String prefix = data.substring(0, commaIndex);
    boolean isBase64 = prefix.contains(";base64");
    String dataString = data.substring(commaIndex + 1);
    return () ->
        new ByteArrayInputStream(
            isBase64 ? Base64.decode(dataString, Base64.DEFAULT) : dataString.getBytes());
  }

  public static String getLastPathSegment(Uri uri) {
    if (isGltfDataUri(uri)) {
      return "file." + getGltfExtensionFromSchemeSpecificPart(uri.getSchemeSpecificPart());
    } else {
      String lastPathSegment = uri.getLastPathSegment();
      if (lastPathSegment == null) {
        // This could be a file:// uri, e.g. if it's loaded out of assets.
        String uriString = uri.toString();
        lastPathSegment = uriString.substring(uriString.lastIndexOf('/') + 1);
      }
      return lastPathSegment;
    }
  }
}
