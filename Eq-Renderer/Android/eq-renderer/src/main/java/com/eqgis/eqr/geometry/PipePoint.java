package com.eqgis.eqr.geometry;

import com.google.ar.sceneform.math.Vector3;

/**
 * 管线顶点
 * @author tanyx 2024/2/11
 * @version 1.0
 * <br/>SampleCode:<br/>
 * <code>
 *
 * </code>
 **/
class PipePoint {
    private Vector3 position;
    private Vector3 direction;

    private float radius;

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
    }

    public Vector3 getDirection() {
        return direction;
    }

    public void setDirection(Vector3 direction) {
        this.direction = direction;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * 添加偏移量
     * @param offset 偏移值
     */
    public void addPosition(Vector3 offset){
        this.position = Vector3.add(this.position,offset);
    }
}
