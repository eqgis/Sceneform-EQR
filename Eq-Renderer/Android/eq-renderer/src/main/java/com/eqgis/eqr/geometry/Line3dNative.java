package com.eqgis.eqr.geometry;

import com.eqgis.sceneform.math.Vector3;
import com.eqgis.sceneform.rendering.Material;
import com.eqgis.sceneform.rendering.ModelRenderable;
import com.eqgis.sceneform.rendering.RenderableDefinition;
import com.eqgis.sceneform.rendering.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Line3dNative
 * @author tanyx 2024/2/13
 * @version 1.0
 * <br/>SampleCode:<br/>
 * <code>
 *
 * </code>
 **/
class Line3dNative {
//    private boolean doubleSide = false;
    private ArrayList<Vertex> m_Vertices;
    private ArrayList<Integer> m_Triangles;
    private RenderableDefinition.Submesh submesh;
    private RenderableDefinition renderableDefinition;


    /**
     * 创建Mesh
     * @param pipePoints 管线点
     * @param numberOfSides 弧段数
     * @return 渲染对象
     */
    void refreshVertex(ArrayList<PipePoint> pipePoints,
            /*弧段数*/int numberOfSides,boolean addStartAndEnd){

        //求取总长度
        float totalDistance = 0f;
        for (int i = 0; i < pipePoints.size() - 1; i++) {
            float sigDistance = Vector3.subtract(pipePoints.get(i + 1).getPosition(),
                    pipePoints.get(i).getPosition()).length();
            totalDistance += sigDistance;
        }

        int pointCount = pipePoints.size();//点数量
        int circularCount = numberOfSides + 1;//弧段数（弧上顶点数）

        final float thetaIncrement = (float) (2 * Math.PI) / numberOfSides;

        //顶点集
        ArrayList<Vertex> vertices = new ArrayList<Vertex>(pointCount * circularCount);
        ArrayList<Vertex> verticesStart = new ArrayList<Vertex>();
        ArrayList<Vertex> verticesEnd = new ArrayList<Vertex>();

        float currentDistance = 0f;//当前距离起点的路程
        for (int i = 0; i < pointCount; i++)
        {
            PipePoint e = pipePoints.get(i);
            Vector3 o = e.getPosition();
            Vector3 direction = e.getDirection();
            float radius = e.getRadius();

            if (i > 0){
                PipePoint b = pipePoints.get(i);
                PipePoint a = pipePoints.get(i - 1);

                float sigDistance = Vector3.subtract(b.getPosition(),
                        a.getPosition()).length();
                currentDistance += sigDistance;//计算当前长度
            }

            float theta = 0;
            float uStep = (float) 1.0 / (circularCount - 1);
            for (int j = 0; j < circularCount; j++)
            {
                float cosTheta = (float) Math.cos(theta);
                float sinTheta = (float) Math.sin(theta);

                Vector3 arc = VectorMathUtils.transformPoint(
                        new Vector3(radius * cosTheta, radius * sinTheta, 0),/*对应forward方向*/
//                        new Vector3(radius * cosTheta, 0, radius * sinTheta),/*对应up方向*/
                        o, direction);

                //法线向量，由圆心指向弧上顶点
                Vector3 normal = Vector3.subtract(arc,o);

                //计算UV坐标
                Vertex.UvCoordinate uvCoordinate;
                {//计算UV坐标
                    if (i == 0){
                        //起始位置
                        uvCoordinate =
                                new Vertex.UvCoordinate(uStep * j,0);

                        //计算起点位置的半球顶点和UV
                        //添加起点处截面
                        if (verticesStart.size() == 0){
                            verticesStart.add(
                                    Vertex.builder()
                                            .setPosition(/*圆心*/o)
                                            .setNormal(direction.negated())
                                            .setUvCoordinate(new Vertex.UvCoordinate(0,0))
                                            .build());
                        }

                        verticesStart.add(
                                Vertex.builder()
                                        .setPosition(/*圆弧上的顶点*/arc)
                                        .setNormal(direction.negated())
                                        .setUvCoordinate(new Vertex.UvCoordinate(1,1))
                                        .build()
                        );


                    }else if (i == pointCount - 1){
                        //结束位置
                        uvCoordinate =
                                new Vertex.UvCoordinate(uStep * j,1);

                        //添加终点处截面
                        if (verticesEnd.size() == 0){
                            verticesEnd.add(
                                    Vertex.builder()
                                            .setPosition(/*圆心*/o)
                                            .setNormal(direction)
                                            .setUvCoordinate(new Vertex.UvCoordinate(0,0))
                                            .build());
                        }

                        verticesEnd.add(
                                Vertex.builder()
                                        .setPosition(/*圆弧上的顶点*/arc)
                                        .setNormal(direction)
                                        .setUvCoordinate(new Vertex.UvCoordinate(1,1))
                                        .build()
                        );
                    }else {

                        //计算纵向比例（0,1）
                        float percent =  currentDistance / totalDistance;
                        //中间位置
                        uvCoordinate = new Vertex.UvCoordinate((float) Math.min(0.99f,Math.max(0.01f,uStep * j)),
                                Math.min(percent,1.0f));
//                        Log.i("test456", "refreshVertex: theta：" + theta + " delta: " + thetaIncrement + " j: " + j + " -x: " + (uStep * j) + "  percent: " + percent);
                    }
                }

                Vertex vertex =
                        Vertex.builder()
                                .setPosition(/*圆弧上的顶点*/arc)
                                .setNormal(normal)
                                .setUvCoordinate(uvCoordinate)
                                .build();
                vertices.add(vertex);
                theta += thetaIncrement;
            }
            //end
        }


        ArrayList<Integer> triangles = getTriangles(pipePoints, circularCount);


        if (addStartAndEnd){
            //添加起点截面的顶点索引，顶点集合
            addTriangles(triangles,circularCount,vertices.size(),false);
            vertices.addAll(verticesStart);

            //添加终点截面的顶点索引（注意朝向与起点面的三角顺序不同），顶点集合
            addTriangles(triangles,circularCount,vertices.size(),true);
            vertices.addAll(verticesEnd);
        }

//        int size = triangles.size();
//        if (doubleSide){
//            //若要双面渲染，则需要加上这个索引
//            for (int i = size - 1; i > -1 ; i--) {
//                triangles.add(triangles.get(i));
//            }
//        }//不再使用这个方式去做双面渲染，直接使用双面渲染的材质（*.filamat）文件

        m_Vertices = vertices;
        m_Triangles = triangles;
    }

