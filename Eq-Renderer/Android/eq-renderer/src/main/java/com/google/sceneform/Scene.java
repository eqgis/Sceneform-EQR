package com.google.sceneform;

import android.media.Image;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.sceneform.collision.Collider;
import com.google.sceneform.collision.CollisionSystem;
import com.google.sceneform.collision.Ray;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.Renderer;
import com.google.sceneform.utilities.EnvironmentalHdrParameters;
import com.google.sceneform.utilities.AndroidPreconditions;
import com.google.sceneform.utilities.Preconditions;

import java.util.ArrayList;

/**
 * Scene场景
 * <p>场景为Node对象，采用树状结构</p>
 */
public class Scene extends NodeParent {
  /**
   * 当一个触摸事件被分派到一个场景时调用的回调的接口定义。如果没有节点使用该事件，则在将触摸事件分派给场景中的节点后调用回调。
   */
  public interface OnTouchListener {
    /**
     * 场景触摸回调
     * <p>会进行射线检测，在HitTestResult中会返回射线检测结果。</p>
     * <br/>
     * 当一个触摸事件被分派到一个场景时调用。
     * 如果没有节点使用该事件，则在将触摸事件分派给场景中的节点后调用回调。
     * 即使触摸不在节点上，也会调用该方法，
     * 在这种情况下{@link HitTestResult#getNode()}将为空。
     * <br/>
     * 需要注意的是，若SceneView额外添加了onTouchListener，
     * 在那里使用时需要注意返回值，防止手势不再向场景分发
     *
     * @see Scene#setOnTouchListener(OnTouchListener)
     * @param hitTestResult 碰撞检测结果
     * @param motionEvent 事件
     * @return 返回true，则表示事件已消费
     */
    boolean onSceneTouch(HitTestResult hitTestResult, MotionEvent motionEvent);
  }

  /**
   * 用于监听触摸事件
   */
  public interface OnPeekTouchListener {
    /**
     * 当一个触摸事件被分派到一个场景时调用。回调将在{@link OnTouchListener}被调用之前被调用。
     * 即使该手势被使用，也会调用该方法，从而可以观察分派到场景的所有运动事件。即使触摸不在节点上，
     * 也会调用该方法，在这种情况下{@link HitTestResult#getNode()}将为空。
     *
     * @see Scene#setOnTouchListener(OnTouchListener)
     * @param hitTestResult 射线检测结果
     * @param motionEvent 手势事件
     */
    void onPeekTouch(HitTestResult hitTestResult, MotionEvent motionEvent);
  }

  /**
   * 场景更新回调
   * <p>在每帧渲染前执行</p>
   */
  public interface OnUpdateListener {
    /**
     * 每一帧渲染前调用
     *
     * @param frameTime 提供当前帧的时间信息
     */
    void onUpdate(FrameTime frameTime);
  }

  private static final String TAG = Scene.class.getSimpleName();
  private static final String DEFAULT_LIGHTPROBE_ASSET_NAME = "small_empty_house_2k";
  private static final String DEFAULT_LIGHTPROBE_RESOURCE_NAME = "sceneform_default_light_probe";
  private static final float DEFAULT_EXPOSURE = 1.0f;
  public static final EnvironmentalHdrParameters DEFAULT_HDR_PARAMETERS =
      EnvironmentalHdrParameters.makeDefault();

  private final Camera camera;
  private Sun sunlightNode;
  @Nullable private final SceneView view;
  private boolean lightProbeSet = false;
  private boolean isUnderTesting = false;

  // Systems.
  final CollisionSystem collisionSystem = new CollisionSystem();
  private final TouchEventSystem touchEventSystem = new TouchEventSystem();

  private final ArrayList<OnUpdateListener> onUpdateListeners = new ArrayList<>();

  @SuppressWarnings("VisibleForTestingUsed")
  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  Scene() {
    view = null;
    camera = new Camera(true);
    isUnderTesting = true;
  }

  /** 构造函数 */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public Scene(SceneView view) {
    Preconditions.checkNotNull(view, "Parameter \"view\" was null.");
    this.view = view;
    camera = new Camera(this);
  }

  /**
   * 载入默认太阳光
   */
  public void initDefaultSunlight() {
    if (!AndroidPreconditions.isMinAndroidApiLevel()) {
      // Enforce min api level 24
      sunlightNode = null;
    }
    sunlightNode = new Sun(this);
  }

  /** 获取场景视图 */
  public SceneView getView() {
    // the view field cannot be marked for the purposes of unit testing.
    // Add this check for static analysis go/nullness.
    if (view == null) {
      throw new IllegalStateException("Scene's view must not be null.");
    }

    return view;
  }

  /**
   * 获取场景中的相机对象
   * <p>相机是Node的子类</p>
   *
   * @return 场景中渲染的相机对象
   */
  public Camera getCamera() {
    return camera;
  }

