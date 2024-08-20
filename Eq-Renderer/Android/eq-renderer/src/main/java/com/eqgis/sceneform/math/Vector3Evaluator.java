package com.eqgis.sceneform.math;

import android.animation.TypeEvaluator;

/**
 * 三维向量（坐标）的估值器
 * <p>多用于位移动画</p>
 * */
public class Vector3Evaluator implements TypeEvaluator<Vector3> {
  @Override
  public Vector3 evaluate(float fraction, Vector3 startValue, Vector3 endValue) {
    return Vector3.lerp(startValue, endValue, fraction);
  }
}
