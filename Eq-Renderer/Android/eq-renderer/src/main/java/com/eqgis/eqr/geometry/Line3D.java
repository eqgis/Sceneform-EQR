package com.eqgis.eqr.geometry;


import com.google.sceneform.Node;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Material;
import com.google.sceneform.rendering.ModelRenderable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Line3D
 * @author tanyx 2024/2/11
 * @version 1.0
 * <br/>SampleCode:<br/>
 * <code>
 *
 * </code>
 **/
public class Line3D extends Node{

    private Line3dNative line3dNative;
    //点集
    private ArrayList<Vector3> points;
    //材质
    private Material material;
    //截面圆半径
    private float radius;
    //表示截面圆的弧段数,默认采用16边形表示圆
    private int edgeNum;

    private ModelRenderable lastRenderable;

    private boolean init = false;

    //desc- 首次 Renderable 构建为异步任务，终止后必须忽略晚到回调。
    private volatile boolean disposed = false;

    private int textureMode = 1;

    private Line3DPipePointMode pipePointMode = Line3DPipePointMode.ORIGINAL;
    //debug
//    public Vector3[] meshPoint;

    /**
     * 构造函数
     */
    public Line3D() {
        this.points = new ArrayList<Vector3>();
        this.line3dNative = new Line3dNative();
        this.edgeNum = 16;
        this.radius = 0.02f;
    }

    /**
     * 添加点
     * @param p 点坐标
     * @return this
     */
    public Line3D addPoint(Vector3 p){
        synchronized (this){
            this.points.add(p);
        }
        return this;
    }

    /**
     * 添加点
     * @param ps 点坐标组
     * @return this
     */
    public Line3D addPoint(Vector3[] ps){
        synchronized (this){
            this.points.addAll(Arrays.asList(ps));
        }
        return this;
    }

    /**
     * 添加点
     * @param ps 点坐标组
     * @return this
     */
    public Line3D addPoint(List<Vector3> ps){
        synchronized (this){
            this.points.addAll(ps);
        }
        return this;
    }

    /**
     * 获取点集
     * @return 点集
     */
    public ArrayList<Vector3> getPointList(){
        synchronized (this){
            return this.points;
        }
    }

    /**
     * 设置点集
     * @param ps 点集
     * @return this
     */
    public Line3D setPointList(ArrayList <Vector3> ps){
        synchronized (this){
            this.points = ps;
        }
        return this;
    }

    /**
     * 刷新渲染对象
     * @return 渲染对象
     */
    public void refresh(){
        if (disposed) {
            return;
        }
        Vector3[] ps;
        synchronized (this){
            ps = new Vector3[points.size()];
            for (int i = 0; i < ps.length; i++) {
                ps[i] = points.get(i);
            }
        }
        if (ps.length < 2){
            //点个数不够
            return;
        }

        boolean addStartAndEnd = true;
        //计算管线点（位置信息and方向向量）
        //更新mesh的顶点（包含对应索引）
        ArrayList<PipePoint> pipePoints;
        switch (pipePointMode){
            case SIN_RESAMPLE:
                //采用Sin的方式对两端顶点的半径进行优化
                pipePoints = line3dNative.genPipePoint_1(ps, radius);
//                addStartAndEnd = false;
                break;
            case ORIGINAL:
            default:
                //不对管线两端接近端点的顶点部分的管线半径进行优化
                pipePoints = line3dNative.genPipePoint_0(ps, radius);
        }

        switch (textureMode){
            case 0:
                //纹理拉伸
                line3dNative.refreshVertex(
                        pipePoints,
                        edgeNum,addStartAndEnd);//拉伸的方式进行贴图
                break;
            case 1:
                //纹理重复排列
                line3dNative.refreshVertex(
                        pipePoints,
                        edgeNum, (float) (Math.PI*2*radius),addStartAndEnd);//重复纹理的方式进行贴图
                break;
            case 2:
                //纹理重复排列，（正向->反向->正向的方式交错排列）
                line3dNative.refreshVertex2(
                        pipePoints,
                        edgeNum, (float) (Math.PI*2*radius),addStartAndEnd);//重复纹理的方式进行贴图
                break;
        }


        if (lastRenderable == null){
            if (!init){//避免多次创建renderable对象，在创建renderer对象期间，跳过渲染
                CompletableFuture<ModelRenderable> renderableCompletableFuture = line3dNative.makeRenderable(material);
                renderableCompletableFuture.thenAccept(new Consumer<ModelRenderable>() {
                    @Override
                    public void accept(ModelRenderable e) {
                        if (disposed) {
                            return;
                        }
                        e.setShadowCaster(false);
                        e.setShadowReceiver(false);
                        Line3D.this.setRenderable(e);
                        lastRenderable = e;
                    }
                });
                init = true;
            }
        }else {
            line3dNative.refreshMesh(lastRenderable);
        }

//        Log.d("IKKYU", "build: " +lastRenderable.toString() + " getSubGeometryCount: " + lastRenderable.getSubGeometryCount());
    }

    //<editor-fold> set and get方法

    /**
     * 获取上一次构建的渲染对象
     * @return 渲染对象
     */
    public ModelRenderable getLastRenderable() {
        return lastRenderable;
    }

    /**
     * 设置材质
     * @param material 材质
     * @return this
     */
    public Line3D setMaterial(Material material) {
        this.material = material;
        return this;
    }

    /**
     * 设置截面圆半径
     * @param radius 圆半径
     * @return this
     */
    public Line3D setRadius(float radius) {
        this.radius = radius;
        return this;
    }


    /**
     * 设置用以表示圆的弧段数量
     * @param edgeNum 弧段数量
     * @return this
     */
    public Line3D setEdgeNum(int edgeNum) {
        this.edgeNum = edgeNum;
        return this;
    }

    /**
     * 设置纹理贴图模式
     * @param model
     * @return
     */
    public Line3D setTextureMode(int model){
        this.textureMode = model;
        return this;
    }

    /**
     * 设置管线顶点计算模式
     * <pre>
     *     {@link Line3DPipePointMode#ORIGINAL}：不修改端点半径（默认）
     *     {@link Line3DPipePointMode#SIN_RESAMPLE}：端点半径经过 Math.sin 方法重采样
     * </pre>
     * @param pipePointMode 管线顶点计算模式，不允许为 null
     * @return 当前 Line3D
     */
    public Line3D setPipePointMode(Line3DPipePointMode pipePointMode) {
        this.pipePointMode = Objects.requireNonNull(pipePointMode, "pipePointMode");
        return this;
    }

    /**
     * 获取截面圆半径
     * @return radius
     */
    public float getRadius() {
        return radius;
    }

    /**
     * 终止 Line3D 的异步构建并解除场景与渲染资源引用。
     * <p>该方法用于页面或 SceneLayout 销毁前的最终释放，调用后不再支持 refresh。</p>
     */
    public void dispose() {
        disposed = true;
        synchronized (this) {
            points.clear();
        }
        setRenderable(null);
        setParent(null);
        lastRenderable = null;
        material = null;
        line3dNative = null;
    }

    //</editor-fold>
}
