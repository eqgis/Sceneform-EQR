package com.google.sceneform.rendering;

import android.content.Context;
import android.opengl.EGLContext;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.eqgis.exception.DeviceNotSupportException;
import com.eqgis.exception.NotSupportException;
import com.google.sceneform.utilities.Preconditions;
import com.google.android.filament.Engine;
import com.google.android.filament.Filament;
import com.google.android.filament.gltfio.Gltfio;

/**
 * Filament Engine实例
 *
 * @hide
 */
public class EngineInstance {
  @Nullable private static IEngine engine = null;
  @Nullable private static EGLContext glContext = null;
  private static boolean headlessEngine = false;
  private static boolean filamentInitialized = false;
  private static Engine.Backend backend = Engine.Backend.OPENGL; // 默认 OpenGL ES

  public static void enableHeadlessEngine() {
    headlessEngine = true;
  }

  public static void disableHeadlessEngine() {
    headlessEngine = false;
  }

  public static boolean isHeadlessMode() {
    return headlessEngine;
  }

    /**
     * 启用Vulkan
     * <p>
     *     默认为OpenGL
     *     注意：不可用Vulkan的情况
     *     使用{@link ExternalTexture}、{@link CameraStream}、{@link  com.google.sceneform.ArSceneView}
     * </p>
     * @param context Context
     */
  public static void enableVulkan(Context context) throws DeviceNotSupportException {
      if (checkDeviceSupportsVulkan(context)){
          backend = Engine.Backend.VULKAN;
      }
      throw new DeviceNotSupportException("not support Vulkan");
  }

  /**
   * 获取filament引擎实例
   * <p>
   *     若没有则创建
   * </p>
   *
   * @throws IllegalStateException
   */
  public static IEngine getEngine() {
    if (!headlessEngine) {
      createEngine();
    } else {
      createHeadlessEngine();
    }
    if (engine == null) {
      throw new IllegalStateException("Filament Engine creation has failed.");
    }
    return engine;
  }


  private static Engine createSharedFilamentEngine() {return null;}







  private static Engine createFilamentEngine() {
    Engine result = createSharedFilamentEngine();
    if (result == null) {
        if (backend != Engine.Backend.OPENGL){
            result = Engine.create(backend);
        }else {
            glContext = GLHelper.makeContext();
            result = Engine.create(glContext);
        }
    }
    return result;
  }


  private static boolean destroySharedFilamentEngine() {return false;}




  private static void destroyFilamentEngine() {
    if (engine != null) {
      if (headlessEngine || !destroySharedFilamentEngine()) {
//        if (glContext != null) {
//          GLHelper.destroyContext(glContext);
//          glContext = null;
//        }
        Preconditions.checkNotNull(engine).destroy();
      }
      engine = null;
      filamentInitialized = false;
    }
  }


  private static boolean loadUnifiedJni() {return false;}




  private static void gltfioInit() {
    Gltfio.init();
    filamentInitialized = true;
  }

  /**
   * Create the engine and GL Context if they have not been created yet.
   *
   * @throws IllegalStateException
   */
  private static void createEngine() {
    if (engine == null) {

//      if (!filamentInitialized) {
//        try {
//          gltfioInit();
//          //加载工具so
//          System.loadLibrary("filament-utils-jni");
//        } catch (UnsatisfiedLinkError err) {
//          // Fallthrough and allow regular Filament to initialize.
//        }
//      }
//      if (!filamentInitialized) {
//        try {
//          Filament.init();
//          filamentInitialized = true;
//        } catch (UnsatisfiedLinkError err) {
//          // For Scene Viewer Filament's jni is included in another lib, try that before failing.
//          if (loadUnifiedJni()) {
//            filamentInitialized = true;
//          } else {
//            throw err;
//          }
//        }
//      }

      engine = new FilamentEngineWrapper(createFilamentEngine());

      // Validate that the Engine and GL Context are valid.
      if (engine == null) {
        throw new IllegalStateException("Filament Engine creation has failed.");
      }
    }
  }

  /** Create a Swiftshader engine for testing. */
  private static void createHeadlessEngine() {
    if (engine == null) {
      try {
        engine = new HeadlessEngineWrapper();
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException("Filament Engine creation failed due to reflection error", e);
      }
      if (engine == null) {
        throw new IllegalStateException("Filament Engine creation has failed.");
      }
    }
  }

  public static void destroyEngine() {
    destroyFilamentEngine();
  }

  public static boolean isEngineDestroyed() {
    return engine == null;
  }

//  private static native Object nCreateEngine();
//
//  private static native void nDestroyEngine();

    /**
     * 判断设备是否支持 Vulkan
     *
     * @return true 支持 Vulkan，false 不支持
     */
    public static boolean checkDeviceSupportsVulkan(Context context) {
        // Android 7.0+ 才支持 Vulkan
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false;
        }

        // 查询系统是否存在 Vulkan feature
        if (context.getPackageManager().hasSystemFeature("android.hardware.vulkan.level")) {
            return true;
        }

        return false;
    }
}
