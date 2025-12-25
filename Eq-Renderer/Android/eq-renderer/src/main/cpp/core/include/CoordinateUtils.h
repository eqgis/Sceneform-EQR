
#include <jni.h>

#ifndef SAMPLE_COORDINATEUTILS_H
#define SAMPLE_COORDINATEUTILS_H

#endif //SAMPLE_COORDINATEUTILS_H

/**
 * 场景坐标转地理坐标
 */
extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_eqgis_eqr_core_CoordinateUtilsNative_jni_1ToGeoLocation(JNIEnv *env, jclass clazz,
                                                            jdouble ref_x, jdouble ref_y,
                                                            jdouble target_x,jdouble target_y,
                                                            jdouble azimuth);
/**
 * 地理坐标转场景坐标
 */
extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_eqgis_eqr_core_CoordinateUtilsNative_jni_1ToScenePosition(JNIEnv *env, jclass clazz,
                                                                   jdouble ref_x, jdouble ref_y,
                                                                   jdouble target_location_x,
                                                                   jdouble target_location_y,
                                                                   jdouble azimuth_rad);

/**
 * 计算两点间的偏移量
 * @param x1
 * @param y1
 * @param x2
 * @param y2
 * @return
 */
double * ComputeTranslation(double x1, double y1,
                            double x2, double y2);

/**
 * 计算球面上两点的距离
 * CGCS2000椭球体 ：半径：6378137,扁率 0.003352810681182
 * @param pntFrom 起始点
 * @param pntTo 结束点
 * @param dPrimeAxis 椭球半径
 * @param dFlattening 椭球扁率
 * @return
 */
double GetSpheroidDistance(double pntFromX,double pntFromY,
                           double pntToX,double pntToY,double dPrimeAxis, double dFlattening);
