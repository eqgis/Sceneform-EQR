package com.eqgis.test.fragments.tutorial;

import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;

import com.eqgis.eqr.geometry.GeometryUtils;
import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.Node;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.MaterialFactory;
import com.google.sceneform.rendering.ViewRenderable;

import java.util.ArrayList;
import java.util.List;

/**
 * Android View 三维渲染教程基类
 * <pre>
 *     为每个 ViewRenderable 教程创建独立 SceneLayout，并统一绘制参考平面和 Cube。
 *     子类只负责创建 Android View，基类负责转换为 ViewRenderable、挂载节点和释放资源。
 * </pre>
 * @author tanyx
 */
public abstract class BaseAndroidViewLessonFragment extends BaseTutorialFragment {
    private static final String TAG = BaseAndroidViewLessonFragment.class.getSimpleName();

    private final List<Node> sceneNodes = new ArrayList<>();
    private final List<View> androidViews = new ArrayList<>();

    /**
     * 初始化 Android View 教程场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected final void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 80);
        sceneLayout.getCamera().setVerticalFovDegrees(67);
        sceneLayout.getCamera().setFarClipPlane(100);
        addReferenceGeometry();
        addViewExamples();
    }

    /**
     * 添加当前子项需要展示的 Android View
     */
    protected abstract void addViewExamples();

    /**
     * 将 Android View 渲染到三维场景
     * @param view 需要渲染的 Android View
     * @param position 世界坐标位置
     * @param rotation 世界坐标旋转
     * @param scale 节点缩放比例
     */
    protected final void addViewRenderable(View view, Vector3 position, Quaternion rotation, float scale) {
        androidViews.add(view);
        buildViewRenderable(
                ViewRenderable.builder().setView(requireContext(), view),
                position,
                rotation,
                scale);
    }

    /**
     * 将单个 XML Layout 渲染到三维场景
     * @param layoutResId Layout 资源 id
     * @param position 世界坐标位置
     * @param rotation 世界坐标旋转
     * @param scale 节点缩放比例
     */
    protected final void addLayoutRenderable(@LayoutRes int layoutResId, Vector3 position,
                                             Quaternion rotation, float scale) {
        buildViewRenderable(
                ViewRenderable.builder().setView(requireContext(), layoutResId),
                position,
                rotation,
                scale);
    }

    /**
     * 创建适合 ViewRenderable 演示的轻量圆角背景
     * @param fillColor 填充颜色
     * @param strokeColor 描边颜色
     * @return 圆角背景对象
     */
    protected final GradientDrawable createRoundedBackground(@ColorInt int fillColor,
                                                              @ColorInt int strokeColor) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(fillColor);
        background.setCornerRadius(dp(14));
        background.setStroke(dp(1), strokeColor);
        return background;
    }

    private void addReferenceGeometry() {
        MaterialFactory.makeTransparentWithColor(
                        requireContext(),
                        new Color(0.18f, 0.48f, 0.88f, 0.34f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    Node planeNode = new Node();
                    planeNode.setRenderable(GeometryUtils.makePlane(
                            new Vector3(4.6f, 1.0f, 3.8f),
                            new Vector3(0, -0.95f, -3.2f),
                            material));
                    attachSceneNode(planeNode);
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建参考平面失败", error);
                    return null;
                });

        MaterialFactory.makeOpaqueWithColor(
                        requireContext(),
                        new Color(1.0f, 0.55f, 0.12f))
                .thenAccept(material -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    Node cubeNode = new Node();
                    cubeNode.setRenderable(GeometryUtils.makeCube(
                            new Vector3(0.58f, 0.58f, 0.58f),
                            Vector3.zero(),
                            material));
                    cubeNode.setWorldPosition(new Vector3(1.25f, -0.62f, -3.0f));
                    cubeNode.setWorldRotation(new Quaternion(Vector3.up(), 28));
                    attachSceneNode(cubeNode);
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建参考 Cube 失败", error);
                    return null;
                });
    }

    private void buildViewRenderable(ViewRenderable.Builder builder, Vector3 position,
                                     Quaternion rotation, float scale) {
        builder.setVerticalAlignment(ViewRenderable.VerticalAlignment.BOTTOM)
                .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
                .build()
                .thenAccept(viewRenderable -> {
                    if (!isSceneActive()) {
                        return;
                    }
                    if (!androidViews.contains(viewRenderable.getView())) {
                        androidViews.add(viewRenderable.getView());
                    }
                    Node node = new Node();
                    node.setRenderable(viewRenderable);
                    node.setWorldScale(Vector3.one().scaled(scale));
                    node.setWorldPosition(position);
                    node.setWorldRotation(rotation);
                    attachSceneNode(node);
                })
                .exceptionally(error -> {
                    Log.e(TAG, "创建 ViewRenderable 失败", error);
                    return null;
                });
    }

    private void attachSceneNode(Node node) {
        if (!isSceneActive()) {
            return;
        }
        node.setParent(sceneLayout.getRootNode());
        sceneNodes.add(node);
    }

    /**
     * 在 SceneLayout 销毁前释放节点及 Android View 资源
     */
    @Override
    protected void onBeforeDestroyScene() {
        for (Node node : sceneNodes) {
            node.setRenderable(null);
            node.setParent(null);
        }
        sceneNodes.clear();
        for (View view : androidViews) {
            releaseAndroidView(view);
        }
        androidViews.clear();
    }

    private void releaseAndroidView(View view) {
        if (view instanceof WebView) {
            WebView webView = (WebView) view;
            //desc- WebView 持有独立渲染和页面资源，切换教程时必须主动停止并销毁。
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.clearHistory();
            webView.removeAllViews();
            webView.destroy();
        }
    }
}
