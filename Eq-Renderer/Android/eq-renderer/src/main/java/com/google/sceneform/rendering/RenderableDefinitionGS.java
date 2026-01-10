package com.google.sceneform.rendering;

import com.eqgis.eqr.data.JPlyGS3dAsset;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.IndexBuffer.Builder.IndexType;
import com.google.android.filament.VertexBuffer;
import com.google.android.filament.VertexBuffer.VertexAttribute;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.utilities.AndroidPreconditions;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * 自定义渲染对象
 * <p>
 *     可以用来动态地构造和修改可渲染对象。
 * </p>
 *
 * @see ModelRenderable.Builder
 * @see ViewRenderable.Builder
 */
public class RenderableDefinitionGS implements IRenderableDefinition{

    private List<Vertex> vertices;
    private List<RenderableDefinition.Submesh> submeshes;

    private static final int BYTES_PER_FLOAT = Float.SIZE / 8;
    private static final int POSITION_SIZE = 3; // x, y, z
    private static final int UV_SIZE = 2;
    private static final int F4_SIZE = 4; // Float4
    private JPlyGS3dAsset asset;
    private int shDegree = 0;

    public void setVertices(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void setSubmeshes(List<RenderableDefinition.Submesh> submeshes) {
        this.submeshes = submeshes;
    }

    public List<RenderableDefinition.Submesh> getSubmeshes() {
        return submeshes;
    }

    public void applyDefinitionToData(
            IRenderableInternalData data,
            ArrayList<Material> materialBindings,
            ArrayList<String> materialNames) {
        AndroidPreconditions.checkUiThread();

        applyDefinitionToDataIndexBuffer(data);
        applyDefinitionToDataVertexBuffer(data);

        // 添加网格数据
        int indexStart = 0;
        materialBindings.clear();
        materialNames.clear();
        for (int i = 0; i < submeshes.size(); i++) {
            RenderableDefinition.Submesh submesh = submeshes.get(i);

            RenderableInternalData.MeshData meshData;
            if (i < data.getMeshes().size()) {
                meshData = data.getMeshes().get(i);
            } else {
                meshData = new RenderableInternalData.MeshData();
                data.getMeshes().add(meshData);
            }

            meshData.indexStart = indexStart;
            meshData.indexEnd = indexStart + submesh.getTriangleIndices().size();
            indexStart = meshData.indexEnd;
            materialBindings.add(submesh.getMaterial());
            final String name = submesh.getName();
            materialNames.add(name != null ? name : "");
        }

        // 移除旧数据
        while (data.getMeshes().size() > submeshes.size()) {
            data.getMeshes().remove(data.getMeshes().size() - 1);
        }
    }

    private void applyDefinitionToDataIndexBuffer(IRenderableInternalData data) {
        // 计算顶点索引
        int numIndices = 0;
        for (int i = 0; i < submeshes.size(); i++) {
            RenderableDefinition.Submesh submesh = submeshes.get(i);
            numIndices += submesh.getTriangleIndices().size();
        }

        // 创建原始IndexBuffer
        IntBuffer rawIndexBuffer = data.getRawIndexBuffer();
        if (rawIndexBuffer == null || rawIndexBuffer.capacity() < numIndices) {
            rawIndexBuffer = IntBuffer.allocate(numIndices);
            data.setRawIndexBuffer(rawIndexBuffer);
        } else {
            rawIndexBuffer.rewind();
        }

        //填充索引数据
        for (int i = 0; i < submeshes.size(); i++) {
            RenderableDefinition.Submesh submesh = submeshes.get(i);
            List<Integer> triangleIndices = submesh.getTriangleIndices();
            for (int j = 0; j < triangleIndices.size(); j++) {
                rawIndexBuffer.put(triangleIndices.get(j));
            }
        }
        rawIndexBuffer.rewind();

        //创建filament的索引缓冲区
        IndexBuffer indexBuffer = data.getIndexBuffer();
        IEngine engine = EngineInstance.getEngine();
        if (indexBuffer == null || indexBuffer.getIndexCount() < numIndices) {
            if (indexBuffer != null) {
                engine.destroyIndexBuffer(indexBuffer);
            }

            indexBuffer =
                    new IndexBuffer.Builder()
                            .indexCount(numIndices)
                            .bufferType(IndexType.UINT)
                            .build(engine.getFilamentEngine());
            data.setIndexBuffer(indexBuffer);
        }

        indexBuffer.setBuffer(engine.getFilamentEngine(), rawIndexBuffer, 0, numIndices);
    }

    private void applyDefinitionToDataVertexBuffer(IRenderableInternalData data) {
        if (vertices.isEmpty()) {
            throw new IllegalArgumentException("RenderableDescription must have at least one vertex.");
        }

        int numVertices = vertices.size();
        Vertex firstVertex = vertices.get(0);

        //计算顶点数据
        EnumSet<VertexAttribute> descriptionAttributes = EnumSet.of(VertexAttribute.POSITION);
        if (firstVertex.getUvCoordinate() != null) {
            descriptionAttributes.add(VertexAttribute.UV0);
        }
        addAttributes(descriptionAttributes);

        //计算vertexBuffer
        VertexBuffer vertexBuffer = data.getVertexBuffer();
        boolean createVertexBuffer = true;
        if (vertexBuffer != null) {
            EnumSet<VertexAttribute> oldAttributes = EnumSet.of(VertexAttribute.POSITION);
            if (data.getRawUvBuffer() != null) {
                oldAttributes.add(VertexAttribute.UV0);
            }
            addAttributes(oldAttributes);

            createVertexBuffer =
                    !oldAttributes.equals(descriptionAttributes)
                            || vertexBuffer.getVertexCount() < numVertices;

            if (createVertexBuffer) {
                EngineInstance.getEngine().destroyVertexBuffer(vertexBuffer);
            }
        }

        if (createVertexBuffer) {
            vertexBuffer = createVertexBuffer(numVertices, descriptionAttributes);
            data.setVertexBuffer(vertexBuffer);
        }

        //创建顶点缓冲区
        FloatBuffer positionBuffer = data.getRawPositionBuffer();
        if (positionBuffer == null || positionBuffer.capacity() < numVertices * POSITION_SIZE) {
            positionBuffer = FloatBuffer.allocate(numVertices * POSITION_SIZE);
            data.setRawPositionBuffer(positionBuffer);
        } else {
            positionBuffer.rewind();
        }

        //创建uvBuffer
        FloatBuffer uvBuffer = data.getRawUvBuffer();
        if (descriptionAttributes.contains(VertexAttribute.UV0)
                && (uvBuffer == null || uvBuffer.capacity() < numVertices * UV_SIZE)) {
            uvBuffer = FloatBuffer.allocate(numVertices * UV_SIZE);
            data.setRawUvBuffer(uvBuffer);
        } else if (uvBuffer != null) {
            uvBuffer.rewind();
        }

        //创建colorBuffer
        FloatBuffer colorBuffer = data.getRawColorBuffer();
        if (descriptionAttributes.contains(VertexAttribute.COLOR)
                && (colorBuffer == null || colorBuffer.capacity() < numVertices * F4_SIZE)) {
            colorBuffer = FloatBuffer.allocate(numVertices * F4_SIZE);
            data.setRawColorBuffer(colorBuffer);
        } else if (colorBuffer != null) {
            colorBuffer.rewind();
        }

        FloatBuffer custom0 = FloatBuffer.allocate(numVertices * F4_SIZE);
        FloatBuffer custom7 = FloatBuffer.allocate(numVertices * F4_SIZE);
        FloatBuffer custom1 = FloatBuffer.allocate(numVertices * F4_SIZE);
        FloatBuffer custom2 = FloatBuffer.allocate(numVertices * F4_SIZE);
        FloatBuffer custom3 = FloatBuffer.allocate(numVertices * F4_SIZE);

        // 更新原始缓冲区并在一次遍历顶点时计算Aabb。
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);


            // Position
            Vector3 position = vertex.getPosition();
            addVector3ToBuffer(position, positionBuffer);

            // Uv
            if (uvBuffer != null) {
                Vertex.UvCoordinate uvCoordinate = vertex.getUvCoordinate();
                if (uvCoordinate == null) {
                    throw new IllegalArgumentException(
                            "Missing UV Coordinate: If any Vertex in a "
                                    + "RenderableDescription has a UV Coordinate, all vertices must have one.");
                }

                addUvToBuffer(uvCoordinate, uvBuffer);
            }

            // Color，实际材质中为scale
            if(asset.scale != null){
                addFloat3ToBuffer(asset.scale[3 * i],asset.scale[3 * i+1], asset.scale[3 * i+2],colorBuffer);
            }
            if (asset.rot != null){
                addFloat4ToBuffer(asset.rot[4*i],asset.rot[4*i+1],asset.rot[4*i+2],asset.rot[4*i+3],custom7);
            }
            float opacityValue = asset.opacity!=null ? asset.opacity[i] : 1.0f;
            addFloat4ToBuffer(asset.f_dc[3 * i],asset.f_dc[3 * i + 1], asset.f_dc[3 * i + 2],opacityValue,custom0);

            if (shDegree > 0){
                addFloat4ToBuffer(asset.f_rest[asset.dimension * i],
                        asset.f_rest[asset.dimension * i + 1],
                        asset.f_rest[asset.dimension * i + 2],
                        asset.f_rest[asset.dimension * i + 3],
                        custom1);
                addFloat4ToBuffer(asset.f_rest[asset.dimension * i + 4],
                        asset.f_rest[asset.dimension * i + 5],
                        asset.f_rest[asset.dimension * i + 6],
                        asset.f_rest[asset.dimension * i + 7],
                        custom2);
                addFloat4ToBuffer(
                        asset.f_rest[asset.dimension * i + 8],
                        asset.f_rest[asset.dimension * i + 9],
                        asset.f_rest[asset.dimension * i + 10],
                        asset.f_rest[asset.dimension * i + 11],
                        custom3);
            }
        }


