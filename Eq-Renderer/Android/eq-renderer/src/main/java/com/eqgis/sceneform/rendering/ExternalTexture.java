package com.eqgis.sceneform.rendering;

import android.graphics.SurfaceTexture;
import android.view.Surface;

import androidx.annotation.Nullable;

import com.eqgis.sceneform.utilities.AndroidPreconditions;
import com.eqgis.sceneform.utilities.Preconditions;
import com.google.android.filament.Stream;
import com.google.android.filament.Texture;
/**
 * 扩展纹理对象
 * <p>
 *     可用于在filament中渲染Android {@link SurfaceTexture} 和 {@link Surface}
 * </p>
 */
public class ExternalTexture {
  private static final String TAG = ExternalTexture.class.getSimpleName();

  @Nullable private final SurfaceTexture surfaceTexture;
  @Nullable private final Surface surface;

  @Nullable private Texture filamentTexture;
  @Nullable private Stream filamentStream;

  /** 构造函数 */
  @SuppressWarnings("initialization")
  public ExternalTexture() {
    // Create the Android surface texture.
    SurfaceTexture surfaceTexture = new SurfaceTexture(0);
    surfaceTexture.detachFromGLContext();
    this.surfaceTexture = surfaceTexture;

    // Create the Android surface.
    surface = new Surface(surfaceTexture);

    // Create the filament stream.
    Stream stream =
            new Stream.Builder()
                    .stream(surfaceTexture).build(EngineInstance.getEngine().getFilamentEngine());

    initialize(stream);
  }

  /** 获取SurfaceTexture对象 */
  public SurfaceTexture getSurfaceTexture() {
    return Preconditions.checkNotNull(surfaceTexture);
  }

  /**
   * 从OpenGL ES纹理中创建一个没有SurfaceTexture的ExternalTexture。
   * 仅供CameraStream创建纹理时使用。
   * @param textureId GL创建的id
   */
  @SuppressWarnings("initialization")
  ExternalTexture(int textureId) {
    // 这个使用场景时，用不到surface和surfaceTexture
    surfaceTexture = null;
    surface = null;

    // Create the filament stream.
    IEngine engine = EngineInstance.getEngine();
    filamentTexture = new Texture.Builder()
            .sampler(Texture.Sampler.SAMPLER_EXTERNAL)
            .importTexture(textureId)
            .build(engine.getFilamentEngine());

    ResourceManager.getInstance()
            .getExternalTextureCleanupRegistry()
            .register(this, new CleanupCallback(filamentTexture, filamentStream));
  }

  /**
   * 获取Surface对象 {@link #getSurfaceTexture()}
   */
  public Surface getSurface() {
    return Preconditions.checkNotNull(surface);
  }

  Texture getFilamentTexture() {
    return Preconditions.checkNotNull(filamentTexture);
  }

  Stream getFilamentStream() {
    return Preconditions.checkNotNull(filamentStream);
  }

  @SuppressWarnings("initialization")
  private void initialize(Stream filamentStream) {
    if (filamentTexture != null) {
      throw new AssertionError("Stream was initialized twice");
    }

    // Create the filament stream.
    IEngine engine = EngineInstance.getEngine();
    this.filamentStream = filamentStream;

    // Create the filament texture.
    filamentTexture =
            new Texture.Builder()
                    .sampler(Texture.Sampler.SAMPLER_EXTERNAL)
                    .format(Texture.InternalFormat.RGB8)
                    .build(engine.getFilamentEngine());

    filamentTexture.setExternalStream(
            engine.getFilamentEngine(),
            filamentStream);
    ResourceManager.getInstance()
            .getExternalTextureCleanupRegistry()
            .register(this, new CleanupCallback(filamentTexture, filamentStream));
  }


  /** Cleanup回调 */
  private static final class CleanupCallback implements Runnable {
    @Nullable private final Texture filamentTexture;
    @Nullable private final Stream filamentStream;

    CleanupCallback(Texture filamentTexture, Stream filamentStream) {
      this.filamentTexture = filamentTexture;
      this.filamentStream = filamentStream;
    }

    @Override
    public void run() {
      AndroidPreconditions.checkUiThread();

      IEngine engine = EngineInstance.getEngine();
      if (engine == null || !engine.isValid()) {
        return;
      }
      if (filamentTexture != null) {
        engine.destroyTexture(filamentTexture);
      }

      if (filamentStream != null) {
        engine.destroyStream(filamentStream);
      }
    }
  }
}
