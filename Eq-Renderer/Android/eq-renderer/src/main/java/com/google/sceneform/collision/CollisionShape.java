package com.google.sceneform.collision;

import com.google.sceneform.utilities.ChangeId;
import com.google.sceneform.common.TransformProvider;

/**
 * 碰撞体形状类
 * <p>可以对其执行碰撞检查的所有形状类型的基类。</p>
 * */
public abstract class CollisionShape {
  private final ChangeId changeId = new ChangeId();

  public abstract CollisionShape makeCopy();

  /**
   * 必须在形状更改时由子类调用，以通知侦听器更改。
   * @hide
   */
  protected void onChanged() {
    changeId.update();
  }

  /** @hide */
  protected abstract boolean rayIntersection(Ray ray, RayHit result);

  /** @hide */
  protected abstract boolean shapeIntersection(CollisionShape shape);

  /** @hide */
  protected abstract boolean sphereIntersection(Sphere sphere);

  /** @hide */
  protected abstract boolean boxIntersection(Box box);

  @SuppressWarnings("initialization")
  CollisionShape() {
    changeId.update();
  }

  ChangeId getId() {
    return changeId;
  }

  abstract CollisionShape transform(TransformProvider transformProvider);

  abstract void transform(TransformProvider transformProvider, CollisionShape result);
}
