package com.eqgis.test;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * 功能示例列表适配器
 * <pre>
 *     用于 CommonSamplesActivity 和 TutorialTopicActivity 顶部横向功能列表。
 *     适配器维护选中位置，并在点击后通知宿主 Activity 切换 Fragment。
 * </pre>
 * @author tanyx
 */
public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private final List<SampleLesson> lessons;
    private final OnLessonClickListener listener;
    private int selectedPosition = 0;

    /**
     * 功能示例点击回调
     */
    public interface OnLessonClickListener {
        /**
         * 点击功能示例
         * @param lesson {@link SampleLesson} 被点击的功能示例
         */
        void onLessonClick(SampleLesson lesson);
    }

    /**
     * 构造函数
     * @param lessons 功能示例列表
     * @param listener 点击回调
     */
    public LessonAdapter(List<SampleLesson> lessons, OnLessonClickListener listener) {
        this.lessons = lessons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        SampleLesson lesson = lessons.get(position);
        holder.txtTitle.setText(lesson.getTitle());
        holder.txtDescription.setText(lesson.getDescription());
        bindSelectedState(holder, position == selectedPosition);
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION || adapterPosition == selectedPosition) {
                return;
            }
            int oldPosition = selectedPosition;
            selectedPosition = adapterPosition;
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            centerSelectedTab(holder.itemView);
            if (listener != null) {
                listener.onLessonClick(lessons.get(selectedPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    /**
     * 绑定 Tab 单选状态
     * @param holder {@link LessonViewHolder} 当前条目
     * @param selected 是否选中
     */
    private void bindSelectedState(@NonNull LessonViewHolder holder, boolean selected) {
        int selectedBackground = color(holder.itemView, R.color.sample_accent_soft);
        int normalBackground = color(holder.itemView, R.color.sample_surface);
        int selectedStroke = color(holder.itemView, R.color.sample_accent);
        int normalStroke = color(holder.itemView, R.color.sample_card_stroke);
        int selectedTitle = color(holder.itemView, R.color.sample_accent);
        int normalTitle = color(holder.itemView, R.color.sample_text_primary);
        int normalDescription = color(holder.itemView, R.color.sample_text_secondary);

        holder.itemView.setSelected(selected);
        holder.itemView.setAlpha(selected ? 1.0f : 0.86f);
        holder.txtTitle.setTextColor(selected ? selectedTitle : normalTitle);
        holder.txtDescription.setTextColor(normalDescription);
        if (holder.cardView != null) {
            holder.cardView.setCardBackgroundColor(selected ? selectedBackground : normalBackground);
            holder.cardView.setStrokeColor(selected ? selectedStroke : normalStroke);
            holder.cardView.setStrokeWidth(selected ? dp(holder.itemView, 2) : dp(holder.itemView, 1));
            holder.cardView.setCardElevation(0);
        }
    }

    /**
     * 读取当前主题下的颜色资源
     * @param view 当前视图
     * @param colorRes 颜色资源 id
     * @return 解析后的颜色值
     */
    private int color(@NonNull View view, int colorRes) {
        return ContextCompat.getColor(view.getContext(), colorRes);
    }

    /**
     * dp 转 px
     * @param view 当前视图
     * @param value dp 值
     * @return px 值
     */
    private int dp(View view, int value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }

    /**
     * 将选中的 Tab 平滑移动到横向列表中心
     * @param selectedView 当前选中的 Tab 视图
     */
    private void centerSelectedTab(@NonNull View selectedView) {
        if (!(selectedView.getParent() instanceof RecyclerView)) {
            return;
        }
        RecyclerView recyclerView = (RecyclerView) selectedView.getParent();
        recyclerView.post(() -> {
            int itemCenter = selectedView.getLeft() + selectedView.getWidth() / 2;
            int recyclerCenter = recyclerView.getWidth() / 2;
            int offset = itemCenter - recyclerCenter;
            //desc- 点击 Tab 后按中心差值滚动，使选中项尽量停在横向列表中间。
            recyclerView.smoothScrollBy(offset, 0);
        });
    }

    /**
     * 功能示例 ViewHolder
     */
    static class LessonViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView txtTitle;
        TextView txtDescription;

        /**
         * 构造函数
         * @param itemView 列表项根视图
         */
        LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            if (itemView instanceof MaterialCardView) {
                cardView = (MaterialCardView) itemView;
            }
            txtTitle = itemView.findViewById(R.id.txtLessonTitle);
            txtDescription = itemView.findViewById(R.id.txtLessonDescription);
        }
    }
}
