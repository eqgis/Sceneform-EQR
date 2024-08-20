package com.eqgis.sceneform;

import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.Nullable;

import com.eqgis.sceneform.collision.Collider;
import com.eqgis.sceneform.collision.CollisionShape;
import com.eqgis.sceneform.collision.Ray;
import com.eqgis.sceneform.common.TransformProvider;
import com.eqgis.sceneform.math.Matrix;
import com.eqgis.sceneform.math.Quaternion;
import com.eqgis.sceneform.math.Vector3;
import com.eqgis.sceneform.rendering.Light;
import com.eqgis.sceneform.rendering.LightInstance;
import com.eqgis.sceneform.rendering.ModelRenderable;
import com.eqgis.sceneform.rendering.Renderer;
import com.eqgis.sceneform.rendering.ViewRenderable;
import com.eqgis.sceneform.utilities.AndroidPreconditions;
import com.eqgis.sceneform.utilities.ChangeId;
import com.eqgis.sceneform.utilities.Preconditions;
import com.eqgis.sceneform.rendering.RenderableInstance;
import com.eqgis.sceneform.rendering.Renderable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 节点对象
 *
 * <p>
 *     节点表示场景图的层次结构中的转换。
 *     它可以包含渲染引擎渲染的可渲染对象，
 *     也可以设置不同的灯光。
 * </p>
 *
 * 每个节点可以有任意数量的子节点和一个父节点。父节点可以是另一个节点，也可以是场景{@link Scene}或{@link Camera}。
 */
public class Node extends NodeParent implements TransformProvider {
    /**
     * 节点的触摸监听事件
     */
    public interface OnTouchListener {
        /**
         * 触摸回调
         * @param hitTestResult 碰撞检测结果
         * @param motionEvent 事件
         * @return 返回true，则表示事件已消费
         */
        boolean onTouch(HitTestResult hitTestResult, MotionEvent motionEvent);
    }

    /**
     * 节点的点击监听事件
     * */
    public interface OnTapListener {
        /**
         * 点击回调
         * @param hitTestResult 碰撞检测结果
         * @param motionEvent 事件
         */
        void onTap(HitTestResult hitTestResult, MotionEvent motionEvent);
    }

    /** 节点的生命周期的接口回调 */
    public interface LifecycleListener {
        /**
         * 节点激活时触发
         *
         * @param node 激活的节点对象
         */
        void onActivated(Node node);

        /**
         * 节点更新时触发
         * <p>每帧更新都触发</p>
         * @param node 更新的节点对象
         * @param frameTime 提供当前帧的时间信息
         */
        void onUpdated(Node node, FrameTime frameTime);

        /**
         * 节点失活时触发
         *
         * @param node 失活的节点对象
         */
        void onDeactivated(Node node);
    }

    /**
     * 节点几何变换监听事件
     */
    public interface TransformChangedListener {

        /**
         * 当节点的Transform发生改变时触发
         * <p>包含Rotation、Position、Scale</p>
         *
         * <p>原始节点是层次结构中触发节点更改的最高级节点。它将始终是同一个节点或它的父节点之一。
         * 也就是说，如果节点A的位置发生了变化，那么这将触发{@link #onTransformChanged(Node, Node)}
         * 对它的所有后代调用，其中originingnode是节点A。
         *
         * @param node 当前发生改变的节点
         * @param originatingNode 触发变换的原始节点
         */
        void onTransformChanged(Node node, Node originatingNode);
    }

    /** 用于跟踪数据，以检测是否在此节点上发生了点击手势。 */
    private static class TapTrackingData {
        // ACTION_DOWN时，点中的节点
        final Node downNode;

        //ACTION_DOWN时，点中的屏幕位置
        final Vector3 downPosition;

        TapTrackingData(Node downNode, Vector3 downPosition) {
            this.downNode = downNode;
            this.downPosition = new Vector3(downPosition);
        }
    }

    private static final float DIRECTION_UP_EPSILON = 0.99f;

    //默认的视图配置
    private static final int DEFAULT_TOUCH_SLOP = 8;

    private static final String DEFAULT_NAME = "Node";

    private static final int LOCAL_TRANSFORM_DIRTY = 1;
    private static final int WORLD_TRANSFORM_DIRTY = 1 << 1;
    private static final int WORLD_INVERSE_TRANSFORM_DIRTY = 1 << 2;
    private static final int WORLD_POSITION_DIRTY = 1 << 3;
    private static final int WORLD_ROTATION_DIRTY = 1 << 4;
    private static final int WORLD_SCALE_DIRTY = 1 << 5;

    private static final int WORLD_DIRTY_FLAGS =
            WORLD_TRANSFORM_DIRTY
                    | WORLD_INVERSE_TRANSFORM_DIRTY
                    | WORLD_POSITION_DIRTY
                    | WORLD_ROTATION_DIRTY
                    | WORLD_SCALE_DIRTY;

    private static final int LOCAL_DIRTY_FLAGS = LOCAL_TRANSFORM_DIRTY | WORLD_DIRTY_FLAGS;

    // 场景对象
    @Nullable private Scene scene;
    // 将父节点存储为节点(如果父节点是节点)以避免强制转换。
    @Nullable private Node parentAsNode;

    // 节点的名称，以便在层次结构中识别它
    @SuppressWarnings("unused")
    private String name = DEFAULT_NAME;

    // 用于比较的名称散列
    private int nameHash = DEFAULT_NAME.hashCode();

    /**
     * 警告:不要直接分配这个属性，除非你知道你在做什么。相反,叫
     * setParent。该字段仅在包中公开，以便类NodeParent可以访问。
     * <p>除了设置这个字段，setParent还会做以下事情:
     * <ul>
     * <li>从其父节点的子节点中删除该节点。
     * <li>将该节点添加到其新父节点的子节点。
     * <li>递归更新节点的转换以反映父节点的变化
     * <li>递归更新场景字段以匹配新的父场景字段。
     * </ul >
     */
    // 节点的父节点可以是节点或场景。
    @Nullable NodeParent parent;

