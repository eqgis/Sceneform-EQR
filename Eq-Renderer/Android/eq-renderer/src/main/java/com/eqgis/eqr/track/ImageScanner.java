package com.eqgis.eqr.track;


import android.graphics.Bitmap;

import com.eqgis.eqr.ar.ARFrame;
import com.eqgis.eqr.layout.ARSceneLayout;
import com.eqgis.sceneform.FrameTime;
import com.eqgis.sceneform.Scene;
import com.eqgis.eqr.ar.ARAugmentedImage;

import java.util.Collection;

/**
 * 图片扫描器
 */
public class ImageScanner {
    private static ImageScanner imageScanner = null;
    private ARSceneLayout layout;

    private ImageDB imageDB;
    private Scene.OnUpdateListener frameListener;
    private boolean isInit = false;
    private ARImageTrackListener trackListener;

    /**
     * 构造函数
     * @param arEffectView
     */
    private ImageScanner(ARSceneLayout arEffectView) {
        this.layout = arEffectView;
        imageDB = ImageDB.getInstance(arEffectView);
    }

    /**
     * 获取实例
     * @param arEffectView
     * @return
     */
    public static ImageScanner getInstance(ARSceneLayout arEffectView){
        if (arEffectView == null){
            throw new NullPointerException();
        }

        if (imageScanner == null){
            imageScanner = new ImageScanner(arEffectView);
        }

        return imageScanner;
    }

    /**
     * 添加图片资源
     * @param resourceName
     */
    public void addImage(String resourceName){
        imageDB.addImage(resourceName);
    }

    public void addImage(String name, Bitmap img){
        imageDB.addImage(name,img);
    }

    public void addImage(String name, Bitmap img,float widthInMeters){
        imageDB.addImage(name,img,widthInMeters);
    }

    /**
     * 添加图片监听事件
     * @param trackListener
     */
    public void setImageListener(final ARImageTrackListener trackListener){
        if (trackListener == null){
            disposeImageListener();
            return;
        }

        this.trackListener = trackListener;
        if (frameListener == null){
            frameListener = new Scene.OnUpdateListener() {
                @Override
                public void onUpdate(FrameTime frameTime) {
                    ARFrame frame = layout.getArFrame();
                    Collection<ARAugmentedImage> augmentedImages = frame.getUpdatedAugmentedImage();
//                    Collection<AugmentedImage> augmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);
                    trackListener.onImageChanged(augmentedImages);
                }
            };
            //scene添加监听事件
            layout.addSceneUpdateListener(frameListener);
        }

        //ImageDB初始化
        if (!isInit){
            imageDB.init();
            isInit = true;
        }
    }

    /**
     * 释放监听事件
     */
    public void disposeImageListener(){
        if (frameListener != null){
            //移除已存在的场景监听事件
            layout.removeSceneUpdateListener(frameListener);
        }
        frameListener = null;
    }

}
