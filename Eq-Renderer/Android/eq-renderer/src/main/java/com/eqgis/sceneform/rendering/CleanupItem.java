package com.eqgis.sceneform.rendering;

/**
 * 清理项
 * <p>
 *     当注册表中以注册的对象被销毁时执行{@link Runnable}
 * </p>
 *
 * <p>对于注册的每个类型为{@code T}的对象，将创建一个{@link CleanupItem}。
 * 注册对象的生命周期将被跟踪，当它被处置时，将运行给定的{@link Runnable}。
 */
class CleanupItem<T> extends java.lang.ref.PhantomReference<T> {
  private final Runnable cleanupCallback;

  /**
   * 构造函数
   * @param trackedObject 在垃圾回收之前要跟踪的对象
   * @param referenceQueue 用于跟踪的引用队列
   * @param cleanupCallback {@link Runnable} 回调
   */
  CleanupItem(
      T trackedObject, java.lang.ref.ReferenceQueue<T> referenceQueue, Runnable cleanupCallback) {
    super(trackedObject, referenceQueue);
    this.cleanupCallback = cleanupCallback;
  }

  /** 执行 {@link Runnable}. */
  void run() {
    cleanupCallback.run();
  }
}
