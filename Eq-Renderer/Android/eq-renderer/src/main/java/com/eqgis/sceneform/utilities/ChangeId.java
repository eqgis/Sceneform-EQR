package com.eqgis.sceneform.utilities;

/**
 * ID对象
 * <p>
 *     通过增加一个整数id来标识对象的状态是否发生了变化。
 *     其他类可以通过轮询查看id是否更改来确定此对象何时更改。
 * </p>
 * 当不安全时，这可以作为事件侦听器订阅模型的替代方案
 * 对象生命周期中的*点，用于取消订阅事件监听，这不会导致内存泄漏。
 * @hide
 */
public class ChangeId {
  public static final int EMPTY_ID = 0;

  private int id = EMPTY_ID;

  public int get() {
    return id;
  }

  public boolean isEmpty() {
    return id == EMPTY_ID;
  }

  public boolean checkChanged(int id) {
    return this.id != id && !isEmpty();
  }

  public void update() {
    id++;

    // Skip EMPTY_ID if the id has cycled all the way around.
    if (id == EMPTY_ID) {
      id++;
    }
  }
}