    /**
     * 创建Mesh
     * <p>拼接的方式进行贴图</p>
     * @param pipePoints
     * @param numberOfSides
     * @param refLength 参考距离，通常根据radius来设置
     */
    void refreshVertex(ArrayList<PipePoint> pipePoints,
            /*弧段数*/int numberOfSides, float refLength, boolean addStartAndEnd){
        //PipePoints的size必须大于等于2，上层做了判断
        int pointCount = pipePoints.size();//点数量
        int circularCount = numberOfSides + 1;//弧段数 + 1（弧上顶点数）

        float thetaIncrement = (float) (2 * Math.PI) / numberOfSides;

        //顶点集
        ArrayList<Vertex> vertices = new ArrayList<Vertex>(2 * pointCount * circularCount);

        float dis = 0f;
        float current_uv_Y = 0f;
        float next_uv_Y;

        int count = 0;//记录当前有多少截面，大于等于pipePoints.size（）
        for (int i = 0; i < pipePoints.size() - 1; i++) {
            PipePoint currentPoint = pipePoints.get(i);
            PipePoint nextPoint = pipePoints.get(i + 1);
            float currentDistance = Vector3.subtract(nextPoint.getPosition(), currentPoint.getPosition()).length();
            dis += currentDistance;

            Vector3 direction = currentPoint.getDirection().normalized();

            PipePoint tmpPoint = new PipePoint();
            tmpPoint.setDirection(direction);
            tmpPoint.setPosition(currentPoint.getPosition());
            //当前插值地方，也直接取相同的半径
            tmpPoint.setRadius(currentPoint.getRadius());

            while (dis > refLength){
                //当大于参考距离时，则需要添加点
                //前截面圆不变，
                addVertex(vertices,tmpPoint,current_uv_Y,circularCount,thetaIncrement);
                count++;
                //后截面圆，采用新增点（差多少补多少距离）
                tmpPoint.addPosition(direction.scaled(refLength * (1.0f - current_uv_Y)));

                addVertex(vertices,tmpPoint,1.0f,circularCount,thetaIncrement);
                count++;
                dis -= refLength;
//                dis = 0;
                current_uv_Y = 0;
            }

            //至此，dis < = reflength
            next_uv_Y = dis / refLength;
            //未超出当前段纹理贴图
            //前截面圆
            addVertex(vertices,tmpPoint,current_uv_Y,circularCount,thetaIncrement);
            count ++;
            //后截面圆
            addVertex(vertices,nextPoint,next_uv_Y,circularCount,thetaIncrement);
            count++;
            current_uv_Y = next_uv_Y;

        }

        //count ： 截面圆计数
        ArrayList<Integer> triangles = new ArrayList<Integer>((circularCount - 1) * (count / 2));
        for (int i = 0; i < count; i = i+2)
        {
            for (int j = 0; j < circularCount - 1; j++) {
                int leftBottom = (i * circularCount) + j;
                int rightBottom = (i * circularCount) + j + 1;
                int leftTop = (i + 1) * circularCount + j;
                int rightTop = (i + 1) * circularCount + j + 1;

                triangles.add(leftTop);
                triangles.add(rightTop);
                triangles.add(rightBottom);
                triangles.add(leftTop);
                triangles.add(rightBottom);
                triangles.add(leftBottom);
            }
        }

        if (addStartAndEnd){
            PipePoint start = pipePoints.get(0);
            PipePoint end = pipePoints.get(pipePoints.size() - 1);
            //添加始终点出的顶点计算
            addStartAndEndVertex(circularCount, thetaIncrement, vertices, triangles, start, end);
        }

        m_Vertices = vertices;
        m_Triangles = triangles;
    }

