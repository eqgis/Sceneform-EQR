package com.eqgis.eqr.geometry;

import com.google.ar.sceneform.math.Matrix;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

/**
 * @author tanyx 2024/2/12
 * @version 1.0
 * <br/>SampleCode:<br/>
 * <code>
 *
 * </code>
 **/
class VectorMathUtils {
    final static Vector3 ORIGIN_POSITION = Vector3.zero();

    /**
     * 坐标变换
     * <p>计算某点vector绕着参考点ref,按direction作为旋转轴，旋转angle角度值后得到的点坐标</p>
     * @param src 源点
     * @param ref 参考点
     * @param angle 角度
     * @param direction 旋转轴
     * @return 输出结果
     */
    static Vector3 transformPoint(Vector3 src, Vector3 ref, float angle, Vector3 direction)
    {
//        Quaternion quaternion = Quaternion.AngleAxis(angle, direction);
//        Matrix4x4 matrix = new Matrix4x4();
//        matrix.SetTRS(pos, quaternion, Vector3.one);
//        vector3 = matrix.MultiplyPoint3x4(vector3);

        Quaternion quaternion = Quaternion.axisAngle(direction,angle);
        Matrix matrix = new Matrix();
        matrix.makeRotation(quaternion);
        matrix.setTranslation(ref);

        return matrix.transformPoint(src);
    }

    /**
     * 坐标变换
     * <p>根据参考点ref位置和方向向量direction，将源src点变换为新坐标</p>
     * @param src 原点
     * @param ref 参考点
     * @param direction 方向向量
     * @return 输出结果
     */
    static Vector3 transformPoint(Vector3 src, Vector3 ref, Vector3 direction)
    {
//        Matrix matrix = new Matrix();
        //direction:方向上的点看向原点，表示姿态四元数
//        Quaternion quaternion = Quaternion.lookRotation(/*direction*/direction, direction).normalized();
//        Quaternion quaternion2 = Quaternion.rotationBetweenVectors(Vector3.forward(), direction).normalized();
//        Quaternion quaternion3 = QuaternionMathUtils.getQuaternionBy2Vector(Vector3.down(),direction).normalized();
//        Log.d("ikkyuQ", "src" + direction.toString() +"  transformPoint: "+quaternion.toString() /*+ "  |||  "+ quaternion2 + " ||| " +quaternion3*/);
//        matrix.makeRotation(quaternion2);
//        matrix.setTranslation(ref);
//        return matrix.transformPoint(src);

//        if (Vector3.angleBetweenVectors(direction,Vector3.down()) < 1.0f){
//            Log.i("IKKYU D", "transformPoint: " + direction.toString() + "  --  " + ref.toString());
//            Vector3 vector3 = Quaternion.rotateVector(Quaternion.rotationBetweenVectors(Vector3.up(), direction).inverted(), src);
//            return Vector3.add(vector3,ref);
//        }

        Vector3 vector3 = Quaternion.rotateVector(Quaternion.rotationBetweenVectors(Vector3.forward(), direction), src);
//        Vector3 vector3 = Quaternion.rotateVector(Quaternion.rotationBetweenVectors(Vector3.up(), direction), src);
        return Vector3.add(vector3,ref);

    }
}
