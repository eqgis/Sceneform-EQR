package com.google.sceneform.rendering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.eqgis.eqr.core.GaussianSorter;
import com.eqgis.eqr.core.PlyGS3dLoader;
import com.eqgis.eqr.data.JPlyGS3dAsset;
import com.google.android.filament.IndexBuffer;
import com.google.android.filament.RenderableManager;
import com.google.android.filament.VertexBuffer;
import com.google.sceneform.collision.Box;
import com.google.sceneform.math.Matrix;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.utilities.LoadHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 3DGS——Splat
 * @author tanyx 2026/1/8
 * @version 1.0
 **/
public class RenderableInternalSplatData extends RenderableInternalData implements LoadRenderableFromUniversalDataTask.IUniversalData {
    private PlyGS3dLoader gs3dLoader;
    private Context context;
    private byte[] byteBuffer;
    private Function<String, Uri> urlResolver;
    private IRenderableDefinition renderableDefinition;
    private Material material;
    private JPlyGS3dAsset asset;

    public VertexBuffer vertexBuffer;
    public IndexBuffer indexBuffer;
    // 对应 UV（注意方向）
    private static final float[][] QUAD_UV = new float[][]{
            {0f, 0f},
            {1f, 0f},
            {1f, 1f},
            {0f, 1f},
    };

    // 单位边长 0.001m → half = 0.0005
    private static final float HALF_SIZE = 0.003f;

    // 本地空间 quad（逆时针）
    private static final Vector3[] QUAD_LOCAL = new Vector3[]{
            new Vector3(-HALF_SIZE, -HALF_SIZE, 0),
            new Vector3( HALF_SIZE, -HALF_SIZE, 0),
            new Vector3( HALF_SIZE,  HALF_SIZE, 0),
            new Vector3(-HALF_SIZE,  HALF_SIZE, 0),
    };
    private GaussianSorter sorter;
    private int[] indicesCache;

    private static final boolean DEBUG_TIME = true;

    public void sortForViewChange(Matrix cameraModelMat, Matrix modelModelMat) {
        if (getIndexBuffer() == null) return;
        sort(cameraModelMat.data,modelModelMat.data);
    }
    private void sort(float[] cameraModelMat,
                        float[] modelModelMat) {
        long t0 = 0;
        if (DEBUG_TIME) t0 = System.nanoTime();

        if (material != null && sorter != null &&
                indicesCache != null && indicesCache.length > 0) {

            long tSort0 = 0;
            if (DEBUG_TIME) tSort0 = System.nanoTime();

            sorter.sort(
                    asset.vertices,
                    modelModelMat,
                    cameraModelMat,
                    indicesCache
            );

            long tSort1 = 0;
            if (DEBUG_TIME) tSort1 = System.nanoTime();

            getIndexBuffer().setBuffer(
                    EngineInstance.getEngine().getFilamentEngine(),
                    IntBuffer.wrap(indicesCache)
            );

            if (DEBUG_TIME) {
                long tEnd = System.nanoTime();
                logTime(t0, tSort0, tSort1, tEnd);
            }
        }
    }

    //    public void prepare(Vector3 camP, Quaternion camR, Vector3 modelP, Quaternion modelR) {
//        if (getIndexBuffer() == null)return;
//
//        long t0 = 0;
//        if (DEBUG_TIME) {
//            t0 = System.nanoTime();
//        }
//
//        // camera quat
//        r[0] = camR.x;
//        r[1] = camR.y;
//        r[2] = camR.z;
//        r[3] = camR.w;
//
//        // model quat
//        r1[0] = modelR.x;
//        r1[1] = modelR.y;
//        r1[2] = modelR.z;
//        r1[3] = modelR.w;
//
//        if (material != null && sorter != null && indicesCache != null && indicesCache.length > 0) {
//
//            long tSort0 = 0;
//            if (DEBUG_TIME) tSort0 = System.nanoTime();
//
//            sorter.sort(
//                    asset.vertices,
//                    r1,
//                    modelP.x, modelP.y, modelP.z,
//                    camP.x, camP.y, camP.z,
//                    r
//            );
//
//            long tSort1 = 0;
//            if (DEBUG_TIME) tSort1 = System.nanoTime();
//
//            sorter.fillIndices(indicesCache);
//
//            long tFill = 0;
//            if (DEBUG_TIME) tFill = System.nanoTime();
//
//            getIndexBuffer().setBuffer(
//                    EngineInstance.getEngine().getFilamentEngine(),
//                    IntBuffer.wrap(indicesCache)
//            );
//
//            if (DEBUG_TIME) {
//                long tEnd = System.nanoTime();
//
//                logTime(
//                        t0,
//                        tSort0, tSort1,
//                        tFill,
//                        tEnd
//                );
//            }
//        }
//    }
    private void logTime(
            long tStart,
            long tSortStart,
            long tSortEnd,
            long tEnd
    ) {
        double totalMs = (tEnd - tStart) / 1_000_000.0;
        double sortMs  = (tSortEnd - tSortStart) / 1_000_000.0;
        double uploadMs = (tEnd - tSortEnd) / 1_000_000.0;

        android.util.Log.d(
                "IKkyu  GaussianSort ",
                String.format(
                        "Total %.2f ms | Sort %.2f ms | Upload %.2f ms",
                        totalMs, sortMs, uploadMs
                )
        );
    }


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