    private void addStartAndEndVertex(int circularCount, float thetaIncrement, ArrayList<Vertex> vertices, ArrayList<Integer> triangles, PipePoint start, PipePoint end) {
        Vector3 startPosition = start.getPosition();
        Vector3 startDirection = start.getDirection();
        Vector3 endPosition = end.getPosition();
        Vector3 endDirection = end.getDirection();
        float startR = start.getRadius();
        float endR = end.getRadius();

        ArrayList<Vertex> verticesStart = new ArrayList<Vertex>(circularCount + 1);
        ArrayList<Vertex> verticesEnd = new ArrayList<Vertex>(circularCount + 1);
        float theta = 0;
        for (int j = 0; j < circularCount; j++)
        {
            float cosTheta = (float) Math.cos(theta);
            float sinTheta = (float) Math.sin(theta);
            Vector3 startArc = VectorMathUtils.transformPoint(
                    new Vector3(startR * cosTheta, startR * sinTheta, 0),/*对应forward方向*/
                    startPosition, startDirection);

            Vector3 endArc = VectorMathUtils.transformPoint(
                    new Vector3(endR * cosTheta, endR * sinTheta, 0),/*对应forward方向*/
                    endPosition, endDirection);


            {
                //添加起点处截面
                if (verticesStart.size() == 0){
                    verticesStart.add(
                            Vertex.builder()
                                    .setPosition(/*圆心*/startPosition)
                                    .setNormal(startDirection.negated())
                                    .setUvCoordinate(new Vertex.UvCoordinate(0,0))
                                    .build());
                }

                verticesStart.add(
                        Vertex.builder()
                                .setPosition(/*圆弧上的顶点*/startArc)
                                .setNormal(startDirection.negated())
                                .setUvCoordinate(new Vertex.UvCoordinate(1,1))
                                .build()
                );

                //添加终点处截面
                if (verticesEnd.size() == 0){
                    verticesEnd.add(
                            Vertex.builder()
                                    .setPosition(/*圆心*/endPosition)
                                    .setNormal(endDirection)
                                    .setUvCoordinate(new Vertex.UvCoordinate(0,0))
                                    .build());
                }

                verticesEnd.add(
                        Vertex.builder()
                                .setPosition(/*圆弧上的顶点*/endArc)
                                .setNormal(endDirection)
                                .setUvCoordinate(new Vertex.UvCoordinate(1,1))
                                .build()
                );
            }


            theta += thetaIncrement;
        }


        //添加起点截面的顶点索引，顶点集合
        addTriangles(triangles, circularCount, vertices.size(),false);
        vertices.addAll(verticesStart);

        //添加终点截面的顶点索引（注意朝向与起点面的三角顺序不同），顶点集合
        addTriangles(triangles, circularCount, vertices.size(),true);
        vertices.addAll(verticesEnd);
    }

