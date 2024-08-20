package com.eqgis.sceneform.utilities;

/**
 * 时间累加器
 * <p>
 *     将时间样本相加。用于跟踪一组代码块的运行时间。
 * </p>
 * */
public class TimeAccumulator {
  private long elapsedTimeMs;
  private long startSampleTimeMs;

  public void beginSample() {
    startSampleTimeMs = System.currentTimeMillis();
  }

  public void endSample() {
    long endSampleTimeMs = System.currentTimeMillis();
    long sampleMs = endSampleTimeMs - startSampleTimeMs;
    elapsedTimeMs += sampleMs;
  }

  public long getElapsedTimeMs() {
    return elapsedTimeMs;
  }
}