    // 本地坐标系变换的属性内容
    private final Vector3 localPosition = new Vector3();
    private final Quaternion localRotation = new Quaternion();
    private final Vector3 localScale = new Vector3();
    private final Matrix cachedLocalModelMatrix = new Matrix();

    // 世界坐标系变换的属性内容
    private final Vector3 cachedWorldPosition = new Vector3();
    private final Quaternion cachedWorldRotation = new Quaternion();
    private final Vector3 cachedWorldScale = new Vector3();
    private final Matrix cachedWorldModelMatrix = new Matrix();
    private final Matrix cachedWorldModelMatrixInverse = new Matrix();

    /**确定何时节点转换的各个方面是修改过时，必须重新计算。*/
    private int dirtyTransformFlags = LOCAL_DIRTY_FLAGS;

    // 状态值
    private boolean enabled = true;
    private boolean active = false;

    // 渲染的实体ID对象索引
    private int renderableId = ChangeId.EMPTY_ID;
    @Nullable private RenderableInstance renderableInstance;
    // TODO: Right now, lightInstance can cause leaks because it subscribes to event
    // 除非调用setLight(null)，否则不会处理Light上的监听器。
    @Nullable private LightInstance lightInstance;

    // 碰撞体信息
    @Nullable private CollisionShape collisionShape;
    @Nullable private Collider collider;

    // 监听事件信息
    @Nullable private OnTouchListener onTouchListener;
    @Nullable private OnTapListener onTapListener;
    private final ArrayList<LifecycleListener> lifecycleListeners = new ArrayList<>();
    private final ArrayList<TransformChangedListener> transformChangedListeners = new ArrayList<>();
    private boolean allowDispatchTransformChangedListeners = true;
    private boolean rotateAlwaysToCamera = false;
    private RotateToCameraType mRotateToCameraType=RotateToCameraType.Horizontal_Vertical;

    // 存储用于检测何时在此节点上发生抽头的数据。
    @Nullable private TapTrackingData tapTrackingData = null;

    /**
     * 构造函数
     * <p>注意：此时未绑定父节点</p>
     * */
    @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
    public Node() {
        AndroidPreconditions.checkUiThread();

        localScale.set(1, 1, 1);
        cachedWorldScale.set(localScale);
    }

    /**
     * 设置此节点的名称。可以使用名称找到节点。多个节点可能具有
     * 相同的名称，在这种情况下调用{@link NodeParent#findByName(String)}将返回第一个
     * 节点使用给定的名称。
     *
     * @param name The name of the node.
     */
    public final void setName(String name) {
        Preconditions.checkNotNull(name, "Parameter \"name\" was null.");

        this.name = name;
        nameHash = name.hashCode();
    }

    /** 获取节点对象的名称 */
    public final String getName() {
        return name;
    }

    /**
     * 设置父节点
     *
     * @see #getParent()
     * @see #getScene()
     * @param parent 此节点将成为其子节点的新父节点。如果为空，则该节点将与其父节点分离。
     */
    public void setParent(@Nullable NodeParent parent) {
        AndroidPreconditions.checkUiThread();

        if (parent == this.parent) {
            return;
        }

        // Disallow dispatching transformed changed here so we don't
        // send it multiple times when setParent is called.
        allowDispatchTransformChangedListeners = false;
        if (parent != null) {
            // If this node already has a parent, addChild automatically removes it from its old parent.
            parent.addChild(this);
        } else {
            this.parent.removeChild(this);
        }
        allowDispatchTransformChangedListeners = true;

        // Make sure transform changed is dispatched.
        markTransformChangedRecursively(WORLD_DIRTY_FLAGS, this);
    }

    /**
     * 获取场景{@link Scene}对象
     */
    @Nullable
    public final Scene getScene() {
        return scene;
    }

    /**
     * 获取父节点
     * @return 父节点
     */
    @Nullable
    public final Node getParent() {
        return parentAsNode;
    }

    /**
     * 如果此节点为顶层，则返回true。如果节点没有父节点，或者父节点是场景，则该节点被认为是顶级节点。
     * <p>
     *     判断是否是顶级节点
     * </p>
     *
     * @return 若返回ture，则本节点是顶级节点
     */
    public boolean isTopLevel() {
        return parent == null || parent == scene;
    }

    /**
     * 递归地检查给定的节点父节点是否是该节点的先代节点。
     *
     * @param ancestor 被检测的节点对象
     * @return 若返回true，则被检测的节点是本节点的先代
     */
    public final boolean isDescendantOf(NodeParent ancestor) {
        Preconditions.checkNotNull(ancestor, "Parameter \"ancestor\" was null.");

        NodeParent currentAncestor = parent;

        // 用于通过层次结构向上迭代，因为NodeParent只是子节点的容器，没有自己的父节点。
        Node currentAncestorAsNode = parentAsNode;

        while (currentAncestor != null) {
            // 确保对currentAncestor而不是current祖宗asnode进行相等性检查，
            // 这样就可以对任何NodeParent而不仅仅是Node工作。
            if (currentAncestor == ancestor) {
                return true;
            }

            if (currentAncestorAsNode != null) {
                currentAncestor = currentAncestorAsNode.parent;
                currentAncestorAsNode = currentAncestorAsNode.parentAsNode;
            } else {
                break;
            }
        }
        return false;
    }

    /**
     * 设置该节点的启用状态。请注意，如果一个节点不是场景的一部分，或者它的父节点处于非活动状态，那么它可能是启用的，但仍然处于非活动状态。
     *
     * @see #isActive()
     * @param enabled 状态值
     */
    public final void setEnabled(boolean enabled) {
        AndroidPreconditions.checkUiThread();

        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
        updateActiveStatusRecursively();
    }

