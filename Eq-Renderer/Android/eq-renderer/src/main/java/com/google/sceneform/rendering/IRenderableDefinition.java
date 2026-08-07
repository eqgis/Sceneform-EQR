package com.google.sceneform.rendering;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用接口
 * @author tanyx 2026/1/8
 * @version 1.0
 **/
public interface IRenderableDefinition {
    /**
     * 设置顶点数据。
     * @param vertices 顶点列表
     */
    void setVertices(List<Vertex> vertices);

    /** @return 顶点列表 */
    List<Vertex> getVertices();

    /** @return 子几何列表 */
    List<RenderableDefinition.SubGeometry> getSubGeometries();

    /**
     * 设置子几何数据。
     * @param subGeometries 子几何列表
     */
    void setSubGeometries(List<RenderableDefinition.SubGeometry> subGeometries);

    /**
     * 获取旧版子网格列表。
     * @return 旧版子网格列表
     * @deprecated 请使用 {@link #getSubGeometries()}。
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    default List<RenderableDefinition.Submesh> getSubmeshes() {
        return (List<RenderableDefinition.Submesh>) (List<?>) getSubGeometries();
    }

    /**
     * 设置旧版子网格列表。
     * @param submeshes 旧版子网格列表
     * @deprecated 请使用 {@link #setSubGeometries(List)}。
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    default void setSubmeshes(List<RenderableDefinition.Submesh> submeshes) {
        setSubGeometries((List<RenderableDefinition.SubGeometry>) (List<?>) submeshes);
    }

    /**
     * 将定义数据应用到内部渲染数据。
     * @param data 内部渲染数据
     * @param materialBindings 材质绑定列表
     * @param materialNames 材质名称列表
     */
    void applyDefinitionToData(
            IRenderableInternalData data,
            ArrayList<Material> materialBindings,
            ArrayList<String> materialNames);
}
