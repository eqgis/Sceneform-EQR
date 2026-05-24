package com.eqgis.test.fragments.common;

import android.net.Uri;
import android.view.MotionEvent;

import com.eqgis.ar.ARAnchor;
import com.eqgis.ar.ARHitResult;
import com.eqgis.ar.ARPlane;
import com.eqgis.ar.OnTapArPlaneListener;
import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.utils.ScaleTool;
import com.eqgis.test.fragments.BaseArSampleFragment;
import com.google.sceneform.AnchorNode;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.ModelRenderable;

/**
 * AR 平面检测常用示例 Fragment
 * <pre>
 *     开启 AR 平面渲染，点击真实平面后创建锚点并加载 GLTF 模型。
 *     当前常用示例默认跳转原 Activity，本类保留给后续 Fragment 化迁移。
 * </pre>
 * @author tanyx
 */
public class ArPlaneCommonFragment extends BaseArSampleFragment {
    @Override
    protected String getLessonTitle() {
        return "AR 平面检测";
    }

    @Override
    protected String getLessonDescription() {
        return "移动手机识别平面，点击平面后创建锚点并放置 GLTF 模型。需要设备支持 ARCore 或 AREngine。";
    }

    /**
     * 初始化 AR 平面检测场景
     * @param sceneLayout {@link SceneLayout} 当前 Fragment 独立持有的渲染布局
     */
    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        sceneLayout.addIndirectLight("enviroments/light/lightroom_ibl.ktx", 100);
        arSceneLayout.setPlaneRendererEnabled(true);
        arSceneLayout.setOnTapPlaneListener(new OnTapArPlaneListener() {
            @Override
            public void onTapPlane(ARHitResult hitResult, ARPlane plane, MotionEvent motionEvent) {
                //desc- 命中平面后创建锚点，模型挂到 AnchorNode 上才能跟随 AR 跟踪结果保持稳定。
                ARAnchor anchor = hitResult.createAnchor();
                AnchorNode modelNode = new AnchorNode(anchor);
                ModelRenderable.builder()
                        .setSource(requireContext(), Uri.parse("gltf/bee.glb"))
                        .setIsFilamentGltf(true)
                        .build()
                        .thenApply(modelRenderable -> {
                            modelNode.setRenderable(modelRenderable);
                            modelNode.setLocalScale(Vector3.one()
                                    .scaled(ScaleTool.calculateUnitsScale(modelRenderable) * 0.1f));
                            modelNode.setParent(arSceneLayout.getRootNode());
                            return null;
                        });
            }
        });
    }
}
