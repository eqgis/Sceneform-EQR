package com.eqgis.test;

/**
 * 示例入口数据
 * <pre>
 *     用于 MainActivity 的一级入口卡片，既可以表示常用示例系列，也可以表示某个教程主题。
 *     topicId 不为空时会传给 TutorialTopicActivity，用于加载指定教程主题。
 * </pre>
 * @author tanyx
 */
public class SampleItem {
    private String title;
    private String description;
    private String badge;
    private int imageResId;
    private String imageAssetPath;
    private Class<?> activityClass;
    private String topicId;

    /**
     * 构造函数
     * @param title 入口标题
     * @param description 入口说明
     * @param imageResId 封面资源 id
     * @param activityClass 点击后跳转的 Activity 类型
     */
    public SampleItem(String title, String description, int imageResId, Class<?> activityClass) {
        this(title, description, null, imageResId, activityClass, null);
    }

    /**
     * 构造函数
     * @param title 入口标题
     * @param description 入口说明
     * @param badge 入口角标
     * @param imageResId 封面资源 id
     * @param activityClass 点击后跳转的 Activity 类型
     * @param topicId 教程主题 id
     */
    public SampleItem(String title, String description, String badge, int imageResId, Class<?> activityClass, String topicId) {
        this(title, description, badge, imageResId, null, activityClass, topicId);
    }

    /**
     * 构造函数
     * @param title 入口标题
     * @param description 入口说明
     * @param badge 入口角标
     * @param imageResId 封面资源 id
     * @param imageAssetPath assets 下的封面路径
     * @param activityClass 点击后跳转的 Activity 类型
     * @param topicId 教程主题 id
     */
    public SampleItem(String title, String description, String badge, int imageResId, String imageAssetPath, Class<?> activityClass, String topicId) {
        this.title = title;
        this.description = description;
        this.badge = badge;
        this.imageResId = imageResId;
        this.imageAssetPath = imageAssetPath;
        this.activityClass = activityClass;
        this.topicId = topicId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getBadge() {
        return badge;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getImageAssetPath() {
        return imageAssetPath;
    }

    public Class<?> getActivityClass() {
        return activityClass;
    }

    public String getTopicId() {
        return topicId;
    }
}
