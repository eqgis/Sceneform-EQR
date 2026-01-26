package com.eqgis.eqr.core;
/**
 * (高斯点云)顶点排序
 * <p>
 *     渲染高斯点云时，采用transparent混合方式，需进行排序。
 *     由于filament暂不支持计算着色器。
 *     故采用cpu实现排序。
 *     性能开销大，谨慎使用
 * </p>
 */
public final class GaussianSorter {

    private final int gaussianCount;
    private final int[] order;
    private final float[] depth;

    private final float[] viewMat = new float[16];
    private final float[] modelViewMat = new float[16];

    public GaussianSorter(int gaussianCount) {
        this.gaussianCount = gaussianCount;
        this.order = new int[gaussianCount];
        this.depth = new float[gaussianCount];

        for (int i = 0; i < gaussianCount; i++) {
            order[i] = i;
        }
    }

    /**
     * 每次调用：必然重新排序
     */
    public void sort(
            float[] localCenters,   // x,y,z * N
            float[] modelMat,       // 4x4 column-major
            float[] cameraModelMat,  // 4x4 column-major
            int[] outIndices

    ) {
        //view = inverse(cameraModel)
        invertRigidTransform(cameraModelMat, viewMat);

        // modelView = view * model
        mulMat4(viewMat, modelMat, modelViewMat);

        //depth = -Z_camera
        for (int i = 0, b = 0; i < gaussianCount; i++, b += 3) {
            float x = localCenters[b];
            float y = localCenters[b + 1];
            float z = localCenters[b + 2];

            float cz =
                    modelViewMat[2]  * x +
                            modelViewMat[6]  * y +
                            modelViewMat[10] * z +
                            modelViewMat[14];

            depth[i] = -cz;
        }

        quickSort(order, depth, 0, gaussianCount - 1);

        // fill indices immediately
        int idx = 0;
        for (int k = 0; k < gaussianCount; k++) {
            int base = order[k] * 4;

            outIndices[idx++] = base;
            outIndices[idx++] = base + 1;
            outIndices[idx++] = base + 2;

            outIndices[idx++] = base;
            outIndices[idx++] = base + 2;
            outIndices[idx++] = base + 3;
        }
    }

    public void sortSingle(
            float[] localCenters,   // x,y,z * N
            float[] modelMat,       // 4x4 column-major
            float[] cameraModelMat,  // 4x4 column-major
            int[] outIndices

    ) {
        //view = inverse(cameraModel)
        invertRigidTransform(cameraModelMat, viewMat);

        // modelView = view * model
        mulMat4(viewMat, modelMat, modelViewMat);

        //depth = -Z_camera
        for (int i = 0, b = 0; i < gaussianCount; i++, b += 3) {
            float x = localCenters[b];
            float y = localCenters[b + 1];
            float z = localCenters[b + 2];

            float cz =
                    modelViewMat[2]  * x +
                            modelViewMat[6]  * y +
                            modelViewMat[10] * z +
                            modelViewMat[14];

            depth[i] = -cz;
        }

        quickSort(order, depth, 0, gaussianCount - 1);

        // fill indices immediately
        for (int k = 0; k < order.length; k++) {
            outIndices[k] = order[k];
        }
    }

    /* ---------------- math helpers ---------------- */

    /**
     * v' = q * v * q^-1
     */
    private static void rotateVectorByQuat(
            float vx, float vy, float vz,
            float[] q,           // x,y,z,w
            float[] out          // size 3
    ) {
        float qx = q[0], qy = q[1], qz = q[2], qw = q[3];

        // t = 2 * cross(q.xyz, v)
        float tx = 2f * (qy * vz - qz * vy);
        float ty = 2f * (qz * vx - qx * vz);
        float tz = 2f * (qx * vy - qy * vx);

        // v' = v + qw * t + cross(q.xyz, t)
        out[0] = vx + qw * tx + (qy * tz - qz * ty);
        out[1] = vy + qw * ty + (qz * tx - qx * tz);
        out[2] = vz + qw * tz + (qx * ty - qy * tx);
    }

    private static void quickSort(int[] order, float[] depth, int left, int right) {
        int i = left, j = right;
        float pivot = depth[order[(left + right) >>> 1]];

        while (i <= j) {
            while (depth[order[i]] > pivot) i++;
            while (depth[order[j]] < pivot) j--;
            if (i <= j) {
                int tmp = order[i];
                order[i] = order[j];
                order[j] = tmp;
                i++;
                j--;
            }
        }

        if (left < j) quickSort(order, depth, left, j);
        if (i < right) quickSort(order, depth, i, right);
    }


    private static void invertRigidTransform(float[] m, float[] out) {
        // rotation transpose
        out[0] = m[0];  out[1] = m[4];  out[2]  = m[8];
        out[4] = m[1];  out[5] = m[5];  out[6]  = m[9];
        out[8] = m[2];  out[9] = m[6];  out[10] = m[10];

        // translation
        float tx = m[12];
        float ty = m[13];
        float tz = m[14];

        out[12] = -(out[0] * tx + out[4] * ty + out[8]  * tz);
        out[13] = -(out[1] * tx + out[5] * ty + out[9]  * tz);
        out[14] = -(out[2] * tx + out[6] * ty + out[10] * tz);

        out[3] = out[7] = out[11] = 0f;
        out[15] = 1f;
    }

    private static void mulMat4(float[] a, float[] b, float[] out) {
        for (int c = 0; c < 4; c++) {
            int ci = c * 4;
            out[ci]     = a[0] * b[ci]     + a[4] * b[ci + 1] + a[8]  * b[ci + 2] + a[12] * b[ci + 3];
            out[ci + 1] = a[1] * b[ci]     + a[5] * b[ci + 1] + a[9]  * b[ci + 2] + a[13] * b[ci + 3];
            out[ci + 2] = a[2] * b[ci]     + a[6] * b[ci + 1] + a[10] * b[ci + 2] + a[14] * b[ci + 3];
            out[ci + 3] = 0f;
        }
        out[15] = 1f;
    }


}
