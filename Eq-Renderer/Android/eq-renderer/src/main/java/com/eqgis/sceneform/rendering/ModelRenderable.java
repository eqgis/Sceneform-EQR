package com.eqgis.sceneform.rendering;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.eqgis.sceneform.resources.ResourceRegistry;
import com.eqgis.sceneform.Node;
import com.eqgis.sceneform.utilities.AndroidPreconditions;


/**
 * 通过使用{@link Node#setRenderable(Renderable)}将其附加到{@link Node}来渲染3D模型。
 * <code>
 *     future = ModelRenderable.builder().setSource(context, R.raw.renderable).build();
 *     renderable = future.thenAccept(...);
 * </code>
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class ModelRenderable extends Renderable {

  private ModelRenderable(Builder builder) {
    super(builder);
  }

  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  private ModelRenderable(ModelRenderable other) {
    super(other);

    copyAnimationFrom(other);
  }

  private void copyAnimationFrom(ModelRenderable other) {return ;}

  /**
   * 创建ModelRenderable实例
   *
   * <p>
   *     创建一个拷贝对象
   * </p>
   */
  @Override
  public ModelRenderable makeCopy() {
    return new ModelRenderable(this);
  }

  /** 创建{@link ModelRenderable} */
  public static Builder builder() {
    AndroidPreconditions.checkMinAndroidApiLevel();
    return new Builder();
  }

  /** 构建者模式 {@link ModelRenderable}. */
  public static final class Builder extends Renderable.Builder<ModelRenderable, Builder> {

    /** @hide */
    @Override
    protected ModelRenderable makeRenderable() {
      return new ModelRenderable(this);
    }

    /** @hide */
    @Override
    protected Class<ModelRenderable> getRenderableClass() {
      return ModelRenderable.class;
    }

    /** @hide */
    @Override
    protected ResourceRegistry<ModelRenderable> getRenderableRegistry() {
      return ResourceManager.getInstance().getModelRenderableRegistry();
    }

    /** @hide */
    @Override
    protected Builder getSelf() {
      return this;
    }
  }
}
