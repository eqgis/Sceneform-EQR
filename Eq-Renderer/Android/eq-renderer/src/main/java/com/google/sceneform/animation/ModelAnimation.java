package com.google.sceneform.animation;

import android.text.TextUtils;
import android.util.FloatProperty;
import android.util.IntProperty;
import android.util.Property;

import com.google.android.filament.gltfio.Animator;

import java.util.concurrent.TimeUnit;

/**
 * ModelAnimation是一组可重用的关键帧轨迹，它表示动画。
 * <p>
 * 这个类提供了对目标时间位置动画化的支持
 * {@link AnimatableModel}
 * <p>
 * <h2>
 * 以下是动画的一些用例:</h2>
 * <ul>
 * <li>
 *     对于一个非常基本的3D模型，比如一个无限旋转的球体，你不需要这样做使用这个类
 * </li>
 * <li>
 *     对于同步动画集，如动画立方体和球体的位置和旋转相同
 *     时间或顺序，请考虑使用{@link android.animation。AnimatorSet}播放
 *     {@link ModelAnimator#ofAnimation(AnimatableModel, String...)}
 *     或{@link ModelAnimator#ofPropertyValuesHolder(AnimatableModel, android.animation.PropertyValuesHolder...)}
 * </li>
 * <li>
 * 如果网格是一个角色，例如，可能有一个模型动画为一个步行周期，一个
 * 第二秒用于跳跃，第三秒用于回避，等等。
 * <br>假设一个角色对象有一个骨架，一个关键帧轨道可以存储数据
 * 下臂骨位置随时间变化，以不同的轨迹数据进行旋转
 * 同一块骨头的变化，三分之一的轨迹位置，另一块骨头的旋转或缩放，等等
 * 很明显，一个ModelAnimation可以作用于许多这样的轨道。
 * <br>假设模型有变形目标(例如一个变形目标显示一个友好的脸)
 * 和另一个显示一张愤怒的脸)，每条轨道都包含了关于a的影响如何的信息
 * 某些变形目标变化期间的剪辑表现。
 * 在这种情况下，你应该管理一个{@link android.animation.ObjectAnimator}来自
 * {@link ModelAnimator#ofAnimation(AnimatableModel, ModelAnimation...)}每个动作。
 * 和{@link android.animation}依次或一起播放它们。
 * </li>
 * </ul>
 */
public class ModelAnimation {

    private AnimatableModel model;
    private int index;
    private String name;
    private float duration;
    private int frameRate;

    /**
     * 全局时间位置，以确保在{@link android.view.Choreographer}的帧回调中按层次顺序调用
     */
    private float timePosition = 0;
    private boolean isDirty = false;

    /**
     * 构造函数
     * 源于{@link Animator}
     * @param name      对应{@link AnimatableModel}的动画名称
     *                  <br>通常在3D建模软件中定义
     * @param index     动画索引
     * @param duration  动画周期
     * @param frameRate 在原始动画资产中定义的每秒帧数
     */
    public ModelAnimation(AnimatableModel model, String name, int index, float duration
            , int frameRate) {
        this.model = model;
        this.index = index;
        this.name = name;
        if (TextUtils.isEmpty(this.name)) {
            this.name = String.valueOf(index);
        }
        this.frameRate = frameRate;
        this.duration = duration;
    }

    /**
     * 获取动画索引
     * 索引是从0开始的
     * {@link AnimatableModel}
     */
    public int geIndex() {
        return index;
    }

    /**
     * 获取动画名称
     * <p>
     *     动画名称通常是在3D建模软件中定义好的
     * </p>
     * @return <code>animation</code>对象名称的弱引用,
     * 或者没有指定，返回<code>String.valueOf(animation.getIndex())</code>
     */
    public String getName() {
        return name;
    }

    /**
     * 返回动画的持续时间
     * <p>单位：秒</p>
     */
    public float getDuration() {
        return duration;
    }

    /**
     *
     * 返回动画的持续时间
     * <p>单位：毫秒</p>
     */
    public long getDurationMillis() {
        return secondsToMillis(getDuration());
    }

    /**
     * 获取每秒帧数
     * {@link android.graphics.drawable.Animatable}.
     *
     * @return 每秒帧数
     */
    public int getFrameRate() {
        return frameRate;
    }

