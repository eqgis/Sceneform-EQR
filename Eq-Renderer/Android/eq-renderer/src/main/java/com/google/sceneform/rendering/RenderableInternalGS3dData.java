package com.google.sceneform.rendering;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eqgis.eqr.core.PlyGS3dLoader;
import com.eqgis.eqr.data.JPlyGS3dAsset;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.RenderableManager;
import com.google.android.filament.VertexBuffer;
import com.google.sceneform.collision.Box;
import com.google.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;

/**
 * 3DGS
 * @author tanyx 2026/1/8
 * @version 1.0
 **/
public class RenderableInternalGS3dData extends RenderableInternalData implements LoadRenderableFromUniversalDataTask.IUniversalData {
    private PlyGS3dLoader gs3dLoader;
    private Context context;
    private byte[] byteBuffer;
    private Function<String, Uri> urlResolver;
    private RenderableDefinitionGS renderableDefinition;
    private Material material;
    private JPlyGS3dAsset asset;

    public VertexBuffer vertexBuffer;
    public IndexBuffer indexBuffer;

    @Override
    public void create(RenderableInstance instance) {
        gs3dLoader = new PlyGS3dLoader();
        asset = gs3dLoader.createAssets(byteBuffer);
        byteBuffer = null;// 主动置空，使其尽早符合回收条件

        Renderable renderable = instance.getRenderable();
        if (renderable.collisionShape == null) {
            com.google.android.filament.Box box = gs3dLoader.getBoundingBox();
            float[] halfExtent = box.getHalfExtent();
            float[] center = box.getCenter();
            Box sfBox = new Box(
                    new Vector3(halfExtent[0], halfExtent[1], halfExtent[2]).scaled(2.0f),
                    new Vector3(center[0], center[1], center[2]));
            renderable.collisionShape = sfBox;
            setExtentsAabb(sfBox.getExtents());
            setCenterAabb(sfBox.getCenter());
        }

        Material.builder()
                .setSource(context,
                        RenderingResources.GetSceneformResource(
                                context, RenderingResources.Resource.PLY_GAUSSIAN_SPLAT_MATERIAL))
                .build()
                .thenAccept(mat -> {
                    material = mat;
                    material.setInt("shDegree ",asset.shDegree);
                    material.setFloat("pointSize",5);
                    MaterialFactory.applyDefaultPbrParams(material);
                    material.setFloat4(MaterialFactory.MATERIAL_COLOR, new Color(1, 1, 1,1));
                    createPrimitive(instance);
                });
    }


    private void createPrimitive(RenderableInstance instance) {
        //若顶点数据存在，则使用顶点颜色

        if (asset.vertices == null) {
            throw new IllegalArgumentException("assets.vertices must not be null");
        }

        int vertexCount = asset.vertices.length / 3;//xyz三个分量

        ArrayList<Integer> triangleIndices = getIndices(vertexCount);
        ArrayList<Vertex> vertices = getVertices(vertexCount);

        //虽不是Mesh，但是复用Mesh数据结构{顶点索引、材质实例}
        RenderableDefinition.Submesh submesh =
                RenderableDefinition.Submesh.builder()
                        .setTriangleIndices(triangleIndices).setMaterial(material).build();

        renderableDefinition = new RenderableDefinitionGS();
        renderableDefinition.setSubmeshes(Collections.singletonList(submesh));
        renderableDefinition.setVertices(vertices);
        renderableDefinition.setGaussianSplat(asset);

        instance.getRenderable().updateFromDefinition(renderableDefinition);
    }

    @Override
    public void buildInstanceData(RenderableInstance instance, int renderedEntity) {
        super.buildInstanceData(instance, renderedEntity);
    }

    /**
     * 获取索引
     */
    @NonNull
    private ArrayList<Integer> getIndices(int vertexCount) {
        ArrayList<Integer> triangleIndices;
        //在 Filament 中，即使渲染的是 POINTS 图元，也必须提供 IndexBuffer，因此若无数据，也必须构造
        triangleIndices = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            triangleIndices.add(i);
        }
        primitiveType = RenderableManager.PrimitiveType.POINTS;
        return triangleIndices;
    }

    /**
     * 处理顶点数据
     */
    private ArrayList<Vertex> getVertices(int vertexCount) {
        ArrayList<Vertex> vertices = new ArrayList<>(vertexCount);

        for (int i = 0; i < vertexCount; i++) {
            Vertex.Builder builder = Vertex.builder()
                    .setPosition(new Vector3(asset.vertices[3 * i], asset.vertices[3 * i + 1], asset.vertices[3 * i + 2]));
            vertices.add(builder.build());
        }
        return vertices;
    }


    @Override
    public void changePrimitiveType(RenderableInstance instance, RenderableManager.PrimitiveType type) {

    }

    /**
     * 获取材质
     * <p>
     *     用于修改点图元尺寸
     *     mat.setFloat(POINT_SIZE_NAME, 10);
     * </p>
     * @return 材质对象
     */
    public Material getMaterial() {
        return material;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (gs3dLoader == null)return;
        gs3dLoader.destroyPlyAsset();
        gs3dLoader = null;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void setData(byte[] bytes) {
        this.byteBuffer = bytes;
    }

    @Override
    public void setUrlResolver(Function<String, Uri> resolver) {
        this.urlResolver = resolver;
    }
}
