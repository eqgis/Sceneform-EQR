package com.eqgis.eqr.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.eqgis.eqr.core.Eqr;
import com.eqgis.eqr.node.RootNode;
import com.eqgis.exception.NotSupportException;
import com.google.android.filament.Engine;
import com.google.android.filament.IndirectLight;
import com.google.android.filament.Skybox;
import com.google.android.filament.utils.KTX1Loader;
import com.google.sceneform.Camera;
import com.google.sceneform.Node;
import com.google.sceneform.Scene;
import com.google.sceneform.SceneView;
import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.EngineInstance;
import com.google.sceneform.utilities.SceneformBufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
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
    private LifecycleListener lifecycleListener;

    protected Context context;
    public SceneView sceneView;

    //场景根节点
    private RootNode rootNode;

    //手势检测器
    private GestureDetector gestureDetector;

    //<editor-fold> 构造函数
    public SceneLayout(Context context) {
        super(context);
        init(context);
    }

    public SceneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SceneLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SceneLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
    //</editor-fold>

    private void init(Context context){
        Eqr.getCoreStatus();
        this.context = context;

        //添加布局
        addLayout();

        rootNode = new RootNode();
        rootNode.setParent(sceneView.getScene());

        //添加默认间接光
        addIndirectLight();
    }

    /**
     * 添加布局
     * @return SceneView
     */
    protected void addLayout() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        sceneView = new SceneView(context);
        sceneView.setLayoutParams(layoutParams);
        this.addView(sceneView);
    }

    /**
     * 添加间接照明
     */
    protected void addIndirectLight() {
        //载入环境光
        try {
            //使用KTXLoader加载环境光
            InputStream inputStream = context.getAssets().open("enviroments/light/lightroom_ibl.ktx");
            ByteBuffer byteBuffer = SceneformBufferUtils.readStream(inputStream);
            inputStream.close();
            if (byteBuffer != null && sceneView.getRenderer() != null){
                Engine engine = EngineInstance.getEngine().getFilamentEngine();

                IndirectLight light = KTX1Loader.INSTANCE
                        .createIndirectLight(engine, byteBuffer,new KTX1Loader.Options());
                light.setIntensity(100);
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

                Skybox skybox = KTX1Loader.INSTANCE.createSkybox(engine,
                        byteBuffer, new KTX1Loader.Options());
                setSkybox(skybox);
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
}