  /**
   * 获取太阳光节点
   * <p>场景中默认的平行光节点</p>
   *
   * @return 已设置Light的Node对象
   */
  @Nullable
  public Node getSunlight() {
    return sunlightNode;
  }

  /**
   * 设置触摸监听事件
   * @param onTouchListener 触摸监听事件
   */
  public void setOnTouchListener(@Nullable OnTouchListener onTouchListener) {
    touchEventSystem.setOnTouchListener(onTouchListener);
  }

  /**
   * 添加触摸监视器监听事件
   * @param onPeekTouchListener 监听事件
   */
  public void addOnPeekTouchListener(OnPeekTouchListener onPeekTouchListener) {
    touchEventSystem.addOnPeekTouchListener(onPeekTouchListener);
  }

  /**
   * 移除监视器事件回调
   * @param onPeekTouchListener 监听事件
   */
  public void removeOnPeekTouchListener(OnPeekTouchListener onPeekTouchListener) {
    touchEventSystem.removeOnPeekTouchListener(onPeekTouchListener);
  }

  /**
   * 添加帧更新监听事件
   * <p>每帧渲染之前回调</p>
   * @param onUpdateListener 监听事件
   */
  public void addOnUpdateListener(OnUpdateListener onUpdateListener) {
    Preconditions.checkNotNull(onUpdateListener, "Parameter 'onUpdateListener' was null.");
    if (!onUpdateListeners.contains(onUpdateListener)) {
      onUpdateListeners.add(onUpdateListener);
    }
  }

  /**
   * 移除帧更新监听事件
   * @param onUpdateListener 监听事件
   */
  public void removeOnUpdateListener(OnUpdateListener onUpdateListener) {
    Preconditions.checkNotNull(onUpdateListener, "Parameter 'onUpdateListener' was null.");
    onUpdateListeners.remove(onUpdateListener);
  }
  /**
   * 移除所有帧更新监听事件
   */
  public void clearOnUpdateListener() {
    onUpdateListeners.clear();
  }

  @Override
  public void onAddChild(Node child) {
    super.onAddChild(child);
    child.setSceneRecursively(this);
  }

  @Override
  public void onRemoveChild(Node child) {
    super.onRemoveChild(child);
    child.setSceneRecursively(null);
  }

  /**
   * 点击测试
   * <p>通过射线检测的方式实现</p>
   *
   * @param motionEvent 手势事件
   * @return 结果包括被点击事件击中的第一个节点(可能为null)，以及关于运动事件在世界空间中击中节点的位置的信息
   */
  public HitTestResult hitTest(MotionEvent motionEvent) {
    Preconditions.checkNotNull(motionEvent, "Parameter \"motionEvent\" was null.");

    if (camera == null) {
      return new HitTestResult();
    }

    Ray ray = camera.motionEventToRay(motionEvent);
    return hitTest(ray);
  }

  /**
   * 点击测试
   * <p>通过射线检测的方式实现</p>
   *
   * @see Camera#screenPointToRay(float, float)
   * @param ray 射线
   * @return 结果包括被点击事件击中的第一个节点(可能为null)，以及关于运动事件在世界空间中击中节点的位置的信息
   */
  public HitTestResult hitTest(Ray ray) {
    Preconditions.checkNotNull(ray, "Parameter \"ray\" was null.");

    HitTestResult result = new HitTestResult();
    Collider collider = collisionSystem.raycast(ray, result);
    if (collider != null) {
      result.setNode((Node) collider.getTransformProvider());
    }

    return result;
  }

  /**
   * 点击测试
   * <p>通过射线检测的方式实现</p>
   *
   * @param motionEvent 手势事件
   * @return 为每个按距离排序的节点填充一个HitTestResultList。如果没有命中节点，则为空。
   */
  public ArrayList<HitTestResult> hitTestAll(MotionEvent motionEvent) {
    Preconditions.checkNotNull(motionEvent, "Parameter \"motionEvent\" was null.");

    if (camera == null) {
      return new ArrayList<>();
    }
    Ray ray = camera.motionEventToRay(motionEvent);
    return hitTestAll(ray);
  }

  /**
   * 点击测试
   * <p>通过射线检测的方式实现</p>
   *
   * @see Camera#screenPointToRay(float, float)
   * @param ray 射线
   * @return 为每个按距离排序的节点填充一个HitTestResultList。如果没有命中节点，则为空。
   */
  public ArrayList<HitTestResult> hitTestAll(Ray ray) {
    Preconditions.checkNotNull(ray, "Parameter \"ray\" was null.");

    ArrayList<HitTestResult> results = new ArrayList<>();

    collisionSystem.raycastAll(
        ray,
        results,
        (result, collider) -> result.setNode((Node) collider.getTransformProvider()),
        () -> new HitTestResult());

    return results;
  }

