package com.eqgis.sceneform.collision;

import android.util.Log;

import com.eqgis.sceneform.common.TransformProvider;
import com.eqgis.sceneform.math.MathHelper;
import com.eqgis.sceneform.math.Matrix;
import com.eqgis.sceneform.math.Quaternion;
import com.eqgis.sceneform.math.Vector3;
import com.eqgis.sceneform.utilities.Preconditions;

/**
 * BOX对象
 * <p>用于碰撞检测</p>
 */
public class Box extends CollisionShape {
  private static final String TAG = Box.class.getSimpleName();
  private final Vector3 center = Vector3.zero();
  private final Vector3 size = Vector3.one();
  private final Matrix rotationMatrix = new Matrix();

  /** 构建一个默认的Box对象 */
  public Box() {}

  /**
   * 构造函数
   * @param size 尺寸
   */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public Box(Vector3 size) {
    this(size, Vector3.zero());
  }

  /**
   * 构造函数
   * @param size 尺寸
   * @param center 中心位置
   */
  @SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
  public Box(Vector3 size, Vector3 center) {
    Preconditions.checkNotNull(center, "Parameter \"center\" was null.");
    Preconditions.checkNotNull(size, "Parameter \"size\" was null.");

    setCenter(center);
    setSize(size);
  }

  /**
   * 设置中心位置
   * @see #getCenter()
   * @param center Box对象的中心位置
   */
  public void setCenter(Vector3 center) {
    Preconditions.checkNotNull(center, "Parameter \"center\" was null.");
    this.center.set(center);
    onChanged();
  }

  /**
   * 获取Box对象的中心位置
   * <p>这里获取的是中心位置的克隆对象</p>
   * @see #setCenter(Vector3)
   * @return 中心位置
   */
  public Vector3 getCenter() {
    return new Vector3(center);
  }

  /**
   * 设置尺寸
   * @see #getSize()
   * @param size 尺寸
   */
  public void setSize(Vector3 size) {
    Preconditions.checkNotNull(size, "Parameter \"size\" was null.");
    this.size.set(size);
    onChanged();
  }

  /**
   * 获取Box对象的尺寸
   *<p>这里获取的是尺寸的克隆对象</p>
   * @see #setSize(Vector3)
   * @return 尺寸
   */
  public Vector3 getSize() {
    return new Vector3(size);
  }

  /**
   * 计算Box的范围
   * @return Box的范围
   */
  public Vector3 getExtents() {
    return getSize().scaled(0.5f);
  }

  /**
   * 设置旋转四元数
   * @see #getRotation()
   * @param rotation 四元数
   */
  public void setRotation(Quaternion rotation) {
    Preconditions.checkNotNull(rotation, "Parameter \"rotation\" was null.");
    rotationMatrix.makeRotation(rotation);
    onChanged();
  }

  /**
   * 获取Box对象的旋转四元数
   * @see #setRotation(Quaternion)
   * @return 四元数
   */
  public Quaternion getRotation() {
    Quaternion result = new Quaternion();
    rotationMatrix.extractQuaternion(result);
    return result;
  }

  @Override
  public Box makeCopy() {
    return new Box(getSize(), getCenter());
  }

  /**
   * 获取原始旋转矩阵，表示盒子的方向。请勿直接修改。
   * 相反，使用设置。
   * @return 原始的旋转矩阵
   */
  Matrix getRawRotationMatrix() {
    return rotationMatrix;
  }