        IEngine engine = EngineInstance.getEngine();
        positionBuffer.rewind();
        int bufferIndex = 0;
        vertexBuffer.setBufferAt(
                engine.getFilamentEngine(), bufferIndex, positionBuffer, 0, numVertices * POSITION_SIZE);

        if (uvBuffer != null) {
            uvBuffer.rewind();
            bufferIndex++;
            vertexBuffer.setBufferAt(
                    engine.getFilamentEngine(), bufferIndex, uvBuffer, 0, numVertices * UV_SIZE);
        }

        bufferIndex = setBufferDataFloat4(colorBuffer, bufferIndex, vertexBuffer, engine, numVertices);
        bufferIndex = setBufferDataFloat4(custom0, bufferIndex, vertexBuffer, engine, numVertices);
        bufferIndex = setBufferDataFloat4(custom7, bufferIndex, vertexBuffer, engine, numVertices);
        switch (shDegree){
            case 0:
                break;
            case 1:
            case 2:
            case 3:
                bufferIndex = setBufferDataFloat4(custom1, bufferIndex, vertexBuffer, engine, numVertices);
                bufferIndex = setBufferDataFloat4(custom2, bufferIndex, vertexBuffer, engine, numVertices);
                setBufferDataFloat4(custom3, bufferIndex, vertexBuffer, engine, numVertices);
                break;
            default:
        }
    }

    private int setBufferDataFloat4(FloatBuffer custom0, int bufferIndex, VertexBuffer vertexBuffer, IEngine engine, int numVertices) {
        if (custom0 != null) {
            custom0.rewind();
            bufferIndex++;
            vertexBuffer.setBufferAt(
                    engine.getFilamentEngine(), bufferIndex, custom0, 0, numVertices * F4_SIZE);
        }
        return bufferIndex;
    }

    private void addAttributes(EnumSet<VertexAttribute> descriptionAttributes) {
        shDegree = Math.min(asset.shDegree, 1); // 支持0~1阶
        descriptionAttributes.add(VertexAttribute.COLOR);

        // --- 计算 f_rest buffer 数量 ---
//        int fRestCoeffCount = shDegree * (shDegree + 2) * 3; // SH系数个数
//        int fRestBufferCount = (fRestCoeffCount + 3) / 4;    // 每个CUSTOM4-6存4个float
//
//        int baseBufferCount = 5; // POSITION, COLOR, TANGENTS, CUSTOM0, CUSTOM7
//        int totalBufferCount = baseBufferCount + fRestBufferCount;

        if (asset.f_dc != null){
            descriptionAttributes.add(VertexAttribute.CUSTOM0);
        }
        if (asset.rot != null){
            descriptionAttributes.add(VertexAttribute.CUSTOM7);
        }
        if (shDegree >= 1) {
            descriptionAttributes.add(VertexAttribute.CUSTOM1);
            descriptionAttributes.add(VertexAttribute.CUSTOM2);
            descriptionAttributes.add(VertexAttribute.CUSTOM3);
        }
//        if (shDegree == 2) {
//            descriptionAttributes.add(VertexAttribute.CUSTOM4);
//            descriptionAttributes.add(VertexAttribute.CUSTOM5);
//            descriptionAttributes.add(VertexAttribute.CUSTOM6);
//        }
    }


    private static VertexBuffer createVertexBuffer(
            int vertexCount, EnumSet<VertexAttribute> attributes) {
        VertexBuffer.Builder builder = new VertexBuffer.Builder();

        builder.vertexCount(vertexCount).bufferCount(attributes.size());

        // Position
        int bufferIndex = 0;
        builder.attribute(
                VertexAttribute.POSITION,
                bufferIndex,
                VertexBuffer.AttributeType.FLOAT3,
                0,
                POSITION_SIZE * BYTES_PER_FLOAT);

        // Uv
        if (attributes.contains(VertexAttribute.UV0)) {
            bufferIndex++;
            builder.attribute(
                    VertexAttribute.UV0,
                    bufferIndex,
                    VertexBuffer.AttributeType.FLOAT2,
                    0,
                    UV_SIZE * BYTES_PER_FLOAT);
        }

        // Color
        if (attributes.contains(VertexAttribute.COLOR)) {
            bufferIndex++;
            builder.attribute(
                    VertexAttribute.COLOR,
                    bufferIndex,
                    VertexBuffer.AttributeType.FLOAT4,
                    0,
                    F4_SIZE * BYTES_PER_FLOAT);
        }

        //desc-CUSTOM0
        if (attributes.contains(VertexAttribute.CUSTOM0)){
            bufferIndex++;
            builder.attribute(
                    VertexAttribute.CUSTOM0,
                    bufferIndex,
                    VertexBuffer.AttributeType.FLOAT4,
                    0,
                    F4_SIZE * BYTES_PER_FLOAT);
        }
        //custom7
        if (attributes.contains(VertexAttribute.CUSTOM7)){
            bufferIndex++;
            builder.attribute(
                    VertexAttribute.CUSTOM7,
                    bufferIndex,
                    VertexBuffer.AttributeType.FLOAT4,
                    0,
                    F4_SIZE * BYTES_PER_FLOAT);
        }
        //desc-custom1
        if (attributes.contains(VertexAttribute.CUSTOM1)){
            bufferIndex++;
            builder.attribute(
                    VertexAttribute.CUSTOM1,
                    bufferIndex,
                    VertexBuffer.AttributeType.FLOAT4,
                    0,
                    F4_SIZE * BYTES_PER_FLOAT);
        }
        //desc-CUSTOM2
        if (attributes.contains(VertexAttribute.CUSTOM2)){
            bufferIndex++;
            builder.attribute(
                    VertexAttribute.CUSTOM2,
                    bufferIndex,
                    VertexBuffer.AttributeType.FLOAT4,
                    0,
                    F4_SIZE * BYTES_PER_FLOAT);
        }
        //desc-CUSTOM3
        if (attributes.contains(VertexAttribute.CUSTOM3)){
            bufferIndex++;
            builder.attribute(
                    VertexAttribute.CUSTOM3,
                    bufferIndex,
                    VertexBuffer.AttributeType.FLOAT4,
                    0,
                    F4_SIZE * BYTES_PER_FLOAT);
        }
        //desc-CUSTOM4
        if (attributes.contains(VertexAttribute.CUSTOM4)){
            bufferIndex++;
            builder.attribute(
                    VertexAttribute.CUSTOM4,
                    bufferIndex,
                    VertexBuffer.AttributeType.FLOAT4,
                    0,
                    F4_SIZE * BYTES_PER_FLOAT);
        }
        //desc-CUSTOM5
        if (attributes.contains(VertexAttribute.CUSTOM5)){
            bufferIndex++;
            builder.attribute(
                    VertexAttribute.CUSTOM5,
                    bufferIndex,
                    VertexBuffer.AttributeType.FLOAT4,
                    0,
                    F4_SIZE * BYTES_PER_FLOAT);
        }
        //desc-CUSTOM6
        if (attributes.contains(VertexAttribute.CUSTOM6)){
            bufferIndex++;
            builder.attribute(
                    VertexAttribute.CUSTOM6,
                    bufferIndex,
                    VertexBuffer.AttributeType.FLOAT4,
                    0,
                    F4_SIZE * BYTES_PER_FLOAT);
        }

        return builder.build(EngineInstance.getEngine().getFilamentEngine());
    }

    private static void addVector3ToBuffer(Vector3 vector3, FloatBuffer buffer) {
        buffer.put(vector3.x);
        buffer.put(vector3.y);
        buffer.put(vector3.z);
    }

    private static void addFloat4ToBuffer(float a,float b,float c,float d, FloatBuffer buffer) {
        buffer.put(a);
        buffer.put(b);
        buffer.put(c);
        buffer.put(d);
    }
    private static void addFloat3ToBuffer(float a,float b,float c,FloatBuffer buffer) {
        buffer.put(a);
        buffer.put(b);
        buffer.put(c);
    }
    private static void addUvToBuffer(Vertex.UvCoordinate uvCoordinate, FloatBuffer buffer) {
        buffer.put(uvCoordinate.x);
        buffer.put(uvCoordinate.y);
    }
    public void setGaussianSplat(JPlyGS3dAsset asset) {
        this.asset = asset;
    }
}
