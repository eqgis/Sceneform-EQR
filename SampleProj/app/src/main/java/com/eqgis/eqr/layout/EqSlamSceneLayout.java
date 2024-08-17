package com.eqgis.eqr.layout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import com.eqgis.slam.core.SlamCore;
import com.eqgis.slam.core.TrackingState;
import com.eqgis.slam.listener.OnPlaneUpdateListener;
import com.eqgis.slam.listener.OnPoseUpdateListener;
import com.eqgis.slam.listener.OnStateUpdateListener;
import com.eqgis.sceneform.Camera;
import com.eqgis.sceneform.SceneView;
import com.eqgis.sceneform.math.Quaternion;
import com.eqgis.sceneform.math.Vector3;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;

/**
 * 单目相机SLAM的场景视图控件
 * <p></p>
 * <pre>SampleCode:
 *  public class TestSlam2Activity extends Activity {
 *
 *     private EqSlamSceneLayout sceneLayout;
 *     @Override
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 *         ActivityCompat.requestPermissions(this,new String[]{
 *                 Manifest.permission.CAMERA,
 *                 Manifest.permission.READ_EXTERNAL_STORAGE,
 *                 Manifest.permission.WRITE_EXTERNAL_STORAGE
 *         },0);
 *
 *         //启用全屏模式
 *         EqSlamSceneLayout.setFullScreenEnable(this,true);
 *
 *         setContentView(R.layout.activity_test_slam2);
 *
 *         sceneLayout = findViewById(R.id.eq_slam_layout);
 *         loadModels();
 *     }
 *
 *     @Override
 *     protected void onResume() {
 *         super.onResume();
 *         sceneLayout.resume();
 *     }
 *
 *     @Override
 *     protected void onPause() {
 *         super.onPause();
 *         sceneLayout.pause();
 *     }
 *
 *     @Override
 *     protected void onDestroy() {
 *         super.onDestroy();
 *         sceneLayout.destroy();
 *     }
 *
 *     public void loadModels() {
 *         RootNode rootNode = sceneLayout.getRootNode();
 *         ModelRenderable
 *                 .builder()
 *                 .setSource(this
 *                         , Uri.parse("models/c.glb"))
 *                 .setIsFilamentGltf(true)
 *                 .setAsyncLoadEnabled(true)
 *                 .build()
 *                 .thenApply(new Function<ModelRenderable, Object>() {
 *                     @Override
 *                     public Object apply(ModelRenderable modelRenderable) {
 *                         Node modelNode1 = new Node();
 *                         modelNode1.setRenderable(modelRenderable);
 *                         modelNode1.setLocalScale(new Vector3(0.05f, 0.05f, 0.05f));
 *                         modelNode1.setLocalPosition(new Vector3(0f, 0f, -0.2f));
 *                         rootNode.addChild(modelNode1);
 *                         return null;
 *                     }
 *                 });
 *
 *     }
 * }
 * </pre>
 *
 * @author tanyx 2024/1/23
 * @version 1.0
 **/
public class EqSlamSceneLayout extends SceneLayout{

    private static boolean fullScreen = false;
    private CameraBridgeViewBase cameraView;
    private Mat frame;
    private boolean start = false;
    private TrackingState trackingState;
    private int pointColor = Color.rgb(28,188,211);;

    private boolean drawPoints = true;

    private int layoutWidth,layoutHeight;

    //屏幕旋转方向
    private int screenRotation = 1;
    private SlamCore slam;

    private Camera camera;

