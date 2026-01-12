package com.google.sceneform.rendering;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用接口
 * @author tanyx 2026/1/8
 * @version 1.0
 **/
public interface IRenderableDefinition {
    void setVertices(List<Vertex> vertices);
    List<Vertex> getVertices();
    List<RenderableDefinition.Submesh> getSubmeshes();
    void setSubmeshes(List<RenderableDefinition.Submesh> submeshes);
    void applyDefinitionToData(
            IRenderableInternalData data,
            ArrayList<Material> materialBindings,
            ArrayList<String> materialNames);
}
