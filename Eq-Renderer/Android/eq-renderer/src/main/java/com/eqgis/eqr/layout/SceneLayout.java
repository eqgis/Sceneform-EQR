package com.eqgis.eqr.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.PixelCopy;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.eqgis.eqr.core.Eqr;
import com.eqgis.eqr.listener.CompleteCallback;
import com.eqgis.eqr.node.RootNode;
import com.eqgis.exception.NotSupportException;
import com.google.android.filament.Engine;
import com.google.android.filament.IndirectLight;
import com.google.android.filament.Skybox;
import com.google.android.filament.utils.KTX1Loader;
import com.google.sceneform.Camera;
import com.google.sceneform.CameraSceneView;
import com.google.sceneform.ExSceneView;
import com.google.sceneform.Node;
import com.google.sceneform.Scene;
import com.google.sceneform.SceneView;
import com.google.sceneform.VrSceneView;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.EngineInstance;
import com.google.sceneform.rendering.ThreadPools;
import com.google.sceneform.utilities.SceneformBufferUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 场景布局控件
 * @version 1.0
 * <br/>SampleCode:<br/>
 * <code>
 *
 * </code>
 **/
public class SceneLayout extends FrameLayout{
    /**
     * 是否采用ExSceneView替代SceneView
     */
    private SceneViewType sceneViewType = SceneViewType.BASE;

    private LifecycleListener lifecycleListener;

    protected Context context;
    /**
     * 场景视图
     */
    SceneView sceneView;

    //场景根节点
    private RootNode rootNode;

    //手势检测器
    private GestureDetector gestureDetector;

    //<editor-fold> 构造函数
    public SceneLayout(Context context) {
        super(context);
    }

    public SceneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SceneLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SceneLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    //</editor-fold>

    /**
     * 初始化控件
     * @param context 上下文
     */
    public SceneLayout init(Context context){
        Eqr.getCoreStatus();
        this.context = context;

        //添加布局
        addLayout();

        rootNode = new RootNode();
        rootNode.setParent(sceneView.getScene());
        return this;
    }

