package com.eqgis.test.fragments.tutorial;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.eqgis.eqr.layout.SceneLayout;
import com.eqgis.eqr.listener.CompleteCallback;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.rendering.Color;

import java.io.File;

/**
 * 场景截图与导出进阶教程。
 * <p>使用 PixelCopy 截取 SceneView，将 JPEG 保存到应用缓存，并通过 FileProvider 安全分享。</p>
 * @author tanyx
 */
public class SceneCaptureExportLessonFragment extends BaseAdvancedLessonFragment {
    private TextView statusView;
    private Button shareButton;
    private File lastCaptureFile;

    @Override
    protected String getLessonTitle() {
        return "场景截图与导出";
    }

    @Override
    protected String getLessonDescription() {
        return "通过 SceneLayout.captureScreen() 使用 PixelCopy 导出场景 JPEG。文件保存到应用缓存，无需存储权限，并使用 FileProvider 授予接收应用临时读取权限。";
    }

    @Override
    protected void onSceneReady(SceneLayout sceneLayout) {
        configureAdvancedScene(sceneLayout, true);
        createCube(new Vector3(-0.75f, -0.15f, -3.0f), 0.65f,
                new Color(0.08f, 0.50f, 1.0f), node -> { });
        createSphere(new Vector3(0.55f, 0.0f, -2.8f), 0.48f,
                new Color(0.96f, 0.56f, 0.12f), node -> { });
        createCube(new Vector3(1.25f, -0.3f, -4.2f), 0.45f,
                new Color(0.15f, 0.78f, 0.50f), node -> { });
    }

    @Override
    protected void onActionsReady(LinearLayout actionContainer) {
        statusView = addPanelText(actionContainer, "等待截图。可先移动相机调整构图。"
        );
        addPanelButton(actionContainer, "截取可见区域").setOnClickListener(view -> captureScene(true));
        addPanelButton(actionContainer, "截取 SceneView 原始尺寸").setOnClickListener(view -> captureScene(false));
        shareButton = addPanelButton(actionContainer, "分享最近截图");
        shareButton.setEnabled(false);
        shareButton.setOnClickListener(view -> shareLastCapture());
        addPanelText(actionContainer,
                "crop=true 会在 SceneView 实际缓冲区大于布局时裁剪到可见区域。截图是异步操作，回调前不要销毁场景或复用目标 Bitmap。"
        );
    }

    private void captureScene(boolean crop) {
        if (!isSceneActive()) {
            return;
        }
        File baseDirectory = requireContext().getExternalCacheDir();
        if (baseDirectory == null) {
            baseDirectory = requireContext().getCacheDir();
        }
        File outputDirectory = new File(baseDirectory, "advanced_screenshots");
        statusView.setText("正在通过 PixelCopy 截图…");
        sceneLayout.captureScreen(outputDirectory.getAbsolutePath(), crop, new CompleteCallback() {
            @Override
            public void onSuccess(Object object) {
                if (!isSceneActive()) {
                    return;
                }
                lastCaptureFile = new File(String.valueOf(object));
                shareButton.setEnabled(lastCaptureFile.isFile());
                statusView.setText("截图成功：\n" + lastCaptureFile.getAbsolutePath()
                        + "\n大小：" + Math.max(1L, lastCaptureFile.length() / 1024L) + " KB");
            }

            @Override
            public void onFailed(String errorMessage) {
                if (isSceneActive()) {
                    statusView.setText("截图失败：" + errorMessage);
                }
            }
        });
    }

    private void shareLastCapture() {
        if (lastCaptureFile == null || !lastCaptureFile.isFile()) {
            return;
        }
        Uri uri = FileProvider.getUriForFile(
                requireContext(), "com.eqgis.eqr.sample.fileprovider", lastCaptureFile);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setClipData(ClipData.newRawUri("scene-capture", uri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "导出场景截图"));
    }

    @Override
    protected void onReleaseAdvancedScene() {
        statusView = null;
        shareButton = null;
        lastCaptureFile = null;
    }
}
