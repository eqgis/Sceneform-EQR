package com.eqgis.eqr.track;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.eqgis.ar.ARAugmentedImageDatabase;
import com.eqgis.ar.ARSession;
import com.eqgis.ar.ARConfig;
import com.eqgis.ar.exceptions.ARCameraException;
import com.eqgis.eqr.layout.ARSceneLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tanyunxiu
 *      SampleCode:
 *         ImageDB imageDB = ImageDB.getInstance(arEffectView);
 *         imageDB.addImage("map.png");
 *         imageDB.init();
 */
class ImageDB {
    private static ImageDB imageDB = null;

    private ARSceneLayout arSceneLayout;
    private Context mContext;
    private ARSession mSession;
    private ARAugmentedImageDatabase augmentedImageDatabase;

    private List<Bitmap> imgList = new ArrayList<Bitmap>();
    private ARConfig config;

    private ImageDB(ARSceneLayout arEffectView) {
        this.arSceneLayout = arEffectView;
        this.mContext = arEffectView.getContext();
        this.mSession = arSceneLayout.getSession();
//        config = new com.supermap.hiar.ARConfig(mSession);
        config = arSceneLayout.getArConfig();
        augmentedImageDatabase = new ARAugmentedImageDatabase(mSession);

    }

    /**
     * 获取实例
     * @param layout
     * @return
     */
    public static ImageDB getInstance(ARSceneLayout layout){
        if (imageDB == null){
            imageDB = new ImageDB(layout);
        }
        return imageDB;
    }

    /**
     * 添加图片
     * @param resource
     */
    public void addImage(String resource){
        String name = resource;

        Bitmap targetIMG = loadAugmentedImage(resource);
        if (targetIMG == null) {
            throw new NullPointerException();
        }

//        augmentedImageDatabase.addImage(name,targetIMG);
//        config.setAugmentedImageDatabase(augmentedImageDatabase);
        this.addImage(name,targetIMG);
    }

    /**
     * 添加图片
     * @param name
     * @param img
     */
    public void addImage(String name,Bitmap img){
        mSession.pause();
        augmentedImageDatabase.addImage(name,img);
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        try {
            mSession.resume();
        } catch (ARCameraException e) {
            throw new RuntimeException("ARSession resume failed.",e);
        }
    }

    /**
     * 添加图片
     * @param name
     * @param img
     * @param widthInMeters 物理世界的图片尺寸
     */
    public void addImage(String name,Bitmap img,float widthInMeters){
        mSession.pause();
        augmentedImageDatabase.addImage(name,img,widthInMeters);
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        try {
            mSession.resume();
        } catch (ARCameraException e) {
            throw new RuntimeException("ARSession resume failed.",e);
        }
    }

    /**
     * 初始化
     */
    public void init(){
        config.setAugmentedImageDatabase(augmentedImageDatabase);

        config.setUpdateMode(ARConfig.UpdateMode.LATEST_CAMERA_IMAGE);
        config.setFocusMode(ARConfig.FocusMode.AUTO_FOCUS);
        mSession.configure(config);
    }

    private Bitmap loadAugmentedImage(String imgName) {
        try {
            InputStream is = mContext.getAssets().open(imgName);
            try {
                return BitmapFactory.decodeStream(is);
            } finally {
                is.close();
            }
        } catch (IOException e) {
            Log.e("ImageLoad", "IO Exception while loading", e);
        }
        return null;
    }

}
