package com.eqgis.sceneform.rendering;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * 线程池
 * @hide
 */
public class ThreadPools {
  private static Executor mainExecutor;
  private static Executor threadPoolExecutor;

  private ThreadPools() {}

  /** 获取主线程Executor */
  public static Executor getMainExecutor() {
    if (mainExecutor == null) {
      mainExecutor =
          new Executor() {
            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void execute(Runnable runnable) {
              handler.post(runnable);
            }
          };
    }
    return mainExecutor;
  }

//  /**
//   * 更新主线程Executor
//   * @param executor
//   * */
//  public static void setMainExecutor(Executor executor) {
//    mainExecutor = executor;
//  }

  /**
   * 获取后台线程的Executor
   * <p>用于异步执行特定任务</p>
   * */
  public static Executor getThreadPoolExecutor() {
    if (threadPoolExecutor == null) {
      return AsyncTask.THREAD_POOL_EXECUTOR;
    }
    return threadPoolExecutor;
  }

//  /**
//   * 设置后台线程Executor
//   */
//  public static void setThreadPoolExecutor(Executor executor) {
//    threadPoolExecutor = executor;
//  }
}