    private void addVertex(ArrayList<Vertex> vertices, PipePoint currentPoint,  float currentUvY,int circularCount,float thetaIncrement) {
        float theta = 0;
        float uStep = (float) 1.0 / (circularCount - 1);//uv-X-SIG
        float currentR = currentPoint.getRadius();//截面半径
        Vertex.Builder vertexBuilder = Vertex.builder();
        for (int i = 0; i < circularCount; i++) {
            float cosTheta = (float) Math.cos(theta);
            float sinTheta = (float) Math.sin(theta);

            Vector3 currentOrigin = currentPoint.getPosition();
            Vector3 currentArc = VectorMathUtils.transformPoint(
                    new Vector3(currentR * cosTheta, currentR * sinTheta, 0),/*对应forward方向*/
//                        new Vector3(radius * cosTheta, 0, radius * sinTheta),/*对应up方向*/
                    currentOrigin, currentPoint.getDirection());

            Vertex currentVertex
                    = vertexBuilder
                    .setPosition(currentArc)
                    .setNormal(/*法线向量，由圆心指向弧上顶点*/Vector3.subtract(currentArc, currentOrigin))
                    .setUvCoordinate(new Vertex.UvCoordinate(uStep * i, currentUvY))
                    .build();
            vertices.add(currentVertex);
            theta += thetaIncrement;
        }

    }

    /**
     * 创建Mesh
     * <p>拼接的方式进行贴图(镜像)</p>
     * @param pipePoints
     * @param numberOfSides
     * @param length
     */
    void refreshVertex2(ArrayList<PipePoint> pipePoints,
            /*弧段数*/int numberOfSides, float length,boolean addStartAndEnd){

        int pointCount = pipePoints.size();//点数量
        int circularCount = numberOfSides + 1;//弧段数（弧上顶点数）

        final float thetaIncrement = (float) (2 * Math.PI) / numberOfSides;

        //顶点集
        ArrayList<Vertex> vertices = new ArrayList<Vertex>(pointCount * circularCount);
        ArrayList<Vertex> verticesStart = new ArrayList<Vertex>();
        ArrayList<Vertex> verticesEnd = new ArrayList<Vertex>();

        float currentDistance = 0f;//当前距离起点的路程
        boolean foward = true;
        for (int i = 0; i < pointCount; i++)
        {
            PipePoint e = pipePoints.get(i);
            Vector3 o = e.getPosition();
            Vector3 direction = e.getDirection();
            float radius = e.getRadius();

            //计算纵向比例（0,1）
            float percent;

            if (i > 0){
                PipePoint b = pipePoints.get(i);
                PipePoint a = pipePoints.get(i - 1);

                float sigDistance = Vector3.subtract(b.getPosition(),
                        a.getPosition()).length();
                currentDistance += sigDistance;//计算当前长度

                if (currentDistance < length){
                    //当前长度小于单段贴图的纹理长度，则
                    percent = Math.min(1.0f,Math.max(0.0f,currentDistance / length));
                    if (!foward){
                        percent = 1-percent;
                    }
                }else {
                    //todo ,这里当前是，正向贴图->反向->正向的方式，与预期全采用正向不一致
                    currentDistance = 0f;
                    if (foward){
                        percent = 1;
                    }else {
                        percent = 0;
                    }
                    foward = !foward;
                }
            }else {
                //第一个点
                percent = 0.0f;
            }


            addSigVertex(radius, pointCount, circularCount, thetaIncrement, vertices, verticesStart, verticesEnd, i, o, direction, percent);
            //end
        }


        ArrayList<Integer> triangles = getTriangles(pipePoints, circularCount);

        if (addStartAndEnd){
            //添加起点截面的顶点索引，顶点集合
            addTriangles(triangles,circularCount,vertices.size(),false);
            vertices.addAll(verticesStart);

            //添加终点截面的顶点索引（注意朝向与起点面的三角顺序不同），顶点集合
            addTriangles(triangles,circularCount,vertices.size(),true);
            vertices.addAll(verticesEnd);
        }

//        int size = triangles.size();
//        if (doubleSide){
//            //若要双面渲染，则需要加上这个索引
//            for (int i = size - 1; i > -1 ; i--) {
//                triangles.add(triangles.get(i));
//            }
//        }//不再使用这个方式去做双面渲染，直接使用双面渲染的材质（*.filamat）文件

        m_Vertices = vertices;
        m_Triangles = triangles;
    }

