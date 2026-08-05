package com.eqgis.eqr.core;


import com.google.android.filament.Engine;
import com.google.android.filament.gltfio.AssetLoader;
import com.google.android.filament.gltfio.UbershaderProvider;
import com.google.sceneform.rendering.EngineInstance;

/**
 * 管理 Filament 资源的共享 {@link UbershaderProvider} 实例。
 * <p>
 * 本管理器确保每个 {@link Engine} 仅创建一个 {@link UbershaderProvider}。
 * 它提供全局访问接口，保证引擎绑定一致性，并支持在引擎或应用销毁时安全释放。
 * <p>
 *
 * @author Ikkyu_tanyx_eqgis 2025年12月26日23:38:41
 * </p>
 */
public final class FilamentMaterialProviderManager {

    /** 共享的 UbershaderProvider 实例 */
    private static volatile UbershaderProvider sUbershaderProvider;

    // 私有化构造，防止实例化
    private FilamentMaterialProviderManager() { }

    /**
     * 获取指定 {@link Engine} 的共享 {@link UbershaderProvider}。
     * <p>
     * 如果尚未创建 provider，则会新建并绑定到该 engine。
     * 如果 provider 已存在，则必须绑定到同一个 engine，否则会抛出异常。
     * </p>
     *
     * @return 共享的 {@link UbershaderProvider} 实例
     * @throws IllegalStateException 如果 provider 已绑定到不同的 Engine
     */
    public static synchronized UbershaderProvider get() {
        if (sUbershaderProvider == null) {
            sUbershaderProvider = new UbershaderProvider(EngineInstance.getEngine().getFilamentEngine());
            return sUbershaderProvider;
        }

        return sUbershaderProvider;
    }

    /**
     * 销毁共享 {@link UbershaderProvider} 持有的材质缓存并释放 Provider。
     * <p>
     * 必须在所有 GLTF {@link com.google.android.filament.MaterialInstance} 已释放后、
     * Filament {@link Engine} 销毁前调用。
     * </p>
     */
    public static synchronized void destroy() {
        if (sUbershaderProvider != null) {
            try {
                //desc- Provider.destroy() 不会释放缓存材质，需先显式销毁材质模板与 dummy texture。
                sUbershaderProvider.destroyMaterials();
            } finally {
                sUbershaderProvider.destroy();
                sUbershaderProvider = null;
            }
        }
    }

}

