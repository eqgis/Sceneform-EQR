package com.eqgis.ar;


import com.google.sceneform.ARPlatForm;
import com.google.ar.core.Pose;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;

/**
 * AR位姿数据类
 * @author tanyx
 */
public class ARPose{

    Pose corepose = null;
    com.huawei.hiar.ARPose hwpose = null;

//    public static final  ARPose IDENTITY = new ARPose(Pose.IDENTITY,com.huawei.hiar.ARPose.IDENTITY);

    ARPose(Object pose){
        //added by ikkyu
        if (pose instanceof Pose){
            corepose = (Pose)pose;
            hwpose = null;
        }else {
            corepose = null;
            hwpose = (com.huawei.hiar.ARPose)pose;
        }
    }

    /**
     * 构造函数
     * <p>pose构造时校正了AREngine的坐标系</p>
     * @param coreobj
     * @param hwobj
     */
    ARPose(Pose coreobj, com.huawei.hiar.ARPose hwobj) {
        if (coreobj==null && hwobj==null){
            throw new IllegalArgumentException();
        }
        corepose = coreobj;
        if (hwobj != null){
            //更新AREngine的pose
            hwpose = hwobj;
        }
    }

    /**
     * 构造函数
     * <p>通过特定的平移向量和旋转向量构造位姿类，其中translation是从目的坐标系到局部坐标系的位移向量，rotation是Hamilton四元组。</p>
     * @param translation 目的坐标系到局部坐标系的位移向量
     * @param rotation 姿态四元数
     */
    public ARPose(float[] translation, float[] rotation) {
        if (ARPlugin.isUsingAREngine()){
            hwpose = new com.huawei.hiar.ARPose(translation,rotation);
        }else {
            corepose = new Pose(translation,rotation);
        }
    }

    /**
     * 根据提供的平移向量构造pose，旋转向量为0。
     */
    public static  ARPose makeTranslation(float tx, float ty, float tz) {
        com.huawei.hiar.ARPose hwpose =  com.huawei.hiar.ARPose.makeTranslation(tx, ty, tz);
        Pose corepose = Pose.makeTranslation(tx,ty,tz);
        return new ARPose(corepose,hwpose);
    }

