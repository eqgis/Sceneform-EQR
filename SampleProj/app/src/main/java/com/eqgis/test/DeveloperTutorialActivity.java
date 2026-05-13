package com.eqgis.test;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * 开发教程入口页
 * <pre>
 *     该页面承载所有教程主题入口，使用一行两个卡片的网格布局。
 *     点击主题后进入 TutorialTopicActivity，由主题页继续展示具体功能 Fragment。
 * </pre>
 * @author tanyx
 */
public class DeveloperTutorialActivity extends AppCompatActivity implements SampleAdapter.OnSampleClickListener {
    private RecyclerView recyclerView;
    private SampleAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private final List<SampleItem> tutorialEntries = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_guide);

        initViews();
        setupRecyclerView();
        loadTutorialEntries();
    }

    /**
     * 初始化教程入口页视图
     */
    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        toolbar.setTitle("开发教程");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        swipeRefresh.setOnRefreshListener(() -> {
            loadTutorialEntries();
            swipeRefresh.setRefreshing(false);
        });
    }

    /**
     * 配置教程主题网格
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new SampleAdapter(tutorialEntries, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 加载教程主题入口
     */
    private void loadTutorialEntries() {
        tutorialEntries.clear();
        tutorialEntries.addAll(SampleCatalog.tutorialEntries());
        adapter.notifyDataSetChanged();
    }

    /**
     * 处理教程主题点击
     * @param sampleItem {@link SampleItem} 教程主题入口数据
     */
    @Override
    public void onSampleClick(SampleItem sampleItem) {
        Intent intent = new Intent(this, TutorialTopicActivity.class);
        intent.putExtra(SampleCatalog.EXTRA_TOPIC_ID, sampleItem.getTopicId());
        startActivity(intent);
    }
}
