package com.eqgis.eqr.utils;

import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Material;
import com.google.sceneform.rendering.ModelRenderable;
import com.google.sceneform.rendering.RenderableDefinition;
import com.google.sceneform.rendering.Vertex;
import com.google.sceneform.utilities.AndroidPreconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 *  Mesh工具类
 * @author tanyx 2024/2/4
 * @version 1.0
 **/
public class MeshUtils {
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

        return makeRenderableByCustomMesh(material, vertices, triangleIndices);
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

        return makeRenderableByCustomMesh(material, vertices, triangleIndices);
    }

    /**
     * 根据自定义网格创建渲染对象
     * @param material
     * @param vertices
     * @param triangleIndices
     * @return
     */
    public static ModelRenderable makeRenderableByCustomMesh(Material material, ArrayList<Vertex> vertices, ArrayList<Integer> triangleIndices) {
        RenderableDefinition.Submesh submesh =
                RenderableDefinition.Submesh.builder().setTriangleIndices(triangleIndices).setMaterial(material).build();

        RenderableDefinition renderableDefinition =
                RenderableDefinition.builder()
                        .setVertices(vertices)
                        .setSubmeshes(Arrays.asList(submesh))
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

        return makeRenderableByCustomMesh(material, vertices, triangleIndices);
    }

    /**
     * 创建球状天空包围盒
     * {@link #makeSphere(float, Vector3, Material) 与本方法仅顶点索引顺序相反}
     * @param radius
     * @param center
     * @param material
     * @return
     */
    @SuppressWarnings("AndroidApiChecker")
    public static ModelRenderable makeSkyBoxSphere(float radius, Vector3 center, Material material) {
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
        return makeRenderableByCustomMesh(material, vertices, innerTriangleIndices);
    }
}