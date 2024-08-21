package com.google.sceneform.resources;

/** 资源池 */
public interface ResourceHolder {
  /**
   * 释放搜集的垃圾对象和数据
   *
   * @return 正在使用的资源计数。
   */
  long reclaimReleasedResources();

  /** 忽略引用计数并释放所有相关资源。 */
  void destroyAllResources();
}