    private static void addSigVertex(float radius, int pointCount, int circularCount, float thetaIncrement, ArrayList<Vertex> vertices, ArrayList<Vertex> verticesStart, ArrayList<Vertex> verticesEnd, int i, Vector3 o, Vector3 direction, float percent) {
        float theta = 0;
        float uStep = (float) 1.0 / (circularCount - 1);
        for (int j = 0; j < circularCount; j++)
        {
            float cosTheta = (float) Math.cos(theta);
            float sinTheta = (float) Math.sin(theta);

            Vector3 arc = VectorMathUtils.transformPoint(
                    new Vector3(radius * cosTheta, radius * sinTheta, 0),/*对应forward方向*/
//                        new Vector3(radius * cosTheta, 0, radius * sinTheta),/*对应up方向*/
                    o, direction);

            //法线向量，由圆心指向弧上顶点
            Vector3 normal = Vector3.subtract(arc, o);

            //计算UV坐标
            Vertex.UvCoordinate uvCoordinate;
            {//计算UV坐标
                if (i == 0){
                    //计算起点位置的半球顶点和UV
                    //添加起点处截面
                    if (verticesStart.size() == 0){
                        verticesStart.add(
                                Vertex.builder()
                                        .setPosition(/*圆心*/o)
                                        .setNormal(direction.negated())
                                        .setUvCoordinate(new Vertex.UvCoordinate(0,0))
                                        .build());
                    }

                    verticesStart.add(
                            Vertex.builder()
                                    .setPosition(/*圆弧上的顶点*/arc)
                                    .setNormal(direction.negated())
                                    .setUvCoordinate(new Vertex.UvCoordinate(1,1))
                                    .build()
                    );
                }else if (i == pointCount - 1)
                {
                    //添加终点处截面
                    if (verticesEnd.size() == 0){
                        verticesEnd.add(
                                Vertex.builder()
                                        .setPosition(/*圆心*/o)
                                        .setNormal(direction)
                                        .setUvCoordinate(new Vertex.UvCoordinate(0,0))
                                        .build());
                    }

                    verticesEnd.add(
                            Vertex.builder()
                                    .setPosition(/*圆弧上的顶点*/arc)
                                    .setNormal(direction)
                                    .setUvCoordinate(new Vertex.UvCoordinate(1,1))
                                    .build()
                    );
                }

                //中间位置
                uvCoordinate = new Vertex.UvCoordinate(uStep * j,
                        percent);
            }

            Vertex vertex =
                    Vertex.builder()
                            .setPosition(/*圆弧上的顶点*/arc)
                            .setNormal(normal)
                            .setUvCoordinate(uvCoordinate)
                            .build();
            vertices.add(vertex);
            theta += thetaIncrement;
        }
    }

    /**
     * 生成渲染对象
     * @param material 材质
     * @return
     */
    CompletableFuture<ModelRenderable> makeRenderable(Material material){
        submesh =
                RenderableDefinition.Submesh.builder().setTriangleIndices(m_Triangles).setMaterial(material).build();

        renderableDefinition =
                RenderableDefinition.builder()
                        .setVertices(m_Vertices)
                        .setSubmeshes(Arrays.asList(submesh))
                        .build();

        CompletableFuture<ModelRenderable> future =
                ModelRenderable.builder().setSource(renderableDefinition).build();
        return future;
    }

    /**
     * 更新Mesh
     * @param modelRenderable 渲染对象
     */
    void refreshMesh(ModelRenderable modelRenderable){
        if (modelRenderable == null)return;
        submesh.setTriangleIndices(m_Triangles);

        renderableDefinition.setVertices(m_Vertices);
        renderableDefinition.setSubmeshes(Arrays.asList(submesh));

        modelRenderable.updateFromDefinition(renderableDefinition);
    }


