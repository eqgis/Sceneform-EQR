package com.eqgis.eqr.core;

/**
 * FilamentAsset的图元工具类
 * @author tanyx 2025/12/26
 * @version 1.0
 **/
public class FilamentPrimitiveUtilsNative {

    /**
     * 在原 Entity 上重建 geometry，仅修改 PrimitiveType
     */
    public static native boolean nRebuildPrimitiveGeometry(
            long nativeEngine,
            long nativeAsset,     // FilamentAsset nativeObject
            int entity,           // 原 Entity (int)
            int primitiveType     // RenderableManager.PrimitiveType value
    );

    /**
     * 根据顶点位置和索引计算法线
     * @param positions 顶点位置
     * @param indices 索引
     * @return 法线
     */
    public static native float[] nComputeVertexNormals( float[] positions,int[] indices);
}

