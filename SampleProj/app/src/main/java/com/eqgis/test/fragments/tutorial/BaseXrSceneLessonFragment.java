package com.eqgis.test.fragments.tutorial;

import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.ARSceneLayout;
import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.utils.ScaleTool;
import com.eqgis.test.R;
import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.MaterialFactory;
import com.google.sceneform.rendering.ModelRenderable;
import com.google.sceneform.rendering.Renderable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * XR 场景教程公共基类
 * <pre>
 *     统一关闭触摸相机手势，保留 AR/Camera/VR 自身的设备姿态控制；提供常驻状态横幅、
 *     GLTF 模型加载、基础参照物创建及销毁前节点解绑，避免各 XR 页面重复生命周期代码。
 * </pre>
 * @author tanyx
 */
public abstract class BaseXrSceneLessonFragment extends BaseTutorialFragment {
    private static final String TAG = BaseXrSceneLessonFragment.class.getSimpleName();

    private final List<Node> xrNodes = new ArrayList<>();
    private TextView statusBanner;

    /**
     * AR、Camera 3DoF 与 VR 视图由设备姿态控制相机，不叠加触摸相机手势
     * @return 始终返回 false
     */
    @Override
    protected boolean isCameraGestureEnabled() {
        return false;
    }

    /**
     * XR 页面使用常驻状态横幅，不创建空的可折叠参数面板
     * @return 始终返回 false
     */
    @Override
    protected boolean shouldOverlayActions() {
        return false;
    }

    /**
     * 配置 XR 页面通用相机与光照
     * @param layout 当前场景布局
     * @param showSkybox 是否显示天空盒；相机背景或 AR 背景下应传 false
     */
    protected final void configureXrScene(SceneLayout layout, boolean showSkybox) {
        layout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 75);
        //desc- AR 相机投影矩阵由 ARCore/AREngine 每帧更新，只有 Camera 3DoF、VR 和普通场景允许手动设置 FOV。
        if (!(layout instanceof ARSceneLayout)) {
            layout.getCamera().setVerticalFovDegrees(58);
        }
        layout.getCamera().setFarClipPlane(100);
        if (showSkybox) {
            layout.setSkybox("enviroments/pillars_2k_skybox.ktx");
        }
    }

    /**
     * 在渲染区域顶部显示不会被折叠的运行状态
     * @param text 状态文案
     * @param warning true 使用警告样式，false 使用信息样式
     */
    protected final void showStatusBanner(String text, boolean warning) {
        if (sceneLayout == null) {
            return;
        }
        if (statusBanner == null) {
            statusBanner = new TextView(requireContext());
            statusBanner.setTextSize(14);
            statusBanner.setTypeface(null, Typeface.BOLD);
            statusBanner.setLineSpacing(dp(2), 1.0f);
            statusBanner.setPadding(dp(14), dp(11), dp(14), dp(11));
            statusBanner.setElevation(dp(8));
            statusBanner.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.TOP | Gravity.START);
            params.setMargins(dp(12), dp(12), dp(12), 0);
            sceneLayout.addView(statusBanner, params);
        }
        statusBanner.setText(text);
        statusBanner.setTextColor(warning ? 0xff6a3b00 : 0xff073b74);
        statusBanner.setBackgroundResource(
                warning ? R.drawable.bg_xr_status_warning : R.drawable.bg_xr_status_info);
    }

    /**
     * 异步加载 GLTF，并在安全的场景代际中返回 Renderable
     * @param assetPath assets 下的 GLTF/GLB 路径
     * @param consumer 加载成功回调
     */
    protected final void loadXrRenderable(String assetPath, Consumer<ModelRenderable> consumer) {
        ModelRenderable.builder()
                .setSource(requireContext(), Uri.parse(assetPath))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(renderable -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    consumer.accept(renderable);
                })
                .exceptionally(error -> {
                    Log.e(TAG, "加载 XR 教程模型失败: " + assetPath, error);
                    if (isSceneActive()) {
                        showStatusBanner("模型资源加载失败，请查看 Logcat。", true);
                    }
                    return null;
                });
    }

    /**
     * 将已加载模型添加到当前场景
     * @param renderable 模型 Renderable
     * @param position 模型局部位置
     * @param relativeScale 相对于单位缩放的比例
     * @return 新建模型节点
     */
    protected final Node addXrModel(ModelRenderable renderable,
                                    Vector3 position,
                                    float relativeScale) {
        Node node = addXrNode(renderable, position);
        node.setLocalScale(Vector3.one().scaled(
                ScaleTool.calculateUnitsScale(renderable) * relativeScale));
        return node;
    }

    /**
     * 创建场景参照 Cube
     * @param position 节点位置
     * @param size Cube 边长
     * @param color 材质颜色
     */
    protected final void addReferenceCube(Vector3 position, float size, Color color) {
        MaterialFactory.makeOpaqueWithColor(requireContext(), color)
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    addXrNode(
                            GeometryUtils.makeCube(
                                    Vector3.one().scaled(size),
                                    Vector3.zero(),
                                    material),
                            position);
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建 XR 参照 Cube 失败", error);
                    return null;
                });
    }

    /**
     * 创建场景参照平面
     * @param position 平面位置
     * @param width 平面宽度
     * @param depth 平面深度
     * @param color 材质颜色
     */
    protected final void addReferencePlane(Vector3 position,
                                           float width,
                                           float depth,
                                           Color color) {
        MaterialFactory.makeOpaqueWithColor(requireContext(), color)
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    addXrNode(
                            GeometryUtils.makePlane(
                                    new Vector3(width, 0, depth),
                                    Vector3.zero(),
                                    material),
                            position);
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建 XR 参照平面失败", error);
                    return null;
                });
    }

    /**
     * 将 Renderable 节点加入场景并纳入当前 Fragment 生命周期
     * @param renderable 渲染对象
     * @param position 节点位置
     * @return 新建节点
     */
    protected final Node addXrNode(Renderable renderable, Vector3 position) {
        Node node = new Node();
        node.setRenderable(renderable);
        node.setLocalPosition(position);
        node.setParent(sceneLayout.getRootNode());
        trackXrNode(node);
        return node;
    }

    /**
     * 将 AnchorNode 等外部创建的节点纳入销毁管理
     * @param node 需要跟随页面销毁的节点
     */
    protected final void trackXrNode(Node node) {
        if (node != null && !xrNodes.contains(node)) {
            xrNodes.add(node);
        }
    }

    /**
     * 子类释放 AR 监听、Anchor 等专有资源
     */
    protected void onReleaseXrScene() {
    }

    /**
     * SceneLayout 销毁前解除 XR 页面持有的节点、监听和横幅引用
     */
    @Override
    protected final void onBeforeDestroyScene() {
        onReleaseXrScene();
        for (Node node : new ArrayList<>(xrNodes)) {
            node.setRenderable(null);
            node.setParent(null);
        }
        xrNodes.clear();
        if (statusBanner != null && statusBanner.getParent() instanceof ViewGroup) {
            ((ViewGroup) statusBanner.getParent()).removeView(statusBanner);
        }
        statusBanner = null;
    }
}
