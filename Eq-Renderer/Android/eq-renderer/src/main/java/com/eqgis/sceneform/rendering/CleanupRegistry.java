package com.eqgis.sceneform.rendering;

import com.eqgis.sceneform.resources.ResourceHolder;

import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Cleanup注册表
 * <p>
 *     通过一个{@link ReferenceQueue}并在队列中的每个对象被垃圾回收后执行一个{@link Runnable}。
 * </p>
 */
public class CleanupRegistry<T> implements ResourceHolder {

  private final HashSet<CleanupItem<T>> cleanupItemHashSet;
  private final ReferenceQueue<T> referenceQueue;

  public CleanupRegistry() {
    this(new HashSet<>(), new ReferenceQueue<>());
  }

  public CleanupRegistry(
      HashSet<CleanupItem<T>> cleanupItemHashSet, ReferenceQueue<T> referenceQueue) {
    this.cleanupItemHashSet = cleanupItemHashSet;
    this.referenceQueue = referenceQueue;
  }

  /**
   * 添加需注册的对象
   *
   * @param trackedObject 需被跟踪的对象
   * @param cleanupCallback 当销毁后需执行的回调
   */
  public void register(T trackedObject, Runnable cleanupCallback) {
    cleanupItemHashSet.add(new CleanupItem<T>(trackedObject, referenceQueue, cleanupCallback));
  }

  /**
   * 轮询{@link ReferenceQueue}以获取垃圾收集的对象，并运行相关的{@link Runnable}
   *
   * @return count of resources remaining.
   */
  @Override
  @SuppressWarnings("unchecked") // safe cast from Reference to a CleanupItem
  public long reclaimReleasedResources() {
    CleanupItem<T> ref = (CleanupItem<T>) referenceQueue.poll();
    while (ref != null) {
      if (cleanupItemHashSet.contains(ref)) {
        ref.run();
        cleanupItemHashSet.remove(ref);
      }
      ref = (CleanupItem<T>) referenceQueue.poll();
    }
    return cleanupItemHashSet.size();
  }

  /** 忽略引用计数并释放所有关联的资源 */
  @Override
  public void destroyAllResources() {
    Iterator<CleanupItem<T>> iterator = cleanupItemHashSet.iterator();
    while (iterator.hasNext()) {
      CleanupItem<T> ref = iterator.next();
      iterator.remove();
      ref.run();
    }
  }
}
