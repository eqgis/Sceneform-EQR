package com.google.sceneform.rendering;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.eqgis.eqr.core.FilamentPrimitiveUtilsNative;
import com.eqgis.eqr.core.PlyLoader;
import com.eqgis.eqr.data.JPlyAsset;
import com.eqgis.eqr.geometry.GeometryUtils;
import com.google.android.filament.Box;
import com.google.android.filament.Engine;
import com.google.android.filament.EntityInstance;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.RenderableManager;
import com.google.android.filament.TransformManager;
import com.google.android.filament.VertexBuffer;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.resources.ResourceRegistry;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
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
        CompletableFuture<Material> materialCompletableFuture =
                Material.builder()
                        .setRegistryId(this)
                        .setSource(
                                context,
                                RenderingResources.GetSceneformResource(
                                        context, RenderingResources.Resource.OPAQUE_COLORED_MATERIAL))
                        .build();
//        CompletableFuture<Material> materialCompletableFuture = MaterialFactory.makeOpaqueWithColor(
//                context,
//                new Color(1, 1, 1,1)
//        );
        materialCompletableFuture.thenAccept(mat -> {
            material = mat;
            MaterialFactory.applyDefaultPbrParams(material);
            material.setFloat4(MaterialFactory.MATERIAL_COLOR, new Color(1, 1, 1,1));
//            ResourceRegistry<Material> registry = ResourceManager.getInstance().getMaterialRegistry();
//            // In this case register a copy of the material.
//            registry.register(mat.registryId, mat);

            ArrayList<Integer> triangleIndices = new ArrayList<>(assets.faces.length);
            for (int v : assets.faces) {
                triangleIndices.add(v);
            }
            int vertexCount = assets.vertices.length / 3;

            ArrayList<Vertex> vertices = new ArrayList<>(vertexCount);

            if (assets.normals == null){
                //补法线，采用平滑法线（Smooth Shading）计算方法
                assets.normals = FilamentPrimitiveUtilsNative.nComputeVertexNormals(assets.vertices, assets.faces);
            }

            if (assets.texcoords == null){
                for (int i = 0; i < vertexCount; i++) {
                    Vertex.Builder builder = Vertex.builder().setPosition(new Vector3(assets.vertices[3 * i], assets.vertices[3 * i + 1], assets.vertices[3 * i + 2]));
                    builder.setNormal(new Vector3(assets.normals[3*i],assets.normals[3*i + 1], assets.normals[3*i+2]));
                    vertices.add(builder.build());
                }
            }else {
                for (int i = 0; i < vertexCount; i++) {
                    Vertex.Builder builder = Vertex.builder().setPosition(new Vector3(assets.vertices[3 * i], assets.vertices[3 * i + 1], assets.vertices[3 * i + 2]));
                    builder.setNormal(new Vector3(assets.normals[3*i],assets.normals[3*i + 1], assets.normals[3*i+2]));
                    builder.setUvCoordinate(new Vertex.UvCoordinate(assets.texcoords[2*i],assets.texcoords[2*i+1]));
                    vertices.add(builder.build());
                }
            }



            RenderableDefinition.Submesh submesh =
                    RenderableDefinition.Submesh.builder()
                            .setTriangleIndices(triangleIndices).setMaterial(material).build();

            renderableDefinition =
                    RenderableDefinition.builder()
                            .setVertices(vertices)
                            .setSubmeshes(Collections.singletonList(submesh))
                            .build();
            instance.getRenderable().updateFromDefinition(renderableDefinition);
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        if (plyLoader == null)return;
        plyLoader.destroy();
        plyLoader = null;
    }
}
