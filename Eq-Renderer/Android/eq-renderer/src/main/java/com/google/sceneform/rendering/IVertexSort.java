package com.google.sceneform.rendering;

import com.google.sceneform.math.Matrix;

/**
 * 顶点排序接口
 * @author tanyx 2026/01/26/20:22
 * @version 1.0
 **/
public interface IVertexSort {
    /**
     * <p>
     *     CPU顶点排序，在使用Transparent混合的材质中，需对顶点进行排序。否则会渲染错乱
     * </p>
     */
    void sortForViewChange(Matrix cameraModelMat, Matrix modelModelMat);
}