    /**
     * 2、生成管线顶点
     * @param createPoint 源点组
     * @param elbowR 弯点半径（圆角半径）
     * @return
     */
    ArrayList<PipePoint> genPipePoint(Vector3[] createPoint, float elbowR,float r){
        ArrayList<PipePoint> pipePoints = new ArrayList<PipePoint>();
        int length = createPoint.length;
        for (int i = 0; i < length; i++)
        {
            if (i == 0)
            {
                addPipePoints(createPoint[i], Vector3.subtract(createPoint[i + 1] , createPoint[i]), /*ref*/ pipePoints,r);
            }
            else if (i == length - 1)
            {
                addPipePoints(createPoint[i], Vector3.subtract(createPoint[i] , createPoint[i - 1]), /*ref*/ pipePoints,r);
            }
            else
            {
                //todo 采用圆角
                addElbowPoint(createPoint[i], createPoint[i - 1], createPoint[i + 1], elbowR, /*ref*/ pipePoints,r);
            }
        }
        return pipePoints;
    }


    /**
     * 生成管线顶点集
     * <p>所有管线顶点的半径相同</p>
     * @param createPoint
     * @param r
     * @return
     */
    ArrayList<PipePoint> genPipePoint_0(Vector3[] createPoint,float r){
        ArrayList<PipePoint> pipePoints = new ArrayList<PipePoint>();
        int length = createPoint.length;
        for (int i = 0; i < length; i++)
        {
            if (i == 0)
            {
                addPipePoints(createPoint[i], Vector3.subtract(createPoint[i + 1] , createPoint[i]), /*ref*/ pipePoints,r);
            }
            else if (i == length - 1)
            {
                addPipePoints(createPoint[i], Vector3.subtract(createPoint[i] , createPoint[i - 1]), /*ref*/ pipePoints,r);
            }
            else
            {
                //不采用圆角，直接计算方向向量
                //已知，A、B、C三点，求拐点的方向向量，采用AB向量的单位向量 + BC向量的单位向量
                Vector3 ab = Vector3.subtract(createPoint[i],createPoint[i - 1]).normalized();
                Vector3 bc = Vector3.subtract(createPoint[i + 1], createPoint[i]).normalized();
                addPipePoints(createPoint[i], Vector3.add(ab,bc) , /*ref*/ pipePoints,r);
            }
        }
        return pipePoints;
    }

    /**
     * 生成管线顶点集
     * <p>采用Sin的方式对两端顶点的半径进行优化</p>
     * @param createPoint
     * @param r
     * @return
     */
    ArrayList<PipePoint> genPipePoint_1(Vector3[] createPoint,float r){
        ArrayList<PipePoint> pipePoints = new ArrayList<PipePoint>();
        int length = createPoint.length;

        int keyPointConnt;
        if (length > 50){
            keyPointConnt = 6;
        }else {
            keyPointConnt = (length / 10) + 1;
        }

        float theta = (float) ((Math.PI / 2) / keyPointConnt);

        for (int i = 0; i < length; i++)
        {
            //计算对应点的Radius
            float radius = r;
            if (i < keyPointConnt){
                radius = (float) (Math.sin(theta * i) * r);
            }

            if (i > (length - keyPointConnt)){
                radius = (float) (Math.sin(((length - 1) - i) * theta) * r);
            }

            if (i == 0)
            {
                addPipePoints(createPoint[i], Vector3.subtract(createPoint[i + 1] , createPoint[i]), /*ref*/ pipePoints,radius);
            }
            else if (i == length - 1)
            {
                addPipePoints(createPoint[i], Vector3.subtract(createPoint[i] , createPoint[i - 1]), /*ref*/ pipePoints,radius);
            }
            else
            {
                //不采用圆角，直接计算方向向量
                //已知，A、B、C三点，求拐点的方向向量，采用AB向量的单位向量 + BC向量的单位向量
                Vector3 ab = Vector3.subtract(createPoint[i],createPoint[i - 1]).normalized();
                Vector3 bc = Vector3.subtract(createPoint[i + 1], createPoint[i]).normalized();
                addPipePoints(createPoint[i], Vector3.add(ab,bc) , /*ref*/ pipePoints,radius);
            }
        }
        return pipePoints;
    }

//    /**
//     * 3、创建网格点
//     * @param pipePoints
//     * @param circular
//     * @return
//     */
//    public Vector3[] createMeshPoint(ArrayList<PipePoint> pipePoints, Vector3[] circular){
//        int length = pipePoints.size();
//        int circularCount = circular.length;
//        Vector3[] meshPoint = new Vector3[length * circularCount];
//        for (int i = 0; i < length; i++)
//        {
//            for (int j = 0; j < circularCount; j++)
//            {
//                PipePoint e = pipePoints.get(i);
////                meshPoint[(i * circularCount) + j] = circular[j]
////                        .fromToMoveRotation(e.getPosition(), e.getDirection());
//                 Vector3 vector3 = VectorMathUtils.transformPoint(
//                        /*圆上的点*/circular[j],
//                        /*圆心*/e.getPosition(),
//                        /*方向*/e.getDirection());
//                meshPoint[(i * circularCount) + j] = vector3;
//            }
//        }
//        return meshPoint;
//    }


