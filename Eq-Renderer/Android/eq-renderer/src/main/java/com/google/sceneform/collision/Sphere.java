package com.google.sceneform.collision;

import android.util.Log;

import com.google.sceneform.common.TransformProvider;
import com.google.sceneform.math.Matrix;
import com.google.sceneform.math.Vector3;
import com.google.sceneform.utilities.Preconditions;

/**
 * 球体对象
 * <p>碰撞检测时用到的球体对象</p>
 */
public class Sphere extends CollisionShape {
  private static final String TAG = Sphere.class.getSimpleName();

  private final Vector3 center = new Vector3();
  private float radius = 1.0f;

  /** 构造函数 */
  public Sphere() {}

  /**
   * 构造函数
   * @param radius 球体对象的半径
   */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public Sphere(float radius) {
    this(radius, Vector3.zero());
  }

  /**
   * 构造函数
   * @param radius 球体对象的半径
   * @param center 球体对象的中心位置
   */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public Sphere(float radius, Vector3 center) {
    Preconditions.checkNotNull(center, "Parameter \"center\" was null.");

    setCenter(center);
    setRadius(radius);
  }

  /**
   * 设置球体对象的中心位置.
   * @see #getCenter()
   * @param center 球体对象的中心位置
   */
  public void setCenter(Vector3 center) {
    Preconditions.checkNotNull(center, "Parameter \"center\" was null.");
    this.center.set(center);
    onChanged();
  }

  /**
   * 获取球体对象的中心位置
   * @see #setCenter(Vector3)
   * @return 中心位置
   */
  public Vector3 getCenter() {
    return new Vector3(center);
  }

  /**
   * 设置球体对象的半径
   * @see #getRadius()
   * @param radius 球体半径
   */
  public void setRadius(float radius) {
    this.radius = radius;
    onChanged();
  }

  /**
   * 获取球体对象的半径
   * @see #setRadius(float)
   * @return 球体对象的半径
   */
  public float getRadius() {
    return radius;
  }

  @Override
  public Sphere makeCopy() {
    return new Sphere(getRadius(), getCenter());
  }

  /** @hide */
  @Override
  protected boolean rayIntersection(Ray ray, RayHit result) {
    Preconditions.checkNotNull(ray, "Parameter \"ray\" was null.");
    Preconditions.checkNotNull(result, "Parameter \"result\" was null.");

    Vector3 rayDirection = ray.getDirection();
    Vector3 rayOrigin = ray.getOrigin();

    Vector3 difference = Vector3.subtract(rayOrigin, center);
    float b = 2.0f * Vector3.dot(difference, rayDirection);
    float c = Vector3.dot(difference, difference) - radius * radius;
    float discriminant = b * b - 4.0f * c;

    if (discriminant < 0.0f) {
      return false;
    }

    float discriminantSqrt = (float) Math.sqrt(discriminant);
    float tMinus = (-b - discriminantSqrt) / 2.0f;
    float tPlus = (-b + discriminantSqrt) / 2.0f;

    if (tMinus < 0.0f && tPlus < 0.0f) {
      return false;
    }

    if (tMinus < 0 && tPlus > 0) {
      result.setDistance(tPlus);
    } else {
      result.setDistance(tMinus);
    }

    result.setPoint(ray.getPoint(result.getDistance()));
    return true;
  }

  /** @hide */
  @Override
  protected boolean shapeIntersection(CollisionShape shape) {
    Preconditions.checkNotNull(shape, "Parameter \"shape\" was null.");
    return shape.sphereIntersection(this);
  }

  /** @hide */
  @Override
  protected boolean sphereIntersection(Sphere sphere) {
    return Intersections.sphereSphereIntersection(this, sphere);
  }

  /** @hide */
  @Override
  protected boolean boxIntersection(Box box) {
    return Intersections.sphereBoxIntersection(this, box);
  }

  @Override
  CollisionShape transform(TransformProvider transformProvider) {
    Preconditions.checkNotNull(transformProvider, "Parameter \"transformProvider\" was null.");

    Sphere result = new Sphere();
    transform(transformProvider, result);
    return result;
  }

  @Override
  void transform(TransformProvider transformProvider, CollisionShape result) {
    Preconditions.checkNotNull(transformProvider, "Parameter \"transformProvider\" was null.");
    Preconditions.checkNotNull(result, "Parameter \"result\" was null.");

    if (!(result instanceof Sphere)) {
      Log.w(TAG, "Cannot pass CollisionShape of a type other than Sphere into Sphere.transform.");
      return;
    }

    Sphere resultSphere = (Sphere) result;

    Matrix modelMatrix = transformProvider.getWorldModelMatrix();

    // Transform the center of the sphere.
    resultSphere.setCenter(modelMatrix.transformPoint(center));

    // Transform the radius of the sphere.
    Vector3 worldScale = new Vector3();
    modelMatrix.decomposeScale(worldScale);
    // Find the max component scale, ignoring sign.
    float maxScale =
        Math.max(
            Math.abs(Math.min(Math.min(worldScale.x, worldScale.y), worldScale.z)),
            Math.max(Math.max(worldScale.x, worldScale.y), worldScale.z));
    resultSphere.radius = radius * maxScale;
  }
}
