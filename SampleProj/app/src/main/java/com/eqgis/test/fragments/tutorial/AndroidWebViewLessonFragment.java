package com.eqgis.test.fragments.tutorial;

import android.graphics.Color;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.sceneform.math.Quaternion;
import com.google.sceneform.math.Vector3;

/**
 * WebView 三维渲染教程
 * <pre>
 *     使用本地 HTML 创建多个 WebView 卡片，不申请网络权限即可观察网页内容的三维渲染效果。
 * </pre>
 * @author tanyx
 */
public class AndroidWebViewLessonFragment extends BaseAndroidViewLessonFragment {
    @Override
    protected String getLessonTitle() {
        return "WebView 渲染";
    }

    @Override
    protected String getLessonDescription() {
        return "把本地 HTML 页面渲染成三维卡片；示例关闭 JavaScript，并在页面销毁时主动释放 WebView。";
    }

    /**
     * 创建多个 WebView 示例
     */
    @Override
    protected void addViewExamples() {
        WebView statusView = createWebCard(
                "设备状态",
                "连接正常 · 48 FPS",
                "#007AFF",
                "#EAF3FF");
        addViewRenderable(
                statusView,
                new Vector3(-0.72f, 0.12f, -2.85f),
                new Quaternion(Vector3.up(), 8),
                0.82f);

        WebView guideView = createWebCard(
                "场景提示",
                "Android HTML 已进入 3D 空间",
                "#34C759",
                "#ECF9F0");
        addViewRenderable(
                guideView,
                new Vector3(0.72f, 0.12f, -2.85f),
                new Quaternion(Vector3.up(), -8),
                0.82f);
    }

    private WebView createWebCard(String title, String content, String accentColor, String backgroundColor) {
        WebView webView = new WebView(requireContext());
        webView.getSettings().setJavaScriptEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayoutParams(new ViewGroup.LayoutParams(dp(270), dp(150)));
        String html = "<!doctype html><html><head><meta name=\"viewport\" content=\"width=device-width\">"
                + "<style>body{margin:0;padding:16px;font-family:sans-serif;background:" + backgroundColor + ";"
                + "color:#1D1D1F;border:1px solid #D2D2D7;border-radius:16px;box-sizing:border-box;}"
                + "h3{margin:0 0 12px;color:" + accentColor + ";font-size:20px;}"
                + "p{margin:0;font-size:15px;line-height:1.5;color:#3A3A3C;}</style></head>"
                + "<body><h3>" + title + "</h3><p>" + content + "</p></body></html>";
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        return webView;
    }
}
