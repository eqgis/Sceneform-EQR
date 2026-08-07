package com.eqgis.test;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.eqgis.eqr.core.Eqr;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * 示例与教程唯一入口页
 * <pre>
 *     当前页面只负责展示一级入口，不直接承载具体渲染逻辑。
 *     首页仅保留“常用示例”和“开发教程”，具体功能继续进入二级页面选择。
 * </pre>
 * @author tanyx
 */
public class MainActivity extends AppCompatActivity implements SampleAdapter.OnSampleClickListener {
    private static final String REPOSITORY_URL = "https://github.com/eqgis/Sceneform-EQR";

    private RecyclerView recyclerView;
    private SampleAdapter adapter;
    private final List<SampleItem> sampleList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestBasePermissions();
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        loadSamples();

        TextView versionInfo = findViewById(R.id.versionInfo);
        if (Eqr.getCoreStatus()) {
            versionInfo.setText("Sceneform-EQR：" + Eqr.getCoreVersion());
        } else {
            Toast.makeText(this, "渲染器不可用", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 申请示例工程基础权限
     */
    private void requestBasePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET
                },
                PackageManager.PERMISSION_GRANTED);
    }

    /**
     * 初始化入口页视图
     */
    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        MaterialCardView repositoryCard = findViewById(R.id.cardRepository);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        setSupportActionBar(toolbar);
        repositoryCard.setOnClickListener(view -> openRepository());
        swipeRefresh.setOnRefreshListener(() -> {
            loadSamples();
            swipeRefresh.setRefreshing(false);
        });
    }

    /** 打开当前示例工程对应的 GitHub 仓库。 */
    private void openRepository() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(REPOSITORY_URL));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, "未找到可打开 GitHub 链接的应用", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 配置系列与主题列表
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new SampleAdapter(sampleList, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 加载入口数据
     */
    private void loadSamples() {
        sampleList.clear();
        sampleList.addAll(SampleCatalog.mainEntries());
        adapter.notifyDataSetChanged();
    }

    /**
     * 处理入口卡片点击
     * @param sampleItem {@link SampleItem} 入口数据
     */
    @Override
    public void onSampleClick(SampleItem sampleItem) {
        Intent intent = new Intent(this, sampleItem.getActivityClass());
        if (sampleItem.getTopicId() != null) {
            intent.putExtra(SampleCatalog.EXTRA_TOPIC_ID, sampleItem.getTopicId());
        }
        startActivity(intent);
    }
}
