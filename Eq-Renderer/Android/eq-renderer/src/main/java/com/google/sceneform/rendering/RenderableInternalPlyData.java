package com.google.sceneform.rendering;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eqgis.eqr.core.FilamentPrimitiveUtilsNative;
import com.eqgis.eqr.core.PlyLoader;
import com.eqgis.eqr.data.JPlyAsset;
import com.google.android.filament.RenderableManager;
import com.google.sceneform.math.Vector3;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;

/**
 * Ply数据
 * <p>
 *     通过自定义Mesh的方式绘制Ply数据（Vertex、Faces）
 * </p>
 * @author tanyx 2026/1/5
 * @version 1.0
 **/
public class RenderableInternalPlyData extends RenderableInternalData{
    public static float DEFAULT_POINT_SIZE = 2;
    private static final String POINT_SIZE_NAME = "pointSize";
    private static final String USE_VERTEX_COLORS = "useVertexColors";
    private PlyLoader plyLoader;
    Context context;
    byte[] byteBuffer;
    @Nullable
    Function<String, Uri> urlResolver;
    RenderableDefinition renderableDefinition;
    private Material material;
    private JPlyAsset assets;

    @Override
    public void create(RenderableInstance instance) {
        super.create(instance);
        plyLoader = new PlyLoader();
        assets = plyLoader.createAssets(byteBuffer);

        Renderable renderable = instance.getRenderable();
        if (renderable.collisionShape == null) {
            com.google.android.filament.Box box = plyLoader.getBoundingBox();
            float[] halfExtent = box.getHalfExtent();
            float[] center = box.getCenter();
            renderable.collisionShape =
                    new com.google.sceneform.collision.Box(
                            new Vector3(halfExtent[0], halfExtent[1], halfExtent[2]).scaled(2.0f),
                            new Vector3(center[0], center[1], center[2]));
        }

        Material.builder()
                .setSource(context,
                        RenderingResources.GetSceneformResource(
                                context, RenderingResources.Resource.PLY_BASE_MATERIAL))
                .build()
                .thenAccept(mat -> {
                    mat.setFloat(POINT_SIZE_NAME, DEFAULT_POINT_SIZE);
                    createPrimitive(instance, mat);
                });
    }

