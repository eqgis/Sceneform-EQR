package com.eqgis.sceneform.rendering;

import androidx.annotation.Nullable;

import com.eqgis.sceneform.resources.ResourceHolder;
import com.eqgis.sceneform.resources.ResourceRegistry;

import java.util.ArrayList;

/**
 * 资源管理器。
 * <p>
 *     从id到已创建资源和专用于异步加载资源的任务执行器的映射。
 * </p>
 * @hide
 */
@SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
public class ResourceManager {
  @Nullable private static ResourceManager instance = null;

  private final ArrayList<ResourceHolder> resourceHolders = new ArrayList<>();
  private final ResourceRegistry<Texture> textureRegistry = new ResourceRegistry<>();
  private final ResourceRegistry<Material> materialRegistry = new ResourceRegistry<>();
  private final ResourceRegistry<ModelRenderable> modelRenderableRegistry =
      new ResourceRegistry<>();

  
  private final ResourceRegistry<ViewRenderable> viewRenderableRegistry = new ResourceRegistry<>();

  private final CleanupRegistry<CameraStream> cameraStreamCleanupRegistry = new CleanupRegistry<>();
  private final CleanupRegistry<ExternalTexture> externalTextureCleanupRegistry = new CleanupRegistry<>();
  private final CleanupRegistry<DepthTexture> depthTextureCleanupRegistry = new CleanupRegistry<>();
  private final CleanupRegistry<Material> materialCleanupRegistry = new CleanupRegistry<>();
  private final CleanupRegistry<RenderableInstance> renderableInstanceCleanupRegistry =
      new CleanupRegistry<>();
  private final CleanupRegistry<Texture> textureCleanupRegistry = new CleanupRegistry<>();

  ResourceRegistry<Texture> getTextureRegistry() {
    return textureRegistry;
  }

  ResourceRegistry<Material> getMaterialRegistry() {
    return materialRegistry;
  }

  ResourceRegistry<ModelRenderable> getModelRenderableRegistry() {
    return modelRenderableRegistry;
  }

  
  ResourceRegistry<ViewRenderable> getViewRenderableRegistry() {
    return viewRenderableRegistry;
  }

  public CleanupRegistry<CameraStream> getCameraStreamCleanupRegistry() {
    return cameraStreamCleanupRegistry;
  }

  CleanupRegistry<ExternalTexture> getExternalTextureCleanupRegistry() {
    return externalTextureCleanupRegistry;
  }

  public CleanupRegistry<DepthTexture> getDepthTextureCleanupRegistry() {
    return depthTextureCleanupRegistry;
  }

  CleanupRegistry<Material> getMaterialCleanupRegistry() {
    return materialCleanupRegistry;
  }

  CleanupRegistry<RenderableInstance> getRenderableInstanceCleanupRegistry() {
    return renderableInstanceCleanupRegistry;
  }

  CleanupRegistry<Texture> getTextureCleanupRegistry() {
    return textureCleanupRegistry;
  }

  public long reclaimReleasedResources() {
    long resourcesInUse = 0;
    for (ResourceHolder registry : resourceHolders) {
      resourcesInUse += registry.reclaimReleasedResources();
    }
    return resourcesInUse;
  }

  /** 强制销毁所有资源 */
  public void destroyAllResources() {
    for (ResourceHolder resourceHolder : resourceHolders) {
      resourceHolder.destroyAllResources();
    }
  }

  public void addResourceHolder(ResourceHolder resource) {
    resourceHolders.add(resource);
  }

  public static ResourceManager getInstance() {
    if (instance == null) {
      instance = new ResourceManager();
    }

    return instance;
  }

  private ResourceManager() {
    addResourceHolder(textureRegistry);
    addResourceHolder(materialRegistry);
    addResourceHolder(modelRenderableRegistry);
    addViewRenderableRegistry();
    addResourceHolder(cameraStreamCleanupRegistry);
    addResourceHolder(externalTextureCleanupRegistry);
    addResourceHolder(depthTextureCleanupRegistry);
    addResourceHolder(materialCleanupRegistry);
    addResourceHolder(renderableInstanceCleanupRegistry);
    addResourceHolder(textureCleanupRegistry);
  }

  
  private void addViewRenderableRegistry() {
    addResourceHolder(viewRenderableRegistry);
  }
}