  /** @hide protected method */
  @Override
  protected boolean rayIntersection(Ray ray, RayHit result) {
    Preconditions.checkNotNull(ray, "Parameter \"ray\" was null.");
    Preconditions.checkNotNull(result, "Parameter \"result\" was null.");

    Vector3 rayDirection = ray.getDirection();
    Vector3 rayOrigin = ray.getOrigin();
    Vector3 max = getExtents();
    Vector3 min = max.negated();

    // tMin is the farthest "near" intersection (amongst the X,Y and Z planes pairs)
    float tMin = Float.MIN_VALUE;

    // tMax is the nearest "far" intersection (amongst the X,Y and Z planes pairs)
    float tMax = Float.MAX_VALUE;

    Vector3 delta = Vector3.subtract(center, rayOrigin);

    // Test intersection with the 2 planes perpendicular to the OBB's x axis.
    float[] axes = rotationMatrix.data;
    Vector3 axis = new Vector3(axes[0], axes[1], axes[2]);
    float e = Vector3.dot(axis, delta);
    float f = Vector3.dot(rayDirection, axis);

    if (!MathHelper.almostEqualRelativeAndAbs(f, 0.0f)) {
      float t1 = (e + min.x) / f;
      float t2 = (e + max.x) / f;

      if (t1 > t2) {
        float temp = t1;
        t1 = t2;
        t2 = temp;
      }

      tMax = Math.min(t2, tMax);
      tMin = Math.max(t1, tMin);

      if (tMax < tMin) {
        return false;
      }
    } else if (-e + min.x > 0.0f || -e + max.x < 0.0f) {
      // Ray is almost parallel to one of the planes.
      return false;
    }

    // Test intersection with the 2 planes perpendicular to the OBB's y axis.
    axis = new Vector3(axes[4], axes[5], axes[6]);
    e = Vector3.dot(axis, delta);
    f = Vector3.dot(rayDirection, axis);

    if (!MathHelper.almostEqualRelativeAndAbs(f, 0.0f)) {
      float t1 = (e + min.y) / f;
      float t2 = (e + max.y) / f;

      if (t1 > t2) {
        float temp = t1;
        t1 = t2;
        t2 = temp;
      }

      tMax = Math.min(t2, tMax);
      tMin = Math.max(t1, tMin);

      if (tMax < tMin) {
        return false;
      }
    } else if (-e + min.y > 0.0f || -e + max.y < 0.0f) {
      // Ray is almost parallel to one of the planes.
      return false;
    }

    // Test intersection with the 2 planes perpendicular to the OBB's z axis.
    axis = new Vector3(axes[8], axes[9], axes[10]);
    e = Vector3.dot(axis, delta);
    f = Vector3.dot(rayDirection, axis);

    if (!MathHelper.almostEqualRelativeAndAbs(f, 0.0f)) {
      float t1 = (e + min.z) / f;
      float t2 = (e + max.z) / f;

      if (t1 > t2) {
        float temp = t1;
        t1 = t2;
        t2 = temp;
      }

      tMax = Math.min(t2, tMax);
      tMin = Math.max(t1, tMin);

      if (tMax < tMin) {
        return false;
      }
    } else if (-e + min.z > 0.0f || -e + max.z < 0.0f) {
      // Ray is almost parallel to one of the planes.
      return false;
    }

    result.setDistance(tMin);
    result.setPoint(ray.getPoint(result.getDistance()));
    return true;
  }

  /** @hide protected method */
  @Override
  protected boolean shapeIntersection(CollisionShape shape) {
    Preconditions.checkNotNull(shape, "Parameter \"shape\" was null.");
    return shape.boxIntersection(this);
  }

  /** @hide protected method */
  @Override
  protected boolean sphereIntersection(Sphere sphere) {
    return Intersections.sphereBoxIntersection(sphere, this);
  }

  /** @hide protected method */
  @Override
  protected boolean boxIntersection(Box box) {
    return Intersections.boxBoxIntersection(this, box);
  }

  @Override
  CollisionShape transform(TransformProvider transformProvider) {
    Preconditions.checkNotNull(transformProvider, "Parameter \"transformProvider\" was null.");

    Box result = new Box();
    transform(transformProvider, result);
    return result;
  }

  @Override
  void transform(TransformProvider transformProvider, CollisionShape result) {
    Preconditions.checkNotNull(transformProvider, "Parameter \"transformProvider\" was null.");
    Preconditions.checkNotNull(result, "Parameter \"result\" was null.");

    if (!(result instanceof Box)) {
      Log.w(TAG, "Cannot pass CollisionShape of a type other than Box into Box.transform.");
      return;
    }

    if (result == this) {
      throw new IllegalArgumentException("Box cannot transform itself.");
    }

    Box resultBox = (Box) result;

    Matrix modelMatrix = transformProvider.getWorldModelMatrix();

    // Transform the center of the box.
    resultBox.center.set(modelMatrix.transformPoint(center));

    // Transform the size of the box.
    Vector3 worldScale = new Vector3();
    modelMatrix.decomposeScale(worldScale);
    resultBox.size.x = size.x * worldScale.x;
    resultBox.size.y = size.y * worldScale.y;
    resultBox.size.z = size.z * worldScale.z;

    // Transform the rotation of the box.
    modelMatrix.decomposeRotation(worldScale, resultBox.rotationMatrix);
    Matrix.multiply(rotationMatrix, resultBox.rotationMatrix, resultBox.rotationMatrix);
  }
}
