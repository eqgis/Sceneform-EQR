package com.google.sceneform.rendering;

/**
 * 在将gltf文件加载到可渲染对象时发生的事件的接口回调。
 * todo 需在renderable中实现这个功能，以便于后续使用
 * */
public interface LoadGltfListener {
  /** 加载状态值 */
  public enum GltfLoadStage {
    LOAD_STAGE_NONE,
    FETCH_MATERIALS,
    DOWNLOAD_MODEL,
    CREATE_LOADER,
    ADD_MISSING_FILES,
    FINISHED_READING_FILES,
    CREATE_RENDERABLE
  }

  void setLoadingStage(GltfLoadStage stage);

  void reportBytesDownloaded(long bytes);

  void onFinishedFetchingMaterials();

  void onFinishedLoadingModel(long durationMs);

  void onFinishedReadingFiles(long durationMs);

  void setModelSize(float modelSizeMeters);

  void onReadingFilesFailed(Exception exception);
}
