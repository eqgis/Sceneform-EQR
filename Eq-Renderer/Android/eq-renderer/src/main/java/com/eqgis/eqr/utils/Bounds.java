package com.eqgis.eqr.utils;

import android.graphics.Rect;
import android.view.View;

import com.eqgis.eqr.layout.SceneLayout;
import com.google.sceneform.AnchorNode;
import com.google.sceneform.math.Matrix;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.ViewRenderable;

import java.util.ArrayList;
import java.util.List;

/**
 * 屏幕层中的bounds
 */
public class Bounds {
    private int[][] bounds = new int[2][2];

    private List<Vector3> arVertexGroup = new ArrayList<Vector3>();

    private SceneLayout sceneLayout;

    public Bounds(SceneLayout layout) {
        this.sceneLayout = layout;
    }

    /**
     * 设置ARViewElement
     *      注：需通过异步的方式获取View
     * @param viewRenderable  View渲染器
     * @param viewAnchorNode View的AR锚节点
     */
    public void setARViewElement(ViewRenderable viewRenderable, AnchorNode viewAnchorNode){
        if (viewRenderable.getView() == null){
            throw new NullPointerException("getView() is null.");
        }
        //生成顶点组
        generateVertexGroup(viewRenderable,viewAnchorNode);
    }

    /**
     * 设置AR坐标组（Vector3）
     * @param arVertexGroup
     */
    public void setVector3Group(List<Vector3> arVertexGroup) {
        this.arVertexGroup = arVertexGroup;
    }

    /**
     * 获取边界(单位：dp)
     *  "x-min: " bounds1[0][0]
     *  "x-max: " bounds1[0][1]
     *  "y-min: " bounds1[1][0]
     *  "y-max: " bounds1[1][1]
     * @return
     */
    public int[][] getBounds() {
        //生成bounds
        this.bounds = ScreenPointTool.generateBounds(this.sceneLayout.getContext(),sceneLayout.getCamera(), this.arVertexGroup);
        return bounds;
    }


    /**
     * 生成顶点组
     *
     * @param element
     * @param viewAnchorNode
     */
    private void generateVertexGroup(ViewRenderable element, AnchorNode viewAnchorNode) {
        //创建4个顶点
        Vector3 left_top = Vector3.zero();
        Vector3 left_bottom = Vector3.zero();
        Vector3 right_top = Vector3.zero();
        Vector3 right_bottom = Vector3.zero();

        View view = element.getView();

        float scaleDpMeter = ScreenPointTool.getDpScale();
        float screenDensity = ScreenPointTool.getScreenDensity(this.sceneLayout.getContext());
        float width = view.getWidth() / screenDensity / scaleDpMeter;//获取的像素宽度 / 屏幕密度 = dp值（dp/真实尺度比例 = 真实距离）
        float height = view.getHeight() / screenDensity / scaleDpMeter;

        left_top.set(-width/2,height/2,0);
        left_bottom.set(-width/2,-height/2,0);
        right_top.set(width/2,height/2,0);
        right_bottom.set(width/2,-height/2,0);

        //转为在场景中的绝对位置
        Matrix transMat = viewAnchorNode.getWorldModelMatrix();
        //并添加至VertexGroup
        arVertexGroup.add(transMat.transformPoint(left_top));
        arVertexGroup.add(transMat.transformPoint(left_bottom));
        arVertexGroup.add(transMat.transformPoint(right_top));
        arVertexGroup.add(transMat.transformPoint(right_bottom));
    }

    /**
     * 获取最小外接矩形
     * @param scale
     * @return
     */
    public Rect getSmallRect(float scale){
        //获取bounds
        int[][] bounds1 = this.getBounds();
        //获取屏幕密度
        float screenDensity = ScreenPointTool.getScreenDensity(this.sceneLayout.getContext());

        int sc = (int) ((bounds1[0][1] - bounds1[0][0]) / scale);
        int sc2 = (int) ((bounds1[1][1] - bounds1[1][0]) / scale);
        //创建rectangle2D
        Rect rectangle2D = new Rect();
        rectangle2D.left = (int) (bounds1[0][0]*screenDensity + sc);
        rectangle2D.right = (int) (bounds1[0][1]*screenDensity - sc);
        rectangle2D.top = (int) (bounds1[1][0]*screenDensity + sc2);
        rectangle2D.bottom = (int) (bounds1[1][1]*screenDensity - sc2);
        return rectangle2D;
    }

    /**
     * 计算ViewElement的AR中的四个顶点的方法
     *             Box collisionShape = (Box) attrBox.getViewRenderable().getCollisionShape();
     *             Vector3 worldPosition = attrBox.getAnchorNode().getWorldPosition();
     *             Vector3 center = Vector3.add(worldPosition,collisionShape.getCenter());
     *             Vector3 extents = collisionShape.getSize().scaled(0.5f);
     *
     *             Vector3 p0 = Vector3.add(center, new Vector3(-extents.x, -extents.y, extents.z));
     *             Vector3 p1 = Vector3.add(center, new Vector3(-extents.x, extents.y, extents.z));
     *             Vector3 p2 = Vector3.add(center, new Vector3(extents.x, extents.y, extents.z));
     *             Vector3 p3 = Vector3.add(center, new Vector3(extents.x, -extents.y, extents.z));
     *
     *             list.clear();
     *             list.add(PointConvertTool.convertToPoint3D(p0));
     *             list.add(PointConvertTool.convertToPoint3D(p1));
     *             list.add(PointConvertTool.convertToPoint3D(p2));
     *             list.add(PointConvertTool.convertToPoint3D(p3));
     * */
}
