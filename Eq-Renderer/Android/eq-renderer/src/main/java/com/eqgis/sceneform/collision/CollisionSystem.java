package com.eqgis.sceneform.collision;

import androidx.annotation.Nullable;

import com.eqgis.sceneform.utilities.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 碰撞系统
 * <p>用于管理场景中的所有碰撞体组件</p>
 * @hide
 */
public class CollisionSystem {
  private static final String TAG = CollisionSystem.class.getSimpleName();

  private final ArrayList<Collider> colliders = new ArrayList<>();

  public void addCollider(Collider collider) {
    Preconditions.checkNotNull(collider, "Parameter \"collider\" was null.");
    colliders.add(collider);
  }

  public void removeCollider(Collider collider) {
    Preconditions.checkNotNull(collider, "Parameter \"collider\" was null.");
    colliders.remove(collider);
  }

  /**
   * 执行射线检测
   * @param ray 射线
   * @param resultHit 射线检测结果
   * @return 碰撞体
   */
  @Nullable
  public Collider raycast(Ray ray, RayHit resultHit) {
    Preconditions.checkNotNull(ray, "Parameter \"ray\" was null.");
    Preconditions.checkNotNull(resultHit, "Parameter \"resultHit\" was null.");

    resultHit.reset();
    Collider result = null;
    RayHit tempResult = new RayHit();
    for (Collider collider : colliders) {
      CollisionShape collisionShape = collider.getTransformedShape();
      if (collisionShape == null) {
        continue;
      }

      if (collisionShape.rayIntersection(ray, tempResult)) {
        if (tempResult.getDistance() < resultHit.getDistance()) {
          resultHit.set(tempResult);
          result = collider;
        }
      }
    }

    return result;
  }

  /**
   * 执行射线检测
   */
  @SuppressWarnings("AndroidApiChecker")
  public <T extends RayHit> int raycastAll(
      Ray ray,
      ArrayList<T> resultBuffer,
      @Nullable BiConsumer<T, Collider> processResult,
      Supplier<T> allocateResult) {
    Preconditions.checkNotNull(ray, "Parameter \"ray\" was null.");
    Preconditions.checkNotNull(resultBuffer, "Parameter \"resultBuffer\" was null.");
    Preconditions.checkNotNull(allocateResult, "Parameter \"allocateResult\" was null.");

    RayHit tempResult = new RayHit();
    int hitCount = 0;

    // Check the ray against all the colliders.
    for (Collider collider : colliders) {
      CollisionShape collisionShape = collider.getTransformedShape();
      if (collisionShape == null) {
        continue;
      }

      if (collisionShape.rayIntersection(ray, tempResult)) {
        hitCount++;
        T result = null;
        if (resultBuffer.size() >= hitCount) {
          result = resultBuffer.get(hitCount - 1);
        } else {
          result = allocateResult.get();
          resultBuffer.add(result);
        }

        result.reset();
        result.set(tempResult);

        if (processResult != null) {
          processResult.accept(result, collider);
        }
      }
    }

    // Reset extra hits in the buffer.
    for (int i = hitCount; i < resultBuffer.size(); i++) {
      resultBuffer.get(i).reset();
    }

    // Sort the hits by distance.
    Collections.sort(resultBuffer, (a, b) -> Float.compare(a.getDistance(), b.getDistance()));

    return hitCount;
  }

  /**
   * 对碰撞体进行相交判断
   * @param collider 碰撞体
   * @return 相交的碰撞体
   */
  @Nullable
  public Collider intersects(Collider collider) {
    Preconditions.checkNotNull(collider, "Parameter \"collider\" was null.");

    CollisionShape collisionShape = collider.getTransformedShape();
    if (collisionShape == null) {
      return null;
    }

    for (Collider otherCollider : colliders) {
      if (otherCollider == collider) {
        continue;
      }

      CollisionShape otherCollisionShape = otherCollider.getTransformedShape();
      if (otherCollisionShape == null) {
        continue;
      }

      if (collisionShape.shapeIntersection(otherCollisionShape)) {
        return otherCollider;
      }
    }

    return null;
  }

  /**
   * 相交判断
   * @param collider 碰撞体
   * @param processResult  结果
   */
  @SuppressWarnings("AndroidApiChecker")
  public void intersectsAll(Collider collider, Consumer<Collider> processResult) {
    Preconditions.checkNotNull(collider, "Parameter \"collider\" was null.");
    Preconditions.checkNotNull(processResult, "Parameter \"processResult\" was null.");

    CollisionShape collisionShape = collider.getTransformedShape();
    if (collisionShape == null) {
      return;
    }

    for (Collider otherCollider : colliders) {
      if (otherCollider == collider) {
        continue;
      }

      CollisionShape otherCollisionShape = otherCollider.getTransformedShape();
      if (otherCollisionShape == null) {
        continue;
      }

      if (collisionShape.shapeIntersection(otherCollisionShape)) {
        processResult.accept(otherCollider);
      }
    }
  }
}
