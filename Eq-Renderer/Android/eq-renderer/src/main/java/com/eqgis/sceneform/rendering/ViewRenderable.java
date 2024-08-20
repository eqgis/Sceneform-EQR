package com.eqgis.sceneform.rendering;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.eqgis.sceneform.resources.ResourceRegistry;
import com.eqgis.sceneform.Node;
import com.eqgis.sceneform.collision.Box;
import com.eqgis.sceneform.common.TransformProvider;
import com.eqgis.sceneform.math.Matrix;
import com.eqgis.sceneform.math.Vector3;
import com.eqgis.sceneform.Scene;
import com.eqgis.sceneform.utilities.AndroidPreconditions;
import com.eqgis.sceneform.utilities.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;

/**
 * 安卓View渲染对象
 * <p>
 *     用于将安卓View渲染到三维场景中
 * </p>
 * 通过使用{@link Node#setRenderable(Renderable)}将2D Android视图附加到{@link Node}，
 * 在3D空间中渲染2D Android视图。默认情况下，视图的大小是{@link Scene}中每250dp表示1米。
 * 使用{@link ViewSizer}来控制{@link Scene}中视图大小的计算方式。
 *
 * <code>
 * future = ViewRenderable.builder().setView(context, R.layout.view).build();
 * viewRenderable = future.thenAccept(...);
 * </code>
 */
@RequiresApi(api = Build.VERSION_CODES.N)

public class ViewRenderable extends Renderable {
  private static final String TAG = ViewRenderable.class.getSimpleName();

  /**
   * 水平对齐方式
   */
  public enum HorizontalAlignment {
    LEFT,
    CENTER,
    RIGHT
  }

  /**
   * 垂直对齐方式
   */
  public enum VerticalAlignment {
    BOTTOM,
    CENTER,
    TOP
  }

  @Nullable private ViewRenderableInternalData viewRenderableData;
  private final View view;

  //用于将最终比例应用于可渲染对象，使其根据视图的大小以适当的大小呈现。
  private final Matrix viewScaleMatrix = new Matrix();

  private ViewSizer viewSizer;
  private VerticalAlignment verticalAlignment = VerticalAlignment.BOTTOM;
  private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;

  @Nullable private Renderer renderer;
  private boolean isInitialized;

  @SuppressWarnings({"initialization"})
  private final RenderViewToExternalTexture.OnViewSizeChangedListener onViewSizeChangedListener =
      (int width, int height) -> {
        if (isInitialized) {
          updateSuggestedCollisionShapeAsync();
        }
      };

  /** 获取Android View */
  public View getView() {
    return view;
  }

  /**
   * 对象拷贝
   */
  @Override
  public ViewRenderable makeCopy() {
    return new ViewRenderable(this);
  }

  /** @hide */
  @SuppressWarnings({"initialization"})
  // Suppress @UnderInitialization warning.
  ViewRenderable(Builder builder, View view) {
    super(builder);

    Preconditions.checkNotNull(view, "Parameter \"view\" was null.");

    this.view = view;
    viewSizer = builder.viewSizer;
    horizontalAlignment = builder.horizontalAlignment;
    verticalAlignment = builder.verticalAlignment;
    RenderViewToExternalTexture renderView =
        new RenderViewToExternalTexture(view.getContext(), view);
    renderView.addOnViewSizeChangedListener(onViewSizeChangedListener);
    viewRenderableData = new ViewRenderableInternalData(renderView);
    viewRenderableData.retain();

    // 这时先默认用空碰撞体。后面的步骤将被修改尺寸以适应视图的大小。
    collisionShape = new Box(Vector3.zero());
  }

  ViewRenderable(ViewRenderable other) {
    super(other);

    view = other.view;
    viewSizer = other.viewSizer;
    horizontalAlignment = other.horizontalAlignment;
    verticalAlignment = other.verticalAlignment;
    viewRenderableData = Preconditions.checkNotNull(other.viewRenderableData);
    viewRenderableData.retain();
    viewRenderableData.getRenderView().addOnViewSizeChangedListener(onViewSizeChangedListener);
  }

  /**
   * 获取{@link ViewSizer}，它控制{@link Scene}中{@link ViewRenderable}的大小。
   */
  public ViewSizer getSizer() {
    return viewSizer;
  }

