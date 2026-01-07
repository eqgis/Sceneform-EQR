package com.eqgis.eqr.core;

import com.eqgis.eqr.data.JPlyAsset;
import com.google.android.filament.Box;

/**
 * Ply文件加载器
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

        //用完即销毁
        destroyPlyAsset();

        boundingBox = new Box((jPlyAsset.aabb[0] + jPlyAsset.aabb[3]) / 2,
                (jPlyAsset.aabb[1] + jPlyAsset.aabb[4]) / 2,
                (jPlyAsset.aabb[2] + jPlyAsset.aabb[5]) / 2,
                (jPlyAsset.aabb[3] - jPlyAsset.aabb[0]) / 2,
                (jPlyAsset.aabb[4] - jPlyAsset.aabb[1]) / 2,
                (jPlyAsset.aabb[5] - jPlyAsset.aabb[2]) / 2);

        return jPlyAsset;
    }


    public Box getBoundingBox() {
        return boundingBox;
    }


    /**
     * 销毁native成资产
     */
    public void destroyPlyAsset() {
        if (mPlyLoaderNativeObject != 0){
            nDestroyPlyAsset(mPlyLoaderNativeObject);
            mPlyLoaderNativeObject = 0;
        }
    }


    public static native long nLoadFromBytes(byte[] plyData);
    public static native void nFill(long mPlyLoaderNativeObject,JPlyAsset jPlyAsset);

    public static native void nDestroyPlyAsset(long nativeObject);

}
