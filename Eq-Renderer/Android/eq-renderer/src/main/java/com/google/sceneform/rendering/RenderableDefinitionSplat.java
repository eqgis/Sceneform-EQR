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
public class RenderableDefinitionSplat implements IRenderableDefinition{

    private List<Vertex> vertices;
    private List<RenderableDefinition.Submesh> submeshes;

    private static final int BYTES_PER_FLOAT = Float.SIZE / 8;
    private static final int POSITION_SIZE = 3; // x, y, z
    private static final int UV_SIZE = 2;
    private static final int F4_SIZE = 4; // Float4
    private JPlyGS3dAsset asset;

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
        addAttributes(descriptionAttributes);//custom012

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

        FloatBuffer custom0 = FloatBuffer.allocate(numVertices * F4_SIZE);
        FloatBuffer custom1 = FloatBuffer.allocate(numVertices * F4_SIZE);
        FloatBuffer custom2 = FloatBuffer.allocate(numVertices * F4_SIZE);
//        FloatBuffer custom3 = FloatBuffer.allocate(numVertices * F4_SIZE);

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

            addVertexBufferFromGaussian(i, custom0, custom1, custom2);
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

        //custom012
        bufferIndex = setBufferDataFloat4(custom0, bufferIndex, vertexBuffer, engine, numVertices);
        bufferIndex = setBufferDataFloat4(custom1, bufferIndex, vertexBuffer, engine, numVertices);
        bufferIndex = setBufferDataFloat4(custom2, bufferIndex, vertexBuffer, engine, numVertices);
    }

    private void addVertexBufferFromGaussian(int vertexIndex, FloatBuffer custom0, FloatBuffer custom1, FloatBuffer custom2) {
        int i = vertexIndex / 4;//一个高斯点对应1个quad，一个quad有4个顶点。
        float opacityValue = asset.opacity!=null ? asset.opacity[i] : 1.0f;
        addFloat4ToBuffer(check01(asset.f_dc[3 * i]),check01(asset.f_dc[3 * i + 1])
                ,check01( asset.f_dc[3 * i + 2]),check01(opacityValue), custom0);

//        float index = (float) (vertexIndex % 4);
//        Log.i("Ikkyu ", "addVertexBufferFromGaussian: scale + "+asset.scale[3*i]+","+asset.scale[3*i+1]+","+
//                asset.scale[3*i+2]);
//        Log.i("Ikkyu ", "addVertexBufferFromGaussian: rot + "+asset.rot[4*i]+","+asset.rot[4*i+1]+","+
//                asset.rot[4*i+2]+", "+ asset.rot[4*i+3]);

        addFloat4ToBuffer(asset.scale[3*i],asset.scale[3*i+1],
                asset.scale[3*i+2],0.05f,custom1);

//        addFloat4ToBuffer(asset.vertices[3*i],asset.vertices[3*i+1],
//                asset.vertices[3*i+2],2.0f,custom2);
        //w x y z - > x y z w
        addFloat4ToBuffer(asset.rot[4*i+1],
                asset.rot[4*i+2], asset.rot[4*i+3],asset.rot[4*i],custom2);

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

        if (asset.f_dc != null){
            descriptionAttributes.add(VertexAttribute.CUSTOM0);
        }
        descriptionAttributes.add(VertexAttribute.CUSTOM1);
        descriptionAttributes.add(VertexAttribute.CUSTOM2);
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
    public float check01(float src){
        float v = src > 1 ? 1 : src;
        return v < 0 ? 0:v;
    }
}
