package com.eqgis.eqr.ar;

import android.media.Image;

import com.google.ar.core.LightEstimate;

/**
 * AR场景光线估计对象
 */
public class ARLightEstimate {
    LightEstimate coreLight = null;
    com.huawei.hiar.ARLightEstimate hwLight = null;

    ARLightEstimate(LightEstimate coreobj, com.huawei.hiar.ARLightEstimate hwobj){
        if (coreobj==null && hwobj==null){
            throw new IllegalArgumentException();
        }
        coreLight = coreobj;
        hwLight = hwobj;
    }

//    public boolean isValid() {
//        if (coreLight!=null){
//            return coreLight.
//        }
//    }

    /**
     * 返回光照估计的有效性。
     * @return 状态
     */
    public  State getState() {
        if (coreLight!=null){
            return State.forARCore( coreLight.getState() );
        }else{
            return State.forHuaWei( hwLight.getState() );
        }
    }

    /**
     * 获取当前相机视野的像素强度，其范围为0.0~1.0，0.0代表黑，1.0代表白。
     * @return 强度
     */
    public float getPixelIntensity() {
        if (coreLight!=null){
            return   coreLight.getPixelIntensity();
        }else{
            return   hwLight.getPixelIntensity();
        }
    }

    public float[] getSphericalHarmonicCoefficients() {
        if (coreLight!=null){
            return   coreLight.getEnvironmentalHdrAmbientSphericalHarmonics();
        }else{
            return   hwLight.getSphericalHarmonicCoefficients();
        }
    }

    public float[] getPrimaryLightDirection() {
        if (coreLight!=null){
            return   coreLight.getEnvironmentalHdrMainLightDirection();
        }else{
            return   hwLight.getPrimaryLightDirection();
        }
    }

    public float getPrimaryLightIntensity() {
        if (coreLight!=null){
            return   coreLight.getEnvironmentalHdrMainLightIntensity()[0];
        }else{
            return   hwLight.getPrimaryLightIntensity();
        }
    }

    public float[] getPrimaryLightColor() {
        if (coreLight!=null){
            float[] colors = new float[4];
            coreLight.getColorCorrection(colors,0);
            return colors;
        }else{
            return   hwLight.getPrimaryLightColor();
        }
    }

    public float[] getEnvironmentalHdrMainLightDirection() {
        if (coreLight!=null){
            return coreLight.getEnvironmentalHdrMainLightDirection();
        }else {
            throw new RuntimeException("not support");
        }
    }

    public float[] getEnvironmentalHdrMainLightIntensity() {
        if (coreLight != null){
            return coreLight.getEnvironmentalHdrMainLightIntensity();
        }else {
            throw new RuntimeException("not support");
        }
    }

    public float[] getEnvironmentalHdrAmbientSphericalHarmonics() {
        if (coreLight != null){
            return coreLight.getEnvironmentalHdrAmbientSphericalHarmonics();
        }else {
            throw new RuntimeException("not support");
        }
    }

    public Image[] acquireEnvironmentalHdrCubeMap() {
        if (coreLight != null){
            return coreLight.acquireEnvironmentalHdrCubeMap();
        }else {
            throw new RuntimeException("not support");
        }
    }

    /**
     * 颜色矫正
     * @param colorCorrectionPixelIntensity
     * @param i
     */
    public void getColorCorrection(float[] colorCorrectionPixelIntensity, int i) {
        if (coreLight != null){
            coreLight.getColorCorrection(colorCorrectionPixelIntensity,i);
        }
        else {
            //构造光线强度
            float pixelIntensity = hwLight.getPixelIntensity();
            colorCorrectionPixelIntensity[0] = 1.0f;
            colorCorrectionPixelIntensity[1] = 1.0f;
            colorCorrectionPixelIntensity[2] = 1.0f;
            colorCorrectionPixelIntensity[3] = pixelIntensity;
        }
    }


//    public ByteBuffer acquireEnvironmentTexture() {
//        coreLight.acquireEnvironmentalHdrCubeMap()
//        long var1 = this.mSession.mNativeHandle;
//        long var3 = this.mNativeHandle;
//        ByteBuffer var5;
//        if ((var5 = this.nativeAcquireEnvironmentTexture(var1, var3)) == null) {
//            Log.e(TAG, "acquire environment texture is null.");
//            return null;
//        } else {
//            return var5.asReadOnlyBuffer();
//        }
//    }




    public static enum State {
        UNKNOWN_STATE(-1),

        /**
         * 无效
         */
        NOT_VALID(0),

        /**
         * 有效
         */
        VALID(1);

        final int nativeCode;

        private State(int nativeCode) {
            this.nativeCode = nativeCode;
        }

        static  State forNumber(int nativeCode) {
            State[] var1;
            int var2 = (var1 = values()).length;

            for(int var3 = 0; var3 < var2; ++var3) {
                State var4;
                if ((var4 = var1[var3]).nativeCode == nativeCode) {
                    return var4;
                }
            }

            return UNKNOWN_STATE;
        }

        static State forARCore(LightEstimate.State state){
            if (state== LightEstimate.State.NOT_VALID){
                return NOT_VALID;
            }else if(state== LightEstimate.State.VALID){
                return VALID;
            }
            return UNKNOWN_STATE;
        }

        static State forHuaWei(com.huawei.hiar.ARLightEstimate.State state){
            if (state== com.huawei.hiar.ARLightEstimate.State.NOT_VALID){
                return NOT_VALID;
            }else if(state== com.huawei.hiar.ARLightEstimate.State.VALID){
                return VALID;
            }
            return UNKNOWN_STATE;
        }

    }

}
