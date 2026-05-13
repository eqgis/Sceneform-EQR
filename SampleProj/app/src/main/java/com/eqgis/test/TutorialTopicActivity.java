package com.eqgis.test;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eqgis.test.fragments.SampleFragmentFactory;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * 教程主题承载页
 * <pre>
 *     MainActivity 传入 topicId 后，本页面加载对应主题下的功能列表。
 *     每个功能示例由独立 Fragment 承载，切换功能时销毁旧 Fragment 并创建新 Fragment。
 * </pre>
 * @author tanyx
 */
public class TutorialTopicActivity extends AppCompatActivity implements LessonAdapter.OnLessonClickListener {
    private SampleTopic topic;
    private Fragment currentFragment;
    private String currentLessonId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_host);

        String topicId = getIntent().getStringExtra(SampleCatalog.EXTRA_TOPIC_ID);
        topic = SampleCatalog.findTopic(topicId);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(topic.getTitle());
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.lessonRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new LessonAdapter(topic.getLessons(), this));

        if (!topic.getLessons().isEmpty()) {
            showLesson(topic.getLessons().get(0), true);
        }
    }

    /**
     * 切换当前教程功能
     * @param lesson {@link SampleLesson} 当前教程功能信息
     */
    @Override
    public void onLessonClick(SampleLesson lesson) {
        showLesson(lesson, false);
    }

    /**
     * 创建并展示功能 Fragment
     * @param lesson {@link SampleLesson} 当前教程功能信息
     * @param force 是否强制展示
     */
    private void showLesson(SampleLesson lesson, boolean force) {
        if (!force && lesson.getId().equals(currentLessonId)) {
            return;
        }
        currentLessonId = lesson.getId();
        currentFragment = SampleFragmentFactory.create(lesson);
        //desc- 教程功能采用一个 Fragment 一个功能，切换 Tab 时必须销毁旧 Fragment 并创建新 Fragment。
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, currentFragment)
                .commitNowAllowingStateLoss();
    }
}
