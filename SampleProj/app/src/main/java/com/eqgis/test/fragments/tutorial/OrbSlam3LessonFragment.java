package com.eqgis.test.fragments.tutorial;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eqgis.test.R;

/**
 * ORB-SLAM3 集成说明教程
 * <pre>
 *     ORB-SLAM3 相关实现受 GPL 许可约束，当前主线 Demo 不打包运行功能。
 *     本页面通过 ScrollView 展示 GPL 分支的布局实现、可用 AAR 和 eq-slam 源码地址，
 *     所有地址均可直接点击并由系统浏览器打开。
 * </pre>
 * @author tanyx
 */
public class OrbSlam3LessonFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_orb_slam3_lesson, container, false);
        enableLink(root, R.id.orbSlamLayoutLink);
        enableLink(root, R.id.orbSlamAarLink);
        enableLink(root, R.id.orbSlamSourceLink);
        return root;
    }

    private void enableLink(View root, int viewId) {
        TextView linkView = root.findViewById(viewId);
        linkView.setMovementMethod(LinkMovementMethod.getInstance());
        linkView.setLinksClickable(true);
    }
}
