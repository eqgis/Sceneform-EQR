package com.eqgis.eqr.layout;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.eqgis.eqr.ar.ARFrame;
import com.eqgis.eqr.ar.ARHitResult;
import com.eqgis.eqr.ar.ARPlane;
import com.eqgis.eqr.ar.ARTrackable;
import com.eqgis.eqr.ar.OnTapArPlaneListener;
import com.eqgis.eqr.ar.TrackingState;
import com.eqgis.eqr.ResourceConfig;
import com.google.android.filament.MaterialInstance;
import com.eqgis.sceneform.ArSceneView;
import com.eqgis.sceneform.rendering.EngineInstance;
import com.eqgis.sceneform.rendering.IEngine;
import com.eqgis.sceneform.rendering.Material;
import com.eqgis.sceneform.rendering.PlaneRenderer;
import com.eqgis.sceneform.rendering.Texture;
import com.eqgis.sceneform.utilities.LoadHelper;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * AR场景助手
 **/
class ARSceneHelper {
    private ArSceneView arSceneView;
    private GestureDetector gestureDetector;
    private Material planeMaterial;
    private OnTapArPlaneListener onTapPlaneListener;

    /**
     * 构造函数
     * @param arSceneView
     */
    public ARSceneHelper(ArSceneView arSceneView) {
        this.arSceneView = arSceneView;
    }

    /**
     * 更新监听事件
     * @param onTapPlaneListener
     */
    public void updateListener(OnTapArPlaneListener onTapPlaneListener) {
        this.onTapPlaneListener = onTapPlaneListener;
    }

    /**
     * 销毁
     */
    public void destroy(){
        IEngine engine = EngineInstance.getEngine();
        if (gestureDetector != null)gestureDetector = null;
        if (planeMaterial != null){
            MaterialInstance filamentMaterialInstance = planeMaterial.getFilamentMaterialInstance();
            engine.destroyMaterialInstance(filamentMaterialInstance);
        }
    }

    /**
     * 添加平面点击监听
     */
    ARSceneHelper addPlaneTapDetector() {
        gestureDetector = new GestureDetector(arSceneView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                ARFrame frame = arSceneView.getArFrame();
                if (frame != null && onTapPlaneListener != null && motionEvent != null
                        && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
                    Iterator iterator = frame.hitTest(motionEvent).iterator();

                    while(iterator.hasNext()) {
                        ARHitResult hit = (ARHitResult)iterator.next();
                        ARTrackable trackable = hit.getTrackable();
                        if (trackable instanceof ARPlane && ((ARPlane)trackable).isPoseInPolygon(hit.getHitPose())) {
                            ARPlane plane = (ARPlane) trackable;
                            onTapPlaneListener.onTapPlane(hit, plane, motionEvent);
                            break;
                        }
                    }
                }
                return false;
            }
        });
//        arSceneView.getScene().addOnPeekTouchListener((hitTestResult, motionEvent) -> {
//            if (hitTestResult.getNode()==null){
//                gestureDetector.onTouchEvent(motionEvent);}
//        });

        return this;
    }

    /**
     * 添加平面渲染
     */
    ARSceneHelper addPlaneRenderer() {
        Context context = arSceneView.getContext();
        if (ResourceConfig.planeTextureId == null){
            //采用默认
            ResourceConfig.planeTextureId = LoadHelper.drawableResourceNameToIdentifier(context, "trigrid");
        }
        // Build texture sampler
        Texture.Sampler sampler = Texture.Sampler.builder()
                .setMinFilter(Texture.Sampler.MinFilter.LINEAR)
                .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
                .setWrapMode(Texture.Sampler.WrapMode.REPEAT).build();

        // Build texture with sampler
        CompletableFuture<Texture> trigrid = Texture.builder()
                .setSource(context, ResourceConfig.planeTextureId)
                .setSampler(sampler).build();

        // Set plane texture
        arSceneView.getPlaneRenderer()
                .getMaterial()
                .thenAcceptBoth(trigrid, new BiConsumer<Material, Texture>() {
                            @Override
                            public void accept(Material material, Texture texture) {
                                //ARCore
                                material.setTexture(PlaneRenderer.MATERIAL_TEXTURE, texture);
                                planeMaterial = material;
                            }
                        }
                );

        return this;
    }
}
