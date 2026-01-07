package com.eqgis.eqr.data;

import androidx.annotation.Keep;

/**
 * Ply数据
 * <p>在Native创建，供Java读取</p>
 */
@Keep
public class JPlyAsset {

    @Keep
    public float[] vertices;

    @Keep
    public float[] normals;

    @Keep
    public float[] texcoords;

    @Keep
    public float[] colors;

    @Keep
    public int[] faces;

    @Keep
    public int[] tripstrip;

    @Keep
    public float[] aabb;
}
