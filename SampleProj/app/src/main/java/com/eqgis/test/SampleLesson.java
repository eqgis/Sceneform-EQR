package com.eqgis.test;

/**
 * 功能示例数据
 * <pre>
 *     用于 CommonSamplesActivity 和 TutorialTopicActivity 的二级功能列表。
 *     常用示例可配置 activityClass 跳转到原 Activity，教程示例则通过 id 映射到对应 Fragment。
 * </pre>
 * @author tanyx
 */
public class SampleLesson {
    private final String id;
    private final String title;
    private final String description;
    private final String codeLocation;
    private final String previewAssetPath;
    private final boolean arRequired;
    private final Class<?> activityClass;

    /**
     * 构造函数
     * @param id 功能唯一 id
     * @param title 功能标题
     * @param description 功能说明
     * @param codeLocation 示例代码位置
     */
    public SampleLesson(String id, String title, String description, String codeLocation) {
        this(id, title, description, codeLocation, null, false, null);
    }

    /**
     * 构造函数
     * @param id 功能唯一 id
     * @param title 功能标题
     * @param description 功能说明
     * @param codeLocation 示例代码位置
     * @param arRequired 是否需要 AR 能力
     */
    public SampleLesson(String id, String title, String description, String codeLocation, boolean arRequired) {
        this(id, title, description, codeLocation, null, arRequired, null);
    }

    /**
     * 构造函数
     * @param id 功能唯一 id
     * @param title 功能标题
     * @param description 功能说明
     * @param codeLocation 示例代码位置
     * @param activityClass 原示例 Activity 类型
     */
    public SampleLesson(String id, String title, String description, String codeLocation, Class<?> activityClass) {
        this(id, title, description, codeLocation, null, false, activityClass);
    }

    public SampleLesson(String id, String title, String description, String codeLocation, String previewAssetPath, Class<?> activityClass) {
        this(id, title, description, codeLocation, previewAssetPath, false, activityClass);
    }

    /**
     * 构造函数
     * @param id 功能唯一 id
     * @param title 功能标题
     * @param description 功能说明
     * @param codeLocation 示例代码位置
     * @param arRequired 是否需要 AR 能力
     * @param activityClass 原示例 Activity 类型
     */
    public SampleLesson(String id, String title, String description, String codeLocation, boolean arRequired, Class<?> activityClass) {
        this(id, title, description, codeLocation, null, arRequired, activityClass);
    }

    public SampleLesson(String id, String title, String description, String codeLocation, String previewAssetPath, boolean arRequired, Class<?> activityClass) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.codeLocation = codeLocation;
        this.previewAssetPath = previewAssetPath;
        this.arRequired = arRequired;
        this.activityClass = activityClass;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCodeLocation() {
        return codeLocation;
    }

    public String getPreviewAssetPath() {
        return previewAssetPath;
    }

    public boolean isArRequired() {
        return arRequired;
    }

    public Class<?> getActivityClass() {
        return activityClass;
    }
}