  /**
   * 设置{@link ViewSizer}，它控制{@link Scene}中{@link ViewRenderable}的大小。
   */
  public void setSizer(ViewSizer viewSizer) {
    Preconditions.checkNotNull(viewSizer, "Parameter \"viewSizer\" was null.");
    this.viewSizer = viewSizer;
    updateSuggestedCollisionShape();
  }

  /**
   * 获取水平对齐方式
   */
  public HorizontalAlignment getHorizontalAlignment() {
    return horizontalAlignment;
  }

  /**
   * 设置水平对齐方式
   * <p>
   *     默认值： {@link HorizontalAlignment#CENTER}
   * </p>
   */
  public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
    this.horizontalAlignment = horizontalAlignment;
    updateSuggestedCollisionShape();
  }

  /**
   * 获取垂直对齐方式
   */
  public VerticalAlignment getVerticalAlignment() {
    return verticalAlignment;
  }

  /**
   * 设置垂直对齐方式
   * <p>
   *     默认值：{@link VerticalAlignment#BOTTOM}
   * </p>
   */
  public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
    this.verticalAlignment = verticalAlignment;
    updateSuggestedCollisionShape();
  }

  /**
   * 获取模型矩阵
   * <p>
   *     从{@link TransformProvider}中获取模型矩阵，用于呈现这个{@link Node}，
   *     并根据视图的米像素比将其缩放到合适的大小。
   * </p>
   * @hide
   * @param originalMatrix
   */
  @Override
  public Matrix getFinalModelMatrix(final Matrix originalMatrix) {
    Preconditions.checkNotNull(originalMatrix, "Parameter \"originalMatrix\" was null.");
    // 当转换提供程序的模型矩阵改变时，最好缓存它。
    //这将需要以每个实例为基础保存矩阵，而不是以每个可渲染为基础。

    Vector3 size = viewSizer.getSize(view);
    viewScaleMatrix.makeScale(new Vector3(size.x, size.y, 1.0f));

    //根据大小预先缩放的对齐设置矩阵的平移。
    //这比分配一个额外的矩阵和做一个矩阵乘法要有效得多。
    viewScaleMatrix.setTranslation(
        new Vector3(
            getOffsetRatioForAlignment(horizontalAlignment) * size.x,
            getOffsetRatioForAlignment(verticalAlignment) * size.y,
            0.0f));

    Matrix.multiply(originalMatrix, viewScaleMatrix, viewScaleMatrix);

    return viewScaleMatrix;
  }

  /** @hide */
  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"}) // CompletableFuture
  void prepareForDraw() {
    if (getId().isEmpty()) {
      return;
    }

    ViewRenderableInternalData data = Preconditions.checkNotNull(viewRenderableData);
    RenderViewToExternalTexture renderViewToExternalTexture = data.getRenderView();

    if (!renderViewToExternalTexture.isAttachedToWindow()
        || !renderViewToExternalTexture.isLaidOut()) {
      //等待视图完成绑定
      return;
    }

    //等待第一次绘制表面纹理后的一帧。
    //解决ViewRenderable在显示前渲染黑色的问题。
    boolean hasDrawnToSurfaceTexture = renderViewToExternalTexture.hasDrawnToSurfaceTexture();
    if (!hasDrawnToSurfaceTexture) {
      return;
    }

    if (!isInitialized) {
      getMaterial()
          .setExternalTexture("viewTexture", renderViewToExternalTexture.getExternalTexture());
      updateSuggestedCollisionShape();

      isInitialized = true;
    }

    if (renderer != null && renderer.isFrontFaceWindingInverted()) {
      getMaterial().setFloat2("offsetUv", 1, 0);
    }

    super.prepareForDraw();
  }

  @Override
  void attachToRenderer(Renderer renderer) {
    Preconditions.checkNotNull(viewRenderableData)
        .getRenderView()
        .attachView(renderer.getViewAttachmentManager());
    this.renderer = renderer;
  }

  @Override
  void detatchFromRenderer() {
    Preconditions.checkNotNull(viewRenderableData).getRenderView().detachView();
    this.renderer = null;
  }

  private void updateSuggestedCollisionShapeAsync() {
    view.post(this::updateSuggestedCollisionShape);
  }

  private void updateSuggestedCollisionShape() {
    if (getId().isEmpty()) {
      return;
    }

    Box box = (Box) collisionShape;
    if (box == null) {
      return;
    }

    IRenderableInternalData renderableData = getRenderableData();
    Vector3 viewSize = viewSizer.getSize(view);

    Vector3 size = renderableData.getSizeAabb();
    size.x *= viewSize.x;
    size.y *= viewSize.y;

    Vector3 center = renderableData.getCenterAabb();
    center.x *= viewSize.x;
    center.y *= viewSize.y;

    //基于对齐方式偏移碰撞形状。
    center.x += getOffsetRatioForAlignment(horizontalAlignment) * size.x;
    center.y += getOffsetRatioForAlignment(verticalAlignment) * size.y;

    box.setSize(size);
    box.setCenter(center);
  }

  private float getOffsetRatioForAlignment(HorizontalAlignment horizontalAlignment) {
    IRenderableInternalData data = getRenderableData();
    Vector3 centerAabb = data.getCenterAabb();
    Vector3 extentsAabb = data.getExtentsAabb();

    switch (horizontalAlignment) {
      case LEFT:
        return -centerAabb.x + extentsAabb.x;
      case CENTER:
        return -centerAabb.x;
      case RIGHT:
        return -centerAabb.x - extentsAabb.x;
    }
    throw new IllegalStateException("Invalid HorizontalAlignment: " + horizontalAlignment);
  }

  private float getOffsetRatioForAlignment(VerticalAlignment verticalAlignment) {
    IRenderableInternalData data = getRenderableData();
    Vector3 centerAabb = data.getCenterAabb();
    Vector3 extentsAabb = data.getExtentsAabb();

    switch (verticalAlignment) {
      case BOTTOM:
        return -centerAabb.y + extentsAabb.y;
      case CENTER:
        return -centerAabb.y;
      case TOP:
        return -centerAabb.y - extentsAabb.y;
    }
    throw new IllegalStateException("Invalid VerticalAlignment: " + verticalAlignment);
  }

  /** @hide */
  @Override
  protected void finalize() throws Throwable {
    try {
      ThreadPools.getMainExecutor().execute(() -> dispose());
    } catch (Exception e) {
      Log.e(TAG, "Error while Finalizing View Renderable.", e);
    } finally {
      super.finalize();
    }
  }

  /** @hide */
  void dispose() {
    AndroidPreconditions.checkUiThread();

    ViewRenderableInternalData viewRenderableData = this.viewRenderableData;
    if (viewRenderableData != null) {
      viewRenderableData.getRenderView().removeOnViewSizeChangedListener(onViewSizeChangedListener);
      viewRenderableData.release();
      this.viewRenderableData = null;
    }
  }

  /** Constructs a {@link ViewRenderable} */
  public static Builder builder() {
    AndroidPreconditions.checkMinAndroidApiLevel();
    return new Builder();
  }

  /** Factory class for {@link ViewRenderable} */
  public static final class Builder extends Renderable.Builder<ViewRenderable, Builder> {
    private static final int DEFAULT_DP_TO_METERS = 250;
    @Nullable private View view;
    private ViewSizer viewSizer = new DpToMetersViewSizer(DEFAULT_DP_TO_METERS);
    private VerticalAlignment verticalAlignment = VerticalAlignment.BOTTOM;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;

    @SuppressWarnings("AndroidApiChecker")
    private OptionalInt resourceId = OptionalInt.empty();

    private Builder() {}

    public Builder setView(Context context, View view) {
      this.view = view;
      this.context = context;
      registryId = view;
      return this;
    }

    @SuppressWarnings("AndroidApiChecker")
    public Builder setView(Context context, int resourceId) {
      this.resourceId = OptionalInt.of(resourceId);
      this.context = context;
      registryId = null;
      return this;
    }

    /**
     * 设置尺寸
     */
    public Builder setSizer(ViewSizer viewSizer) {
      Preconditions.checkNotNull(viewSizer, "Parameter \"viewSizer\" was null.");
      this.viewSizer = viewSizer;
      return this;
    }

    /**
     * 设置水平对齐方式
     */
    public Builder setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
      this.horizontalAlignment = horizontalAlignment;
      return this;
    }

    /**
     * 设置垂直对齐方式
     */
    public Builder setVerticalAlignment(VerticalAlignment verticalAlignment) {
      this.verticalAlignment = verticalAlignment;
      return this;
    }

    @Override
    @SuppressWarnings("AndroidApiChecker") // java.util.concurrent.CompletableFuture
    public CompletableFuture<ViewRenderable> build() {
      if (!hasSource() && context != null) {
        //对于ViewRenderables, registryId必须来自View，而不是RCB源。
        //如果源是View，使用它作为registryId。如果视图为空，则源是资源id，并且registryId也应该为空。
        registryId = view;

        CompletableFuture<Void> setSourceFuture = Material.builder()
                .setSource(
                        context,
                        RenderingResources.GetSceneformResource(
                                context, RenderingResources.Resource.VIEW_RENDERABLE_MATERIAL))
                .build()
                .thenAccept(
                        material -> {

                          ArrayList<Vertex> vertices = new ArrayList<>();
                          vertices.add(Vertex.builder()
                                  .setPosition(new Vector3(-0.5f, 0.0f, 0.0f))
                                  .setNormal(new Vector3(0.0f, 0.0f, 1.0f))
                                  .setUvCoordinate(new Vertex.UvCoordinate(0.0f, 0.0f))
                                  .build());
                          vertices.add(Vertex.builder()
                                  .setPosition(new Vector3(0.5f, 0.0f, 0.0f))
                                  .setNormal(new Vector3(0.0f, 0.0f, 1.0f))
                                  .setUvCoordinate(new Vertex.UvCoordinate(1.0f, 0.0f))
                                  .build());
                          vertices.add(Vertex.builder()
                                  .setPosition(new Vector3(-0.5f, 1.0f, 0.0f))
                                  .setNormal(new Vector3(0.0f, 0.0f, 1.0f))
                                  .setUvCoordinate(new Vertex.UvCoordinate(0.0f, 1.0f))
                                  .build());
                          vertices.add(Vertex.builder()
                                  .setPosition(new Vector3(0.5f, 1.0f, 0.0f))
                                  .setNormal(new Vector3(0.0f, 0.0f, 1.0f))
                                  .setUvCoordinate(new Vertex.UvCoordinate(1.0f, 1.0f))
                                  .build());
                          ArrayList<Integer> triangleIndices = new ArrayList<>();
                          triangleIndices.add(0);
                          triangleIndices.add(1);
                          triangleIndices.add(2);
                          triangleIndices.add(1);
                          triangleIndices.add(3);
                          triangleIndices.add(2);
                          RenderableDefinition.Submesh submesh =
                                  RenderableDefinition.Submesh.builder().setTriangleIndices(triangleIndices).setMaterial(material).build();
                          setSource(
                                  RenderableDefinition.builder()
                                          .setVertices(vertices)
                                          .setSubmeshes(Arrays.asList(submesh))
                                          .build()
                          );
                        }
                );
        return setSourceFuture.thenCompose((Void) -> super.build());
      }

      return super.build();
    }

    @Override
    protected ViewRenderable makeRenderable() {
      if (this.view != null) {
        return new ViewRenderable(this, view);
      } else {
        return new ViewRenderable(this, inflateViewFromResourceId());
      }
    }

    /** @hide */
    @Override
    protected Class<ViewRenderable> getRenderableClass() {
      return ViewRenderable.class;
    }

    /** @hide */
    @Override
    protected ResourceRegistry<ViewRenderable> getRenderableRegistry() {
      return ResourceManager.getInstance().getViewRenderableRegistry();
    }

    /** @hide */
    @Override
    protected Builder getSelf() {
      return this;
    }

    /** @hide */
    @SuppressWarnings("AndroidApiChecker")
    @Override
    protected void checkPreconditions() {
      super.checkPreconditions();

      boolean hasView = resourceId.isPresent() || view != null;

      if (!hasView) {
        throw new AssertionError("ViewRenderable must have a source.");
      }

      if (resourceId.isPresent() && view != null) {
        throw new AssertionError(
            "ViewRenderable must have a resourceId or a view as a source. This one has both.");
      }
    }

    @SuppressWarnings("AndroidApiChecker")
    private View inflateViewFromResourceId() {
      if (context == null) {
        throw new AssertionError("Context cannot be null");
      }

      //需要一个虚拟ViewGroup作为根，以便加载View。
      ViewGroup dummy = new FrameLayout(context);
      return LayoutInflater.from(context).inflate(resourceId.getAsInt(), dummy, false);
    }
  }
}
