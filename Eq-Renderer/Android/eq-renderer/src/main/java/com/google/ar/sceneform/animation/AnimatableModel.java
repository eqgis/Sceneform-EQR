package com.google.ar.sceneform.animation;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.text.TextUtils;
import android.view.animation.LinearInterpolator;

import com.google.ar.sceneform.utilities.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * 动画对象
 * 对象动画转换可以在所需的帧回调位置完成。
 * {@link ModelAnimation}
 * {@link ObjectAnimator}
 * {@link com.google.android.filament.gltfio.Animator}。
 */
public interface AnimatableModel {

    /**
     * 获取关联的{@link ModelAnimation}在给定的索引或抛出
     * 和{@link IndexOutOfBoundsException}。
     * @param animationIndex 动画索引
     */
    ModelAnimation getAnimation(int animationIndex);

    /**
     * 获取模型动画对象 {@link ModelAnimation}的动画数量
     */
    int getAnimationCount();

    /**
     * 当{@link ModelAnimation}处于dirty状态时触发
     */
    default void onModelAnimationChanged(ModelAnimation animation) {
        if(applyAnimationChange(animation)) {
            animation.setDirty(false);
        }
    }

    /**
     * 当{@link ModelAnimation}收到任何属性更改时发生。
     * <br>根据返回的值，{@link ModelAnimation}将把他的isDirty设置为false或不。
     * <br>你可以选择在{@link ObjectAnimator}上应用更改
     * {@link android.view.Choreographer。FrameCallback}或者使用你自己的
     * {@link android.view.Choreographer.FrameCallback}
     * <br>时间位置应该应用在全局{@link android.view.Choreographer}
     * 回调以确保转换按层次顺序应用。
     * @return true表示更改已被应用/处理
     */
    boolean applyAnimationChange(ModelAnimation animation);

    /**
     * 通过名称获取关联的{@link ModelAnimation}，如果给定的名称不存在，则为空。
     */
    default ModelAnimation getAnimation(String name) {
        int index = getAnimationIndex(name);
        return index != -1 ? getAnimation(index) : null;
    }

    /**
     * 通过名称获取关联的{@link ModelAnimation}，如果不存在则抛出异常
     */
    default ModelAnimation getAnimationOrThrow(String name) {
        return Preconditions.checkNotNull(getAnimation(name), "No animation found with the given name");
    }

