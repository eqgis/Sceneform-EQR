package com.google.sceneform.rendering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.sceneform.resources.ResourceRegistry;
import com.google.sceneform.utilities.AndroidPreconditions;
import com.google.sceneform.utilities.LoadHelper;
import com.google.sceneform.utilities.Preconditions;
import com.google.android.filament.android.TextureHelper;
import com.google.ar.core.annotations.UsedByNative;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/** 纹理对象
 *
 * */
@SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"}) // CompletableFuture
@RequiresApi(api = Build.VERSION_CODES.N)
@UsedByNative("material_java_wrappers.h")
public class Texture {
  private static final String TAG = Texture.class.getSimpleName();

  /** 纹理使用类型。 */
  public enum Usage {
    /** 颜色贴图 */
    COLOR,
    /** 纹理包含法线贴图 */
    NORMAL,
    /** 纹理包含任意的数据 */
    DATA
  }

  //将mipCount设置为最大数量的级别，filament将根据需要夹紧它。
  //这将确保所有的mip级别被填满，直到1x1。
  private static final int MIP_LEVELS_TO_GENERATE = 0xff;

  @Nullable private final TextureInternalData textureData;

  /** 构建一个默认纹理对象 */
  public static Builder builder() {
    AndroidPreconditions.checkMinAndroidApiLevel();

    return new Builder();
  }

  @SuppressWarnings({"initialization"})
  @UsedByNative("material_java_wrappers.h")
  private Texture(TextureInternalData textureData) {
    this.textureData = textureData;
    textureData.retain();
    ResourceManager.getInstance()
            .getTextureCleanupRegistry()
            .register(this, new CleanupCallback(textureData));
  }

  Sampler getSampler() {
    return Preconditions.checkNotNull(textureData).getSampler();
  }

  /**
   * 获取filament的纹理对象
   * @hide
   */
  com.google.android.filament.Texture getFilamentTexture() {
    return Preconditions.checkNotNull(textureData).getFilamentTexture();
  }

  private static com.google.android.filament.Texture.InternalFormat getInternalFormatForUsage(
          Usage usage) {
    com.google.android.filament.Texture.InternalFormat format;

    switch (usage) {
      case COLOR:
        format = com.google.android.filament.Texture.InternalFormat.SRGB8_A8;
        break;
      case NORMAL:
      case DATA:
      default:
        format = com.google.android.filament.Texture.InternalFormat.RGBA8;
        break;
    }
    return format;
  }

  /** 建造者模式 */
  public static final class Builder {
    /** {@link Texture}将从这个可调用对象的内容构造 */
    @Nullable private Callable<InputStream> inputStreamCreator = null;

    @Nullable private Bitmap bitmap = null;
    @Nullable private TextureInternalData textureInternalData = null;

    private Usage usage = Usage.COLOR;
    /** 可通过注册表启用重用 */
    @Nullable private Object registryId = null;

    private boolean inPremultiplied = true;

    private Sampler sampler = Sampler.builder().build();

    private static final int MAX_BITMAP_SIZE = 4096;

    /**构造函数*/
    private Builder() {}

    /**
     * 设置数据源
     * @param sourceUri URI
     */
    public Builder setSource(Context context, Uri sourceUri) {
      Preconditions.checkNotNull(sourceUri, "Parameter \"sourceUri\" was null.");

      registryId = sourceUri;
      setSource(LoadHelper.fromUri(context, sourceUri));
      return this;
    }

    /**
     * 设置数据源
     * @param inputStreamCreator Callable<InputStream>
     */
    public Builder setSource(Callable<InputStream> inputStreamCreator) {
      Preconditions.checkNotNull(inputStreamCreator, "Parameter \"inputStreamCreator\" was null.");

      this.inputStreamCreator = inputStreamCreator;
      bitmap = null;
      return this;
    }

    /**
     * 设置数据源
     * @param resource 资源Id
     */
    public Builder setSource(Context context, int resource) {
      setSource(LoadHelper.fromResource(context, resource));
      registryId = context.getResources().getResourceName(resource);
      return this;
    }

