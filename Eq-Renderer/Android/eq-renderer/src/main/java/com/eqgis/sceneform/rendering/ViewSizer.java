package com.eqgis.sceneform.rendering;

import android.view.View;

import com.eqgis.sceneform.math.Vector3;
import com.eqgis.sceneform.Node;
import com.eqgis.sceneform.Scene;

/**
 * 接口用于控制{@link Scene}中{@link ViewRenderable}的大小。
 * 视图显示的最终大小将是{@link ViewSizer}的大小，
 * 由{@link ViewRenderable}所附加的{@link Node}的{@link Node#getWorldScale()}缩放。
 */

public interface ViewSizer {
  /**
   * 计算{@link Scene}中所需的视图大小。
   * <p>
   *     {@link Vector3#x}表示宽度，{@link Vector3#y}表示高度。
   * </p>
   * @param view 视图
   * @return Vector3
   */
  Vector3 getSize(View view);
}
