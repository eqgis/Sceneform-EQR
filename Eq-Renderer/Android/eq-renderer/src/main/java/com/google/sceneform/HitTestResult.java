package com.google.sceneform;

import androidx.annotation.Nullable;

import com.google.sceneform.collision.RayHit;

/**
 * 点击测试结果
 * <p>存储对Scene调用的结果。hitTest和Scene.hitTestAll。包含命中测试命中的节点及其相关信息。</p>
 */
public class HitTestResult extends RayHit {
  @Nullable private Node node;

  /** @hide */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public void setNode(@Nullable Node node) {
    this.node = node;
  }

  /**
   * 获取被命中测试的节点。没有命中时为空。
   *
   * @return 被点中的节点对象
   */
  @Nullable
  public Node getNode() {
    return node;
  }

  /** @hide */
  public void set(HitTestResult other) {
    super.set(other);
    setNode(other.node);
  }

  /** @hide */
  @Override
  public void reset() {
    super.reset();
    node = null;
  }
}
