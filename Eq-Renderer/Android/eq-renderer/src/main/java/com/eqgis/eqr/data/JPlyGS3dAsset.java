package com.eqgis.eqr.data;

import androidx.annotation.Keep;

/**
 * 3DGS数据(Ply格式)
 * <p>在Native创建，供Java读取</p>
 * @author tanyx 2026年1月7日21:39:09
 * @version 1.0
 */
@Keep
public class JPlyGS3dAsset {

    @Keep
    public float[] vertices;

    @Keep
    public float[] normals;

    @Keep
    public float[] aabb;

    // 3DGS特定属性
    @Keep public float[] f_dc;        // 球谐DC分量 [r, g, b] × N
    @Keep public float[] f_rest;      // 球谐剩余分量 (45维 × N)
    @Keep public float[] opacity;     // 不透明度 × N
    @Keep public float[] scale;       // scale_x, scale_y, scale_z × N
    @Keep public float[] rot;    // 四元数 rot_w, rot_x, rot_y, rot_z × N

    // 元数据
    @Keep public boolean is3DGS;      // 是否为3DGS文件
    @Keep public int pointCount;      // 高斯点数量
    @Keep public int shDegree;        // 球谐阶数

    //维度
    public int dimension;

}
