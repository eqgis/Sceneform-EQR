package com.google.sceneform;

import android.view.MotionEvent;

import androidx.annotation.Nullable;

import com.google.sceneform.utilities.Preconditions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * 触摸事件系统
 * <p>管理场景中的触摸事件</p>
 *
 * <p>触摸事件传播的方式反映了触摸传播到Android视图的方式。
 *
 * <p>当ACTION_DOWN事件发生时，它代表一个手势的开始。ACTION_UP或ACTION_CANCEL表示一个手势何时结束。
 * 当一个手势开始时，完成以下操作:
 *
 * <ul>
 *   <li>在被scene.hitTest检测到时，调用 {@link Node#dispatchTouchEvent(HitTestResult, MotionEvent)}
 *   <li>如果 {@link Node#dispatchTouchEvent(HitTestResult, MotionEvent)} 返回 false, 递归向上通过
 *   节点的父节点并调用{@link Node#dispatchTouchEvent(HitTestResult, MotionEvent)} 直到返回 true。
 *   <li>如果每个节点都返回false，则忽略手势和后续事件手势不会传递给任何节点。
 *   <li>如果其中一个节点返回true，那么该节点将接收该手势的所有未来触摸事件。
 * </ul>
 *
 * @hide
 */
public class TouchEventSystem {
  private Method motionEventSplitMethod;
  private final Object[] motionEventSplitParams = new Object[1];

  /**
   * 跟踪哪些节点正在处理哪些指针Id的事件。作为一个链表实现，用于存储触摸目标的有序列表。
   */
  private static class TouchTarget {
    public static final int ALL_POINTER_IDS = -1; // all ones

    // 触摸的目标
    public Node node;

    // 目标捕获的所有指针的指针id的组合位掩码。
    public int pointerIdBits;

    // 目标列表的下一个目标
    @Nullable public TouchTarget next;
  }

  @Nullable private Scene.OnTouchListener onTouchListener;
  private final ArrayList<Scene.OnPeekTouchListener> onPeekTouchListeners = new ArrayList<>();

  // 处理当前手势的触摸监听器。
  @Nullable private Scene.OnTouchListener handlingTouchListener = null;

  // 当前正在处理一组指针的触摸的节点的链表。
  @Nullable private TouchTarget firstHandlingTouchTarget = null;

  public TouchEventSystem() {}

  /**
   * 获取当前为触摸事件注册的回调。
   *
   * @see #setOnTouchListener(Scene.OnTouchListener)
   * @return the attached touch listener
   */
  @Nullable
  public Scene.OnTouchListener getOnTouchListener() {
    return onTouchListener;
  }

  /**
   * 注册一个在场景被触摸时调用的回调函数。
   * 在任何节点接收到事件之前调用回调。
   * 如果回调处理事件，则节点永远不会接收到手势。
   * @param onTouchListener the touch listener to attach
   */
  public void setOnTouchListener(@Nullable Scene.OnTouchListener onTouchListener) {
    this.onTouchListener = onTouchListener;
  }

  /**
   * 添加监视器监听事件
   * 将在{@link Scene.OnTouchListener}被调用。
   * 即使手势被消耗，也会调用该方法，从而可以观察分派到场景的所有动作事件。
   * 即使触摸不在节点上，也会调用该方法，
   * 在这种情况下{@link HitTestResult#getNode()}将为空。
   * 侦听器将按照它们被添加的顺序调用。
   * @param onPeekTouchListener the peek touch listener to add
   */
  public void addOnPeekTouchListener(Scene.OnPeekTouchListener onPeekTouchListener) {
    if (!onPeekTouchListeners.contains(onPeekTouchListener)) {
      onPeekTouchListeners.add(onPeekTouchListener);
    }
  }

  /**
   * 移除监视器事件
   *
   * @param onPeekTouchListener the peek touch listener to remove
   */
  public void removeOnPeekTouchListener(Scene.OnPeekTouchListener onPeekTouchListener) {
    onPeekTouchListeners.remove(onPeekTouchListener);
  }

  public void onTouchEvent(HitTestResult hitTestResult, MotionEvent motionEvent) {
    Preconditions.checkNotNull(hitTestResult, "Parameter \"hitTestResult\" was null.");
    Preconditions.checkNotNull(motionEvent, "Parameter \"motionEvent\" was null.");

    int actionMasked = motionEvent.getActionMasked();

    // down的话，清理所有
    if (actionMasked == MotionEvent.ACTION_DOWN) {
      clearTouchTargets();
    }

    // 将触摸事件分派给peek触摸监听器，它将接收所有事件，即使手势正在被使用。
    for (Scene.OnPeekTouchListener onPeekTouchListener : onPeekTouchListeners) {
      onPeekTouchListener.onPeekTouch(hitTestResult, motionEvent);
    }

    // 如果触摸监听器已经在处理手势，总是分派给它。
    if (handlingTouchListener != null) {
      tryDispatchToSceneTouchListener(hitTestResult, motionEvent);
    } else {

      TouchTarget newTouchTarget = null;
      boolean alreadyDispatchedToNewTouchTarget = false;
      boolean alreadyDispatchedToAnyTarget = false;
      Node hitNode = hitTestResult.getNode();

      // 场景中有新pointer被触摸
      // 为这个pointer找到合适的触摸目标。
      if ((actionMasked == MotionEvent.ACTION_DOWN
          || (actionMasked == MotionEvent.ACTION_POINTER_DOWN))) {
        int actionIndex = motionEvent.getActionIndex();
        int idBitsToAssign = 1 << motionEvent.getPointerId(actionIndex);

        // 清除此指针Pointer id的早期触摸目标，以防它们变得不同步。
        removePointersFromTouchTargets(idBitsToAssign);

        // 查看此事件是否发生在已经是触摸目标的节点上。
        if (hitNode != null) {
          newTouchTarget = getTouchTargetForNode(hitNode);
          if (newTouchTarget != null) {
            // 给现有的触摸目标一个新的pointer，除了它正在处理的那个。
            newTouchTarget.pointerIdBits |= idBitsToAssign;
          } else {
            Node handlingNode =
                dispatchTouchEvent(motionEvent, hitTestResult, hitNode, idBitsToAssign, true);
            if (handlingNode != null) {
              newTouchTarget = addTouchTarget(handlingNode, idBitsToAssign);
              alreadyDispatchedToNewTouchTarget = true;
            }
            alreadyDispatchedToAnyTarget = true;
          }
        }

        if (newTouchTarget == null && firstHandlingTouchTarget != null) {
          // 没有找到接收事件的现有目标。
          // 将指针分配给最近最少添加的目标。
          newTouchTarget = firstHandlingTouchTarget;
          while (newTouchTarget.next != null) {
            newTouchTarget = newTouchTarget.next;
          }
          newTouchTarget.pointerIdBits |= idBitsToAssign;
        }
      }

      // 分发事件给触摸目标
      if (firstHandlingTouchTarget != null) {
        TouchTarget target = firstHandlingTouchTarget;
        while (target != null) {
          TouchTarget next = target.next;
          if (!alreadyDispatchedToNewTouchTarget || target != newTouchTarget) {
            dispatchTouchEvent(
                motionEvent, hitTestResult, target.node, target.pointerIdBits, false);
          }
          target = next;
        }
      } else if (!alreadyDispatchedToAnyTarget) {
        tryDispatchToSceneTouchListener(hitTestResult, motionEvent);
      }
    }

    if (actionMasked == MotionEvent.ACTION_CANCEL || actionMasked == MotionEvent.ACTION_UP) {
      clearTouchTargets();
    } else if (actionMasked == MotionEvent.ACTION_POINTER_UP) {
      int actionIndex = motionEvent.getActionIndex();
      int idBitsToRemove = 1 << motionEvent.getPointerId(actionIndex);
      removePointersFromTouchTargets(idBitsToRemove);
    }
  }

  private boolean tryDispatchToSceneTouchListener(
      HitTestResult hitTestResult, MotionEvent motionEvent) {
    //down时，给触摸监听器一个捕获时机。
    if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
      // 如果侦听器处理手势，则事件永远不会传播到节点。
      if (onTouchListener != null && onTouchListener.onSceneTouch(hitTestResult, motionEvent)) {
        // 触摸监听器正在处理手势，提前返回。
        handlingTouchListener = onTouchListener;
        return true;
      }
    } else if (handlingTouchListener != null) {
      handlingTouchListener.onSceneTouch(hitTestResult, motionEvent);
      return true;
    }

    return false;
  }

  private MotionEvent splitMotionEvent(MotionEvent motionEvent, int idBits) {
    if (motionEventSplitMethod == null) {
      try {
        Class<MotionEvent> motionEventClass = MotionEvent.class;
        motionEventSplitMethod = motionEventClass.getMethod("split", int.class);
      } catch (ReflectiveOperationException ex) {
        throw new RuntimeException("Splitting MotionEvent not supported.", ex);
      }
    }

    try {
      motionEventSplitParams[0] = idBits;
      Object result = motionEventSplitMethod.invoke(motionEvent, motionEventSplitParams);
      // MotionEvent——split保证返回NonNull结果，但是需要null检查
      if (result != null) {
        return (MotionEvent) result;
      } else {
        return motionEvent;
      }
    } catch (InvocationTargetException | IllegalAccessException ex) {
      throw new RuntimeException("Unable to split MotionEvent.", ex);
    }
  }

  private void removePointersFromTouchTargets(int pointerIdBits) {
    TouchTarget predecessor = null;
    TouchTarget target = firstHandlingTouchTarget;
    while (target != null) {
      TouchTarget next = target.next;
      if ((target.pointerIdBits & pointerIdBits) != 0) {
        target.pointerIdBits &= ~pointerIdBits;
        if (target.pointerIdBits == 0) {
          if (predecessor == null) {
            firstHandlingTouchTarget = next;
          } else {
            predecessor.next = next;
          }
          target = next;
          continue;
        }
      }
      predecessor = target;
      target = next;
    }
  }

  @Nullable
  private TouchTarget getTouchTargetForNode(Node node) {
    for (TouchTarget target = firstHandlingTouchTarget; target != null; target = target.next) {
      if (target.node == node) {
        return target;
      }
    }
    return null;
  }

  @Nullable
  private Node dispatchTouchEvent(
      MotionEvent motionEvent,
      HitTestResult hitTestResult,
      Node node,
      int desiredPointerIdBits,
      boolean bubble) {
    // 计算要传递的指针的数量。
    int eventPointerIdBits = getPointerIdBits(motionEvent);
    int finalPointerIdBits = eventPointerIdBits & desiredPointerIdBits;

    // 如果由于某种原因，我们最终处于不一致的状态，
    // 看起来我们可能会产生一个没有指针的运动事件，然后删除该事件。
    if (finalPointerIdBits == 0) {
      return null;
    }

    // 如果需要，根据事件中包含的指针id与节点正在处理的指针id进行比较，拆分运动事件。
    MotionEvent finalEvent = motionEvent;
    boolean needsRecycle = false;
    if (finalPointerIdBits != eventPointerIdBits) {
      finalEvent = splitMotionEvent(motionEvent, finalPointerIdBits);
      needsRecycle = true;
    }

    // 将事件沿层次结构向上冒泡，直到节点处理该事件，或者到达根节点。
    Node resultNode = node;
    while (resultNode != null) {
      if (resultNode.dispatchTouchEvent(hitTestResult, finalEvent)) {
        break;
      } else {
        if (bubble) {
          resultNode = resultNode.getParent();
        } else {
          resultNode = null;
        }
      }
    }

    if (resultNode == null) {
      tryDispatchToSceneTouchListener(hitTestResult, finalEvent);
    }

    if (needsRecycle) {
      finalEvent.recycle();
    }

    return resultNode;
  }

  private int getPointerIdBits(MotionEvent motionEvent) {
    int idBits = 0;
    int pointerCount = motionEvent.getPointerCount();
    for (int i = 0; i < pointerCount; i++) {
      idBits |= 1 << motionEvent.getPointerId(i);
    }
    return idBits;
  }

  private TouchTarget addTouchTarget(Node node, int pointerIdBits) {
    final TouchTarget target = new TouchTarget();
    target.node = node;
    target.pointerIdBits = pointerIdBits;
    target.next = firstHandlingTouchTarget;
    firstHandlingTouchTarget = target;
    return target;
  }

  private void clearTouchTargets() {
    handlingTouchListener = null;
    firstHandlingTouchTarget = null;
  }
}
