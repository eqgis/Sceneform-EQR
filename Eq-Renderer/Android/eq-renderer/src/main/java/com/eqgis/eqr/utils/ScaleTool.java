package com.eqgis.eqr.utils;

import com.google.sceneform.collision.Box;
import com.google.sceneform.collision.CollisionShape;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Renderable;

/**
 * 缩放比例工具类
 * @author tanyx 2024/7/25
 * @version 1.0
 * <br/>SampleCode:<br/>
 * <code>
 *
 * </code>
 **/
public class ScaleTool {

    /**
     * 计算缩放成单位尺寸（最长边为1m）所用的比例
     * <p>需要在Node.setRenderable(modelRenderable);后使用才有效</p>
     * @param renderable 渲染对象。
     * @return
     */
    public static float calculateUnitsScale(Renderable renderable){
        CollisionShape collisionShape = renderable.getCollisionShape();

        if (collisionShape instanceof Box){
            Box box = (Box) collisionShape;
            Vector3 size = box.getSize();
            float max = Math.max(size.x,Math.max(size.y,size.z));
            return 1.0f / max;
        }

        return 1.0f;
    }
}
