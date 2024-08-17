package com.eqgis.sceneform.math;

import android.animation.TypeEvaluator;

/**
 * 四元数的估值器
 * <p>多用于旋转动画</p>
 * */
public class QuaternionEvaluator implements TypeEvaluator<Quaternion> {
  @Override
  public Quaternion evaluate(float fraction, Quaternion startValue, Quaternion endValue) {
    return Quaternion.slerp(startValue, endValue, fraction);
  }
}
