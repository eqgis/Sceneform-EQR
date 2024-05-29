package com.eqgis.eqr.utils;

import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

/**
 * Pose工具
 * 四元数、欧拉角、旋转矩阵转换
 */
public class PoseUtils {

    /**
     * 欧拉角to四元数
     * 欧拉角单位为度
     * @return
     */
    public static Quaternion toQuaternion(float v1, float v2, float v3){

        float c1 = (float) Math.cos(Math.toRadians(v1 / 2));
        float c2 = (float) Math.cos(Math.toRadians(v2 / 2));
        float c3 = (float) Math.cos(Math.toRadians(v3 / 2));

        float s1 = (float) Math.sin(Math.toRadians(v1 / 2));
        float s2 = (float) Math.sin(Math.toRadians(v2 / 2));
        float s3 = (float) Math.sin(Math.toRadians(v3 / 2));

        return new Quaternion(
                s1 * c2 * c3 - c1 * s2 * s3,
                c1 * s2 * c3 + s1 * c2 * s3,
                c1 * c2 * s3 - s1 * s2 * c3,
                c2 * c3 * c1 + s2 * s3 * s1
        );
    }

    /**
     * 四元数转欧拉角
     * <p>
     *     由于欧拉角度,具有奇异性,因此使用时需要注意限定范围
     *     例如,(0,0,-90)与(-90,90,90)是同一个姿态
     * </p>
     * @param q
     * @return
     */
    public static Vector3 toEulerAngle(Quaternion q){
        float x = (float) Math.toDegrees(Math.atan2(2*(q.w*q.x + q.y*q.z), 1-2*(q.x * q.x + q.y*q.y)));
        float y = (float) Math.toDegrees(Math.asin( 2 * (q.w * q.y - q.x * q.z)));
        float z = (float) Math.toDegrees(Math.atan2(2*(q.w*q.z + q.x*q.y), 1-2*(q.y * q.y + q.z*q.z)));
        return new Vector3(x,y,z);
    }
    /**
     * 四元数转欧拉角
     * @param mat
     * @return
     */
    public static Vector3 toEulerAngle(float[] mat){
        float x = (float) Math.toDegrees(Math.atan2(mat[7],mat[8]));
        float y = (float) Math.toDegrees(Math.atan2(-mat[6],Math.sqrt(mat[7] * mat[7] + mat[8] * mat[8])));
        float z = (float) Math.toDegrees(Math.atan2(mat[3],mat[0]));
        return new Vector3(x,y,z);
    }

    /**
     * 四元数转旋转矩阵
     * @param q
     * @return
     */
    public static float[] toRotationMatrix(Quaternion q){
        float[] mat = new float[9];
        mat[0] = 1 - 2 * q.y * q.y - 2 * q.z * q.z;
        mat[1] = 2 * q.x * q.y - 2 * q.w * q.z;
        mat[2] = 2 * q.x * q.z + 2 * q.w * q.y;
        mat[3] = 2 * q.x * q.y + 2 * q.w * q.z;
        mat[4] = 1 - 2 * q.x * q.x - 2 * q.z * q.z;
        mat[5] = 2 * q.y * q.z - 2 * q.w * q.x;
        mat[6] = 2 * q.x * q.z - 2 * q.w * q.y;
        mat[7] = 2 * q.y * q.z + 2 * q.w * q.x;
        mat[8] = 1 - 2 * q.x * q.x - 2 * q.y * q.y;
        return mat;
    }

    /**
     * 旋转矩阵转四元数
     * @param rotationMat
     * @return
     */
    public static Quaternion toQuaternion(float[] rotationMat){
        float w = (float) (Math.sqrt(1 + rotationMat[0] + rotationMat[4] + rotationMat[8]) / 2);
        float x = (rotationMat[7] - rotationMat[5]) / (4 * w);
        float y = (rotationMat[2] - rotationMat[6]) / (4 * w);
        float z = (rotationMat[3] - rotationMat[1]) / (4 * w);
        return new Quaternion(x,y,z,w);
    }
}
