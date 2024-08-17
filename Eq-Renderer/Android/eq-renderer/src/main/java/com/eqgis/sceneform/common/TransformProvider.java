package com.eqgis.sceneform.common;

import com.eqgis.sceneform.math.Matrix;
import com.eqgis.sceneform.Node;

/**
 * TransformProvider
 * <p>用于模型变换，需实现模型矩阵获取的接口</p>
 * See {@link Node}.
 * @hide
 */
public interface TransformProvider {
  /**
   * 获取世界坐标系的下的模型矩阵
   * @return 矩阵
   */
  Matrix getWorldModelMatrix();
}
