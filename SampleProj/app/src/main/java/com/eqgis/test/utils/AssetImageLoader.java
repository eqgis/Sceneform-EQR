package com.eqgis.test.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * 图片加载工具类
 * @author tanyx 2025/11/5
 * @version 1.0
 **/
public class AssetImageLoader {
    /**
     * 从 assets 目录读取图片并返回 Bitmap
     *
     * @param context 上下文（Activity 或 Application）
     * @param assetPath 资源路径，例如 "img/earth.png"
     * @return Bitmap 对象，如果读取失败则返回 null
     */
    public static Bitmap loadBitmapFromAssets(Context context, String assetPath) {
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(assetPath);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException ignored) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
        return bitmap;
    }
}
