package com.eqgis.test.fragments;

import com.eqgis.test.SampleLesson;

/**
 * 教程功能宿主接口
 * <pre>
 *     用于可以在同一个 Fragment 内切换不同功能内容的宿主。
 *     当前教程页默认使用独立 Fragment 模式，该接口保留给后续特殊主题复用。
 * </pre>
 * @author tanyx
 */
public interface LessonHostFragment {
    /**
     * 展示指定功能示例
     * @param lesson {@link SampleLesson} 功能示例数据
     */
    void showLesson(SampleLesson lesson);
}
