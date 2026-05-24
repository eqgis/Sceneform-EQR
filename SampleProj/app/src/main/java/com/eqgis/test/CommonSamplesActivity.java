package com.eqgis.test;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eqgis.test.fragments.SampleFragmentFactory;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

/**
 * 常用示例归档页
 * <pre>
 *     常用示例页采用 Fragment 展示标题、说明和代码位置，底部按钮跳转到原有 Activity。
 *     这样可以保持旧示例逻辑不变，同时把入口结构整理成统一的系列页。
 * </pre>
 * @author tanyx
 */
public class CommonSamplesActivity extends AppCompatActivity implements LessonAdapter.OnLessonClickListener {
    private List<SampleLesson> lessons;
    private String currentLessonId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_host);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("常用示例");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        lessons = SampleCatalog.commonLessons();
        RecyclerView recyclerView = findViewById(R.id.lessonRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new LessonAdapter(lessons, this));

        if (!lessons.isEmpty()) {
            showLesson(lessons.get(0), true);
        }
    }

    /**
     * 切换当前归档条目
     * @param lesson {@link SampleLesson} 当前常用示例信息
     */
    @Override
    public void onLessonClick(SampleLesson lesson) {
        showLesson(lesson, false);
    }

    /**
     * 展示常用示例说明 Fragment
     * @param lesson {@link SampleLesson} 当前常用示例信息
     * @param force 是否强制展示
     */
    private void showLesson(SampleLesson lesson, boolean force) {
        if (!force && lesson.getId().equals(currentLessonId)) {
            return;
        }
        currentLessonId = lesson.getId();
        //desc- Tab 切换必须 replace 当前 Fragment，让旧页面走 onDestroyView 释放资源。
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, SampleFragmentFactory.create(lesson))
                .commitNowAllowingStateLoss();
    }
}