    /**
     * 获取此节点的启用状态。请注意，如果一个节点不是场景的一部分，
     * 或者它的父节点处于非活动状态，那么它可能是启用的，但仍然处于非活动状态。
     *
     * @see #isActive()
     * @return 状态值
     */
    public final boolean isEnabled() {
        return enabled;
    }

    /**
     * 判断节点的激活状态
     * <br/>
     * 如果节点是活动的，则返回true。如果一个节点满足所有的条件，则认为它是活动的
     * 以下条件:
     * <ul>
     *   <li>节点是场景的一部分。
     *   <li>节点的父节点是已激活的。
     *   <li>节点是已启用的。
     * </ul>
     * 已激活的节点有以下行为:
     * <ul>
     * <li>节点的{@link #onUpdate(FrameTime)}函数将在每帧被调用。
     * <li>节点的{@link #getRenderable()}将被渲染。
     * <li>节点的{@link #getCollisionShape()}将在调用Scene.hitTest时进行检查。
     * <li>节点的{@link #onTouchEvent(HitTestResult, MotionEvent)}函数将被调用节点被触摸。
     * </ul>
     *
     * @see #onActivate()
     * @see #onDeactivate()
     * @return 节点的激活状态
     */
    public final boolean isActive() {
        return active;
    }

    /**
     * 设置触摸监听事件
     */
    public void setOnTouchListener(@Nullable OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    /**
     * 设置点击监听事件
     * @see OnTapListener
     */
    public void setOnTapListener(@Nullable OnTapListener onTapListener) {
        if (onTapListener != this.onTapListener) {
            tapTrackingData = null;
        }

        this.onTapListener = onTapListener;
    }

    /**
     * 添加生命周期的回调
     */
    public void addLifecycleListener(LifecycleListener lifecycleListener) {
        if (!lifecycleListeners.contains(lifecycleListener)) {
            lifecycleListeners.add(lifecycleListener);
        }
    }

    /**
     * 移除生命周期的回调事件
     * */
    public void removeLifecycleListener(LifecycleListener lifecycleListener) {
        lifecycleListeners.remove(lifecycleListener);
    }

    /**
     * 添加几何变换的更新事件
     * */
    public void addTransformChangedListener(TransformChangedListener transformChangedListener) {
        if (!transformChangedListeners.contains(transformChangedListener)) {
            transformChangedListeners.add(transformChangedListener);
        }
    }

    /**
     * 移除几何变换的更新事件
     * */
    public void removeTransformChangedListener(TransformChangedListener transformChangedListener) {
        transformChangedListeners.remove(transformChangedListener);
    }

    @Override
    protected final boolean canAddChild(Node child, StringBuilder failureReason) {
        if (!super.canAddChild(child, failureReason)) {
            return false;
        }

        if (isDescendantOf(child)) {
            failureReason.append("Cannot add child: A node's parent cannot be one of its descendants.");
            return false;
        }

        return true;
    }

    @Override
    protected final void onAddChild(Node child) {
        super.onAddChild(child);
        child.parentAsNode = this;
        child.markTransformChangedRecursively(WORLD_DIRTY_FLAGS, child);
        child.setSceneRecursively(scene);
    }

    @Override
    protected final void onRemoveChild(Node child) {
        super.onRemoveChild(child);
        child.parentAsNode = null;
        child.markTransformChangedRecursively(WORLD_DIRTY_FLAGS, child);
        child.setSceneRecursively(null);
    }

    private final void markTransformChangedRecursively(int flagsToMark, Node originatingNode) {
        boolean needsRecursion = false;

        if ((dirtyTransformFlags & flagsToMark) != flagsToMark) {
            dirtyTransformFlags |= flagsToMark;

            if ((dirtyTransformFlags & WORLD_TRANSFORM_DIRTY) == WORLD_TRANSFORM_DIRTY
                    && collider != null) {
                collider.markWorldShapeDirty();
            }

            needsRecursion = true;
        }

        if (originatingNode.allowDispatchTransformChangedListeners) {
            dispatchTransformChanged(originatingNode);
            needsRecursion = true;
        }

        if (needsRecursion) {
            // 使用for而不是foreach来避免不必要的分配。
            List<Node> children = getChildren();
            for (int i = 0; i < children.size(); i++) {
                Node node = children.get(i);
                node.markTransformChangedRecursively(flagsToMark, originatingNode);
            }
        }
    }

    /**
     * 获取本地坐标系下的相对位置
     * <p>
     *     若本节点是顶级节点({@link #isTopLevel()} is true)，则结果与{@link #getWorldPosition()}相同
     * </p>
     * @see #setLocalPosition(Vector3)
     * @return 位置
     */
    public final Vector3 getLocalPosition() {
        return new Vector3(localPosition);
    }

    /**
     * 获取本地坐标系下的相对旋转四元数
     * @see #setLocalRotation(Quaternion)
     * @return 旋转四元数
     */
    public final Quaternion getLocalRotation() {
        return new Quaternion(localRotation);
    }

    /**
     * 获取本地坐标系下的相对比例
     * <p>
     *     若本节点是顶级节点({@link #isTopLevel()} is true)，则结果与{@link #getWorldScale()}相同
     * </p>
     * @see #setLocalScale(Vector3)
     * @return 比例
     */
    public final Vector3 getLocalScale() {
        return new Vector3(localScale);
    }

    /**
     * 获取世界坐标系下的绝对位置
     * @see #setWorldPosition(Vector3)
     * @return 位置
     */
    public final Vector3 getWorldPosition() {
        return new Vector3(getWorldPositionInternal());
    }

    /**
     * 获取世界坐标系下的旋转四元数
     * @see #setWorldRotation(Quaternion)
     * @return 旋转四元数
     */
    public final Quaternion getWorldRotation() {
        return new Quaternion(getWorldRotationInternal());
    }

    /**
     * 获取世界坐标系下的比例
     * @see #setWorldScale(Vector3)
     * @return 比例
     */
    public final Vector3 getWorldScale() {
        return new Vector3(getWorldScaleInternal());
    }

    /**
     * 在本地坐标系下设置相抵位置
     * @see #getLocalPosition()
     * @param position 位置
     */
    public void setLocalPosition(Vector3 position) {
        Preconditions.checkNotNull(position, "Parameter \"position\" was null.");

        localPosition.set(position);
        markTransformChangedRecursively(LOCAL_DIRTY_FLAGS, this);
    }

    /**
     * 在本地坐标系下设置相对的旋转四元数
     * <p>
     *     若本节点是顶级节点，则与{@link #setWorldRotation(Quaternion)}同样效果
     * </p>
     * @see #getLocalRotation()
     * @param rotation 旋转四元数
     */
    public void setLocalRotation(Quaternion rotation) {
        Preconditions.checkNotNull(rotation, "Parameter \"rotation\" was null.");

        localRotation.set(rotation);
        markTransformChangedRecursively(LOCAL_DIRTY_FLAGS, this);
    }

    /**
     * 在本地坐标系下设置相对比例
     * <p>
     *     若本节点是顶级节点，则与{@link #setWorldScale(Vector3)}同样效果
     * </p>
     * @see #getLocalScale()
     * @param scale 比例
     */
    public void setLocalScale(Vector3 scale) {
        Preconditions.checkNotNull(scale, "Parameter \"scale\" was null.");

        localScale.set(scale);
        markTransformChangedRecursively(LOCAL_DIRTY_FLAGS, this);
    }

    /**
     * 在世界坐标系下设置绝对位置
     * @see #getWorldPosition()
     * @param position 位置信息
     */
    public void setWorldPosition(Vector3 position) {
        Preconditions.checkNotNull(position, "Parameter \"position\" was null.");

        if (parentAsNode == null) {
            localPosition.set(position);
        } else {
            localPosition.set(parentAsNode.worldToLocalPoint(position));
        }

        markTransformChangedRecursively(LOCAL_DIRTY_FLAGS, this);

        //更新缓存
        cachedWorldPosition.set(position);
        dirtyTransformFlags &= ~WORLD_POSITION_DIRTY;
    }

    /**
     * 在世界坐标系下设置绝对的旋转四元数
     * @see #getWorldRotation()
     * @param rotation 旋转四元数
     */
    public void setWorldRotation(Quaternion rotation) {
        Preconditions.checkNotNull(rotation, "Parameter \"rotation\" was null.");

        if (parentAsNode == null) {
            localRotation.set(rotation);
        } else {
            localRotation.set(
                    Quaternion.multiply(parentAsNode.getWorldRotationInternal().inverted(), rotation));
        }

        markTransformChangedRecursively(LOCAL_DIRTY_FLAGS, this);

        //更新缓存
        cachedWorldRotation.set(rotation);
        dirtyTransformFlags &= ~WORLD_ROTATION_DIRTY;
    }

    /**
     * 在世界坐标系下设置比例
     * @see #getWorldScale()
     * @param scale 比例
     */
    public void setWorldScale(Vector3 scale) {
        Preconditions.checkNotNull(scale, "Parameter \"scale\" was null.");

        if (parentAsNode != null) {
            Node parentAsNode = this.parentAsNode;

            // 计算尺度为1的矩阵。
            // 禁止在这里改变调度变换，这样我们就不会在setWorldScale中多次发送事件
            allowDispatchTransformChangedListeners = false;
            setLocalScale(Vector3.one());
            allowDispatchTransformChangedListeners = true;
            Matrix localModelMatrix = getLocalModelMatrixInternal();

            Matrix.multiply(
                    parentAsNode.getWorldModelMatrixInternal(), localModelMatrix, cachedWorldModelMatrix);

            //这两个矩阵都会被重新计算，因此我们可以将它们用作临时存储。
            Matrix worldS = localModelMatrix;
            worldS.makeScale(scale);

            Matrix inv = cachedWorldModelMatrix;
            Matrix.invert(cachedWorldModelMatrix, inv);

            Matrix.multiply(inv, worldS, inv);
            inv.decomposeScale(localScale);
            setLocalScale(localScale);
        } else {
            setLocalScale(scale);
        }

        //更新缓存
        cachedWorldScale.set(scale);
        dirtyTransformFlags &= ~WORLD_SCALE_DIRTY;
    }

    /**
     * 将本地坐标系下的坐标转换为世界坐标系下的坐标
     *
     * @param point 本地坐标系下的坐标
     * @return 世界坐标系下的坐标
     */
    public final Vector3 localToWorldPoint(Vector3 point) {
        Preconditions.checkNotNull(point, "Parameter \"point\" was null.");

        return getWorldModelMatrixInternal().transformPoint(point);
    }

    /**
     * 将世界坐标系下的坐标转换为本地坐标系下的坐标
     *
     * @param point 世界坐标系下的坐标
     * @return 本地坐标系下的坐标
     */
    public final Vector3 worldToLocalPoint(Vector3 point) {
        Preconditions.checkNotNull(point, "Parameter \"point\" was null.");

        return getWorldModelMatrixInverseInternal().transformPoint(point);
    }

    /**
     * 将方向从该节点的本地坐标系转换为世界坐标系。不受节点位置或规模的影响。
     *
     * @param direction 待转换的方向向量
     * @return 世界坐标系下的方向向量
     */
    public final Vector3 localToWorldDirection(Vector3 direction) {
        Preconditions.checkNotNull(direction, "Parameter \"direction\" was null.");

        return Quaternion.rotateVector(getWorldRotationInternal(), direction);
    }

    /**
     * 将方向从该节点的世界坐标系转换为本地坐标系。不受节点位置或规模的影响。
     *
     * @param direction 待转换的方向向量
     * @return 本地坐标系下的方向向量
     */
    public final Vector3 worldToLocalDirection(Vector3 direction) {
        Preconditions.checkNotNull(direction, "Parameter \"direction\" was null.");

        return Quaternion.inverseRotateVector(getWorldRotationInternal(), direction);
    }

    /**
     * 获取该节点的世界空间向前的向量(-z)。
     * @return Vector3
     */
    public final Vector3 getForward() {
        return localToWorldDirection(Vector3.forward());
    }

    /**
     * 获取该节点的世界空间向后的向量(+z)。
     * @return Vector3
     */
    public final Vector3 getBack() {
        return localToWorldDirection(Vector3.back());
    }

    /**
     * 获取该节点的世界空间向右的向量(+x)。
     * @return Vector3
     */
    public final Vector3 getRight() {
        return localToWorldDirection(Vector3.right());
    }

    /**
     * 获取该节点的世界空间向左的向量(-x)。
     * @return Vector3
     */
    public final Vector3 getLeft() {
        return localToWorldDirection(Vector3.left());
    }

    /**
     * 获取该节点的世界空间向上的向量(+y)。
     * @return Vector3
     */
    public final Vector3 getUp() {
        return localToWorldDirection(Vector3.up());
    }

    /**
     * 获取该节点的世界空间向下的向量(-y)。
     * @return Vector3
     */
    public final Vector3 getDown() {
        return localToWorldDirection(Vector3.down());
    }

    /**
     * 设置渲染对象
     * @see ModelRenderable
     * @see ViewRenderable
     * @param renderable 渲染对象，若为null，则移除已渲染的对象
     * @return 创建的渲染实例
     */
    public RenderableInstance setRenderable(@Nullable Renderable renderable) {
        AndroidPreconditions.checkUiThread();

        // Renderable hasn't changed, return early.
        if (renderableInstance != null && renderableInstance.getRenderable() == renderable) {
            return renderableInstance;
        }

        if (renderableInstance != null) {
            if (active) {
                renderableInstance.detachFromRenderer();//setRenderableInstance again
            }
            //free memory
            renderableInstance.destroy();
            renderableInstance = null;
        }

        //added by ikkyu
//    if (renderableInstance != null) {
//      renderableInstance.destroy();
//      renderableInstance = null;
//    }

        if (renderable != null) {
            RenderableInstance instance = renderable.createInstance(this);
            if (active && (scene != null && !scene.isUnderTesting())) {
                instance.attachToRenderer(getRendererOrDie());
            }
            renderableInstance = instance;
            renderableId = renderable.getId().get();
        } else {
            renderableId = ChangeId.EMPTY_ID;
        }

        refreshCollider();

        return renderableInstance;
    }

    /**
     * 获取要为此节点显示的渲染对象。
     * @return 渲染对象
     */
    @Nullable
    public Renderable getRenderable() {
        if (renderableInstance == null) {
            return null;
        }

        return renderableInstance.getRenderable();
    }

    /**
     * 设置用于检测此{@link Node}的碰撞的形状。
     * <p>
     *     如果没有设置形状，并且设置了{@link Node#setRenderable(Renderable)}，
     *     则使用{@link Renderable#getCollisionShape()}来检测该{@link Node}的碰撞。
     * </p>
     * @see Scene#hitTest(Ray)
     * @see Scene#hitTestAll(Ray)
     * @see Scene#overlapTest(Node)
     * @see Scene#overlapTestAll(Node)
     * @param collisionShape 表示几何形状，如球体、盒子。如果为空，则该节点的当前碰撞形状将被删除。
     */
    public void setCollisionShape(@Nullable CollisionShape collisionShape) {
        AndroidPreconditions.checkUiThread();

        this.collisionShape = collisionShape;
        refreshCollider();
    }

    /**
     * 获取碰撞体形状
     * @see Scene#hitTest(Ray)
     * @see Scene#hitTestAll(Ray)
     * @see Scene#overlapTest(Node)
     * @see Scene#overlapTestAll(Node)
     * @return 几何形状，如球体、盒子。如果为空，则该节点的当前碰撞形状将被删除。
     */
    @Nullable
    public CollisionShape getCollisionShape() {
        if (collider != null) {
            return collider.getShape();
        }

        return null;
    }

    /**
     * 设置光源信息
     * <p>
     *     通过{@link  Light.Builder}创建光源 {@link Light}
     * </p>
     * @param light 光源
     */
    public void setLight(@Nullable Light light) {
        //如果这是相同的灯已经设置，直接返回
        if (getLight() == light) {
            return;
        }

        //销毁实例
        destroyLightInstance();

        if (light != null) {
            createLightInstance(light);
        }
    }

    /** 获取当前的光源 */
    @Nullable
    public Light getLight() {
        if (lightInstance != null) {
            return lightInstance.getLight();
        }
        return null;
    }

    /**
     * 设置节点在世界空间中看向的方向。
     * 调用此函数后，{@link Node#getForward()}将匹配传入的查找方向。
     * 向上方向将决定节点在该方向周围的方向。看向方向和向上方向不能重合(平行)，否则方向无效。
     *
     * @param lookDirection 在世界空间中看向的方向的矢量
     * @param upDirection 表示要使用的有效向上向量的向量，例如Vector3.up()
     */
    public final void setLookDirection(Vector3 lookDirection, Vector3 upDirection) {
        final Quaternion rotation = Quaternion.lookRotation(lookDirection, upDirection);
        setWorldRotation(rotation);
    }

    /**
     * 设置节点在世界空间中看向的方向。
     * 调用此方法后，{@link Node#getForward()}将匹配传入的查找方向。
     * 世界空间向上(0,1,0)将用于确定节点围绕方向的方向。
     *
     * @param lookDirection 在世界空间中表示期望的观看方向的矢量
     */
    public final void setLookDirection(Vector3 lookDirection) {
        // 默认的向上的方向
        Vector3 upDirection = Vector3.up();

        // 首先确定看向和默认向上方向是否足够远，以产生一个数值稳定的叉积。
        final float directionUpMatch = Math.abs(Vector3.dot(lookDirection, upDirection));
        if (directionUpMatch > DIRECTION_UP_EPSILON) {
            // 如果方向向量和向上向量重合，选择一个新的向上向量。
            upDirection = new Vector3(0.0f, 0.0f, 1.0f);
        }

        // 最后用适当的向上向量更新旋转姿态。
        setLookDirection(lookDirection, upDirection);
    }

    /** @hide */
    @Override
    public final Matrix getWorldModelMatrix() {
        return getWorldModelMatrixInternal();
    }

    /**
     * 激活时触发，供子类重写时使用
     * @see #isActive()
     * @see #isEnabled()
     */
    public void onActivate() {
        // （可选）重写
    }

    /**
     * 失活时触发，供子类重写时使用
     * @see #isActive()
     * @see #isEnabled()
     */
    public void onDeactivate() {
        // （可选）重写
    }

    /**
     * 更新
     * <p>每帧更新前调用</p>
     * @param frameTime 时间信息
     */
    public void onUpdate(FrameTime frameTime) {
        // Optionally override.
        if(rotateAlwaysToCamera &&
                getScene() != null &&
                getScene().getCamera() != null
        ) {
            Vector3 cameraPosition = getScene().getCamera().getWorldPosition();
            Vector3 cardPosition = getWorldPosition();
            ;
            switch (mRotateToCameraType){
                case Horizontal_Vertical:
                    break;
                case Horizontal:
                    cameraPosition.y=cardPosition.y;
                    break;
                case Vertical:
                    cameraPosition.x=cardPosition.x;
                    break;
            }

            Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
            Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
            setWorldRotation(lookRotation);
        }
    }
    public boolean isRotateAlwaysToCamera() {
        return rotateAlwaysToCamera;
    }

    public RotateToCameraType getRotateToCameraType() {
        return mRotateToCameraType;
    }

    public void setRotateToCameraType(RotateToCameraType rotateToCameraType) {
        mRotateToCameraType = rotateToCameraType;
    }

    /**
     * 设置是否让节点始终朝向相机
     * @param rotateAlwaysToCamera 启用状态
     */
    public void setRotateAlwaysToCamera(boolean rotateAlwaysToCamera) {
        this.rotateAlwaysToCamera = rotateAlwaysToCamera;
    }
    /**
     * 分发触摸事件
     * <p>内部调用</p>
     */
    public boolean onTouchEvent(HitTestResult hitTestResult, MotionEvent motionEvent) {
        Preconditions.checkNotNull(hitTestResult, "Parameter \"hitTestResult\" was null.");
        Preconditions.checkNotNull(motionEvent, "Parameter \"motionEvent\" was null.");

        boolean handled = false;

        // 重置点击跟踪数据，如果一个新的手势已经开始或如果节点已处于非活动状态。
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_DOWN || !isActive()) {
            tapTrackingData = null;
        }

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                // 只有在设置了点击监听器的情况下才开始添加点击手势。
                // 允许事件在没有侦听器时向上冒泡到节点的父节点。
                if (onTapListener == null) {
                    break;
                }

                Node hitNode = hitTestResult.getNode();
                if (hitNode == null) {
                    break;
                }

                Vector3 downPosition = new Vector3(motionEvent.getX(), motionEvent.getY(), 0.0f);
                tapTrackingData = new TapTrackingData(hitNode, downPosition);
                handled = true;
                break;
            //对于ACTION_MOVE和ACTION_UP，我们需要确保点击手势仍然有效。
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                //分配给局部变量。
                TapTrackingData tapTrackingData = this.tapTrackingData;
                if (tapTrackingData == null) {
                    break;
                }

                //确定触摸移动了多少。
                float touchSlop = getScaledTouchSlop();
                Vector3 upPosition = new Vector3(motionEvent.getX(), motionEvent.getY(), 0.0f);
                float touchDelta = Vector3.subtract(tapTrackingData.downPosition, upPosition).length();

                //确定是否仍在触摸此节点或子节点。
                hitNode = hitTestResult.getNode();
                boolean isHitValid = hitNode == tapTrackingData.downNode;

                //确定这是否是一个有效的点击。
                boolean isTapValid = isHitValid || touchDelta < touchSlop;
                if (isTapValid) {
                    handled = true;
                    //如果这是一个ACTION_UP事件，那么调用此。
                    if (actionMasked == MotionEvent.ACTION_UP && onTapListener != null) {
                        onTapListener.onTap(hitTestResult, motionEvent);
                        this.tapTrackingData = null;
                    }
                } else {
                    this.tapTrackingData = null;
                }
                break;
            default:
                // Do nothing.
        }