  /**
   * 相交测试
   *
   * <p>获取当前Node的碰撞体相交的其他Node对象</p>
   * @see #overlapTestAll(Node)
   * @param node 用于检测的Node对象
   * @return 与测试节点重叠的节点。如果没有节点与测试节点重叠，则该值为空。如果多个节点与测试节点重叠，则这可以是其中的任何一个。
   */
  @Nullable
  public Node overlapTest(Node node) {
    Preconditions.checkNotNull(node, "Parameter \"node\" was null.");

    Collider collider = node.getCollider();
    if (collider == null) {
      return null;
    }

    Collider intersectedCollider = collisionSystem.intersects(collider);
    if (intersectedCollider == null) {
      return null;
    }

    return (Node) intersectedCollider.getTransformProvider();
  }

  /**
   * 相交测试
   *
   * <p>获取当前Node的碰撞体相交的其他Node对象</p>
   * @see #overlapTestAll(Node)
   * @param node 用于检测的Node对象
   * @return 与测试节点重叠的节点。如果没有节点与测试节点重叠，则该值为空。如果多个节点与测试节点重叠，返回所有Node的集合。
   */
  public ArrayList<Node> overlapTestAll(Node node) {
    Preconditions.checkNotNull(node, "Parameter \"node\" was null.");

    ArrayList<Node> results = new ArrayList<>();

    Collider collider = node.getCollider();
    if (collider == null) {
      return results;
    }

    collisionSystem.intersectsAll(
        collider,
        (Collider intersectedCollider) ->
            results.add((Node) intersectedCollider.getTransformProvider()));

    return results;
  }

  /** 判断是否处于测试阶段，内部调测使用。可将部分功能标记为测试默认，编译调测时能够通过某些方法 */
  boolean isUnderTesting() {
    return isUnderTesting;
  }

  /**
   * 设置场景是否应该预期使用Hdr光估计，以便filament设置可以适当调整。
   * @hide
   */
  public void setUseHdrLightEstimate(boolean useHdrLightEstimate) {
    if (view != null) {
      Renderer renderer = Preconditions.checkNotNull(view.getRenderer());
      renderer.setUseHdrLightEstimate(useHdrLightEstimate);
    }
  }

  /**
   * 设置环境Hdr
   * @hide
   */
  // incompatible types in argument.
  @SuppressWarnings("nullness:argument.type.incompatible")
  
  public void setEnvironmentalHdrLightEstimate(
      @Nullable float[] sphericalHarmonics,
      @Nullable float[] direction,
      Color colorCorrection,
      float relativeIntensity,
      @Nullable Image[] cubeMap) {
    float exposure;
    EnvironmentalHdrParameters hdrParameters;
    if (view == null) {
      exposure = DEFAULT_EXPOSURE;
      hdrParameters = DEFAULT_HDR_PARAMETERS;
    } else {
      Renderer renderer = Preconditions.checkNotNull(view.getRenderer());
      exposure = renderer.getExposure();
      hdrParameters = renderer.getEnvironmentalHdrParameters();
    }

    if (sunlightNode != null && direction != null) {
      sunlightNode.setEnvironmentalHdrLightEstimate(
          direction, colorCorrection, relativeIntensity, exposure, hdrParameters);
    }
  }

  /**
   * 设置光估计来调节场景照明和强度。渲染的灯光将使用这些值和光的颜色和强度的组合。
   * 白色校正值和像素强度值为1意味着不改变光线设置。
   *
   * <p>
   *     内部使用，以根据ARCore的值调整照明。
   *     AR场景会自动调用它，可能会覆盖其他设置。
   *     在大多数情况下，不需要显式地调用它。
   * </p>
   *
   * @param colorCorrection 要被调整的场景光颜色
   * @param pixelIntensity 要被调整的场景光强
   */
  void setLightEstimate(Color colorCorrection, float pixelIntensity) {
    if (sunlightNode != null) {
      sunlightNode.setLightEstimate(colorCorrection, pixelIntensity);
    }
  }

  public void onTouchEvent(MotionEvent motionEvent) {
    Preconditions.checkNotNull(motionEvent, "Parameter \"motionEvent\" was null.");

    // TODO: Investigate API for controlling what node's can be hit by the hitTest.
    // i.e. layers, disabling collision shapes.
    HitTestResult hitTestResult = hitTest(motionEvent);
    touchEventSystem.onTouchEvent(hitTestResult, motionEvent);
  }

  public void dispatchUpdate(FrameTime frameTime) {
    for (OnUpdateListener onUpdateListener : onUpdateListeners) {
      onUpdateListener.onUpdate(frameTime);
    }

    callOnHierarchy(node -> node.dispatchUpdate(frameTime));
  }
}
