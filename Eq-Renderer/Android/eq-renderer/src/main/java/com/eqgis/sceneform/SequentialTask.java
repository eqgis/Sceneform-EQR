package com.eqgis.sceneform;

import android.annotation.TargetApi;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 连续任务
 * <p>
 *     按序执行多个 {@link Runnable}，并将其加入{@link CompletableFuture}.
 * </p>
 * 这通常用于与Engine创建的相同线程，本开源版本的Engine是在UI线程中创建，故而这里多用于UI线程
 */
@TargetApi(24)
@SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
public class SequentialTask {
  @Nullable private CompletableFuture<Void> future;

  /**
   * 向当前future追加新的Runnable，或创建新的Runnable。
   * @return 当前的CompletableFuture
   */
  @MainThread
  public CompletableFuture<Void> appendRunnable(Runnable action, Executor executor) {
    if (future != null && !future.isDone()) {
      future = future.thenRunAsync(action, executor);
    } else {
      future = CompletableFuture.runAsync(action, executor);
    }
    return future;
  }

  /** 任务完成返回True */
  @MainThread
  public boolean isDone() {
    if (future == null) {
      return true;
    }
    if (future.isDone()) {
      future = null;
      return true;
    }
    return false;
  }
}
