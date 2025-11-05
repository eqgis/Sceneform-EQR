package com.eqgis.test.samples;

import android.os.Bundle;

import com.eqgis.eqr.layout.EqSlamSceneLayout;
import com.eqgis.test.BaseActivity;
import com.eqgis.test.R;
import com.eqgis.test.scene.GltfSampleScene;

/**
 * SLAM 场景示例 Activity
 * <p>
 *     本示例展示了如何使用 {@link EqSlamSceneLayout} 创建一个基于 ORB-SLAM3实现的三维空间场景，
 *     结合设备的相机输入与空间追踪，实现真实空间中的虚拟物体叠加。
 * </p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *     <li>启用 SLAM 模式的三维场景布局 {@link EqSlamSceneLayout}</li>
 *     <li>通过 {@link GltfSampleScene} 加载 GLTF 模型资源</li>
 *     <li>可扩展控制特征点绘制（示例中默认开启）</li>
 * </ul>
 *
 * <h3>使用说明</h3>
 * <p>
 *     运行本 Activity 后，系统将初始化 SLAM 场景布局；
 *     程序默认启用全屏显示，并加载 {@link GltfSampleScene} 示例模型。
 *     若需关闭特征点绘制，可通过：
 *     <pre>
 *         ((EqSlamSceneLayout) sceneLayout).setDrawPoints(false);
 *     </pre>
 *     进行控制。
 * </p>
 *
 * <h3>布局文件</h3>
 * <p>对应布局：{@code res/layout/activity_slam_scene.xml}</p>
 *
 * <h3>AAR依赖：</h3>
 * <pre>
 * libs/
 * └── eq-slam-1.0.x.aar : 是EQ基于“开源ORB-SLAM3”封装的安卓依赖库，适用于特定安卓设备(需要相机标定)。
 * </pre>
 */
public class SlamSceneActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //启用全屏
        EqSlamSceneLayout.setFullScreenEnable(this,true);

        setContentView(R.layout.activity_slam_scene);
        sceneLayout = findViewById(R.id.slam_scene_layout);
        sceneLayout.init(this);
        /*EqSlamSceneLayout eqSlamSceneLayout = (EqSlamSceneLayout) sceneLayout;
        //关闭特征点绘制
        eqSlamSceneLayout.setDrawPoints(false);*/
        //加载GLTF模型
        sampleScene = new GltfSampleScene();
        sampleScene.create(this,sceneLayout.getRootNode());
    }
}