    /**
     * 获取总共的帧数
     * @return 帧数
     */
    public int getFrameCount() {
        return timeToFrame(getDuration(), getFrameRate());
    }

    /**
     * 获取当前动画位置的时间位置(以秒为单位)。
     * @return 0到{@link #getDuration()}
     * @see #getDuration()
     */
    public float getTimePosition() {
        return timePosition;
    }

    /**
     * 将动画的当前位置(查找)设置为指定的时间位置(以秒为单位)。
     * <p>
     *     这个方法也适用 {@link AnimatableModel}的rotation, translation, and scale变换
     * </p>
     * @param timePosition 从0到{@link #getDuration()}之间的时间位置
     * @see #getDuration()
     */
    public void setTimePosition(float timePosition) {
        this.timePosition = timePosition;
        setDirty(true);
    }

    /**
     * 获取当前动画位置的帧位置
     * @return 在0到{@link this#getFrameCount()}之间的位置
     * @see #getTimePosition()
     * @see #getFrameCount()
     */
    public int getFramePosition() {
        return getFrameAtTime(getTimePosition());
    }

    /**
     * 设置当前帧位置
     * @param frameNumber Frame number in the timeline. Between 0 and {@link #getFrameCount()}
     * @see #setTimePosition(float)
     * @see #getFrameCount()
     */
    public void setFramePosition(int frameNumber) {
        setTimePosition(getTimeAtFrame(frameNumber));
    }

    /**
     * 获取当前动画位置的播放百分比
     *
     * @return 取值区间[0,1]
     * @see #getTimePosition()
     */
    public float getFractionPosition() {
        return getFractionAtTime(getTimePosition());
    }

    /**
     * 根据百分比设置当前动画的播放位置
     * @param fractionPosition 播放位置，取值区间[0,1]
     * @see #setTimePosition(float)
     */
    public void setFractionPosition(float fractionPosition) {
        setTimePosition(getTimeAtFraction(fractionPosition));
    }

    /**
     * 判断当前是否已发生改变
     * 内部方法
     * @return 若发生改变则返回true
     */
    public boolean isDirty() {
        return isDirty;
    }