    /**
     * 添加管线点（截面圆心，截面法线的方向向量）
     */
    private void addPipePoints(Vector3 position, Vector3 direction, ArrayList<PipePoint> pipePoints,float radius) {
        PipePoint pipePoint = new PipePoint();
        pipePoint.setPosition(position);
        pipePoint.setDirection(direction);
        pipePoint.setRadius(radius);
        //去掉方向向量为（0,0,0）的点,采用模长过滤 todo
        if (direction.length() < 0.000001f){
            return;
        }
        pipePoints.add(pipePoint);
    }

    /**
     * 添加弯处管线点（截面圆心，截面法线的方向向量）
     */
    private void addElbowPoint(Vector3 focus, Vector3 front, Vector3 back, float r, ArrayList<PipePoint> pipePoints,float radius){
        //焦点前后向量
        Vector3 frontVec = Vector3.subtract(focus,front);
        Vector3 backVec = Vector3.subtract(back,focus);
        //得到前后切点
        Vector3 frontVecN = frontVec.normalized();
        Vector3 tangencyFront = Vector3.add(
                frontVecN.scaled(frontVec.length() - r),
                front);

        Vector3 backVecN = backVec.normalized();
        Vector3 tangencyBack = Vector3.add(backVecN.scaled(r), focus);

        //todo check
/*        float angle_fo = Vector3.angleBetweenVectors(frontVec.negated(),backVec) / 2;
        double v = r / Math.tan(Math.toRadians(angle_fo));
        Vector3 tangencyFront =
                Vector3.add(frontVec.normalized().scaled((float)(frontVec.length() - v)),
                                front);
        Vector3 tangencyBack =
                Vector3.add(backVec.normalized().scaled((float) v),
                        focus);*/

        //得到内切圆圆心
        Vector3 circulPoint = getCirculPoint(focus, tangencyFront, tangencyBack);
        //得到弯头分段
        Vector3[] circulSection = getCirculSection(tangencyFront, tangencyBack, circulPoint);
        //得到两个焦点向量的法线
        Vector3 normal = Vector3.cross(frontVec, backVec).normalized();

        //增加管线点
        addPipePoints(tangencyFront, getDirection(tangencyFront, circulPoint, normal), /*ref*/ pipePoints,radius);
        int length = circulSection.length;
//        for (int i = 1; i < length-1; i++)
        for (int i = 0; i < length; i++)
        {
            addPipePoints(circulSection[i], getDirection(circulSection[i], circulPoint, normal), /*ref*/ pipePoints,radius);
        }
        addPipePoints(tangencyBack, getDirection(tangencyBack, circulPoint, normal), /*ref*/ pipePoints,radius);

    }

    /**
     * 获取弯点在内切圆上的切线方向
     * @return
     */
    private Vector3 getDirection(Vector3 self, Vector3 circulPoint, Vector3 normal) {
//        Vector3 vector = circulPoint - self;
        Vector3 vector = Vector3.subtract(circulPoint, self);
        return Vector3.cross(vector, normal).normalized();
    }