    /**
     * 获取感兴趣的动画名称的从0开始的索引，如果没有找到则为-1。
     */
    default int getAnimationIndex(String name) {
        for (int i = 0; i < getAnimationCount(); i++) {
            if (TextUtils.equals(getAnimation(i).getName(), name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取{@link ModelAnimation}的指定索引的动画名称
     * <p>
     * 此名称对应于在可渲染资源中定义和导出的名称。
     * 通常是在3D制作软件中定义的动作名称。
     * </p>
     * @return  {@link ModelAnimation}的字符串名称，
     * <code>String.valueOf(animation.getIndex())</code>>如果没有指定。
     */
    default String getAnimationName(int animationIndex) {
        return getAnimation(animationIndex).getName();
    }

    /**
     * 获取{@link ModelAnimation}的所有名称
     * <p>
     * 此名称对应于在可渲染资源中定义和导出的名称。
     * 通常是在3D制作软件中定义的动作名称。
     * </p>
     * @return  {@link ModelAnimation}的字符串名称，
     * <code>String.valueOf(animation.getIndex())</code>>如果没有指定。
     */
    default List<String> getAnimationNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < getAnimationCount(); i++) {
            names.add(getAnimation(i).getName());
        }
        return names;
    }

    /**
     * 判断是否有动画
     * @return Return true if {@link #getAnimationCount()} > 0
     */
    default boolean hasAnimations() {
        return getAnimationCount() > 0;
    }

    /**
     * 将动画的当前位置设置为指定的时间位置(以秒为单位)。这次应该是
     * <p>
     *     此方法将应用旋转，平移和缩放到已被渲染目标。<code>TransformManager</code>
     * </p>
     * @param timePosition 以秒为单位的经过时间。在0和{@link ModelAnimation#getDuration()}的最大值之间。
     * @see ModelAnimation#getDuration()
     */
    default void setAnimationsTimePosition(float timePosition) {
        for (int i = 0; i < getAnimationCount(); i++) {
            getAnimation(i).setTimePosition(timePosition);
        }
    }

    /**
     * 设置(查找)所有动画的当前位置到指定的帧数到{@link ModelAnimation#getFrameRate()}
     * <p>
     *     此方法将应用旋转，平移和缩放到已被渲染目标。<code>TransformManager</code>
     * </p>
     * @param framePosition 介于0和{@link ModelAnimation#getFrameCount()}之间。
     * @see ModelAnimation#getFrameCount()
     */
    default void setAnimationsFramePosition(int framePosition) {
        for (int i = 0; i < getAnimationCount(); i++) {
            getAnimation(i).setFramePosition(framePosition);
        }
    }

    /**
     * 为所有{@link ModelAnimation}构造并返回一个{@link ObjectAnimator}
     * <h3>不要忘记调用{@link ObjectAnimator#start()}</h3>
     *
     * @param repeat 重复/循环动画
     * @return ObjectAnimator对象
     * @see ModelAnimator#ofAnimationTime(AnimatableModel, ModelAnimation, float...)
     */
    default ObjectAnimator animate(boolean repeat) {
        ObjectAnimator animator = ModelAnimator.ofAllAnimations(this);
        if(repeat) {
            animator.setRepeatCount(ValueAnimator.INFINITE);
        }
        return animator;
    }

    /**
     * 根据名称为目标{@link ModelAnimation}构造并返回一个{@link ObjectAnimator}
     * <br><b>不要忘记调用{@link ObjectAnimator#start()}</b>
     * @param animationNames 动画组名称
     *                       <br>该名称应与中定义和导出的名称相对应
     *                       <br>通常是在3D创建软件中定义的动作名称。
     *                       {@link ModelAnimation#getName()}
     * @return ObjectAnimator对象
     * @see ModelAnimator#ofAnimationTime(AnimatableModel, ModelAnimation, float...)
     */
    default ObjectAnimator animate(String... animationNames) {
        return ModelAnimator.ofAnimation(this, animationNames);
    }

    /**
     * 为目标{@link ModelAnimation}构造并返回一个{@link ObjectAnimator}对象的给定索引。
     * <br><b>不要忘记调用{@link ObjectAnimator#start()}</b>
     *
     * @param animationIndexes 动画索引组
     * @return ObjectAnimator
     * @see ModelAnimator#ofAnimationTime(AnimatableModel, ModelAnimation, float...)
     */
    default ObjectAnimator animate(int... animationIndexes) {
        return ModelAnimator.ofAnimation(this, animationIndexes);
    }

    /**
     * 为目标{@link ModelAnimation}构造并返回一个{@link ObjectAnimator}这个对象。
     * <b> setAutoCancel(true)对不同动画的新调用不起作用。</b>
     * <br>默认情况下，该方法将This应用于返回的ObjectAnimator:
     * <ul>
     * <li>持续时间值到max {@link ModelAnimation#getDuration()}以便匹配原来的动画速度。</li>
     * <li>将插值器设置为{@link LinearInterpolator}以匹配自然动画插值。</li>
     * </ul>
     * <br><b>不要忘记调用{@link ObjectAnimator#start()}</b>
     *
     * @param animations 动画组
     * @return ObjectAnimator
     * @see ModelAnimator#ofAnimationTime(AnimatableModel, ModelAnimation, float...)
     */
    default ObjectAnimator animate(ModelAnimation... animations) {
        return ModelAnimator.ofAnimation(this, animations);
    }
}