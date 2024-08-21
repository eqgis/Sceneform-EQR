package com.google.sceneform.resources;

import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;

import com.google.sceneform.utilities.Preconditions;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 资源注册器
 * <p>用于跟踪已经加载和正在加载的资源。</p>
 * 注册中心只维护弱引用，并不阻止资源被引用收集。
 * @hide
 */
// TODO: 当资源注册表变大时，自动从资源注册表中删除无用的弱引用。
public class ResourceRegistry<T> implements ResourceHolder {
  private static final String TAG = ResourceRegistry.class.getSimpleName();

  private final Object lock = new Object();

  @GuardedBy("lock")
  private final Map<Object, WeakReference<T>> registry = new HashMap<>();

  @GuardedBy("lock")
  private final Map<Object, CompletableFuture<T>> futureRegistry = new HashMap<>();

  /**
   * 返回一个未来的资源，以前注册相同的id。如果资源还没有
   * 已注册或已被垃圾收集，返回null。未来可能会是一种资源
   * 已经完成加载，在这种情况下{@link CompletableFuture#isDone()}将为真。
   */
  @Nullable
  public CompletableFuture<T> get(Object id) {
    Preconditions.checkNotNull(id, "Parameter 'id' was null.");

    synchronized (lock) {
      // 如果资源已经完成加载，则向该资源返回一个完成的future。
      WeakReference<T> reference = registry.get(id);
      if (reference != null) {
        T resource = reference.get();
        if (resource != null) {
          return CompletableFuture.completedFuture(resource);
        } else {
          registry.remove(id);
        }
      }

      // 如果资源正在加载中，则直接返回future。
      // 如果id未注册，则该值为空。
      return futureRegistry.get(id);
    }
  }

  public void remove(Object id){
    synchronized (lock){
      registry.remove(id);
      if (futureRegistry.get(id) != null){
        CompletableFuture<T> futureResource = futureRegistry.get(id);
        if (futureResource != null && !futureResource.isDone()){
          futureResource.cancel(true);
        }
        futureRegistry.remove(id);
      }
    }
  }

  /**
   * 通过id向资源注册一个future。如果注册的资源已经完成加载，
   * 使用{@link CompletableFuture#completedFuture(Object)}。
   */
  public void register(Object id, CompletableFuture<T> futureResource) {
    Preconditions.checkNotNull(id, "Parameter 'id' was null.");
    Preconditions.checkNotNull(futureResource, "Parameter 'futureResource' was null.");

    //如果future已经完成，将其添加到已加载的资源的注册表中
    //尽早返回。
    if (futureResource.isDone()) {
      if (futureResource.isCompletedExceptionally()) {
        return;
      }

      //关闭向getNow传递null的警告。getNow没有注释，但它允许为空。
      //另外，这里有一个前提条件检查。
      @SuppressWarnings("nullness")
      T resource = Preconditions.checkNotNull(futureResource.getNow(null));

      synchronized (lock) {
        registry.put(id, new WeakReference<>(resource));

        //如果id以前在futurregistry中注册过，请确保将其删除。
        futureRegistry.remove(id);
      }

      return;
    }

    synchronized (lock) {
      futureRegistry.put(id, futureResource);

      // 如果id以前在futurregistry中注册过，请确保将其删除。如果id以前在已完成的注册中心中注册过，请确保将其删除。
      registry.remove(id);
    }

    @SuppressWarnings({"FutureReturnValueIgnored", "unused"})
    CompletableFuture<Void> registerFuture =
            futureResource.handle(
                    (result, throwable) -> {
                      synchronized (this) {
                        //检查以确保注册表中的future是这个future。
                        //否则，这个id已经被其他资源覆盖了。
                        synchronized (lock) {
                          CompletableFuture<T> futureReference = futureRegistry.get(id);
                          if (futureReference == futureResource) {
                            futureRegistry.remove(id);
                            if (throwable == null) {
                              //只有在没有例外的情况下才添加引用。
                              registry.put(id, new WeakReference<>(result));
                            }
                          }
                        }
                      }
                      return null;
                    });
  }

  /**
   * 删除所有缓存项。取消任何正在进行的期货交易。中的取消不会中断工作进步。它只是阻止了最后阶段的开始。
   */
  @Override
  public void destroyAllResources() {
    synchronized (lock) {
      Iterator<Map.Entry<Object, CompletableFuture<T>>> iterator =
              futureRegistry.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<Object, CompletableFuture<T>> entry = iterator.next();
        iterator.remove();
        CompletableFuture<T> futureResource = entry.getValue();
        if (!futureResource.isDone()) {
          futureResource.cancel(true);
        }
      }

      registry.clear();
    }
  }

  @Override
  public long reclaimReleasedResources() {
    //注册表中的资源也被其他资源Holder持有。这个返回0，其他Holder处理。
    return 0;
  }
}