    /**
     * 设置数据源
     *
     * <p>位图必须满足以下条件才能被使用:
     *
     * <ul>
     *     <li>{@link Bitmap#getConfig()}必须是{@link Bitmap.Config #ARGB_8888}。
     *     <li>{@link Bitmap# ispremultiply()}必须为真。
     *     <li>宽度和高度必须小于4096像素。
     * </ul>
     *
     * @param bitmap {@link Bitmap} source of texture data
     * @throws IllegalArgumentException if the bitmap isn't valid
     */
    public Builder setSource(Bitmap bitmap) {
      Preconditions.checkNotNull(bitmap, "Parameter \"bitmap\" was null.");

      if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
        throw new IllegalArgumentException(
                "Invalid Bitmap: Bitmap's configuration must be "
                        + "ARGB_8888, but it was "
                        + bitmap.getConfig());
      }

      if (bitmap.hasAlpha() && !bitmap.isPremultiplied()) {
        throw new IllegalArgumentException("Invalid Bitmap: Bitmap must be premultiplied.");
      }

//      if (bitmap.getWidth() > MAX_BITMAP_SIZE || bitmap.getHeight() > MAX_BITMAP_SIZE) {
//        throw new IllegalArgumentException(
//                "Invalid Bitmap: Bitmap width and height must be "
//                        + "smaller than 4096. Bitmap was "
//                        + bitmap.getWidth()
//                        + " width and "
//                        + bitmap.getHeight()
//                        + " height.");
//      }

      this.bitmap = bitmap;
      // TODO: don't overwrite calls to setRegistryId
      registryId = null;
      inputStreamCreator = null;
      return this;
    }

    /**
     * 直接设置纹理的内部数据。
     * @hide
     */
    public Builder setData(TextureInternalData textureInternalData) {
      this.textureInternalData = textureInternalData;
      return this;
    }

    /**
     * 通过{@link InputStream}加载的纹理是否应该使用预乘alpha加载。
     *
     * @param inPremultiplied 通过{@link InputStream}加载的纹理是否应该用预乘alpha加载。默认值为true。
     */
    Builder setPremultiplied(boolean inPremultiplied) {
      this.inPremultiplied = inPremultiplied;
      return this;
    }

    /**
     * 允许{@link Texture}被重用。如果registryId是非空的，它将被保存在一个注册表中，并且注册表将在构建之前检查这个id。
     * @param registryId 允许跳过该函数并重用之前的纹理。
     */
    public Builder setRegistryId(Object registryId) {
      this.registryId = registryId;
      return this;
    }

    /**
     * 将{@link Texture}标记为包含颜色、正常或任意数据。颜色是默认的。
     *
     * @param usage 设置{@link Texture}中的数据类型。
     */
    public Builder setUsage(Usage usage) {
      this.usage = usage;
      return this;
    }

    /**
     * 设置采样器参数
     * @param sampler Controls appearance of the {@link Texture}
     */
    public Builder setSampler(Sampler sampler) {
      this.sampler = sampler;
      return this;
    }

    /**
     * 创建
     */
    public CompletableFuture<Texture> build() {
      AndroidPreconditions.checkUiThread();
      Object registryId = this.registryId;
      if (registryId != null) {
        // See if a texture has already been registered by this id, if so re-use it.
        ResourceRegistry<Texture> registry = ResourceManager.getInstance().getTextureRegistry();
        @Nullable CompletableFuture<Texture> textureFuture = registry.get(registryId);
        if (textureFuture != null) {
          return textureFuture;
        }
      }

      if (textureInternalData != null && registryId != null) {
        throw new IllegalStateException("Builder must not set both a bitmap and filament texture");
      }

      CompletableFuture<Texture> result;
      if (this.textureInternalData != null) {
        result = CompletableFuture.completedFuture(new Texture(this.textureInternalData));
      } else {
        CompletableFuture<Bitmap> bitmapFuture;
        if (inputStreamCreator != null) {
          bitmapFuture = makeBitmap(inputStreamCreator, inPremultiplied);
        } else if (bitmap != null) {
          bitmapFuture = CompletableFuture.completedFuture(bitmap);
        } else {
          throw new IllegalStateException("Texture must have a source.");
        }

        result =
                bitmapFuture.thenApplyAsync(
                        loadedBitmap -> {
                          TextureInternalData textureData =
                                  makeTextureData(loadedBitmap, sampler, usage, MIP_LEVELS_TO_GENERATE);
                          return new Texture(textureData);
                        },
                        ThreadPools.getMainExecutor());
      }

      if (registryId != null) {
        ResourceRegistry<Texture> registry = ResourceManager.getInstance().getTextureRegistry();
        registry.register(registryId, result);
      }

      FutureHelper.logOnException(
              TAG, result, "Unable to load Texture registryId='" + registryId + "'");
      return result;
    }