    @Override
    public void dispose() {
        super.dispose();
        if (plyLoader == null)return;
        plyLoader.destroyPlyAsset();
        plyLoader = null;
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

    private void createPrimitive(RenderableInstance instance, Material mat) {
        material = mat;
        MaterialFactory.applyDefaultPbrParams(material);
        material.setFloat4(MaterialFactory.MATERIAL_COLOR, new Color(1, 1, 1,1));
        //若顶点数据存在，则使用顶点颜色
        material.setBoolean(USE_VERTEX_COLORS,assets.colors != null);

        if (assets.vertices == null) {
            throw new IllegalArgumentException("assets.vertices must not be null");
        }

        int vertexCount = assets.vertices.length / 3;

        if (assets.normals == null && assets.faces != null){
            //补法线，采用平滑法线（Smooth Shading）计算方法
            assets.normals = FilamentPrimitiveUtilsNative.nComputeVertexNormals(assets.vertices, assets.faces);
        }

        ArrayList<Integer> triangleIndices = getIndices(vertexCount);
        ArrayList<Vertex> vertices = getVertices(vertexCount);

        RenderableDefinition.Submesh submesh =
                RenderableDefinition.Submesh.builder()
                        .setTriangleIndices(triangleIndices).setMaterial(material).build();

        renderableDefinition =
                RenderableDefinition.builder()
                        .setVertices(vertices)
                        .setSubmeshes(Collections.singletonList(submesh))
                        .build();
        instance.getRenderable().updateFromDefinition(renderableDefinition);
    }

    /**
     * 获取索引
     */
    @NonNull
    private ArrayList<Integer> getIndices(int vertexCount) {
        ArrayList<Integer> triangleIndices;
        if (assets.faces == null){
            if (assets.tripstrip != null){
                triangleIndices = new ArrayList<>(assets.tripstrip.length);
                for (int v : assets.tripstrip) {
                    triangleIndices.add(v);
                }
                primitiveType = RenderableManager.PrimitiveType.TRIANGLE_STRIP;
            }else {
                //在 Filament 中，即使渲染的是 POINTS 图元，也必须提供 IndexBuffer，因此若无数据，也必须构造
                triangleIndices = new ArrayList<>(vertexCount);
                for (int i = 0; i < vertexCount; i++) {
                    triangleIndices.add(i);
                }
                primitiveType = RenderableManager.PrimitiveType.POINTS;
            }
        }else {
            triangleIndices = new ArrayList<>(assets.faces.length);
            for (int v : assets.faces) {
                triangleIndices.add(v);
            }
            primitiveType = RenderableManager.PrimitiveType.TRIANGLES;
        }
        return triangleIndices;
    }

    /**
     * 处理顶点数据
     */
    private ArrayList<Vertex> getVertices(int vertexCount) {
        ArrayList<Vertex> vertices = new ArrayList<>(vertexCount);

        if (assets.normals != null){
            if (assets.texcoords == null){
                //优先使用UV
                if (assets.colors == null){
                    for (int i = 0; i < vertexCount; i++) {
                        Vertex.Builder builder = Vertex.builder().setPosition(new Vector3(assets.vertices[3 * i], assets.vertices[3 * i + 1], assets.vertices[3 * i + 2]));
                        builder.setNormal(new Vector3(assets.normals[3*i],assets.normals[3*i + 1], assets.normals[3*i+2]));
                        vertices.add(builder.build());
                    }
                }else {
                    for (int i = 0; i < vertexCount; i++) {
                        Vertex.Builder builder = Vertex.builder().setPosition(new Vector3(assets.vertices[3 * i], assets.vertices[3 * i + 1], assets.vertices[3 * i + 2]));
                        builder.setNormal(new Vector3(assets.normals[3*i],assets.normals[3*i + 1], assets.normals[3*i+2]));
                        builder.setColor(new Color(assets.colors[4*i],assets.colors[4*i+1],
                                assets.colors[4*i+2],assets.colors[4*i+3]));
                        vertices.add(builder.build());
                    }
                }
            }else {
                for (int i = 0; i < vertexCount; i++) {
                    Vertex.Builder builder = Vertex.builder().setPosition(new Vector3(assets.vertices[3 * i], assets.vertices[3 * i + 1], assets.vertices[3 * i + 2]));
                    builder.setNormal(new Vector3(assets.normals[3*i],assets.normals[3*i + 1], assets.normals[3*i+2]));
                    builder.setUvCoordinate(new Vertex.UvCoordinate(assets.texcoords[2*i],assets.texcoords[2*i+1]));
                    vertices.add(builder.build());
                }
            }
        }else {
            if (assets.texcoords == null){
                if (assets.colors == null){
                    for (int i = 0; i < vertexCount; i++) {
                        Vertex.Builder builder = Vertex.builder().setPosition(new Vector3(assets.vertices[3 * i], assets.vertices[3 * i + 1], assets.vertices[3 * i + 2]));
                        vertices.add(builder.build());
                    }
                }else {
                    for (int i = 0; i < vertexCount; i++) {
                        Vertex.Builder builder = Vertex.builder().setPosition(new Vector3(assets.vertices[3 * i], assets.vertices[3 * i + 1], assets.vertices[3 * i + 2]));
                        builder.setColor(new Color(assets.colors[4*i],assets.colors[4*i+1],
                                assets.colors[4*i+2],assets.colors[4*i+3]));
                        vertices.add(builder.build());
                    }
                }
            }else {
                for (int i = 0; i < vertexCount; i++) {
                    Vertex.Builder builder = Vertex.builder().setPosition(new Vector3(assets.vertices[3 * i], assets.vertices[3 * i + 1], assets.vertices[3 * i + 2]));
                    builder.setUvCoordinate(new Vertex.UvCoordinate(assets.texcoords[2*i],assets.texcoords[2*i+1]));
                    vertices.add(builder.build());
                }
            }
        }
        return vertices;
    }
}