    /**
     * 设置该对象属性的状态为更改。
     * 通知{@link AnimatableModel}来处理它。
     */
    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
        if (isDirty) {
            model.onModelAnimationChanged(this);
        }
    }

    /**
     * 获取帧位置经过的时间(以秒为单位)
     * @param frame 当前帧索引
     * @return 已经过的时间
     */
    public float getTimeAtFrame(int frame) {
        return frameToTime(frame, getFrameRate());
    }

    /**
     * 获取当前时间经过的帧数
     * @param time 已经过的时间，单位：秒
     * @return 当前时间对应的帧数
     */
    public int getFrameAtTime(float time) {
        return timeToFrame(time, getFrameRate());
    }

    /**
     * 根据百分比获取当前时间
     * @param fraction 百分比，取值区间[0,1]
     * @return 已经过的时间
     */
    public float getTimeAtFraction(float fraction) {
        return fractionToTime(fraction, getDuration());
    }

    /**
     * 根据已过的时间获取播放的百分比
     * @param time 已经过的时间
     * @return 百分比，取值区间[0,1]
     */
    public float getFractionAtTime(float time) {
        return timeToFraction(time, getDuration());
    }

    /**
     * 将帧位置转为时刻
     * @param frame     帧位置
     * @param frameRate 每秒帧数
     * @return 已过的时间
     */
    public static float frameToTime(int frame, int frameRate) {
        return (float) frame / (float) frameRate;
    }

    /**
     * 将时间转为帧位置
     * @param time      已过的时间
     * @param frameRate 每秒帧数
     * @return 指定时间对应的帧索引
     */
    public static int timeToFrame(float time, int frameRate) {
        return (int) (time * frameRate);
    }

    /**
     * 将百分比位置转换为已过的时间
     * @param fraction 百分比，取值区间[0,1]
     * @param duration 动画持续时长
     * @return 已过的时间
     */
    public static float fractionToTime(float fraction, float duration) {
        return fraction * duration;
    }

    /**
     * 将已过的时间转换为百分比
     * @param time     已过的时间
     * @param duration 动画持续时长
     * @return 百分比
     */
    public static float timeToFraction(float time, float duration) {
        return time / duration;
    }

    /**
     * 将秒转换为毫秒
     * @param time 时间，单位：秒
     * @return 时间，单位：毫秒
     */
    public static long secondsToMillis(float time) {
        return (long) (time * (float) TimeUnit.SECONDS.toMillis(1));
    }

    /**
     * 对时间位置的包装类，用于处理{@link ModelAnimation#setTimePosition(float)} and {@link ModelAnimation#getTimePosition()}
     */
    public static final FloatProperty<ModelAnimation> TIME_POSITION = new FloatProperty<ModelAnimation>("timePosition") {
        @Override
        public void setValue(ModelAnimation object, float value) {
            object.setTimePosition(value);
        }

        @Override
        public Float get(ModelAnimation object) {
            return object.getTimePosition();
        }
    };

    /**
     * 对帧位置的包装类，用于处理{@link ModelAnimation#setFramePosition(int)} and {@link ModelAnimation#getFramePosition()}
     */
    public static final Property<ModelAnimation, Integer> FRAME_POSITION = new IntProperty<ModelAnimation>("framePosition") {
        @Override
        public void setValue(ModelAnimation object, int value) {
            object.setFramePosition(value);
        }

        @Override
        public Integer get(ModelAnimation object) {
            return object.getFramePosition();
        }
    };

    /**
     * 对播放百分比位置的包装类，用于处理{@link ModelAnimation#setFractionPosition(float)} and {@link ModelAnimation#getFractionPosition()}
     */
    public static final Property<ModelAnimation, Float> FRACTION_POSITION = new FloatProperty<ModelAnimation>("fractionPosition") {
        @Override
        public void setValue(ModelAnimation object, float value) {
            object.setFractionPosition(value);
        }

        @Override
        public Float get(ModelAnimation object) {
            return object.getFractionPosition();
        }
    };

    /**
     * 这个类保存了一个属性的信息和属性的值
     * 应该在动画中使用。
     * PropertyValuesHolder对象可以用ObjectAnimator或并行操作几个不同的属性。
     * <p>
     *     使用{@link PropertyValuesHolder}提供一个处理过的{@link ModelAnimator}取消
     *     因为我们的目标是相同的对象，并且那些PropertyValuesHolder具有相同的属性名称
     * </p>
     */
    public static class PropertyValuesHolder {

        /**
         * 构造并返回一个带有给定时间值的PropertyValuesHolder。
         * @param times {@link ModelAnimation}动画的间隔时间。时间值必须在0和{@link ModelAnimation#getDuration()}之间
         * @return PropertyValuesHolder构造的PropertyValuesHolder对象。
         */
        public static android.animation.PropertyValuesHolder ofTime(float... times) {
            return android.animation.PropertyValuesHolder.ofFloat(ModelAnimation.TIME_POSITION, times);
        }

        /**
         * 构造并返回一个带有一组给定帧值的PropertyValuesHolder。
         * <b><u>Warning</u></b>
         * 每个PropertyValuesHolder应用一个修改的时间位置
         * animation应该使用ModelAnimation。TIME_POSITION而不是它自己的属性
         * 可能取消任何ObjectAnimator操作时间修改ModelAnimation。
         * {@link android.animation.ObjectAnimator#setAutoCancel(boolean)} 会对不同的属性名没有影响
         * <p>
         * @param frames 动画关键帧
         * @return PropertyValuesHolder
         */
        public static android.animation.PropertyValuesHolder ofFrame(int... frames) {
            return android.animation.PropertyValuesHolder.ofInt(ModelAnimation.FRAME_POSITION, frames);
        }

        /**
         * 造并返回一个带有一组给定百分比的PropertyValuesHolder。
         * <b><u>Warning</u></b>
         * 每个PropertyValuesHolder应用一个修改的时间位置
         * animation应该使用ModelAnimation。TIME_POSITION而不是它自己的属性
         * 可能取消任何ObjectAnimator操作时间修改ModelAnimation。
         * {@link android.animation.ObjectAnimator#setAutoCancel(boolean)} 会对不同的属性名没有影响
         * <p>
         * @param fractions 动画关键百分比
         * @return PropertyValuesHolder
         */
        public static android.animation.PropertyValuesHolder ofFraction(float... fractions) {
            return android.animation.PropertyValuesHolder.ofFloat(ModelAnimation.FRACTION_POSITION, fractions);
        }
    }
}