    private static CompletableFuture<Bitmap> makeBitmap(
            Callable<InputStream> inputStreamCreator, boolean inPremultiplied) {
      return CompletableFuture.supplyAsync(
              () -> {
                // Read the texture file.
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                options.inPremultiplied = inPremultiplied;
                Bitmap bitmap;

                // Open and read the texture file.
                try (InputStream inputStream = inputStreamCreator.call()) {
                  bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                } catch (Exception e) {
                  throw new IllegalStateException(e);
                }

                if (bitmap == null) {
                  throw new IllegalStateException(
                          "Failed to decode the texture bitmap. The InputStream was not a valid bitmap.");
                }

                if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                  throw new IllegalStateException("Texture must use ARGB8 format.");
                }

                return bitmap;
              },
              ThreadPools.getThreadPoolExecutor());
    }

    private static TextureInternalData makeTextureData(
            Bitmap bitmap, Sampler sampler, Usage usage, int mipLevels) {
      IEngine engine = EngineInstance.getEngine();

      // Due to fun ambiguities between Texture (RenderCore) and Texture (Filament)
      // Texture references must be fully qualified giving rise to the following monstrosity
      // of verbosity.
      final com.google.android.filament.Texture.InternalFormat textureInternalFormat =
              getInternalFormatForUsage(usage);
      final com.google.android.filament.Texture.Sampler textureSampler =
              com.google.android.filament.Texture.Sampler.SAMPLER_2D;

      com.google.android.filament.Texture filamentTexture =
              new com.google.android.filament.Texture.Builder()
                      .width(bitmap.getWidth())
                      .height(bitmap.getHeight())
                      .depth(1)
                      .levels(mipLevels)
                      .sampler(textureSampler)
                      .format(textureInternalFormat)
                      .build(engine.getFilamentEngine());

      TextureHelper.setBitmap(engine.getFilamentEngine(), filamentTexture, 0, bitmap);

      if (mipLevels > 1) {
        filamentTexture.generateMipmaps(engine.getFilamentEngine());
      }

      return new TextureInternalData(filamentTexture, sampler);
    }
  }

  // LINT.IfChange(api)
  /** 采样设置 */
  @UsedByNative("material_java_wrappers.h")
  public static class Sampler {
    /** Options for Minification Filter function. */
    @UsedByNative("material_java_wrappers.h")
    public enum MinFilter {
      @UsedByNative("material_java_wrappers.h")
      NEAREST,
      @UsedByNative("material_java_wrappers.h")
      LINEAR,
      @UsedByNative("material_java_wrappers.h")
      NEAREST_MIPMAP_NEAREST,
      @UsedByNative("material_java_wrappers.h")
      LINEAR_MIPMAP_NEAREST,
      @UsedByNative("material_java_wrappers.h")
      NEAREST_MIPMAP_LINEAR,
      @UsedByNative("material_java_wrappers.h")
      LINEAR_MIPMAP_LINEAR
    }

    /** Options for Magnification Filter function. */
    @UsedByNative("material_java_wrappers.h")
    public enum MagFilter {
      @UsedByNative("material_java_wrappers.h")
      NEAREST,
      @UsedByNative("material_java_wrappers.h")
      LINEAR
    }

    /** Options for Wrap Mode function. */
    @UsedByNative("material_java_wrappers.h")
    public enum WrapMode {
      @UsedByNative("material_java_wrappers.h")
      CLAMP_TO_EDGE,
      @UsedByNative("material_java_wrappers.h")
      REPEAT,
      @UsedByNative("material_java_wrappers.h")
      MIRRORED_REPEAT
    }

    private final MinFilter minFilter;
    private final MagFilter magFilter;
    private final WrapMode wrapModeS;
    private final WrapMode wrapModeT;
    private final WrapMode wrapModeR;

    private Sampler(Builder builder) {
      this.minFilter = builder.minFilter;
      this.magFilter = builder.magFilter;
      this.wrapModeS = builder.wrapModeS;
      this.wrapModeT = builder.wrapModeT;
      this.wrapModeR = builder.wrapModeR;
    }

    /**
     * Get the minifying function used whenever the level-of-detail function determines that the
     * texture should be minified.
     */
    public MinFilter getMinFilter() {
      return minFilter;
    }

    /**
     * Get the magnification function used whenever the level-of-detail function determines that the
     * texture should be magnified.
     */
    public MagFilter getMagFilter() {
      return magFilter;
    }

    /**
     * Get the wrap mode for texture coordinate S. The wrap mode determines how a texture is
     * rendered for uv coordinates outside the range of [0, 1].
     */
    public WrapMode getWrapModeS() {
      return wrapModeS;
    }

    /**
     * Get the wrap mode for texture coordinate T. The wrap mode determines how a texture is
     * rendered for uv coordinates outside the range of [0, 1].
     */
    public WrapMode getWrapModeT() {
      return wrapModeT;
    }

    /**
     * Get the wrap mode for texture coordinate R. The wrap mode determines how a texture is
     * rendered for uv coordinates outside the range of [0, 1].
     */
    public WrapMode getWrapModeR() {
      return wrapModeR;
    }

    public static Builder builder() {
      return new Builder()
              .setMinFilter(MinFilter.LINEAR_MIPMAP_LINEAR)
              .setMagFilter(MagFilter.LINEAR)
              .setWrapMode(WrapMode.CLAMP_TO_EDGE);
    }

    /** Builder for constructing Sampler objects. */
    public static class Builder {
      private MinFilter minFilter;
      private MagFilter magFilter;
      private WrapMode wrapModeS;
      private WrapMode wrapModeT;
      private WrapMode wrapModeR;

      /** Set both the texture minifying function and magnification function. */
      public Builder setMinMagFilter(MagFilter minMagFilter) {
        return setMinFilter(MinFilter.values()[minMagFilter.ordinal()]).setMagFilter(minMagFilter);
      }

      /**
       * Set the minifying function used whenever the level-of-detail function determines that the
       * texture should be minified.
       */
      public Builder setMinFilter(MinFilter minFilter) {
        this.minFilter = minFilter;
        return this;
      }

      /**
       * Set the magnification function used whenever the level-of-detail function determines that
       * the texture should be magnified.
       */
      public Builder setMagFilter(MagFilter magFilter) {
        this.magFilter = magFilter;
        return this;
      }

      /**
       * Set the wrap mode for all texture coordinates. The wrap mode determines how a texture is
       * rendered for uv coordinates outside the range of [0, 1].
       */
      public Builder setWrapMode(WrapMode wrapMode) {
        return setWrapModeS(wrapMode).setWrapModeT(wrapMode).setWrapModeR(wrapMode);
      }

      /**
       * Set the wrap mode for texture coordinate S. The wrap mode determines how a texture is
       * rendered for uv coordinates outside the range of [0, 1].
       */
      public Builder setWrapModeS(WrapMode wrapMode) {
        wrapModeS = wrapMode;
        return this;
      }

      /**
       * Set the wrap mode for texture coordinate T. The wrap mode determines how a texture is
       * rendered for uv coordinates outside the range of [0, 1].
       */
      public Builder setWrapModeT(WrapMode wrapMode) {
        wrapModeT = wrapMode;
        return this;
      }

      /**
       * Set the wrap mode for texture coordinate R. The wrap mode determines how a texture is
       * rendered for uv coordinates outside the range of [0, 1].
       */
      public Builder setWrapModeR(WrapMode wrapMode) {
        wrapModeR = wrapMode;
        return this;
      }

      /** Construct a Sampler from the properties of the Builder. */
      public Sampler build() {
        return new Sampler(this);
      }
    }
  }
  // LINT.ThenChange(
  //     //depot/google3/third_party/arcore/ar/sceneform/loader/model/material_java_wrappers.h:api
  // )

  /** Cleanup回调 */
  private static final class CleanupCallback implements Runnable {
    private final TextureInternalData textureData;

    CleanupCallback(TextureInternalData textureData) {
      this.textureData = textureData;
    }

    @Override
    public void run() {
      AndroidPreconditions.checkUiThread();
      if (textureData != null) {
        textureData.release();
      }
    }
  }
}
