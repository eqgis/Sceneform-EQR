package com.eqgis.test.fragments;

import com.eqgis.eqr.layout.ARSceneLayout;
import com.eqgis.eqr.layout.SceneLayout;

/**
 * AR 渲染示例 Fragment 基类
 * <pre>
 *     在 BaseSampleFragment 的基础上创建 ARSceneLayout。
 *     子类可以通过 arSceneLayout 访问平面检测、Session 和 AR 根节点能力。
 * </pre>
 * @author tanyx
 */
public abstract class BaseArSampleFragment extends BaseSampleFragment {
    protected ARSceneLayout arSceneLayout;

    /**
     * 创建 AR 场景布局
     * @return 当前 Fragment 独立持有的 {@link ARSceneLayout}
     */
    @Override
    protected SceneLayout createSceneLayout() {
        arSceneLayout = new ARSceneLayout(requireContext());
        return arSceneLayout;
    }
}