    /**
     * 添加布局
     * @return SceneView
     */
    protected void addLayout() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        layoutParams.gravity = Gravity.CENTER;
        switch (sceneViewType){
            case BASE:
                sceneView = new SceneView(context);
                break;
            case EXTENSION:
                sceneView = new ExSceneView(context);
                setTransparent(false);
                break;
            case CAMERA:
                sceneView = new CameraSceneView(context);
                setTransparent(false);
                break;
            case VR:
                sceneView = new VrSceneView(context);
                break;
        }
        sceneView.setLayoutParams(layoutParams);
        this.addView(sceneView);
    }

    /**
     * 添加默认的环境照明
     */
    public void addDefaultIndirectLight() {
        //载入环境光
        try {
            //使用KTXLoader加载环境光
            InputStream inputStream = context.getAssets().open("enviroments/light/lightroom_ibl.ktx");
            ByteBuffer byteBuffer = SceneformBufferUtils.readStream(inputStream);
            inputStream.close();
            if (byteBuffer != null && sceneView.getRenderer() != null){
                Engine engine = EngineInstance.getEngine().getFilamentEngine();

                //filament版本由1.53.4升级至1.67.1，接口变更
                IndirectLight light = KTX1Loader.INSTANCE
                        .createIndirectLight(engine, byteBuffer,new KTX1Loader.Options()).getIndirectLight();
                if (light != null) {
                    light.setIntensity(100);
                }
                setIndirectLight(light);
            }
        } catch (IOException e) {
            throw new IllegalStateException("*.ktx was not found.");
        }
    }

    /**
     * 添加间接光
     * @param assetsFilePath 文件路径
     * @param intensity 光强度
     */
    public void addIndirectLight(String assetsFilePath, int intensity) {
        //载入环境光
        try {
            //使用KTXLoader加载环境光
            InputStream inputStream = context.getAssets().open(assetsFilePath);
            ByteBuffer byteBuffer = SceneformBufferUtils.readStream(inputStream);
            inputStream.close();
            if (byteBuffer != null && sceneView.getRenderer() != null){
                Engine engine = EngineInstance.getEngine().getFilamentEngine();

//                IndirectLight light = KTX1Loader.INSTANCE
//                        .createIndirectLight(engine, byteBuffer,new KTX1Loader.Options());
                //filament版本由1.53.4升级至1.67.1，接口变更
                IndirectLight light = KTX1Loader.INSTANCE
                        .createIndirectLight(engine, byteBuffer, new KTX1Loader.Options()).getIndirectLight();
                if (light != null) {
                    light.setIntensity(intensity);
                }
                setIndirectLight(light);
            }
        } catch (IOException e) {
            throw new IllegalStateException("*.ktx was not found.");
        }
    }

    /**
     * 获取场景中的相机实体
     * @return
     */
    public Camera getCamera(){
        return sceneView.getScene().getCamera();
    }

    /**
     * 获取场景中的根节点
     * @return
     */
    public RootNode getRootNode(){
        return rootNode;
    }

    /**
     * 移除节点
     * @param node
     */
    public void removeNode(Node node){
        List<Node> children = node.getChildren();
        if (children.size() == 0){
            //不移除根节点
            if (node instanceof RootNode)return;

            if(node.getRenderableInstance() != null){
                //销毁node节点的图形资源占用
                node.getRenderableInstance().destroy();
            }
            node.setParent(null);
            node.setEnabled(false);
            return;
        }
        while (children.size()!=0){
            removeNode(children.get(0));
        }
    }

    /**
     * 唤醒
     */
    public void resume() {
        if (sceneView ==null){
            return;
        }

        try {
            sceneView.resume();
        } catch (Exception e) {
            Log.e(SceneLayout.class.getSimpleName(), "onResume: ", e);
        }finally {
            if (lifecycleListener != null)
                lifecycleListener.onResume();
        }
    }

    /**
     * 暂停
     */
    public void pause(){
        if (sceneView !=null) {
            sceneView.pause();
        }
        if (lifecycleListener != null)
            lifecycleListener.onPause();
    }

    /**
     * 销毁
     */
    public void destroy(){
        if (sceneView !=null) {
            //deleteNode(rootNode);
            sceneView.destroy();
        }
        if (lifecycleListener != null)
            lifecycleListener.onDestroy();
    }

    /**
     * 添加场景更新监听事件
     * @param onUpdateListener 更新监听事件
     */
    public void addSceneUpdateListener(Scene.OnUpdateListener onUpdateListener){
        if (onUpdateListener == null)return;
        sceneView.getScene().addOnUpdateListener(onUpdateListener);
    }

    /**
     * 移除场景更新监听事件
     * @param onUpdateListener
     */
    public void removeSceneUpdateListener(Scene.OnUpdateListener onUpdateListener){
        if (onUpdateListener == null)return;
        sceneView.getScene().removeOnUpdateListener(onUpdateListener);
    }

    /**
     * 设置视图是否为背景透明
     * @param transparent boolean
     */
    public void setTransparent(boolean transparent){
        if (this instanceof ARSceneLayout){
            throw new NotSupportException("The method was not supported.");
        }
        sceneView.setTransparent(transparent);
    }

    /**
     * 设置生命周期监听事件
     * @param lifecycleListener
     */
    protected void setLifecycleListener(LifecycleListener lifecycleListener){
        this.lifecycleListener = lifecycleListener;
    }

    /**
     * 获取某位置朝向相机时的元素的旋转角度
     * @param position
     * @return
     */
    public Quaternion getLookRotation(Vector3 position) {
        final Vector3 cameraPosition = sceneView.getScene().getCamera().getWorldPosition();
        Vector3 direction = Vector3.subtract(cameraPosition, position);
        return Quaternion.lookRotation(direction, Vector3.up());
    }

    /**
     * 设置间接光
     * <p>
     *     间接光会产生一个照明,这些照明时从场景中其它物体上反射而形成的。
     *     该节点会向场景中添加间接光,不会使用光线跟踪。
     * </p>
     * @param light
     */
    public void setIndirectLight(IndirectLight light) {
        Objects.requireNonNull(this.sceneView.getRenderer()).setIndirectLight(light);
    }

    /**
     * 获取间接光对象
     * @return 间接光
     */
    public IndirectLight getIndirectLight(){
        return Objects.requireNonNull(this.sceneView.getRenderer()).getIndirectLight();
    }

    /**
     * 添加天空盒
     * @param assetsPath Assets目录下ktx文件路径
     *                   如："enviroments/pillars_2k_skybox.ktx"
     */
    public void setSkybox(String assetsPath) {
        //载入环境光
        try {
            //使用KTXLoader加载环境光
            InputStream inputStream = context.getAssets().open(assetsPath);
            ByteBuffer byteBuffer = SceneformBufferUtils.readStream(inputStream);
            inputStream.close();
            if (byteBuffer != null && sceneView.getRenderer() != null){
                Engine engine = EngineInstance.getEngine().getFilamentEngine();

//                Skybox skybox = KTX1Loader.INSTANCE.createSkybox(engine,
//                        byteBuffer, new KTX1Loader.Options());
//                setSkybox(skybox);

                //filament版本由1.53.4升级至1.67.1，接口变更
                KTX1Loader.SkyboxBundle skyboxBundle = KTX1Loader.INSTANCE.createSkybox(engine,
                        byteBuffer, new KTX1Loader.Options());
                setSkybox(skyboxBundle.getSkybox());
            }
        } catch (IOException e) {
            throw new IllegalStateException("*.ktx was not found.");
        }
    }

    /**
     * 设置天空盒
     * @param skybox 天空盒
     */
    public void setSkybox(Skybox skybox){
        Objects.requireNonNull(this.sceneView.getRenderer()).setSkybox(skybox);
    }

    /**
     * 获取天空盒
     * @return 天空盒
     */
    public Skybox getSkybox(){
        return Objects.requireNonNull(this.sceneView.getRenderer()).getSkybox();
    }

    /**
     * 设置场景视图的类型
     * <p>在{@link #init(Context)}之前调用</p>
     */
    public SceneLayout setSceneViewType(SceneViewType type){
         this.sceneViewType = type;
         return this;
    }

    /**
     * 获取场景视图
     * @return 场景视图
     */
    public SceneView getSceneView(){
        return this.sceneView;
    }

    /**
     * 截屏
     * @param folderPath 截屏结果保存的文件夹路径
     * @param crop 是否裁剪，当实际画面超过布局视图的尺寸时，将进行裁剪
     * @param completeCallback 回调事件
     * @return 返回保存的文件名
     */
    public void captureScreen(String folderPath, boolean crop, CompleteCallback completeCallback) {
        folderPath = /*去掉头尾空白的*/folderPath.trim();

        if (folderPath.endsWith("/") || folderPath.endsWith("\\")){
            //去掉最后一个字符
            folderPath = folderPath.substring(0,folderPath.length() - 1);
        }
        File file = new File(folderPath);

        @SuppressLint("SimpleDateFormat")
        final String filePath = folderPath + "/" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".jpeg";

        int sceneViewWidth = sceneView.getWidth();
        int sceneViewHeight = sceneView.getHeight();
        int layoutWidth = SceneLayout.this.getWidth();
        int layoutHeight = SceneLayout.this.getHeight();

        {
            if (!file.exists()){
                file.mkdirs();
            }

            final Bitmap tmpBm= Bitmap.createBitmap(sceneViewWidth, sceneViewHeight, Bitmap.Config.ARGB_8888);

            if (crop && (sceneViewWidth > layoutWidth || sceneViewHeight > layoutHeight)){
                //需要进行尺寸校正，cameraSceneView中用到
                int left = (sceneViewWidth - layoutWidth) / 2;
                int top = (sceneViewHeight - layoutHeight) / 2;
                PixelCopy.request(sceneView, tmpBm, new PixelCopy.OnPixelCopyFinishedListener() {
                    @Override
                    public void onPixelCopyFinished(int copyResult) {
                        if (copyResult == PixelCopy.SUCCESS){
                            //Step-1 裁剪图片
                            Bitmap croppedBitmap = Bitmap.createBitmap(tmpBm, left, top, layoutWidth, layoutHeight);
                            try {
                                //Step-2 将位图写入jpg格式
                                File file = new File(filePath);
                                FileOutputStream fos = new FileOutputStream(file);
                                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100,fos);

                                //Step-3 读写jpg的EXIF信息
                                ExifInterface exifInterface = new ExifInterface(filePath);
                                exifInterface.setAttribute(ExifInterface.TAG_COPYRIGHT,"Sceneform-EQR");
                                //desc-保存
                                exifInterface.saveAttributes();

                                fos.close();
                                //成功回调
                                completeCallback.onSuccess(filePath);
                            } catch (IOException e) {
                                Log.e("Capture Action", "onPixelCopyFinished  FileNotFoundException: ");
                                //失败回调
                                completeCallback.onFailed(e.getMessage());
                            }finally {
                                tmpBm.recycle();
                                croppedBitmap.recycle();
                            }
                        }else {
                            tmpBm.recycle();
                            completeCallback.onFailed("CopyResult=" + copyResult);
                        }
                    }
                },getHandler());
            }else {
                PixelCopy.request(sceneView, tmpBm, new PixelCopy.OnPixelCopyFinishedListener() {
                    @Override
                    public void onPixelCopyFinished(int copyResult) {
                        if (copyResult == PixelCopy.SUCCESS){
                            try {
                                //Step-2 将位图写入jpg格式
                                File file = new File(filePath);
                                FileOutputStream fos = new FileOutputStream(file);
                                tmpBm.compress(Bitmap.CompressFormat.JPEG, 100,fos);

                                //Step-3 读写jpg的EXIF信息
                                ExifInterface exifInterface = new ExifInterface(filePath);
                                exifInterface.setAttribute(ExifInterface.TAG_COPYRIGHT,"Sceneform-EQR");
                                //desc-保存
                                exifInterface.saveAttributes();

                                fos.close();
                                //成功回调
                                completeCallback.onSuccess(filePath);
                            } catch (IOException e) {
                                Log.e("Capture Action", "onPixelCopyFinished  FileNotFoundException: ");
                                //失败回调
                                completeCallback.onFailed(e.getMessage());
                            }finally {
                                tmpBm.recycle();
                            }
                        }else {
                            tmpBm.recycle();
                            completeCallback.onFailed("CopyResult=" + copyResult);
                        }
                    }
                },getHandler());
            }
        }
//        ThreadPools.getThreadPoolExecutor().execute(()->);
    }
}