    /**
     * 根据提供的平移向量构造pose，旋转向量为0。
     */
    public static  ARPose makeTranslation(float[] translation) {
        if (translation != null && translation.length >= 3) {
            return makeTranslation(translation[0], translation[1], translation[2]);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 根据提供的旋转向量构造pose，平移向量为0。
     */
    public static  ARPose makeRotation(float axisX, float axisY, float axisZ, float axisW) {
        com.huawei.hiar.ARPose hwpose =  com.huawei.hiar.ARPose.makeRotation(axisX,axisY,axisZ,axisW);
        Pose corepose = Pose.makeRotation(axisX,axisY,axisZ,axisW);
        return new ARPose(corepose,hwpose);
    }

    /**
     * 根据提供的旋转向量构造pose，平移向量为0。
     */
    public static  ARPose makeRotation(float[] quaternion) {
        if (quaternion != null && quaternion.length >= 4) {
            return makeRotation(quaternion[0], quaternion[1], quaternion[2], quaternion[3]);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 返回pose1与pose2的插值pose，在平移向量上执行线性插值，在旋转向量上执行球面线性插值。
     * @param pose1 用于计算插值的Pose变量。
     * @param pose2 用于计算插值的Pose变量。
     * @param proportion 用于计算插值的比例信息。
     * @return pose1与pose2的插值pose。
     */
    public static ARPose makeInterpolated(ARPose pose1, ARPose pose2, float  proportion) {
        if (ARPlugin.isUsingARCore()){
            Pose pose = Pose.makeInterpolated(pose1.corepose, pose2.corepose, proportion);
            return new ARPose(pose,null);
        }else {
            com.huawei.hiar.ARPose hwPose = com.huawei.hiar.ARPose.makeInterpolated(pose1.hwpose,pose2.hwpose,proportion);
            return new ARPose(null,hwPose);
        }
    }

    /**
     * 提取仅包含平移向量的位姿数据。
     * @return 仅包含this平移向量的位姿数据。
     */
    public ARPose extractTranslation() {
        if (corepose!=null && hwpose!=null) {
            return new ARPose(corepose.extractTranslation(), hwpose.extractTranslation());
        }else if(corepose!=null){
            return new ARPose(corepose.extractTranslation(), null);
        }else{
            return new ARPose(null, hwpose.extractTranslation());
        }
    }

    /**
     * 提取仅包含旋转姿态的的位姿数据
     * @return
     */
    public  ARPose extractRotation() {
        if (corepose!=null && hwpose!=null) {
            return new ARPose(corepose.extractRotation(),hwpose.extractTranslation());
        }else if(corepose!=null){
            return new ARPose(corepose.extractRotation(),null);
        }else{
            return new ARPose(null,hwpose.extractTranslation());
        }

    }

    /**
     * 将位姿数据转换成4X4的矩阵
     * @param dest 转换后的矩阵信息。
     * @param offset 存放在dest中的初始偏移量。
     */
    public void toMatrix(float[] dest, int offset) {
        if (corepose!=null){
            corepose.toMatrix(dest,offset);
        }else{
            hwpose.toMatrix(dest,offset);
        }
    }

    /**
     * 获取构造ARPose时使用的平移向量x分量。
     * @return
     */
    public float tx() {
        if (corepose!=null){
            return corepose.tx();
        }else{
            return hwpose.tx();
        }
    }

    /**
     * 获取构造ARPose时使用的平移向量y分量。
     * @return
     */
    public float ty() {
        if (corepose!=null){
            return corepose.ty();
        }else{
            return hwpose.ty();
        }
    }

    /**
     * 获取构造ARPose时使用的平移向量z分量。
     * @return
     */
    public float tz() {
        if (corepose!=null){
            return corepose.tz();
        }else{
            return hwpose.tz();
        }
    }

    /**
     * 获取构造ARPose时使用的旋转变量中的x分量。
     * @return
     */
    public float qx() {
        if (corepose!=null){
            return corepose.qx();
        }else{
            return hwpose.qx();
        }
    }

    /**
     * 获取构造ARPose时使用的旋转变量中的y分量。
     * @return
     */
    public float qy() {
        if (corepose!=null){
            return corepose.qy();
        }else{
            return hwpose.qy();
        }
    }

    /**
     * 获取构造ARPose时使用的旋转变量中的z分量。
     * @return
     */
    public float qz() {
        if (corepose!=null){
            return corepose.qz();
        }else{
            return hwpose.qz();
        }
    }

    /**
     * 获取构造ARPose时使用的旋转变量中的w分量。
     * @return
     */
    public float qw() {
        if (corepose!=null){
            return corepose.qw();
        }else{
            return hwpose.qw();
        }
    }

    /**
     * 获取平移向量，放入dest中，初始偏移为offset。
     * @param dest 获取得到的平移变量。
     * @param offset 存放在dest中的初始偏移量。
     */
    public void getTranslation(float[] dest, int offset) {
        if (corepose!=null){
            corepose.getTranslation(dest,offset);
        }else{
            hwpose.getTranslation(dest,offset);
        }
    }

    /**
     * 获取平移向量
     * @return
     */
    public float[] getTranslation() {
        float[] var1 = new float[3];
        this.getTranslation(var1, 0);
        return var1;
    }

    /**
     * 获取旋转四元组，放入dest中，初始偏移为offset。dest为存放结果的数组，其中的数据存放顺序为x、y、z、w。
     * @param dest 获取得到的旋转四元组信息。
     * @param offset 存放在dest中的初始偏移量。
     */
    public void getRotationQuaternion(float[] dest, int offset) {
        if (corepose!=null){
            corepose.getRotationQuaternion(dest,offset);
        }else{
            hwpose.getRotationQuaternion(dest,offset);
        }
    }

    /**
     * 获取旋转四元组，放入dest中，初始偏移为offset。dest为存放结果的数组，其中的数据存放顺序为x、y、z、w。
     * @return
     */
    public float[] getRotationQuaternion() {
        float[] var1 = new float[4];
        this.getRotationQuaternion(var1, 0);
        return var1;
    }

    /**
     * 返回ARPose在X轴的单位坐标向量，返回的数组长度是3。
     * @return
     */
    public float[] getXAxis() {
        return this.getTransformedAxis(0, 1.0F);
    }

    /**
     * 返回ARPose在Y轴的单位坐标向量，返回的数组长度是3。
     * @return
     */
    public float[] getYAxis() {
        return this.getTransformedAxis(1, 1.0F);
    }

    /**
     * 返回ARPose在Z轴的单位坐标向量，返回的数组长度是3。
     * @return
     */
    public float[] getZAxis() {
        return this.getTransformedAxis(2, 1.0F);
    }

    /**
     * 获取局部坐标系的某个坐标轴单位坐标向量，axis的取值0=X，1=Y，2=Z；scale为轴向量的长度；结果存入dest数组中，初始偏移为offset。
     * @param axis 需要获取的坐标轴参数，取值0代表X轴，1代表Y轴，2代表Z轴。
     * @param scale 轴向量长度。
     * @param dest 获取得到的坐标轴单位坐标向量。
     * @param offset 存放在dest中的初始偏移量。
     */
    public void getTransformedAxis(int axis, float scale, float[] dest, int offset) {
        if (corepose!=null) {
            corepose.getTransformedAxis(axis,scale,dest,offset);
        } else {
            hwpose.getTransformedAxis(axis,scale,dest,offset);
        }
    }

    /**
     * 返回局部坐标系的某个坐标轴单位坐标向量，axis的取值0=X, 1=Y, 2=Z；scale为轴向量长度。
     * @param axis 需要获取的坐标轴参数，取值0=X, 1=Y, 2=Z。
     * @param scale 轴向量长度。
     * @return
     */
    public float[] getTransformedAxis(int axis, float scale) {
        float[] result = new float[3];
        this.getTransformedAxis(axis, scale, result, 0);
        return result;
    }

    /**
     * 返回this与rhs的复合。一个点通过该方法返回的pose的变换等价于先通过rhs变换，然后通过this变换。返回结果满足如下关系，result.toMatrix() = this.toMatrix() * rhs.toMatrix()。
     * @param rhs 待转换的位姿信息。
     * @return
     */
    public ARPose compose(ARPose rhs) {
//        if (corepose!=null && hwpose!=null) {
//            return new ARPose( corepose.compose(pose.corepose),hwpose.compose(pose.hwpose) );
//        }else if(corepose!=null){
//            return new ARPose( corepose.compose(pose.corepose),null );
//        }else{
//            return new ARPose( null,hwpose.compose(pose.hwpose) );
//        }
        if (ARPlatForm.isArEngine()){
            return new ARPose( null,hwpose.compose(rhs.hwpose));
        }else {
            return new ARPose( corepose.compose(rhs.corepose),null );
        }

    }

    /**
     * 返回执行相反转换的pose（世界坐标系->局部坐标系）。
     * @return
     */
    public ARPose inverse() {
        if (corepose!=null){
            Pose inverse = corepose.inverse();
            return new ARPose(inverse);
        }else {
            return new ARPose(hwpose.inverse());
        }
    }

    /**
     * 根据this的旋转向量，对坐标点只进行旋转操作，不执行平移。
     * @param source 需要执行旋转的输入向量。
     * @return
     */
    public float[] rotateVector(float[] source) {
        if (corepose!=null){
            return corepose.rotateVector(source);
        }else {
            //AREngine如此写
            float[] dst = new float[source.length];
            //vectorIn是输入的向量，inOffset是其初始偏移；vectorOut是输出向量，outOffset是其初始偏移。
            hwpose.rotateVector(source,0,dst,0);
            return dst;
        }
    }


    //<editor-fold> - 使AREngine的坐标系与ARCore保持一致

    /**
     * 在AREngine平台下更新AR位姿数据
     */
    public static ARPose updatePoseOnAREngine(ARPose arPose){
        if (!ARPlugin.isUsingFixedCoordinate())return arPose;
        if (!ARPlugin.isUsingAREngine()){
            return arPose;
        }
        //计算绕点旋转后的坐标，仅修改X,Z，y轴值不变
        float offsetAngle = ARCamera.getOffsetAngle();
        Quaternion offsetRotation = new Quaternion(Vector3.up(), offsetAngle);
        //坐标旋转，相机坐标绕场景原点旋转，得到的新坐标作为相机位置
        Vector3 newPosition = Quaternion.rotateVector(offsetRotation, new Vector3(arPose.tx(),arPose.ty(),arPose.tz()));

        //desc-坐标系转换，右手系（X-right，Y-up）转换为（x-right，y-forward）
        Quaternion newQuaternion = new Quaternion(arPose.qx(),-arPose.qz(), arPose.qy(), arPose.qw());
        //四元数转欧拉角
        Vector3 vec = toEulerAngle(newQuaternion);
        //水平面上，forward的偏移角度
        vec.z += ARCamera.getOffsetAngle();;
        //欧拉角转四元数
        Quaternion quaternion = toQuaternion(vec.x, vec.y, vec.z);
        //desc-坐标系转换右手系（x-right，y-forward）转换为右手系（X-right，Y-up）
        Quaternion quaternion1 = new Quaternion(quaternion.x, quaternion.z, -quaternion.y, quaternion.w);
//    Log.e("IKKYU-C", "updateTrackedPose: "+vector3  + "--------"+vec + "Position " + newPosition);
        return new ARPose(null,new com.huawei.hiar.ARPose(new float[]{newPosition.x,newPosition.y,newPosition.z},
                new float[]{quaternion1.x,quaternion1.y,quaternion1.z,quaternion1.w}));
    }

    /**
     * 欧拉角to四元数
     * 欧拉角单位为度
     * @return
     */
    private static Quaternion toQuaternion(float v1, float v2, float v3){
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
     * @param q
     * @return
     */
    private static Vector3 toEulerAngle(Quaternion q){
        float x = (float) Math.toDegrees(Math.atan2(2*(q.w*q.x + q.y*q.z), 1-2*(q.x * q.x + q.y*q.y)));
        float y = (float) Math.toDegrees(Math.asin( 2 * (q.w * q.y - q.x * q.z)));
        float z = (float) Math.toDegrees(Math.atan2(2*(q.w*q.z + q.x*q.y), 1-2*(q.y * q.y + q.z*q.z)));
        return new Vector3(x,y,z);
    }
    //</editor-fold>
}
