package com.eqgis.eqr.core;

/**
 * <p></p>
 * <pre>SampleCode:
 * </pre>
 *
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
}

