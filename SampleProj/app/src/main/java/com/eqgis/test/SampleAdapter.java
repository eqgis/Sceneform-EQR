package com.eqgis.test;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eqgis.test.utils.AssetImageLoader;

import java.util.List;

/**
 * 示例一级入口适配器
 * <pre>
 *     负责渲染 MainActivity 中的系列和教程主题卡片。
 *     点击事件通过 OnSampleClickListener 回传给 Activity 处理跳转。
 * </pre>
 * @author tanyx
 */
public class SampleAdapter extends RecyclerView.Adapter<SampleAdapter.SampleViewHolder> {

    private final List<SampleItem> sampleList;
    private final OnSampleClickListener listener;

    /**
     * 示例入口点击回调
     */
    public interface OnSampleClickListener {
        /**
         * 点击示例入口
         * @param sampleItem {@link SampleItem} 被点击的入口数据
         */
        void onSampleClick(SampleItem sampleItem);
    }

    /**
     * 构造函数
     * @param sampleList 示例入口列表
     * @param listener 点击回调
     */
    public SampleAdapter(List<SampleItem> sampleList, OnSampleClickListener listener) {
        this.sampleList = sampleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sample, parent, false);
        return new SampleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SampleViewHolder holder, int position) {
        SampleItem item = sampleList.get(position);
        holder.txtTitle.setText(item.getTitle());
        holder.txtDescription.setText(item.getDescription());

        if (item.getBadge() == null || item.getBadge().length() == 0) {
            holder.txtBadge.setVisibility(View.GONE);
        } else {
            holder.txtBadge.setVisibility(View.VISIBLE);
            holder.txtBadge.setText(item.getBadge());
        }

        if (item.getImageAssetPath() != null && item.getImageAssetPath().length() > 0) {
            Bitmap bitmap = AssetImageLoader.loadBitmapFromAssets(holder.imgSample.getContext(), item.getImageAssetPath());
            if (bitmap != null) {
                holder.imgSample.setImageBitmap(bitmap);
            } else {
                holder.imgSample.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else if (item.getImageResId() != 0) {
            holder.imgSample.setImageResource(item.getImageResId());
        } else {
            holder.imgSample.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSampleClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sampleList.size();
    }

    /**
     * 示例入口 ViewHolder
     */
    static class SampleViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSample;
        TextView txtTitle;
        TextView txtDescription;
        TextView txtBadge;

        /**
         * 构造函数
         * @param itemView 列表项根视图
         */
        SampleViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSample = itemView.findViewById(R.id.imgSample);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtBadge = itemView.findViewById(R.id.txtBadge);
        }
    }
}
