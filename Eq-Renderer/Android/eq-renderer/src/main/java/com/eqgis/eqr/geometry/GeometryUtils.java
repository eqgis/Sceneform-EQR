package com.eqgis.eqr.geometry;


import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.filament.RenderableManager;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;
import com.google.sceneform.rendering.Material;
import com.google.sceneform.rendering.ModelRenderable;
import com.google.sceneform.rendering.RenderableDefinition;
import com.google.sceneform.rendering.Vertex;
import com.google.sceneform.utilities.AndroidPreconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 创建基本图元、立方体、圆柱、球体和平面等动态几何体。
 * @author tanyunxiu
 * @date 2020年9月28日
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class GeometryUtils {
    private static final float DEGENERATE_EPSILON = 1.0e-12f;
    private static final Color WHITE_VERTEX_COLOR = new Color(1.0f, 1.0f, 1.0f, 1.0f);

    /**
     * 创建圆柱
     * @param radius
     * @param height
     * @param center
     * @param material
     * @return
     */
    @SuppressWarnings("AndroidApiChecker")
    public static ModelRenderable makeCylinder(
            float radius, float height, Vector3 center, Material material) {
        AndroidPreconditions.checkMinAndroidApiLevel();

        final int numberOfSides = 16;
        final float halfHeight = height / 2;
        final float thetaIncrement = (float) (2 * Math.PI) / numberOfSides;

        float theta = 0;
        float uStep = (float) 1.0 / numberOfSides;

        ArrayList<Vertex> vertices = new ArrayList<Vertex>((numberOfSides + 1) * 4);
        ArrayList<Vertex> lowerCapVertices = new ArrayList<Vertex>(numberOfSides + 1);
        ArrayList<Vertex> upperCapVertices = new ArrayList<Vertex>(numberOfSides + 1);
        ArrayList<Vertex> upperEdgeVertices = new ArrayList<Vertex>(numberOfSides + 1);

        // Generate vertices along the sides of the cylinder.
        for (int side = 0; side <= numberOfSides; side++) {
            float cosTheta = (float) Math.cos(theta);
            float sinTheta = (float) Math.sin(theta);

            // Calculate edge vertices along bottom of cylinder
            Vector3 lowerPosition = new Vector3(radius * cosTheta, -halfHeight, radius * sinTheta);
            Vector3 normal = new Vector3(lowerPosition.x, 0, lowerPosition.z).normalized();
            lowerPosition = Vector3.add(lowerPosition, center);
            Vertex.UvCoordinate uvCoordinate = new Vertex.UvCoordinate(uStep * side, 0);

            Vertex vertex =
                    Vertex.builder()
                            .setPosition(lowerPosition)
                            .setNormal(normal)
                            .setUvCoordinate(uvCoordinate)
                            .build();
            vertices.add(vertex);

            // Create a copy of lower vertex with bottom-facing normals for cap.
            vertex =
                    Vertex.builder()
                            .setPosition(lowerPosition)
                            .setNormal(Vector3.down())
                            .setUvCoordinate(new Vertex.UvCoordinate((cosTheta + 1f) / 2, (sinTheta + 1f) / 2))
                            .build();
            lowerCapVertices.add(vertex);

            // Calculate edge vertices along top of cylinder
            Vector3 upperPosition = new Vector3(radius * cosTheta, halfHeight, radius * sinTheta);
            normal = new Vector3(upperPosition.x, 0, upperPosition.z).normalized();
            upperPosition = Vector3.add(upperPosition, center);
            uvCoordinate = new Vertex.UvCoordinate(uStep * side, 1);

            vertex =
                    Vertex.builder()
                            .setPosition(upperPosition)
                            .setNormal(normal)
                            .setUvCoordinate(uvCoordinate)
                            .build();
            upperEdgeVertices.add(vertex);

            // Create a copy of upper vertex with up-facing normals for cap.
            vertex =
                    Vertex.builder()
                            .setPosition(upperPosition)
                            .setNormal(Vector3.up())
                            .setUvCoordinate(new Vertex.UvCoordinate((cosTheta + 1f) / 2, (sinTheta + 1f) / 2))
                            .build();
            upperCapVertices.add(vertex);

            theta += thetaIncrement;
        }
        vertices.addAll(upperEdgeVertices);

        // Generate vertices for the centers of the caps of the cylinder.
        final int lowerCenterIndex = vertices.size();
        vertices.add(
                Vertex.builder()
                        .setPosition(Vector3.add(center, new Vector3(0, -halfHeight, 0)))
                        .setNormal(Vector3.down())
                        .setUvCoordinate(new Vertex.UvCoordinate(.5f, .5f))
                        .build());
        vertices.addAll(lowerCapVertices);

        final int upperCenterIndex = vertices.size();
        vertices.add(
                Vertex.builder()
                        .setPosition(Vector3.add(center, new Vector3(0, halfHeight, 0)))
                        .setNormal(Vector3.up())
                        .setUvCoordinate(new Vertex.UvCoordinate(.5f, .5f))
                        .build());
        vertices.addAll(upperCapVertices);

        ArrayList<Integer> triangleIndices = new ArrayList<Integer>();

        // Create triangles for each side
        for (int side = 0; side < numberOfSides; side++) {
            int bottomLeft = side;
            int bottomRight = side + 1;
            int topLeft = side + numberOfSides + 1;
            int topRight = side + numberOfSides + 2;

            // First triangle of side.
            triangleIndices.add(bottomLeft);
            triangleIndices.add(topRight);
            triangleIndices.add(bottomRight);

            // Second triangle of side.
            triangleIndices.add(bottomLeft);
            triangleIndices.add(topLeft);
            triangleIndices.add(topRight);

            // Add bottom cap triangle.
            triangleIndices.add(lowerCenterIndex);
            triangleIndices.add(lowerCenterIndex + side + 1);
            triangleIndices.add(lowerCenterIndex + side + 2);

            // Add top cap triangle.
            triangleIndices.add(upperCenterIndex);
            triangleIndices.add(upperCenterIndex + side + 2);
            triangleIndices.add(upperCenterIndex + side + 1);
        }

        RenderableDefinition.SubGeometry subGeometry =
                RenderableDefinition.SubGeometry.builder().setTriangleIndices(triangleIndices).setMaterial(material).build();

        RenderableDefinition renderableDefinition =
                RenderableDefinition.builder()
                        .setVertices(vertices)
                        .setSubGeometries(Arrays.asList(subGeometry))
                        .build();

        CompletableFuture<ModelRenderable> future =
                ModelRenderable.builder().setSource(renderableDefinition).build();

        ModelRenderable result;
        try {
            result = future.get();
        } catch (ExecutionException ex) {
            throw new AssertionError("Error creating renderable.", ex);
        } catch (InterruptedException ex) {
            throw new AssertionError("Error creating renderable.", ex);
        }

        if (result == null) {
            throw new AssertionError("Error creating renderable.");
        }

        return result;
    }
    /**
     * 创建立方体
     * @param size
     * @param center
     * @param material
     * @return
     */
    @SuppressWarnings("AndroidApiChecker")
    public static ModelRenderable makeCube(Vector3 size, Vector3 center, Material material) {
        AndroidPreconditions.checkMinAndroidApiLevel();

        Vector3 extents = size.scaled(0.5f);

        Vector3 p0 = Vector3.add(center, new Vector3(-extents.x, -extents.y, extents.z));
        Vector3 p1 = Vector3.add(center, new Vector3(extents.x, -extents.y, extents.z));
        Vector3 p2 = Vector3.add(center, new Vector3(extents.x, -extents.y, -extents.z));
        Vector3 p3 = Vector3.add(center, new Vector3(-extents.x, -extents.y, -extents.z));
        Vector3 p4 = Vector3.add(center, new Vector3(-extents.x, extents.y, extents.z));
        Vector3 p5 = Vector3.add(center, new Vector3(extents.x, extents.y, extents.z));
        Vector3 p6 = Vector3.add(center, new Vector3(extents.x, extents.y, -extents.z));
        Vector3 p7 = Vector3.add(center, new Vector3(-extents.x, extents.y, -extents.z));

        Vector3 up = Vector3.up();
        Vector3 down = Vector3.down();
        Vector3 front = Vector3.forward();
        Vector3 back = Vector3.back();
        Vector3 left = Vector3.left();
        Vector3 right = Vector3.right();

        Vertex.UvCoordinate uv00 = new Vertex.UvCoordinate(0.0f, 0.0f);
        Vertex.UvCoordinate uv10 = new Vertex.UvCoordinate(1.0f, 0.0f);
        Vertex.UvCoordinate uv01 = new Vertex.UvCoordinate(0.0f, 1.0f);
        Vertex.UvCoordinate uv11 = new Vertex.UvCoordinate(1.0f, 1.0f);

        ArrayList<Vertex> vertices =
                new ArrayList<Vertex>(
                        Arrays.asList(
                                // Bottom
                                Vertex.builder().setPosition(p0).setNormal(down).setUvCoordinate(uv01).build(),
                                Vertex.builder().setPosition(p1).setNormal(down).setUvCoordinate(uv11).build(),
                                Vertex.builder().setPosition(p2).setNormal(down).setUvCoordinate(uv10).build(),
                                Vertex.builder().setPosition(p3).setNormal(down).setUvCoordinate(uv00).build(),
                                // Left
                                Vertex.builder().setPosition(p7).setNormal(left).setUvCoordinate(uv01).build(),
                                Vertex.builder().setPosition(p4).setNormal(left).setUvCoordinate(uv11).build(),
                                Vertex.builder().setPosition(p0).setNormal(left).setUvCoordinate(uv10).build(),
                                Vertex.builder().setPosition(p3).setNormal(left).setUvCoordinate(uv00).build(),
                                // Front
                                Vertex.builder().setPosition(p4).setNormal(front).setUvCoordinate(uv01).build(),
                                Vertex.builder().setPosition(p5).setNormal(front).setUvCoordinate(uv11).build(),
                                Vertex.builder().setPosition(p1).setNormal(front).setUvCoordinate(uv10).build(),
                                Vertex.builder().setPosition(p0).setNormal(front).setUvCoordinate(uv00).build(),
                                // Back
                                Vertex.builder().setPosition(p6).setNormal(back).setUvCoordinate(uv01).build(),
                                Vertex.builder().setPosition(p7).setNormal(back).setUvCoordinate(uv11).build(),
                                Vertex.builder().setPosition(p3).setNormal(back).setUvCoordinate(uv10).build(),
                                Vertex.builder().setPosition(p2).setNormal(back).setUvCoordinate(uv00).build(),
                                // Right
                                Vertex.builder().setPosition(p5).setNormal(right).setUvCoordinate(uv01).build(),
                                Vertex.builder().setPosition(p6).setNormal(right).setUvCoordinate(uv11).build(),
                                Vertex.builder().setPosition(p2).setNormal(right).setUvCoordinate(uv10).build(),
                                Vertex.builder().setPosition(p1).setNormal(right).setUvCoordinate(uv00).build(),
                                // Top
                                Vertex.builder().setPosition(p7).setNormal(up).setUvCoordinate(uv01).build(),
                                Vertex.builder().setPosition(p6).setNormal(up).setUvCoordinate(uv11).build(),
                                Vertex.builder().setPosition(p5).setNormal(up).setUvCoordinate(uv10).build(),
                                Vertex.builder().setPosition(p4).setNormal(up).setUvCoordinate(uv00).build()));

        final int numSides = 6;
        final int verticesPerSide = 4;
        final int trianglesPerSide = 2;

        ArrayList<Integer> triangleIndices =
                new ArrayList<Integer>(numSides * trianglesPerSide * 6);
        for (int i = 0; i < numSides; i++) {
            // First triangle for this side.
            triangleIndices.add(3 + verticesPerSide * i);
            triangleIndices.add(1 + verticesPerSide * i);
            triangleIndices.add(0 + verticesPerSide * i);

            // Second triangle for this side.
            triangleIndices.add(3 + verticesPerSide * i);
            triangleIndices.add(2 + verticesPerSide * i);
            triangleIndices.add(1 + verticesPerSide * i);
        }

        //backPolygon
        for (int i = numSides * trianglesPerSide * 3 - 1; i > -1 ; i--) {
            triangleIndices.add(triangleIndices.get(i));
        }

        RenderableDefinition.SubGeometry subGeometry =
                RenderableDefinition.SubGeometry.builder().setTriangleIndices(triangleIndices).setMaterial(material).build();

        RenderableDefinition renderableDefinition =
                RenderableDefinition.builder()
                        .setVertices(vertices)
                        .setSubGeometries(Arrays.asList(subGeometry))
                        .build();

        CompletableFuture<ModelRenderable> future =
                ModelRenderable.builder().setSource(renderableDefinition).build();

        ModelRenderable result;
        try {
            result = future.get();
        } catch (ExecutionException ex) {
            throw new AssertionError("Error creating renderable.", ex);
        } catch (InterruptedException ex) {
            throw new AssertionError("Error creating renderable.", ex);
        }

        if (result == null) {
            throw new AssertionError("Error creating renderable.");
        }

        return result;
    }


    /**
     * 创建球体
     * @param radius
     * @param center
     * @param material
     * @return
     */
    @SuppressWarnings("AndroidApiChecker")
    public static ModelRenderable makeSphere(float radius, Vector3 center, Material material) {
        AndroidPreconditions.checkMinAndroidApiLevel();

        final int stacks = 24;
        final int slices = 24;

        // 创建顶点组
        ArrayList<Vertex> vertices = new ArrayList<Vertex>((slices + 1) * stacks + 2);
        float pi = (float) Math.PI;
        float doublePi = pi * 2.0f;

        for (int stack = 0; stack <= stacks; stack++) {
            float phi = pi * (float) stack / stacks;
            float sinPhi = (float) Math.sin(phi);
            float cosPhi = (float) Math.cos(phi);

            for (int slice = 0; slice <= slices; slice++) {
                float theta = doublePi * (float) (slice == slices ? 0 : slice) / slices;
                float sinTheta = (float) Math.sin(theta);
                float cosTheta = (float) Math.cos(theta);

                Vector3 position = new Vector3(sinPhi * cosTheta, cosPhi, sinPhi * sinTheta).scaled(radius);
                Vector3 normal = position.normalized();
                position = Vector3.add(position, center);
                Vertex.UvCoordinate uvCoordinate =
                        new Vertex.UvCoordinate(
                                1.0f - ((float) slice / slices), 1.0f - ((float) stack / stacks));

                Vertex vertex =
                        Vertex.builder()
                                .setPosition(position)
                                .setNormal(normal)
                                .setUvCoordinate(uvCoordinate)
                                .build();

                vertices.add(vertex);
            }
        }

        // 创建三角
        int numFaces = vertices.size();
        int numTriangles = numFaces * 2;
        int numIndices = numTriangles * 3;
        ArrayList<Integer> triangleIndices = new ArrayList<Integer>(numIndices);

        int v = 0;
        for (int stack = 0; stack < stacks; stack++) {
            for (int slice = 0; slice < slices; slice++) {
                // Skip triangles at the caps that would have an area of zero.
                boolean topCap = stack == 0;
                boolean bottomCap = stack == stacks - 1;

                int next = slice + 1;

                if (!topCap) {
                    triangleIndices.add(v + slice);
                    triangleIndices.add(v + next);
                    triangleIndices.add(v + slice + slices + 1);
                }

                if (!bottomCap) {
                    triangleIndices.add(v + next);
                    triangleIndices.add(v + next + slices + 1);
                    triangleIndices.add(v + slice + slices + 1);
                }
            }
            v += slices + 1;
        }

        RenderableDefinition.SubGeometry subGeometry =
                RenderableDefinition.SubGeometry.builder().setTriangleIndices(triangleIndices).setMaterial(material).build();
        RenderableDefinition renderableDefinition =
                RenderableDefinition.builder()
                        .setVertices(vertices)
                        .setSubGeometries(Arrays.asList(subGeometry))
                        .build();

        CompletableFuture<ModelRenderable> future =
                ModelRenderable.builder().setSource(renderableDefinition).build();

        ModelRenderable result;
        try {
            result = future.get();
        } catch (ExecutionException ex) {
            throw new AssertionError("Error creating renderable.", ex);
        }catch (InterruptedException ex) {
            throw new AssertionError("Error creating renderable.", ex);
        }

        if (result == null) {
            throw new AssertionError("Error creating renderable.");
        }

        return result;
    }

    /**
     * 创建球状天空包围盒
     * {@link GeometryUtils#makeSphere(float, Vector3, Material) 与本方法仅顶点索引顺序相反}
     * @param radius
     * @param center
     * @param material
     * @return
     */
    @SuppressWarnings("AndroidApiChecker")
    public static ModelRenderable makeInnerSphere(float radius, Vector3 center, Material material) {
        AndroidPreconditions.checkMinAndroidApiLevel();

        final int stacks = 24;
        final int slices = 24;

        // 创建顶点组
        ArrayList<Vertex> vertices = new ArrayList<Vertex>((slices + 1) * stacks + 2);
        float pi = (float) Math.PI;
        float doublePi = pi * 2.0f;

        for (int stack = 0; stack <= stacks; stack++) {
            float phi = pi * (float) stack / stacks;
            float sinPhi = (float) Math.sin(phi);
            float cosPhi = (float) Math.cos(phi);

            for (int slice = 0; slice <= slices; slice++) {
                float theta = doublePi * (float) (slice == slices ? 0 : slice) / slices;
                float sinTheta = (float) Math.sin(theta);
                float cosTheta = (float) Math.cos(theta);

                Vector3 position = new Vector3(sinPhi * cosTheta, cosPhi, sinPhi * sinTheta).scaled(radius);
                Vector3 normal = position.normalized();
                position = Vector3.add(position, center);
                Vertex.UvCoordinate uvCoordinate =
                        new Vertex.UvCoordinate(
                                1.0f - ((float) slice / slices), 1.0f - ((float) stack / stacks));

                Vertex vertex =
                        Vertex.builder()
                                .setPosition(position)
                                .setNormal(normal)
                                .setUvCoordinate(uvCoordinate)
                                .build();

                vertices.add(vertex);
            }
        }

        // 创建三角
        int numFaces = vertices.size();
        int numTriangles = numFaces * 2;
        int numIndices = numTriangles * 3;
        ArrayList<Integer> triangleIndices = new ArrayList<Integer>(numIndices);

        int v = 0;
        for (int stack = 0; stack < stacks; stack++) {
            for (int slice = 0; slice < slices; slice++) {
                // Skip triangles at the caps that would have an area of zero.
                boolean topCap = stack == 0;
                boolean bottomCap = stack == stacks - 1;

                int next = slice + 1;

                if (!topCap) {
                    triangleIndices.add(v + slice);
                    triangleIndices.add(v + next);
                    triangleIndices.add(v + slice + slices + 1);
                }

                if (!bottomCap) {
                    triangleIndices.add(v + next);
                    triangleIndices.add(v + next + slices + 1);
                    triangleIndices.add(v + slice + slices + 1);
                }
            }
            v += slices + 1;
        }

        //desc-updated by tanyx 2021年11月16日18:12:03
        ArrayList<Integer> innerTriangleIndices = new ArrayList<Integer>(numIndices);
        for (int i = triangleIndices.size() - 1; i > 0 ; i--) {
            innerTriangleIndices.add(triangleIndices.get(i));
        }
        RenderableDefinition.SubGeometry subGeometry =
                RenderableDefinition.SubGeometry.builder()
                        .setTriangleIndices(/*triangleIndices*/innerTriangleIndices)
                        .setMaterial(material)
                        .build();
        RenderableDefinition renderableDefinition =
                RenderableDefinition.builder()
                        .setVertices(vertices)
                        .setSubGeometries(Arrays.asList(subGeometry))
                        .build();

        CompletableFuture<ModelRenderable> future =
                ModelRenderable.builder().setSource(renderableDefinition).build();

        ModelRenderable result;
        try {
            result = future.get();
        } catch (ExecutionException ex) {
            throw new AssertionError("Error creating renderable.", ex);
        }catch (InterruptedException ex) {
            throw new AssertionError("Error creating renderable.", ex);
        }

        if (result == null) {
            throw new AssertionError("Error creating renderable.");
        }

        return result;
    }

    /**
     * 创建竖直四边形
     * <p>
     *     Quad 位于 XY 平面，宽度使用 {@code size.x}，高度使用 {@code size.y}，
     *     {@code size.z} 不参与尺寸计算。正面法向为 {@link Vector3#back()}，即 Z 轴正方向。
     *     UV 从左下角 {@code (0, 0)} 到右上角 {@code (1, 1)}，适合直接显示普通纹理或外部视频纹理。
     * </p>
     * @param size Quad 尺寸，x 为宽度、y 为高度
     * @param center Quad 中心点
     * @param material 材质
     * @return 法向朝 Z 轴正方向的竖直 Quad 渲染对象
     */
    public static ModelRenderable makeQuad(Vector3 size, Vector3 center, Material material) {
        AndroidPreconditions.checkMinAndroidApiLevel();
        Vector3 extents = size.scaled(0.5f);

        Vector3 p0 = Vector3.add(center, new Vector3(-extents.x, -extents.y, 0));
        Vector3 p1 = Vector3.add(center, new Vector3(extents.x, -extents.y, 0));
        Vector3 p2 = Vector3.add(center, new Vector3(extents.x, extents.y, 0));
        Vector3 p3 = Vector3.add(center, new Vector3(-extents.x, extents.y, 0));

        Vector3 back = Vector3.back();
        Vertex.UvCoordinate uv00 = new Vertex.UvCoordinate(0.0f, 0.0f);
        Vertex.UvCoordinate uv10 = new Vertex.UvCoordinate(1.0f, 0.0f);
        Vertex.UvCoordinate uv11 = new Vertex.UvCoordinate(1.0f, 1.0f);
        Vertex.UvCoordinate uv01 = new Vertex.UvCoordinate(0.0f, 1.0f);

        ArrayList<Vertex> vertices = new ArrayList<>(Arrays.asList(
                Vertex.builder().setPosition(p0).setNormal(back).setUvCoordinate(uv00).build(),
                Vertex.builder().setPosition(p1).setNormal(back).setUvCoordinate(uv10).build(),
                Vertex.builder().setPosition(p2).setNormal(back).setUvCoordinate(uv11).build(),
                Vertex.builder().setPosition(p3).setNormal(back).setUvCoordinate(uv01).build()));

        //desc- 逆时针绕序使几何正面与顶点法向一致，均朝向 Z 轴正方向。
        List<Integer> triangleIndices = Arrays.asList(0, 1, 2, 2, 3, 0);
        RenderableDefinition.SubGeometry subGeometry = RenderableDefinition.SubGeometry.builder()
                .setTriangleIndices(triangleIndices)
                .setMaterial(material)
                .build();
        RenderableDefinition renderableDefinition = RenderableDefinition.builder()
                .setVertices(vertices)
                .setSubGeometries(Arrays.asList(subGeometry))
                .build();

        CompletableFuture<ModelRenderable> future = ModelRenderable.builder()
                .setSource(renderableDefinition)
                .build();
        ModelRenderable result;
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

    /**
     * 创建平面
     * <p>法向量向上</p>
     * @param size 尺寸
     * @param center 中心点
     * @param material 材质
     * @return 渲染对象
     */
    public static ModelRenderable makePlane(Vector3 size, Vector3 center, Material material) {
        AndroidPreconditions.checkMinAndroidApiLevel();
        Vector3 extents = size.scaled(0.5f);
        Vector3 p0 = Vector3.add(center, new Vector3(-extents.x, 0, -extents.z));
        Vector3 p1 = Vector3.add(center, new Vector3(-extents.x, 0, extents.z));
        Vector3 p2 = Vector3.add(center, new Vector3(extents.x, 0, extents.z));
        Vector3 p3 = Vector3.add(center, new Vector3(extents.x, 0, -extents.z));

        //法向量
        Vector3 up = Vector3.up();
        Vertex.UvCoordinate uv00 = new Vertex.UvCoordinate(0.0f, 0.0f);
        Vertex.UvCoordinate uv10 = new Vertex.UvCoordinate(1.0f, 0.0f);
        Vertex.UvCoordinate uv01 = new Vertex.UvCoordinate(0.0f, 1.0f);
        Vertex.UvCoordinate uv11 = new Vertex.UvCoordinate(1.0f, 1.0f);

        ArrayList<Vertex> vertices =
                new ArrayList<Vertex>(
                        Arrays.asList(Vertex.builder().setPosition(p0).setNormal(up).setUvCoordinate(uv01).build(),
                                Vertex.builder().setPosition(p1).setNormal(up).setUvCoordinate(uv11).build(),
                                Vertex.builder().setPosition(p2).setNormal(up).setUvCoordinate(uv10).build(),
                                Vertex.builder().setPosition(p3).setNormal(up).setUvCoordinate(uv00).build()));

        List<Integer> triangleIndices = Arrays.asList(0, 1, 2, 2, 3, 0);
        RenderableDefinition.SubGeometry subGeometry =
                RenderableDefinition.SubGeometry.builder().setTriangleIndices(triangleIndices).setMaterial(material).build();

        RenderableDefinition renderableDefinition =
                RenderableDefinition.builder()
                        .setVertices(vertices)
                        .setSubGeometries(Arrays.asList(subGeometry))
                        .build();
        CompletableFuture<ModelRenderable> future =
                ModelRenderable.builder().setSource(renderableDefinition).build();

        ModelRenderable result;
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


    /**
     * 根据空间坐标创建独立点图元。
     * <p>坐标列表中的每个元素对应一个点，材质应使用
     * {@link com.google.sceneform.rendering.MaterialFactory#makePointsWithColor} 创建。</p>
     * <pre>{@code
     * List<Vector3> points = Arrays.asList(
     *         new Vector3(-0.5f, 0.0f, -3.0f),
     *         new Vector3(0.0f, 0.5f, -3.0f),
     *         new Vector3(0.5f, 0.0f, -3.0f));
     * ModelRenderable renderable = GeometryUtils.makePoints(points, pointMaterial);
     * }</pre>
     *
     * @param vertexPositions 点的空间坐标，至少包含一个坐标
     * @param material 点图元材质
     * @return 使用 {@link RenderableManager.PrimitiveType#POINTS} 绘制的渲染对象
     * @throws IllegalArgumentException 当坐标列表为空或包含空元素时抛出
     */
    public static ModelRenderable makePoints(List<Vector3> vertexPositions, Material material) {
        validateVertexPositions(vertexPositions, 1, "POINTS");
        return buildPrimitiveRenderable(
                createPositionVertices(vertexPositions),
                createSequentialIndices(vertexPositions.size()),
                RenderableManager.PrimitiveType.POINTS,
                material);
    }

    /**
     * 根据空间坐标创建多条相互独立的宽线段。
     * <p>每两个连续坐标定义一条线段。内部会把每条线展开为两个三角形，并自动写入
     * {@code CUSTOM0} 顶点属性；材质应使用
     * {@link com.google.sceneform.rendering.MaterialFactory#makeLinesWithColor} 创建。</p>
     * <pre>{@code
     * List<Vector3> lines = Arrays.asList(
     *         new Vector3(-1.0f, 0.0f, -3.0f),
     *         new Vector3(0.0f, 1.0f, -3.0f),  // 第一条线
     *         new Vector3(0.0f, -1.0f, -3.0f),
     *         new Vector3(1.0f, 0.0f, -3.0f)); // 第二条线
     * ModelRenderable renderable = GeometryUtils.makeLines(lines, lineMaterial);
     * }</pre>
     *
     * @param vertexPositions 线段端点坐标，数量必须为不小于 2 的偶数
     * @param material 屏幕空间宽线材质
     * @return 实际使用三角形拓扑绘制的宽线渲染对象
     * @throws IllegalArgumentException 当坐标数量不符合 LINES 拓扑或包含零长度线段时抛出
     */
    public static ModelRenderable makeLines(List<Vector3> vertexPositions, Material material) {
        validateVertexPositions(vertexPositions, 2, "LINES");
        if ((vertexPositions.size() & 1) != 0) {
            throw new IllegalArgumentException("LINES requires an even number of vertex positions.");
        }
        PrimitiveGeometryData geometry = new PrimitiveGeometryData();
        for (int index = 0; index < vertexPositions.size(); index += 2) {
            addWideLineSegment(
                    geometry, vertexPositions.get(index), vertexPositions.get(index + 1));
        }
        return buildPrimitiveRenderable(
                geometry.vertices,
                geometry.indices,
                RenderableManager.PrimitiveType.TRIANGLES,
                material);
    }

    /**
     * 根据空间坐标创建一条连续宽折线。
     * <p>相邻坐标依次组成线段，例如 4 个坐标会生成 3 条线段。内部会自动完成三角形
     * 展开与 {@code CUSTOM0} 顶点属性填充；材质应使用
     * {@link com.google.sceneform.rendering.MaterialFactory#makeLinesWithColor} 创建。</p>
     * <pre>{@code
     * List<Vector3> lineStrip = Arrays.asList(
     *         new Vector3(-1.0f, -0.5f, -3.0f),
     *         new Vector3(-0.3f, 0.5f, -3.0f),
     *         new Vector3(0.3f, -0.5f, -3.0f),
     *         new Vector3(1.0f, 0.5f, -3.0f));
     * ModelRenderable renderable = GeometryUtils.makeLineStrip(lineStrip, lineMaterial);
     * }</pre>
     *
     * @param vertexPositions 连续折线坐标，至少包含两个坐标
     * @param material 屏幕空间宽线材质
     * @return 实际使用三角形拓扑绘制的连续宽线渲染对象
     * @throws IllegalArgumentException 当坐标不足或包含零长度线段时抛出
     */
    public static ModelRenderable makeLineStrip(
            List<Vector3> vertexPositions, Material material) {
        validateVertexPositions(vertexPositions, 2, "LINE_STRIP");
        PrimitiveGeometryData geometry = new PrimitiveGeometryData();
        for (int index = 0; index + 1 < vertexPositions.size(); index++) {
            addWideLineSegment(
                    geometry, vertexPositions.get(index), vertexPositions.get(index + 1));
        }
        return buildPrimitiveRenderable(
                geometry.vertices,
                geometry.indices,
                RenderableManager.PrimitiveType.TRIANGLES,
                material);
    }

    /**
     * 根据空间坐标创建多个相互独立的三角形。
     * <p>每三个连续坐标定义一个三角形，顶点绕序决定正面方向。工具类会自动计算面法线，
     * 并根据坐标跨度最大的两个轴生成 0 到 1 范围的平面 UV。</p>
     * <pre>{@code
     * List<Vector3> triangles = Arrays.asList(
     *         new Vector3(-0.8f, -0.5f, -3.0f),
     *         new Vector3(0.8f, -0.5f, -3.0f),
     *         new Vector3(0.0f, 0.7f, -3.0f));
     * ModelRenderable renderable = GeometryUtils.makeTriangles(triangles, material);
     * }</pre>
     *
     * @param vertexPositions 三角形顶点坐标，数量必须为不小于 3 的 3 的倍数
     * @param material 三角形材质
     * @return 使用 {@link RenderableManager.PrimitiveType#TRIANGLES} 绘制的渲染对象
     * @throws IllegalArgumentException 当坐标数量不符合 TRIANGLES 拓扑或三角形退化时抛出
     */
    public static ModelRenderable makeTriangles(
            List<Vector3> vertexPositions, Material material) {
        validateVertexPositions(vertexPositions, 3, "TRIANGLES");
        if (vertexPositions.size() % 3 != 0) {
            throw new IllegalArgumentException(
                    "TRIANGLES requires a multiple of three vertex positions.");
        }
        List<Vector3> normals = createTriangleNormals(vertexPositions, false);
        return buildPrimitiveRenderable(
                createSurfaceVertices(vertexPositions, normals),
                createSequentialIndices(vertexPositions.size()),
                RenderableManager.PrimitiveType.TRIANGLES,
                material);
    }

    /**
     * 根据空间坐标创建连续三角带。
     * <p>前三个坐标组成第一个三角形，之后每增加一个坐标生成一个新三角形。工具类会按
     * TRIANGLE_STRIP 的奇偶绕序累计平滑法线，并生成平面 UV。</p>
     * <pre>{@code
     * List<Vector3> triangleStrip = Arrays.asList(
     *         new Vector3(-1.0f, 0.5f, -3.0f),
     *         new Vector3(-1.0f, -0.5f, -3.0f),
     *         new Vector3(0.0f, 0.5f, -3.0f),
     *         new Vector3(0.0f, -0.5f, -3.0f),
     *         new Vector3(1.0f, 0.5f, -3.0f),
     *         new Vector3(1.0f, -0.5f, -3.0f));
     * ModelRenderable renderable = GeometryUtils.makeTriangleStrip(triangleStrip, material);
     * }</pre>
     *
     * @param vertexPositions 三角带顶点坐标，至少包含三个坐标
     * @param material 三角带材质
     * @return 使用 {@link RenderableManager.PrimitiveType#TRIANGLE_STRIP} 绘制的渲染对象
     * @throws IllegalArgumentException 当坐标不足或三角带包含退化三角形时抛出
     */
    public static ModelRenderable makeTriangleStrip(
            List<Vector3> vertexPositions, Material material) {
        validateVertexPositions(vertexPositions, 3, "TRIANGLE_STRIP");
        List<Vector3> normals = createTriangleNormals(vertexPositions, true);
        return buildPrimitiveRenderable(
                createSurfaceVertices(vertexPositions, normals),
                createSequentialIndices(vertexPositions.size()),
                RenderableManager.PrimitiveType.TRIANGLE_STRIP,
                material);
    }

    /** 校验基本图元坐标列表。 */
    private static void validateVertexPositions(
            List<Vector3> vertexPositions, int minimumSize, String primitiveName) {
        if (vertexPositions == null || vertexPositions.size() < minimumSize) {
            throw new IllegalArgumentException(
                    primitiveName + " requires at least " + minimumSize + " vertex positions.");
        }
        for (int index = 0; index < vertexPositions.size(); index++) {
            if (vertexPositions.get(index) == null) {
                throw new IllegalArgumentException(
                        primitiveName + " vertex position at index " + index + " is null.");
            }
        }
    }

    /** 将空间坐标转换为仅包含位置属性的顶点。 */
    private static List<Vertex> createPositionVertices(List<Vector3> vertexPositions) {
        List<Vertex> vertices = new ArrayList<>(vertexPositions.size());
        for (Vector3 position : vertexPositions) {
            vertices.add(Vertex.builder().setPosition(position).build());
        }
        return vertices;
    }

    /** 为顶点创建从 0 开始的一一对应索引。 */
    private static List<Integer> createSequentialIndices(int vertexCount) {
        List<Integer> indices = new ArrayList<>(vertexCount);
        for (int index = 0; index < vertexCount; index++) {
            indices.add(index);
        }
        return indices;
    }

    /** 添加一条由两个三角形组成的屏幕空间宽线段。 */
    private static void addWideLineSegment(
            PrimitiveGeometryData geometry, Vector3 start, Vector3 end) {
        if (Vector3.subtract(end, start).lengthSquared() <= DEGENERATE_EPSILON) {
            //"Line segment endpoints must not overlap."
            return;
        }
        int baseIndex = geometry.vertices.size();
        addWideLineVertex(geometry, start, end, 1.0f);
        addWideLineVertex(geometry, start, end, -1.0f);
        addWideLineVertex(geometry, end, start, -1.0f);
        addWideLineVertex(geometry, end, start, 1.0f);

        geometry.indices.add(baseIndex);
        geometry.indices.add(baseIndex + 1);
        geometry.indices.add(baseIndex + 2);
        geometry.indices.add(baseIndex + 2);
        geometry.indices.add(baseIndex + 1);
        geometry.indices.add(baseIndex + 3);
    }

    /** 添加宽线材质要求的位置、颜色和 CUSTOM0 顶点属性。 */
    private static void addWideLineVertex(
            PrimitiveGeometryData geometry,
            Vector3 position,
            Vector3 otherEndpoint,
            float side) {
        geometry.vertices.add(Vertex.builder()
                .setPosition(position)
                .setColor(WHITE_VERTEX_COLOR)
                .setCustom0(new Vertex.Float4(
                        otherEndpoint.x, otherEndpoint.y, otherEndpoint.z, side))
                .build());
    }

    /** 计算独立三角形或三角带的逐顶点法线。 */
    private static List<Vector3> createTriangleNormals(
            List<Vector3> positions, boolean triangleStrip) {
        List<Vector3> normals = new ArrayList<>(positions.size());
        for (int index = 0; index < positions.size(); index++) {
            normals.add(Vector3.zero());
        }

        int triangleCount = triangleStrip ? positions.size() - 2 : positions.size() / 3;
        for (int triangle = 0; triangle < triangleCount; triangle++) {
            int first = triangleStrip ? triangle : triangle * 3;
            int second = first + 1;
            int third = first + 2;
            //desc- TRIANGLE_STRIP 会交替顶点绕序，交换奇数三角形的前两个顶点以保持法线方向一致。
            if (triangleStrip && (triangle & 1) != 0) {
                int swap = first;
                first = second;
                second = swap;
            }
            Vector3 edge1 = Vector3.subtract(positions.get(second), positions.get(first));
            Vector3 edge2 = Vector3.subtract(positions.get(third), positions.get(first));
            Vector3 faceNormal = Vector3.cross(edge1, edge2);
            if (faceNormal.lengthSquared() <= DEGENERATE_EPSILON) {
                continue;
            }
            normals.set(first, Vector3.add(normals.get(first), faceNormal));
            normals.set(second, Vector3.add(normals.get(second), faceNormal));
            normals.set(third, Vector3.add(normals.get(third), faceNormal));
        }

        for (int index = 0; index < normals.size(); index++) {
            normals.set(index, normals.get(index).normalized());
        }
        return normals;
    }

    /** 创建带自动法线和平面 UV 的表面顶点。 */
    private static List<Vertex> createSurfaceVertices(
            List<Vector3> positions, List<Vector3> normals) {
        List<Vertex.UvCoordinate> uvCoordinates = createPlanarUvCoordinates(positions);
        List<Vertex> vertices = new ArrayList<>(positions.size());
        for (int index = 0; index < positions.size(); index++) {
            vertices.add(Vertex.builder()
                    .setPosition(positions.get(index))
                    .setNormal(normals.get(index))
                    .setUvCoordinate(uvCoordinates.get(index))
                    .build());
        }
        return vertices;
    }

    /** 根据坐标跨度最大的两个轴生成平面 UV。 */
    private static List<Vertex.UvCoordinate> createPlanarUvCoordinates(List<Vector3> positions) {
        float[] minimum = {Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY};
        float[] maximum = {Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
        for (Vector3 position : positions) {
            float[] values = {position.x, position.y, position.z};
            for (int axis = 0; axis < 3; axis++) {
                minimum[axis] = Math.min(minimum[axis], values[axis]);
                maximum[axis] = Math.max(maximum[axis], values[axis]);
            }
        }
        float[] ranges = {
                maximum[0] - minimum[0],
                maximum[1] - minimum[1],
                maximum[2] - minimum[2]
        };
        int uAxis = 0;
        for (int axis = 1; axis < 3; axis++) {
            if (ranges[axis] > ranges[uAxis]) {
                uAxis = axis;
            }
        }
        int vAxis = uAxis == 0 ? 1 : 0;
        for (int axis = 0; axis < 3; axis++) {
            if (axis != uAxis && ranges[axis] > ranges[vAxis]) {
                vAxis = axis;
            }
        }

        List<Vertex.UvCoordinate> result = new ArrayList<>(positions.size());
        for (Vector3 position : positions) {
            float[] values = {position.x, position.y, position.z};
            float u = ranges[uAxis] > 0.0f
                    ? (values[uAxis] - minimum[uAxis]) / ranges[uAxis]
                    : 0.5f;
            float v = ranges[vAxis] > 0.0f
                    ? (values[vAxis] - minimum[vAxis]) / ranges[vAxis]
                    : 0.5f;
            result.add(new Vertex.UvCoordinate(u, v));
        }
        return result;
    }

    /** 构建指定拓扑的动态 ModelRenderable。 */
    private static ModelRenderable buildPrimitiveRenderable(
            List<Vertex> vertices,
            List<Integer> indices,
            RenderableManager.PrimitiveType primitiveType,
            Material material) {
        AndroidPreconditions.checkMinAndroidApiLevel();
        if (material == null) {
            throw new IllegalArgumentException("Primitive material must not be null.");
        }
        RenderableDefinition.SubGeometry subGeometry = RenderableDefinition.SubGeometry.builder()
                .setTriangleIndices(indices)
                .setMaterial(material)
                .build();
        RenderableDefinition definition = RenderableDefinition.builder()
                .setVertices(vertices)
                .setSubGeometries(Collections.singletonList(subGeometry))
                .setPrimitiveType(primitiveType)
                .build();
        CompletableFuture<ModelRenderable> future = ModelRenderable.builder()
                .setSource(definition)
                .build();
        try {
            ModelRenderable renderable = future.get();
            if (renderable == null) {
                throw new AssertionError("Error creating primitive renderable.");
            }
            return renderable;
        } catch (ExecutionException exception) {
            throw new AssertionError("Error creating primitive renderable.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while creating primitive renderable.", exception);
        }
    }

    /** 基本图元构建过程使用的顶点与索引容器。 */
    private static final class PrimitiveGeometryData {
        final List<Vertex> vertices = new ArrayList<>();
        final List<Integer> indices = new ArrayList<>();
    }

}
