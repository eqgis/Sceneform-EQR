package com.google.sceneform;

import java.util.concurrent.TimeUnit;

/**
 * 当前帧的时间信息对象
 * <p>
 *     提供当前帧的时间信息。
 * </p>
 * */
public class FrameTime {
  private long lastNanoTime = 0;
  private long deltaNanoseconds = 0;

  private static final float NANOSECONDS_TO_SECONDS = 1.0f / 1_000_000_000.0f;

  /** 获取这一帧和上一帧之间的时间，以秒为单位。 */
  public float getDeltaSeconds() {
    return deltaNanoseconds * NANOSECONDS_TO_SECONDS;
  }

  /** 获取距离开始时的时间 */
  public float getStartSeconds() {
    return lastNanoTime * NANOSECONDS_TO_SECONDS;
  }

  /**
   * 获取这一帧和上一帧之间的时间
   * @param unit 时间单位
   * @return 两帧之间的时间
   */
  public long getDeltaTime(TimeUnit unit) {
    return unit.convert(deltaNanoseconds, TimeUnit.NANOSECONDS);
  }

  /**
   * 获取距离开始时的时间
   *
   * @param unit 时间单位
   * @return 距离开始时的时间
   */
  public long getStartTime(TimeUnit unit) {
    return unit.convert(lastNanoTime, TimeUnit.NANOSECONDS);
  }

  /** FrameTime仅在内部创建。通常在onUpdate事件中。 */
  public FrameTime() {}

  public void update(long frameTimeNanos) {
    deltaNanoseconds = (lastNanoTime == 0) ? 0 : (frameTimeNanos - lastNanoTime);
    lastNanoTime = frameTimeNanos;
  }
}
