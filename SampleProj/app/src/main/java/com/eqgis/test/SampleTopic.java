package com.eqgis.test;

import java.util.List;

/**
 * 教程主题数据
 * <pre>
 *     一个主题对应 TutorialTopicActivity 中的一组功能 Fragment。
 *     主题只描述学习方向和功能列表，不直接持有渲染资源。
 * </pre>
 * @author tanyx
 */
public class SampleTopic {
    private final String id;
    private final String title;
    private final String description;
    private final List<SampleLesson> lessons;

    /**
     * 构造函数
     * @param id 主题唯一 id
     * @param title 主题标题
     * @param description 主题说明
     * @param lessons 主题下的功能示例列表
     */
    public SampleTopic(String id, String title, String description, List<SampleLesson> lessons) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.lessons = lessons;
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

    public List<SampleLesson> getLessons() {
        return lessons;
    }
}