    /**
     * CV的相机监听
     */
    private CameraBridgeViewBase.CvCameraViewListener2 cameraListener = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) {
            //Frame的width和height
            //同步sceneView的尺寸
            LayoutParams layoutParams = new LayoutParams(width,height);
            layoutParams.gravity = Gravity.CENTER;
            sceneView.setLayoutParams(layoutParams);
            cameraView.setLayoutParams(layoutParams);
            if (fullScreen){
                //计算缩放系数
                float scaleX = (float) layoutWidth / width;
                float scaleY = (float) layoutHeight / height;
                float scale = Math.max(scaleX,scaleY);
                if (scale > 1.0f){
                    cameraView.setScaleX(scale);
                    cameraView.setScaleY(scale);
                    sceneView.setScaleX(scale);
                    sceneView.setScaleY(scale);
                }
            }

            //更新状态
            start = true;
            //重置图像处理系数
//            slam.resetFrameScale(Math.min());
//            Log.i("IKKYU", "TEST:onCameraViewStarted ");
        }

        @Override
        public void onCameraViewStopped() {
            start = false;
        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            frame = inputFrame.rgba();
            //判断横竖屏用于进行图像的旋转
            switch (screenRotation){
                case 1:
                    break;
                case 2:
                    Core.rotate(frame, frame, Core.ROTATE_90_COUNTERCLOCKWISE);
                    break;
                case 3:
                    Core.rotate(frame, frame, Core.ROTATE_180);
                    break;
                case 0:
                    Core.rotate(frame, frame, Core.ROTATE_90_CLOCKWISE);
                    break;
            }
//            Log.i(EqSlamSceneLayout.class.getSimpleName(), "onCameraFrame: ");
            if (slam != null && start){
                trackingState = slam.track(frame.getNativeObjAddr());

                if (drawPoints){
                    slam.drawKeyPoints(frame.getNativeObjAddr(),/*0xFF6200EE*/pointColor);
                }
            }
            return frame;
        }
    };

    /**
     * cvLoader回调
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(EqSlamSceneLayout.class.getSimpleName(), "OpenCV loaded successfully");
                    cameraView.setCvCameraViewListener(cameraListener);
                    cameraView.enableView();
//                    Log.i("IKKYU", "TEST:onManagerConnected ");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    //<editor-fold> - 构造函数
    public EqSlamSceneLayout(Context context) {
        super(context);
    }

    public EqSlamSceneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EqSlamSceneLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EqSlamSceneLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    //</editor-fold>

    @Override
    protected void addLayout() {
        sceneView = new SceneView(context);
        sceneView.setTransparent(true);

        cameraView = new JavaCameraView(context,CameraBridgeViewBase.CAMERA_ID_BACK);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        sceneView.setLayoutParams(layoutParams);
        cameraView.setLayoutParams(layoutParams);
        this.addView(cameraView);
        this.addView(sceneView);

        slam = new SlamCore(context).init(0.3);

        camera = sceneView.getScene().getCamera();
        slam.setOnPoseUpdateListener(new OnPoseUpdateListener() {
            @Override
            public void onUpdate(float[] viewMatrix, float[] cameraPose) {
                camera.updateTrackedPose(viewMatrix,cameraPose);
            }
        });
    }


    @SuppressLint({"DrawAllocation", "RtlHardcoded"})
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.layoutHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.layoutWidth = MeasureSpec.getSize(widthMeasureSpec);

        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        screenRotation = windowManager.getDefaultDisplay().getRotation();
//        Log.i("IKKYU", "TEST:onMeasure ");
    }

    @Override
    public void resume() {
        super.resume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(EqSlamSceneLayout.class.getSimpleName(), "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, context, mLoaderCallback);
        } else {
            Log.d(EqSlamSceneLayout.class.getSimpleName(), "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        //默认设为true
        cameraView.setCameraPermissionGranted();

        //同步投影矩阵
        sceneView.getScene().getCamera().updateProjectionMatrix(slam.calculateProjectionMatrix(),
                (float) slam.getNearClipPlane(), (float) slam.getFarClipPlane());

    }

    @Override
    public void pause() {
        super.pause();
        if (cameraView != null)
            cameraView.disableView();

    }

    @Override
    public void destroy() {
        super.destroy();
        if (cameraView != null)
            cameraView.disableView();
    }

    /**
     * 释放资源
     */
    public void release(){
        //释放SLAM系统
        slam.dispose();
    }

    /**
     * 显示FPS
     * <p>调试使用</p>
     */
    private void setFpsVisible(boolean visible){
        if (cameraView != null){
            if (visible){
                cameraView.enableFpsMeter();
            }else {
                cameraView.disableFpsMeter();
            }
        }
    }

    /**
     * 获取跟踪状态
     * @return
     */
    public TrackingState getTrackingState() {
        return trackingState;
    }

    /**
     * 设置全屏模式启用状态
     * <p>在resume之前执行</p>
     * @param enable 设置为true时，启用全屏
     */
    public static void setFullScreenEnable(Activity activity,boolean enable){
        fullScreen = enable;
        if (fullScreen){
            hideBottomUIMenu(activity);
            hideTopUI(activity);
        }
    }

    /**
     * 判断是否绘制特征点
     * @return boolean
     */
    public boolean isDrawPoints() {
        return drawPoints;
    }

    /**
     * 设置是否启用特征点绘制
     * @param drawPoints
     */
    public void setDrawPoints(boolean drawPoints) {
        this.drawPoints = drawPoints;
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    private static void hideBottomUIMenu(Activity activity) {
        int flags;
        int curApiVersion = Build.VERSION.SDK_INT;
        // This work only for android 4.4+
        if(curApiVersion >= Build.VERSION_CODES.KITKAT){
            // This work only for android 4.4+
            // hide navigation bar permanently in android activity
            // touch the screen, the navigation bar will not show
//            flags = View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
        }else{
            // touch the screen, the navigation bar will show
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
// must be executed in main thread :)
        activity.getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    /**
     * 隐藏顶部UI
     * @param activity
     */
    private static void hideTopUI(Activity activity){
        // 隐藏标题栏
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 设置跟踪状态更新监听事件
     * @param listener 监听事件
     */
    public void setTrackingStateUpdateListener(OnStateUpdateListener listener){
        slam.setOnStateUpdateListener(listener);
    }

    /**
     * 设置平面更新监听
     * @param listener 监听事件
     */
    public void setOnPlaneUpdateListener(OnPlaneUpdateListener listener){
        slam.setOnPlaneUpdateListener(listener);
    }

    /**
     * 检测平面
     */
    public boolean detectPlane(){
        return slam.detectPlane();
    }

    /**
     * 重置根节点的重力方向
     * @param accX X方向的加速度，向下
     * @param accY Y方向的加速度，向右
     * @param accZ Z方向的加速度，向前
     */
    public void resetRootGravity(float accX, float accY, float accZ){
        Vector3 accVector = new Vector3(accX,accY,accZ);
        float length = accVector.length();
        double cosY = Math.acos(accY / length);
        double cosZ = Math.acos(accZ / length);

        Quaternion rotationX = new Quaternion(Vector3.left(), (float) Math.toDegrees(cosZ) - 90);
        Quaternion rotationZ = new Quaternion(Vector3.forward(), (float) Math.toDegrees(cosY) - 90);
        //重置根节点
        getRootNode().setWorldRotation(Quaternion.multiply(rotationZ,rotationX));
    }
}
