package com.eqgis.sceneform.resources;

/**
 * 共享参考对象
 * <p>用于使用引用计数管理共享对象的内存。</p>
 * @hide
 */
public abstract class SharedReference {
  private int referenceCount = 0;

  public void retain() {
    referenceCount++;
  }

  public void release() {
    referenceCount--;
    dispose();
  }

  protected abstract void onDispose();

  private void dispose() {
    if (referenceCount > 0) {
      return;
    }

    onDispose();
  }
}