        return handled;
    }

    /**
     * 几何变换更新时触发
     * <p>供子类重写</p>
     */
    public void onTransformChange(Node originatingNode) {
        //重写
    }

    /**
     * 遍历层次结构并在每个节点(包括此节点)上调用方法。遍历首先是深度。
     *
     * @param consumer 要在每个节点上调用的方法
     */
    @SuppressWarnings("AndroidApiChecker")
    @Override
    public void callOnHierarchy(Consumer<Node> consumer) {
        consumer.accept(this);
        super.callOnHierarchy(consumer);
    }

    /**
     * 查找满足条件的节点
     * @param condition 查找条件
     * @return 返回匹配条件的第一个节点，否则返回null
     */
    @SuppressWarnings("AndroidApiChecker")
    @Override
    @Nullable
    public Node findInHierarchy(Predicate<Node> condition) {
        if (condition.test(this)) {
            return this;
        }

        return super.findInHierarchy(condition);
    }

    @Override
    public String toString() {
        return name + "(" + super.toString() + ")";
    }

    /** 返回该节点的父节点。 */
    @Nullable
    final NodeParent getNodeParent() {
        return parent;
    }

    @Nullable
    final Collider getCollider() {
        return collider;
    }

    int getNameHash() {
        return nameHash;
    }

    /**
     * 如果节点处于活动状态，则调用onUpdate。由SceneView用来调度更新。
     * @param frameTime 时间信息
     */
    final void dispatchUpdate(FrameTime frameTime) {
        if (!isActive()) {
            return;
        }

        //当渲染对象发生变化时的更新状态。
        Renderable renderable = getRenderable();
        if (renderable != null && renderable.getId().checkChanged(renderableId)) {
            // 刷新碰撞器，以确保它使用正确的碰撞形状，现在可渲染的已经改变。
            refreshCollider();
            renderableId = renderable.getId().get();
        }

        onUpdate(frameTime);

        for (LifecycleListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.onUpdated(this, frameTime);
        }
    }

    /**
     * 分发触摸事件
     */
    boolean dispatchTouchEvent(HitTestResult hitTestResult, MotionEvent motionEvent) {
        Preconditions.checkNotNull(hitTestResult, "Parameter \"hitTestResult\" was null.");
        Preconditions.checkNotNull(motionEvent, "Parameter \"motionEvent\" was null.");

        if (!isActive()) {
            return false;
        }

        if (dispatchToViewRenderable(motionEvent)) {
            return true;
        }

        if (onTouchListener != null && onTouchListener.onTouch(hitTestResult, motionEvent)) {
            return true;
        }

        return onTouchEvent(hitTestResult, motionEvent);
    }


    private boolean dispatchToViewRenderable(MotionEvent motionEvent) {
        return ViewTouchHelpers.dispatchTouchEventToView(this, motionEvent);
    }

    /**
     * 警告:不要直接调用这个函数，除非你知道你在做什么。设置场景
     * 并将其递归传播给所有子字段。这是自动调用时的节点
     * 从场景中添加/删除或其父更改。
     *@param scene 要设置的场景。如果为空，则场景设置为空。
     */
    final void setSceneRecursively(@Nullable Scene scene) {
        AndroidPreconditions.checkUiThread();

        //首先，设置此节点和所有子节点的场景。
        setSceneRecursivelyInternal(scene);

        //然后，递归地更新此节点和所有子节点的活动状态。
        updateActiveStatusRecursively();
    }

    @Nullable
    public RenderableInstance getRenderableInstance() {
        return renderableInstance;
    }

    Matrix getLocalModelMatrixInternal() {
        if ((dirtyTransformFlags & LOCAL_TRANSFORM_DIRTY) == LOCAL_TRANSFORM_DIRTY) {
            cachedLocalModelMatrix.makeTrs(localPosition, localRotation, localScale);
            dirtyTransformFlags &= ~LOCAL_TRANSFORM_DIRTY;
        }

        return cachedLocalModelMatrix;
    }

    Matrix getWorldModelMatrixInverseInternal() {
        if ((dirtyTransformFlags & WORLD_INVERSE_TRANSFORM_DIRTY) == WORLD_INVERSE_TRANSFORM_DIRTY) {
            //求逆矩阵，用于世界坐标系到本地坐标系的转换
            Matrix.invert(getWorldModelMatrixInternal(), cachedWorldModelMatrixInverse);
            dirtyTransformFlags &= ~WORLD_INVERSE_TRANSFORM_DIRTY;
        }

        return cachedWorldModelMatrixInverse;
    }

    private void setSceneRecursivelyInternal(@Nullable Scene scene) {
        this.scene = scene;
        for (Node node : getChildren()) {
            node.setSceneRecursively(scene);
        }
    }

    private void updateActiveStatusRecursively() {
        final boolean shouldBeActive = shouldBeActive();
        if (active != shouldBeActive) {
            if (shouldBeActive) {
                activate();
            } else {
                deactivate();
            }
        }

        for (Node node : getChildren()) {
            node.updateActiveStatusRecursively();
        }
    }

    private boolean shouldBeActive() {
        if (!enabled) {
            return false;
        }

        if (scene == null) {
            return false;
        }

        if (parentAsNode != null && !parentAsNode.isActive()) {
            return false;
        }

        return true;
    }

    private void activate() {
        AndroidPreconditions.checkUiThread();

        if (active) {
            //这应该永远不会被抛出
            throw new AssertionError("Cannot call activate while already active.");
        }

        active = true;

        if ((scene != null && !scene.isUnderTesting()) && renderableInstance != null) {
            renderableInstance.attachToRenderer(getRendererOrDie());
        }

        if (lightInstance != null) {
            lightInstance.attachToRenderer(getRendererOrDie());
        }

        if (collider != null && scene != null) {
            collider.setAttachedCollisionSystem(scene.collisionSystem);
        }

        onActivate();

        for (LifecycleListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.onActivated(this);
        }
    }

    private void deactivate() {
        AndroidPreconditions.checkUiThread();

        if (!active) {
            //这应该永远不会被抛出
            throw new AssertionError("Cannot call deactivate while already inactive.");
        }

        active = false;

        if (renderableInstance != null) {
            renderableInstance.detachFromRenderer();//active
        }

        if (lightInstance != null) {
            lightInstance.detachFromRenderer();
        }

        if (collider != null) {
            collider.setAttachedCollisionSystem(null);
        }

        onDeactivate();

        for (LifecycleListener lifecycleListener : lifecycleListeners) {
            lifecycleListener.onDeactivated(this);
        }
    }

    private void dispatchTransformChanged(Node originatingNode) {
        onTransformChange(originatingNode);

        for (int i = 0; i < transformChangedListeners.size(); i++) {
            transformChangedListeners.get(i).onTransformChanged(this, originatingNode);
        }
    }

    private void refreshCollider() {
        CollisionShape finalCollisionShape = collisionShape;

        //如果没有设置碰撞形状，则从可渲染对象返回到碰撞形状(如果有可渲染对象)。
        Renderable renderable = getRenderable();
        if (finalCollisionShape == null && renderable != null) {
            finalCollisionShape = renderable.getCollisionShape();
        }

        if (finalCollisionShape != null) {
            //如果碰撞体还不存在，就创建。
            if (collider == null) {
                collider = new Collider(this, finalCollisionShape);

                //如果节点已经激活，则将碰撞体附加到碰撞系统。
                if (active && scene != null) {
                    collider.setAttachedCollisionSystem(scene.collisionSystem);
                }
            } else if (collider.getShape() != finalCollisionShape) {
                //如果需要，将碰撞体的形状设置为新的形状。
                collider.setShape(finalCollisionShape);
            }
        } else if (collider != null) {
            //处理旧的碰撞体
            collider.setAttachedCollisionSystem(null);
            collider = null;
        }
    }

    private int getScaledTouchSlop() {
        Scene scene = getScene();
        if (scene == null
                || !AndroidPreconditions.isAndroidApiAvailable()
                || AndroidPreconditions.isUnderTesting()) {
            return DEFAULT_TOUCH_SLOP;
        }

        SceneView view = scene.getView();
        ViewConfiguration viewConfiguration = ViewConfiguration.get(view.getContext());
        return viewConfiguration.getScaledTouchSlop();
    }

    private Matrix getWorldModelMatrixInternal() {
        if ((dirtyTransformFlags & WORLD_TRANSFORM_DIRTY) == WORLD_TRANSFORM_DIRTY) {
            if (parentAsNode == null) {
                cachedWorldModelMatrix.set(getLocalModelMatrixInternal().data);
            } else {
                Matrix.multiply(
                        parentAsNode.getWorldModelMatrixInternal(),
                        getLocalModelMatrixInternal(),
                        cachedWorldModelMatrix);
            }

            dirtyTransformFlags &= ~WORLD_TRANSFORM_DIRTY;
        }

        return cachedWorldModelMatrix;
    }

    /**
     * 内部方便函数，用于访问cachedWorldPosition，确保缓存的值
     * 在被访问之前被更新。内部使用，而不是getWorldPosition，因为
     * getWorldPosition是不可变的，因此需要分配一个新的Vector每次使用。
     * @return The cachedWorldPosition.
     */
    private Vector3 getWorldPositionInternal() {
        if ((dirtyTransformFlags & WORLD_POSITION_DIRTY) == WORLD_POSITION_DIRTY) {
            if (parentAsNode != null) {
                getWorldModelMatrixInternal().decomposeTranslation(cachedWorldPosition);
            } else {
                cachedWorldPosition.set(localPosition);
            }
            dirtyTransformFlags &= ~WORLD_POSITION_DIRTY;
        }

        return cachedWorldPosition;
    }

    /**
     * 内部方便函数访问cachedworldrotion，确保缓存的值
     * 在被访问之前被更新。在内部使用，而不是getworldrotion，因为
     * getworldrotion被写为不可变的，因此需要分配一个新的四元数用于每次使用。
     * @return The cachedWorldRotation.
     */
    private Quaternion getWorldRotationInternal() {
        if ((dirtyTransformFlags & WORLD_ROTATION_DIRTY) == WORLD_ROTATION_DIRTY) {
            if (parentAsNode != null) {
                getWorldModelMatrixInternal()
                        .decomposeRotation(getWorldScaleInternal(), cachedWorldRotation);
            } else {
                cachedWorldRotation.set(localRotation);
            }
            dirtyTransformFlags &= ~WORLD_ROTATION_DIRTY;
        }

        return cachedWorldRotation;
    }

    /**
     * 内部方便函数访问cachedWorldScale，确保缓存的值是
     * 在被访问前更新。内部使用，而不是getWorldScale，因为getWorldScale
     * 被写为不可变的，因此需要为每次使用分配一个新的Vector3。
     * @return The cachedWorldScale.
     */
    private Vector3 getWorldScaleInternal() {
        if ((dirtyTransformFlags & WORLD_SCALE_DIRTY) == WORLD_SCALE_DIRTY) {
            if (parentAsNode != null) {
                getWorldModelMatrixInternal().decomposeScale(cachedWorldScale);
            } else {
                cachedWorldScale.set(localScale);
            }
            dirtyTransformFlags &= ~WORLD_SCALE_DIRTY;
        }

        return cachedWorldScale;
    }

    private void createLightInstance(Light light) {
        lightInstance = light.createInstance(this);
        if (lightInstance == null) {
            throw new NullPointerException("light.createInstance() failed - which should not happen.");
        }
        if (active) {
            lightInstance.attachToRenderer(getRendererOrDie());
        }
    }

    private void destroyLightInstance() {
        //如果light实例已经为空，那么就不需要做任何事情，只需返回即可。
        if (lightInstance == null) {
            return;
        }

        if (active) {
            lightInstance.detachFromRenderer();
        }
        lightInstance.dispose();
        lightInstance = null;
    }

    private Renderer getRendererOrDie() {
        if (scene == null) {
            throw new IllegalStateException("Unable to get Renderer.");
        }

        return Preconditions.checkNotNull(scene.getView().getRenderer());
    }
    boolean isSelect=false;//设置是否选中标识
    public boolean isSelect(){
        return isSelect;
    }
    public void setSelect(boolean select){
        isSelect=select;
    }
    public void setChildrenEnabled(boolean enabled) {
        List<Node> children = getChildren();
        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            child.setEnabled(enabled);
        }
    }
    public enum RotateToCameraType{
        Horizontal,
        Vertical,
        Horizontal_Vertical;
    }

    public void destroy(){
//        if (renderableInstance != null) {
//            renderableInstance.destroy();
//        }
//        List<Node> children =  getChildren();
//        for (Node child : children){
//            child.destroyRenderableInstance();
//        }
        setEnabled(false);
        setParent(null);
        destroyRenderInstance(this);
    }

    private void destroyRenderInstance(Node node){
        List<Node> children = node.getChildren();
        if (children.size() == 0){
            if(node.getRenderableInstance() != null){
                node.getRenderableInstance().destroy();
            }
//            node.setParent(null);
//            node.setEnabled(false);
            return;
        }
        while (children.size()!=0){
            destroyRenderInstance(children.get(0));
        }
    }
}
