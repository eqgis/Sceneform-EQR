package com.eqgis.eqr.geometry;

/**
 * Line3D 管线顶点计算模式。
 * <pre>
 *     控制管线端点附近的半径是否进行重采样，用于在保持原始半径和优化端点过渡之间切换。
 * </pre>
 * @author tanyx
 */
public enum Line3DPipePointMode {
    /** 不修改端点附近的半径，保持输入半径。 */
    ORIGINAL,

    /** 使用 Math.sin 对端点附近的半径进行重采样，使端点过渡更平滑。 */
    SIN_RESAMPLE
}