    /**
     * 获取弯头分段
     * @param tangency1 切点1
     * @param tangency2 切点2
     * @param circulPoint 切点圆心
     * @return 点组
     */
    private Vector3[] getCirculSection(Vector3 tangency1, Vector3 tangency2, Vector3 circulPoint) {
//        Vector3 vector0 = tangency1 - circulPoint;
//        Vector3 vector4 = tangency2 - circulPoint;
        Vector3 vector0 = Vector3.subtract(tangency1, circulPoint);
        Vector3 vector4 = Vector3.subtract(tangency2, circulPoint);

        float dis = vector0.length();
//        Vector3 vector2 = (vector0 + vector4).normalized * dis;
        Vector3 vector2 = Vector3.add(vector0, vector4).normalized().scaled(dis);
//        Vector3 vector1 = (vector0 + vector2).normalized * dis;
        Vector3 vector1 = Vector3.add(vector0, vector2).normalized().scaled(dis);
//        Vector3 vector3 = (vector4 + vector2).normalized * dis;
        Vector3 vector3 = Vector3.add(vector4, vector2).normalized().scaled(dis);

        Vector3[] vector3s = new Vector3[3];
//        vector3s[0] = vector1 + circulPoint;
//        vector3s[1] = vector2 + circulPoint;
//        vector3s[2] = vector3 + circulPoint;
        vector3s[0] = Vector3.add(vector1 , circulPoint);
        vector3s[1] = Vector3.add(vector2 , circulPoint);
        vector3s[2] = Vector3.add(vector3 , circulPoint);
        return vector3s;
    }

    /**
     * 获取管线弯头内切圆的圆心
     * @param focus 交点
     * @param tangencyFront 前切点
     * @param tangencyBack 后切点
     * @return
     */
    private Vector3 getCirculPoint(Vector3 focus, Vector3 tangencyFront, Vector3 tangencyBack) {
//        Vector3 vector1 = tangency1 - focus;
//        Vector3 vector2 = tangency2 - focus;
        Vector3 vector1 = Vector3.subtract(tangencyFront,focus);
        Vector3 vector2 = Vector3.subtract(tangencyBack,focus);
//        Vector3 vector0 = (vector1 + vector2).normalized;
        Vector3 vector0 = Vector3.add(vector1,vector2).normalized();
        //返回两个向量的夹角，小于等于180度
//        float angle = Vector3.Angle(vector1, vector0);
        float angle = Vector3.angleBetweenVectors(vector1, vector0);
//        float dis = vector1.magnitude / Mathf.Cos(angle * Mathf.Deg2Rad);
        float dis = (float) (vector1.length() / Math.cos(Math.toRadians(angle)));

//        return (vector0 * dis) + focus;
        return Vector3.add(vector0.scaled(dis),focus);
    }


    /**
     * 获取顶点索引
     * @param pipePoints 管线点
     * @param count 弧段数
     * @return
     */
    private ArrayList<Integer> getTriangles(List<PipePoint> pipePoints, int count){
        int length = pipePoints.size();
        ArrayList<Integer> triangleIndices = new ArrayList<Integer>(6 * (length - 1) * count);
        for (int i = 0; i < length - 1; i++)
        {
            for (int j = 0; j < count; j++)
            {
                triangleIndices.add(((i + 1) * count) + j);
                triangleIndices.add(((i + 1) * count) + j + 1);
                triangleIndices.add((i * count) + j + 1);
                triangleIndices.add(((i + 1) * count) + j);
                triangleIndices.add((i * count) + j + 1);
                triangleIndices.add((i * count) + j);
            }
        }
        return triangleIndices;
    }

    //端点处截面圆顶点索引
    private void addTriangles(ArrayList<Integer> triangleIndices, int count,int offset,boolean isEnd){
        int startIndex = offset + 1;//offset作为圆点的索引了，
        if (isEnd){
            for (int i = 0; i < count - 1; i++) {
                triangleIndices.add(offset);
                triangleIndices.add(startIndex + i + 1);
                triangleIndices.add(startIndex + i);
            }
        }else {
            for (int i = 0; i < count - 1; i++) {
                triangleIndices.add(offset);
                triangleIndices.add(startIndex + i);
                triangleIndices.add(startIndex + i + 1);
            }
        }
    }
}
