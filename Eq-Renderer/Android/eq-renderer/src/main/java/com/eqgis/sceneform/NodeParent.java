package com.eqgis.sceneform;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

import com.eqgis.sceneform.utilities.AndroidPreconditions;
import com.eqgis.sceneform.utilities.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 节点对象基类
 *
 * <p>
 *     类{@link Node}和{@link Scene}都是nodeparent。
 *     要使{@link Node}成为另一个{@link Node}或{@link Scene}的子节点，
 *     请使用{@link Node#setParent(NodeParent)}。
 * </p>
 */
public abstract class NodeParent {
  private final ArrayList<Node> children = new ArrayList<>();
  private final List<Node> unmodifiableChildren = Collections.unmodifiableList(children);

  // 子节点集合
  private final ArrayList<Node> iterableChildren = new ArrayList<>();

  // 如果子节点集合自上次更新iterableChildren以来发生了变化，则为True。
  private boolean isIterableChildrenDirty;

  // 用于跟踪iterableChildren列表当前是否正在迭代。
  // 迭代计数，用于处理重入口(迭代中的迭代)。
  private int iteratingCounter;

  /** 返回父节点的子节点的不可变列表。 */
  public final List<Node> getChildren() {
    return unmodifiableChildren;
  }

  /**
   * 添加节点作为此NodeParent的子节点。如果节点已经有父节点，则从旧的父节点中删除该节点。
   * 如果该节点已经是该NodeParent的直接子节点，则不会进行任何更改。
   *
   * @param child 子节点
   * @throws IllegalArgumentException 如果子对象与父对象相同或者父对象是子对象的后代，则抛出异常
   */
  public final void addChild(Node child) {
    Preconditions.checkNotNull(child, "Parameter \"child\" was null.");
    AndroidPreconditions.checkUiThread();

    // 父节点未变，提前返回
    if (child.parent == this) {
      return;
    }

    StringBuilder failureReason = new StringBuilder();
    if (!canAddChild(child, failureReason)) {
      throw new IllegalArgumentException(failureReason.toString());
    }

    onAddChild(child);
  }

  /**
   * 从这个NodeParent的子节点中删除一个节点。
   * 如果该节点不是该NodeParent的直接子节点，则不会进行任何更改。
   *
   * @param child 要删除的子节点
   */
  public final void removeChild(Node child) {
    Preconditions.checkNotNull(child, "Parameter \"child\" was null.");
    AndroidPreconditions.checkUiThread();

    if(child.getRenderableInstance() != null){
      //销毁node节点的图形资源占用
      child.getRenderableInstance().destroy();
    }

    // Return early if this parent doesn't contain the child.
    if (!children.contains(child)) {
      return;
    }

    onRemoveChild(child);
  }

  /**
   * 遍历层次结构并在每个节点上调用一个方法。遍历首先是深度。
   * 如果这个NodeParent是一个Node，遍历从这个NodeParent开始，否则遍历从它的子节点开始。
   *
   * @param consumer 每个节点都触发
   */
  @SuppressWarnings("AndroidApiChecker")
  public void callOnHierarchy(Consumer<Node> consumer) {
    Preconditions.checkNotNull(consumer, "Parameter \"consumer\" was null.");

    ArrayList<Node> iterableChildren = getIterableChildren();
    startIterating();
    for (int i = 0; i < iterableChildren.size(); i++) {
      Node child = iterableChildren.get(i);
      child.callOnHierarchy(consumer);
    }
    stopIterating();
  }

  /**
   * 遍历层次结构以查找满足条件的第一个节点。
   * 遍历首先是深度。如果这个NodeParent是一个Node，
   * 遍历从这个NodeParent开始，否则遍历从它的子节点开始。
   *
   * @param condition 检索条件
   * @return 返回符合条件的第一个节点，否则返回null
   */
  @SuppressWarnings("AndroidApiChecker")
  @Nullable
  public Node findInHierarchy(Predicate<Node> condition) {
    Preconditions.checkNotNull(condition, "Parameter \"condition\" was null.");

    ArrayList<Node> iterableChildren = getIterableChildren();
    Node found = null;
    startIterating();
    for (int i = 0; i < iterableChildren.size(); i++) {
      Node child = iterableChildren.get(i);
      found = child.findInHierarchy(condition);
      if (found != null) {
        break;
      }
    }
    stopIterating();
    return found;
  }

  /**
   * 遍历层次结构以查找具有给定名称的第一个节点。
   *
   * <p>
   *     遍历首先是深度。如果这个NodeParent是一个Node，遍历从这个NodeParent开始，否则遍历从它的子节点开始。
   * </p>
   *
   * @param name 要查找的节点名称
   * @return 如果找到该节点，则为该节点，否则为空
   */
  @SuppressWarnings("AndroidApiChecker")
  @Nullable
  public Node findByName(String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    int hashToFind = name.hashCode();
    Node found =
        findInHierarchy(
            (node) -> {
              String nodeName = node.getName();
              return (node.getNameHash() != 0 && node.getNameHash() == hashToFind)
                  || (nodeName != null && nodeName.equals(name));
            });

    return found;
  }

  protected boolean canAddChild(Node child, StringBuilder failureReason) {
    Preconditions.checkNotNull(child, "Parameter \"child\" was null.");
    Preconditions.checkNotNull(failureReason, "Parameter \"failureReason\" was null.");

    if (child == this) {
      failureReason.append("Cannot add child: Cannot make a node a child of itself.");
      return false;
    }

    return true;
  }

  @CallSuper
  protected void onAddChild(Node child) {
    Preconditions.checkNotNull(child, "Parameter \"child\" was null.");

    NodeParent previousParent = child.getNodeParent();
    if (previousParent != null) {
      previousParent.removeChild(child);
    }

    children.add(child);
    child.parent = this;

    isIterableChildrenDirty = true;
  }

  @CallSuper
  protected void onRemoveChild(Node child) {
    Preconditions.checkNotNull(child, "Parameter \"child\" was null.");

    children.remove(child);
    child.parent = null;

    isIterableChildrenDirty = true;
  }

  private ArrayList<Node> getIterableChildren() {
    if (isIterableChildrenDirty && !isIterating()) {
      iterableChildren.clear();
      iterableChildren.addAll(children);
      isIterableChildrenDirty = false;
    }

    return iterableChildren;
  }

  private void startIterating() {
    iteratingCounter++;
  }

  private void stopIterating() {
    iteratingCounter--;
    if (iteratingCounter < 0) {
      throw new AssertionError("stopIteration was called without calling startIteration.");
    }
  }

  private boolean isIterating() {
    return iteratingCounter > 0;
  }
}
