package com.eqgis.eqr.core;


import android.content.Context;
import android.util.Log;

import com.eqgis.eqr.data.JPlyAsset;
import com.google.android.filament.Box;
import com.google.android.filament.Engine;
import com.google.android.filament.Entity;
import com.google.android.filament.EntityManager;
import com.google.android.filament.RenderableManager;

/**
 * <p></p>
 * <pre>SampleCode:
 * </pre>
 *
 * @author tanyx 2026/1/4
 * @version 1.0
 **/
public class PlyLoader {
    private long mPlyLoaderNativeObject;
    private Box boundingBox;

    public PlyLoader() {
    }

    public JPlyAsset createAssets(byte[] buffer){
        JPlyAsset jPlyAsset = new JPlyAsset();
        mPlyLoaderNativeObject = nLoadFromBytes(buffer);
        nFill(mPlyLoaderNativeObject, jPlyAsset);

        boundingBox = new Box((jPlyAsset.aabb[0] + jPlyAsset.aabb[3]) / 2,
                (jPlyAsset.aabb[1] + jPlyAsset.aabb[4]) / 2,
                (jPlyAsset.aabb[2] + jPlyAsset.aabb[5]) / 2,
                (jPlyAsset.aabb[3] - jPlyAsset.aabb[0]) / 2,
                (jPlyAsset.aabb[4] - jPlyAsset.aabb[1]) / 2,
                (jPlyAsset.aabb[5] - jPlyAsset.aabb[2]) / 2);

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < jPlyAsset.faces.length; i++) {
            stringBuilder.append(" ").append(jPlyAsset.faces[i]);
        }
        Log.i("IKKYU ", "createAssets: faces-> " + stringBuilder.toString());
        System.out.println();

        Log.d(PlyLoader.class.getSimpleName(), "Ikkyu createAssets: "+mPlyLoaderNativeObject);
        return jPlyAsset;
    }


    public Box getBoundingBox() {
        return boundingBox;
    }

    /**
     * 获取数据的根节点实体对象
     * <p>
     *     Filament创建的Entity对象
     * </p>
     */
//    public @Entity int getRoot() {
//        return root;
//    }

    public void destroy() {
        if (mPlyLoaderNativeObject != 0){
            nDestroyPlyAsset(mPlyLoaderNativeObject);
        }
    }


    public static native long nLoadFromBytes(byte[] plyData);
    public static native void nFill(long mPlyLoaderNativeObject,JPlyAsset jPlyAsset);

    public static native void nDestroyPlyAsset(long nativeObject);

}