        Bitmap bitmap = loadBitmapFromAssets("texture/d_gaussian_context.png");

        if (bitmap != null){
            CompletableFuture<Texture> textureCompletableFuture = Texture.builder()
                    .setSource(bitmap)
                    .build();
            textureCompletableFuture.thenAccept(new Consumer<Texture>() {
                @Override
                public void accept(Texture texture) {
                    Material.builder()
                            .setSource(context,
                                    LoadHelper.rawResourceNameToIdentifier(context,"sceneform_gaussian_splat_alpha"))
                            .build()
                            .thenAccept(mat -> {
                                material = mat;
                                material.setInt("shDegree ",asset.shDegree);
                                material.setTexture("texture", texture);
                                createPrimitive(instance);
                            });
                }
            });
        }
    }


    private void createPrimitive(RenderableInstance instance) {
        //若顶点数据存在，则使用顶点颜色

        if (asset.vertices == null) {
            throw new IllegalArgumentException("assets.vertices must not be null");
        }

        int vertexCount = asset.vertices.length / 3;//xyz三个分量

        sorter = new GaussianSorter(vertexCount);

        ArrayList<Integer> triangleIndices = getIndices(vertexCount);
        indicesCache = new int[triangleIndices.size()];
        ArrayList<Vertex> vertices = getVertices(vertexCount);

        //虽不是Mesh，但是复用Mesh数据结构{顶点索引、材质实例}
        RenderableDefinition.Submesh submesh =
                RenderableDefinition.Submesh.builder()
                        .setTriangleIndices(triangleIndices).setMaterial(material).build();

//        renderableDefinition =
//                RenderableDefinition.builder()
//                        .setVertices(vertices)
//                        .setSubmeshes(Collections.singletonList(submesh))
//                        .build();
        renderableDefinition = new RenderableDefinitionSplat();
        renderableDefinition.setSubmeshes(Collections.singletonList(submesh));
        renderableDefinition.setVertices(vertices);
        if (renderableDefinition instanceof RenderableDefinitionSplat){
            ((RenderableDefinitionSplat) renderableDefinition).setGaussianSplat(asset);
        }

        instance.getRenderable().updateFromDefinition(renderableDefinition);
    }

    @Override
    public void buildInstanceData(RenderableInstance instance, int renderedEntity) {
        super.buildInstanceData(instance, renderedEntity);
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

//    private ArrayList<Vertex> getVertices(int gaussianCount) {
//        ArrayList<Vertex> vertices = new ArrayList<>(gaussianCount * 4);
//
//        final float HALF_SIZE = 0.002f; // 0.001m / 2
//
//        // unit quad in local space
//        Vector3[] quadLocal = new Vector3[]{
//                new Vector3(-HALF_SIZE, -HALF_SIZE, 0),
//                new Vector3( HALF_SIZE, -HALF_SIZE, 0),
//                new Vector3( HALF_SIZE,  HALF_SIZE, 0),
//                new Vector3(-HALF_SIZE,  HALF_SIZE, 0),
//        };
//
//        for (int i = 0; i < gaussianCount; i++) {
//
//            // center
//            Vector3 center = new Vector3(
//                    asset.vertices[3 * i],
//                    asset.vertices[3 * i + 1],
//                    asset.vertices[3 * i + 2]
//            );
//
//            // scale
//            float sx = asset.scale != null ? asset.scale[3 * i]     : 1.0f;
//            float sy = asset.scale != null ? asset.scale[3 * i + 1] : 1.0f;
//            float sz = asset.scale != null ? asset.scale[3 * i + 2] : 1.0f;
//
//            // quaternion (w, x, y, z)
//            float qw = asset.rot != null ? asset.rot[4 * i]     : 1.0f;
//            float qx = asset.rot != null ? asset.rot[4 * i + 1] : 0.0f;
//            float qy = asset.rot != null ? asset.rot[4 * i + 2] : 0.0f;
//            float qz = asset.rot != null ? asset.rot[4 * i + 3] : 0.0f;
//
//            for (int v = 0; v < 4; v++) {
//
//                // scale quad
//                Vector3 p = new Vector3(
//                        quadLocal[v].x * sx,
//                        quadLocal[v].y * sy,
//                        quadLocal[v].z * sz
//                );
//
//                // rotate by quaternion
//                p = rotateByQuaternion(p, qw, qx, qy, qz);
//
//                // translate to center
//                p = Vector3.add(center, p);
//
//                vertices.add(
//                        Vertex.builder()
//                                .setPosition(p)
//                                .build()
//                );
//            }
//        }
//
//        return vertices;
//    }
private ArrayList<Vertex> getVertices(int gaussianCount) {
    ArrayList<Vertex> vertices = new ArrayList<>(gaussianCount * 4);

    for (int i = 0; i < gaussianCount; i++) {

        // center
        Vector3 center = new Vector3(
                asset.vertices[3 * i],
                asset.vertices[3 * i + 1],
                asset.vertices[3 * i + 2]
        );

        // scale
        float sx = asset.scale != null ? asset.scale[3 * i]     : 1.0f;
        float sy = asset.scale != null ? asset.scale[3 * i + 1] : 1.0f;
        float sz = asset.scale != null ? asset.scale[3 * i + 2] : 1.0f;

        // quaternion (w, x, y, z)
        float qw = asset.rot != null ? asset.rot[4 * i]     : 1.0f;
        float qx = asset.rot != null ? asset.rot[4 * i + 1] : 0.0f;
        float qy = asset.rot != null ? asset.rot[4 * i + 2] : 0.0f;
        float qz = asset.rot != null ? asset.rot[4 * i + 3] : 0.0f;

        for (int v = 0; v < 4; v++) {

            // 1. local quad
            Vector3 p = QUAD_LOCAL[v];

            // 2. scale
            Vector3 ps = new Vector3(
                    p.x * sx,
                    p.y * sy,
                    p.z * sz
            );

            // 3. rotate
            Vector3 pr = rotateByQuaternion(ps, qw, qx, qy, qz);

            // 4. translate
            Vector3 world = Vector3.add(center, pr);

            vertices.add(
                    Vertex.builder()
                            .setPosition(world)
                            .setUvCoordinate(new Vertex.UvCoordinate(
                                    QUAD_UV[v][0],
                                    QUAD_UV[v][1]
                            ))
                            .build()
            );
        }
    }

    return vertices;
}


    private static Vector3 rotateByQuaternion(
            Vector3 v, float w, float x, float y, float z) {

        // q * v * q^-1
        float vx = v.x;
        float vy = v.y;
        float vz = v.z;

        // t = 2 * cross(q.xyz, v)
        float tx = 2f * (y * vz - z * vy);
        float ty = 2f * (z * vx - x * vz);
        float tz = 2f * (x * vy - y * vx);

        // v' = v + w * t + cross(q.xyz, t)
        return new Vector3(
                vx + w * tx + (y * tz - z * ty),
                vy + w * ty + (z * tx - x * tz),
                vz + w * tz + (x * ty - y * tx)
        );
    }
    @NonNull
    private ArrayList<Integer> getIndices(int gaussianCount) {
        ArrayList<Integer> triangleIndices = new ArrayList<>(gaussianCount * 6);

        for (int i = 0; i < gaussianCount; i++) {
            int base = i * 4;

            triangleIndices.add(base);
            triangleIndices.add(base + 1);
            triangleIndices.add(base + 2);

            triangleIndices.add(base);
            triangleIndices.add(base + 2);
            triangleIndices.add(base + 3);
        }

        primitiveType = RenderableManager.PrimitiveType.TRIANGLES;
        return triangleIndices;
    }

    public Bitmap loadBitmapFromAssets(String path){
        //加载纹理
        Bitmap bitmap=null;
        try {
            InputStream is = context.getAssets().open(path);
            try {
                bitmap= BitmapFactory.decodeStream(is);
            } finally {
                is.close();
                return bitmap;
            }
        } catch (IOException e) {
            return null;
        }
    }

}
