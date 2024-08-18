package com.eqgis.sceneform.utilities;

/**
 * 计算一系列数据的指数加权移动平均线。
 * <p>
 *     移动平均：一种通过使用过去若干时间段的平均值计算得出的平均值。移动平均值会定期变化，最早的数值会被基于最新数据的数值所替代。
 * </p>
 * @hide
 */
public class MovingAverage {
  private double average;
  private final double weight;

  public static final double DEFAULT_WEIGHT = 0.9f;

  /**
   * 构造函数
   * <p>权重是0和1之间的比率，表示前一个平均值的多少
   * *与新样品作比较。如果权重为0.9，则保留之前平均值的90%
   * *新样品的10%加到平均值中。
   *
   * @param initialSample 第一个样本的平均值
   */
  public MovingAverage(double initialSample) {
    this(initialSample, DEFAULT_WEIGHT);
  }

  /**
   * 构造函数
   * <p>权重是0和1之间的比率，表示前一个平均值的多少
   * *与新样品作比较。如果权重为0.9，则保留之前平均值的90%
   * *新样品的10%加到平均值中。
   *
   * @param initialSample 第一个样本的平均值
   * @param weight 添加样品时使用的权重
   */
  public MovingAverage(double initialSample, double weight) {
    average = initialSample;
    this.weight = weight;
  }

  /** 添加一个样本并计算一个新的平均值。 */
  public void addSample(double sample) {
    average = weight * average + (1.0 - weight) * sample;
  }

  /** 返回所有样本的当前平均值。 */
  public double getAverage() {
    return average;
  }
}
