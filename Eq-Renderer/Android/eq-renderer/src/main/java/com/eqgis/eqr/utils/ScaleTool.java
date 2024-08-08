package com.eqgis.eqr.utils;

//import com.eqgis.sceneform.collision.Box;
//import com.eqgis.sceneform.collision.CollisionShape;
import com.google.android.filament.Box;
import com.google.android.filament.gltfio.FilamentAsset;
import com.eqgis.sceneform.rendering.RenderableInstance;

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
//    public static float calculateUnitsScale(Renderable renderable){
//        CollisionShape collisionShape = renderable.getCollisionShape();
//
//        if (collisionShape instanceof Box){
//            Box box = (Box) collisionShape;
//            Vector3 size = box.getSize();
//            float max = Math.max(size.x,Math.max(size.y,size.z));
//            return 1.0f / max;
//        }
//
//        return 1.0f;
//    }
    public static float calculateUnitsScale(RenderableInstance renderable){
        FilamentAsset filamentAsset = renderable.getFilamentAsset();
        if (filamentAsset == null)return 0.0f;
        Box boundingBox = filamentAsset.getBoundingBox();

//        if (collisionShape instanceof Box){
//            Box box = (Box) collisionShape;
//            Vector3 size = box.getSize();
//            float max = Math.max(size.x,Math.max(size.y,size.z));
//            return 1.0f / max;
//        }
        float[] halfExtent = boundingBox.getHalfExtent();
        if (Float.isNaN(halfExtent[0])){
            return 0.0f;
        }
        float max = 2.0f * Math.max(halfExtent[0],Math.max(halfExtent[1],halfExtent[2]));
        return 1.0f / max;
    }
}
