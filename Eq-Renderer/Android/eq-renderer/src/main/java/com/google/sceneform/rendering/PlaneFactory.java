package com.google.sceneform.rendering;

import androidx.annotation.Nullable;

import com.google.sceneform.math.Vector3;
import com.google.sceneform.utilities.AndroidPreconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 平面工厂
 */
public class PlaneFactory {
    private static final int COORDS_PER_TRIANGLE = 3;

    /**
     * 创建一个可渲染的平面对象
     *
     * @param size     平面的尺寸
     * @param center   平面的中心位置
     * @param material 平面的材质对象
     * @return 渲染对象
     */
    @SuppressWarnings("AndroidApiChecker")
    // CompletableFuture requires api level 24
    public static ModelRenderable makePlane(Vector3 size, Vector3 center, Material material) {
        AndroidPreconditions.checkMinAndroidApiLevel();

        Vector3 extents = size.scaled(0.5f);

        Vector3 p0 = Vector3.add(center, new Vector3(-extents.x, -extents.y, extents.z));
        Vector3 p1 = Vector3.add(center, new Vector3(-extents.x, extents.y, -extents.z));
        Vector3 p2 = Vector3.add(center, new Vector3(extents.x, extents.y, -extents.z));
        Vector3 p3 = Vector3.add(center, new Vector3(extents.x, -extents.y, extents.z));

        Vector3 front = new Vector3();

        Vertex.UvCoordinate uv00 = new Vertex.UvCoordinate(0.0f, 0.0f);
        Vertex.UvCoordinate uv10 = new Vertex.UvCoordinate(1.0f, 0.0f);
        Vertex.UvCoordinate uv01 = new Vertex.UvCoordinate(0.0f, 1.0f);
        Vertex.UvCoordinate uv11 = new Vertex.UvCoordinate(1.0f, 1.0f);

        ArrayList<Vertex> vertices = new ArrayList<>(
                Arrays.asList(
                        Vertex.builder().setPosition(p0).setNormal(front).setUvCoordinate(uv00).build(),
                        Vertex.builder().setPosition(p1).setNormal(front).setUvCoordinate(uv01).build(),
                        Vertex.builder().setPosition(p2).setNormal(front).setUvCoordinate(uv11).build(),
                        Vertex.builder().setPosition(p3).setNormal(front).setUvCoordinate(uv10).build()
                )
        );

        final int trianglesPerSide = 2;

        ArrayList<Integer> triangleIndices = new ArrayList<>(trianglesPerSide * COORDS_PER_TRIANGLE);
        // First triangle.
        triangleIndices.add(3);
        triangleIndices.add(1);
        triangleIndices.add(0);

        // Second triangle.
        triangleIndices.add(3);
        triangleIndices.add(2);
        triangleIndices.add(1);

        RenderableDefinition.Submesh submesh = RenderableDefinition.Submesh.builder()
                .setTriangleIndices(triangleIndices)
                .setMaterial(material)
                .build();

        RenderableDefinition renderableDefinition = RenderableDefinition.builder()
                .setVertices(vertices)
                .setSubmeshes(Arrays.asList(submesh))
                .build();

        CompletableFuture<ModelRenderable> future = ModelRenderable.builder()
                .setSource(renderableDefinition)
                .build();

        @Nullable ModelRenderable result;
        try {
            result = future.get();
        } catch (ExecutionException | InterruptedException ex) {
            throw new AssertionError("Error creating renderable.", ex);
        }

        if (result == null) {
            throw new AssertionError("Error creating renderable.");
        }

        return result;
    }
}
