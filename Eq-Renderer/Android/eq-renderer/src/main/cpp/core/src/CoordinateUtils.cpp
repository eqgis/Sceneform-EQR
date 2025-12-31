//
// Created by IKKYU on 2023/6/20.
//

#include <jni.h>
#include <valarray>
#include "../include/CoordinateUtils.h"
#include "../include/Core.h"

//! Pi
#ifndef PI
#define PI      3.1415926535897932384626433833
#endif

#define TILE_GROUP_SIZE 128
//! Euler constant
#define EULER   2.7182818284590452353602874713

//! Multiplier for degrees to radians
#define DTOR    0.0174532925199432957692369077

//! Multiplier for radians to degrees
#define RTOD    57.295779513082320876798154814


double * ComputeTranslation(double x1, double y1,
                            double x2, double y2) {
    double res[2] = {0,0};
    if (x1 == x2 && y1 == y2){
        return res;
    }

    double myLocationX = x1;
    double myLocationY = y2;

    int flagX = 1;
    int flagY = 1;
    if (x1 > x2){
        flagX = -1;
    }
    if (y1 > y2){
        flagY = -1;
    }

//    float x = (float) distance(myLocation1, locationB);
//    float y = (float) distance(myLocation1, locationA);
//    return new float[]{flagX * x, flagY * y};
//    CGCS2000: 6378137, 0.00335281068

    double x = GetSpheroidDistance(myLocationX,myLocationY,x2,y2,6378137, 0.00335281068);
    double y = GetSpheroidDistance(myLocationX,myLocationY,x1,y1,6378137, 0.00335281068);

    res[0] = flagX * x;
    res[1] = flagY * y;
    return res;
}

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
                           double pntToX,double pntToY,double dPrimeAxis, double dFlattening)
{

    if (pntFromX > 180.0 || pntFromX < (-180.0)
        ||pntToX > 180.0 || pntToX < (-180.0)
        ||pntFromY > (90.0)  || pntFromY < (-90.0)
        ||pntToY > (90.0)  || pntToY < (-90.0))
    {
        return  0;
    }
//    ASSERT(dPrimeAxis>0.0);
//    ASSERT(dFlattening>0.0 && dFlattening<1.0);
    if(dPrimeAxis < 0 || dFlattening < 0 || dFlattening >1)
    {
//        ASSERT(FALSE);
        return -1 ;
    }

    if(pntFromX == pntToX && pntFromY == pntToY)
    {
        return 0;
    }

    int nDistUnits = 10000;
    const double a = dPrimeAxis * nDistUnits / 10000.0;				//6378137
    const double f = dFlattening;									//1/298.257;
    const double b = a * (1-f);
    const double e22 = (a * a - b * b) / (b*b);
    const double _dPhi1 = pntFromY * PI/180;						//37.7 * PI/180;
    const double _dPhi2 = pntToY * PI/180;							//37.71666666666 * PI/180;
    const double _dNamda1 = pntFromX * PI/180 ;					//143.5 * PI/180;
    const double _dNamda2 = pntToX * PI/180;						//142.5 * PI/180;
    const double EPS10 = 1e-10;										//迭代计算的精度控制

    double _dTanU1 = (1-f) * tan(_dPhi1);
    double _dTanU2 = (1-f) * tan(_dPhi2);
    double dU1 = atan(_dTanU1);
    double dU2 = atan(_dTanU2);

    double dCosU2 = cos(dU2);
    double dSinU2 = sin(dU2);

    double dCosU1 = cos(dU1);
    double dSinU1 = sin(dU1);
    double _dNamdaNew = _dNamda2 - _dNamda1;
    double _dOmiga = _dNamdaNew;
    double _dNamdaOld = 0.0;

    double dSinNamda = 0.0;
    double dCosNamda = 0.0;
    double dProxy1  = 0.0;
    double _dSin2Sigma = 0.0;
    double _dCosSigma = 0.0;
    double _dSigma  = 0.0;
    double _dSinSigma = 0.0;
    double _dSinAlpha = 0.0;
    double dAlpha  = 0.0;
    double _dCos2Alpha  = 0.0;
    double _dCos_2Sigmam  = 0.0;

    int i =0;
    do
    {
        _dNamdaOld = _dNamdaNew;

        dSinNamda = sin(_dNamdaNew);
        dCosNamda = cos(_dNamdaNew);

        dProxy1  = dCosU1 * dSinU2 - dSinU1 * dCosU2 * dCosNamda ;

        _dSin2Sigma = dCosU2 * dCosU2 * dSinNamda * dSinNamda + dProxy1 * dProxy1;
        _dCosSigma = dSinU1 * dSinU2 + dCosU1 * dCosU2 * dCosNamda;
        _dSinSigma = sqrt(_dSin2Sigma);
        _dSigma = atan2(_dSinSigma, _dCosSigma);
        _dSinAlpha = dCosU1 * dCosU2 * dSinNamda / _dSinSigma;
        dAlpha = asin(_dSinAlpha);
        double dCosAlpha = cos(dAlpha);
        _dCos2Alpha = dCosAlpha * dCosAlpha;
        _dCos_2Sigmam = _dCosSigma - (2 * dSinU1 * dSinU2 / _dCos2Alpha );

        double _dC = (f/16) * _dCos2Alpha * (4 + f * ( 4 - 3 * _dCos2Alpha ));
        double dProxy2 = _dCos_2Sigmam + _dC * _dCosSigma * (2 * _dCos_2Sigmam * _dCos_2Sigmam - 1);
        _dNamdaNew = _dOmiga + (1 - _dC) * f * _dSinAlpha * ( _dSigma + _dC * _dSinSigma * dProxy2 );
        i++;
    }while ( fabs((_dNamdaNew - _dNamdaOld)) > EPS10 && i < 2000);
    double _d2u = _dCos2Alpha * e22;
    double _dA = 1 + (_d2u / 16384) * (4096 + _d2u * ( -768 + _d2u * (320 - 175 * _d2u)));
    double _dB = (_d2u / 1024) * (256 + _d2u * (-128 + _d2u * (74 - 47 * _d2u)));
    double dProxy3 = (_dB/6) * _dCos_2Sigmam * ( -3 + 4 * _dSin2Sigma) * (-3 + 4 * _dCos_2Sigmam * _dCos_2Sigmam);
    double dProxy4 = _dCosSigma * (2 * _dCos_2Sigmam * _dCos_2Sigmam -1);
    double _dDeltaSigma = _dB * _dSinSigma * ( _dCos_2Sigmam + (_dB/4) * (dProxy4 - dProxy3));
    double dS = b * _dA * ( _dSigma - _dDeltaSigma);
    double dAlpha12 = atan2( (dCosU2 * dSinNamda), (dCosU1 * dSinU2 - dSinU1 * dCosU2 * dCosNamda));
    double dAlpha21 = atan2( (dCosU1 * dSinNamda), (dCosU1 * dSinU2 * dCosNamda - dSinU1 * dCosU2));
    dAlpha12 *= (180/PI);
    dAlpha21 *= (180/PI);

    if(dAlpha12 < -EPS10)
        dAlpha12 += 360;
    dAlpha21 += 180;

    return dS * 10000 / nDistUnits;
}



extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_eqgis_eqr_core_CoordinateUtilsNative_jni_1ToGeoLocation(JNIEnv *env, jclass clazz,
                                                            jdouble ref_x, jdouble ref_y,
                                                            jdouble target_x,jdouble target_y,
                                                            jdouble azimuthRad) {
    if (!EQR::CORE_STATUS) return NULL;

    //1 计算正北方向为Y轴，建立的平面坐标系的对应的目标点的x、y坐标（arPosition逆时针旋转azimuth）
    double x = target_x * cos(-azimuthRad) - target_y * sin(-azimuthRad);
    double y = target_x * sin(-azimuthRad) + target_y * cos(-azimuthRad);
//    Location virtueLocation = new Location(deviceLocation.getLongitude() + 0.0001, deviceLocation.getLatitude() + 0.0001, 0);
    double virtueLocationLon = ref_x + 0.0001;
    double virtueLocationLat = ref_y + 0.0001;

    //计算偏移量
    double *offset = ComputeTranslation(ref_x,ref_y,virtueLocationLon,virtueLocationLat);
    double deltaX = abs(0.0001 / *offset);
    double deltaY = abs(0.0001 / *(offset+1));
    double vv1 = x * deltaX + ref_x;
    double vv2 = y * deltaY + ref_y;

    //Location(vv1, vv2, arPosition.z + deviceLocation.getHeight());
    //构造返回数据
    double outArray[] = {vv1,vv2};
    jdoubleArray  outJNIArray = env->NewDoubleArray(2);
    if (NULL == outJNIArray)return NULL;
    //向jdoubleArray写入数据
    env->SetDoubleArrayRegion(outJNIArray,0,2,outArray);
    return outJNIArray;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_eqgis_eqr_core_CoordinateUtilsNative_jni_1ToScenePosition(JNIEnv *env, jclass clazz,
                                                                   jdouble ref_x, jdouble ref_y,
                                                                   jdouble target_location_x,
                                                                   jdouble target_location_y,
                                                                   jdouble azimuth_rad) {
    if (!EQR::CORE_STATUS) return NULL;

    double *offset = ComputeTranslation(ref_x, ref_y, target_location_x, target_location_y);
    double deX = *offset;
    double deY = *(offset + 1);
    // 2 Coordinate conversion according to azimuth to calculate ar coordinates
    double x = deX * cos(azimuth_rad) - deY * sin(azimuth_rad);
    double y = deX * sin(azimuth_rad) + deY * cos(azimuth_rad);

    //构造返回数据
    double outArray[] = {x,y};
    jdoubleArray  outJNIArray = env->NewDoubleArray(2);
    if (NULL == outJNIArray)return NULL;
    //向jdoubleArray写入数据
    env->SetDoubleArrayRegion(outJNIArray,0,2,outArray);
    return outJNIArray;
}