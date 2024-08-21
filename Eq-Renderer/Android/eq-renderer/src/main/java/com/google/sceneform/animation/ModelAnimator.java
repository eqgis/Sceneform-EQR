package com.google.sceneform.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.text.TextUtils;
import android.util.Property;
import android.view.animation.LinearInterpolator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 模型动画器
 * <p>用于支持 {@link AnimatableModel}</p>
 * <h2>用法</h2>
 * <pre>
 * {@link ModelAnimator#ofAnimation(AnimatableModel, String...)}
 * </pre>
 * <pre>
 * {@link ModelAnimator#ofAnimationTime(AnimatableModel, String, float...)}
 * </pre>
 * <pre>
 * {@link ModelAnimator#ofAnimationFrame(AnimatableModel, String, int...)}
 * </pre>
 * <pre>
 * {@link ModelAnimator#ofAnimationFraction(AnimatableModel, String, float...)}
 * </pre>
 */
public class ModelAnimator {

    /**
     * 创建动画对象
     * <b>setAutoCancel(true) 不起作用</b>
     * <p>不要忘记调用 {@link ObjectAnimator#start()}</p>
     * @param model {@link AnimatableModel}
     * @return ObjectAnimator
     * @see #ofAnimation(AnimatableModel, ModelAnimation...)
     */
    public static ObjectAnimator ofAllAnimations(AnimatableModel model) {
        ModelAnimation[] animations = new ModelAnimation[model.getAnimationCount()];
        for (int i = 0; i < animations.length; i++) {
            animations[i] = model.getAnimation(i);
        }
        return ofAnimation(model, animations);
    }

    /**
     * 创建动画对象集
     * <b>setAutoCancel(true) 不起作用</b>
     * <p>不要忘记调用 {@link ObjectAnimator#start()}</p>
     * @param model {@link AnimatableModel}
     * @param animationNames 动画名称组
     * @return The constructed ObjectAnimator
     * @see #ofAnimation(AnimatableModel, ModelAnimation...)
     */
    public static List<ObjectAnimator> ofMultipleAnimations(AnimatableModel model
            , String... animationNames) {
        List<ObjectAnimator> objectAnimators = new ArrayList<>();
        for (int i = 0; i < animationNames.length; i++) {
            objectAnimators.add(ofAnimation(model, animationNames[i]));
        }
        return objectAnimators;
    }

    /**
     * 创建动画对象
     * <b>setAutoCancel(true) 不起作用</b>
     * <p>不要忘记调用 {@link ObjectAnimator#start()}</p>
     * @param model {@link AnimatableModel}
     * @param animationNames 动画名称组
     * <br>该名称应与中定义和导出的名称对应模型。
     * <br>通常是在3D创建软件中定义的动作名称。{@link ModelAnimation#getName()}
     * @return The constructed ObjectAnimator
     * @see #ofAnimation(AnimatableModel, ModelAnimation...)
     */
    public static ObjectAnimator ofAnimation(AnimatableModel model, String... animationNames) {
        ModelAnimation[] animations = new ModelAnimation[animationNames.length];
        for (int i = 0; i < animationNames.length; i++) {
            animations[i] = getAnimationByName(model, animationNames[i]);
        }
        return ofAnimation(model, animations);
    }

    /**
     * 创建动画对象
     * <b>setAutoCancel(true) 不起作用</b>
     * <p>不要忘记调用 {@link ObjectAnimator#start()}</p>
     * @param model            {@link AnimatableModel}
     * @param animationIndexes 索引
     * @return ObjectAnimator
     * @see #ofAnimation(AnimatableModel, ModelAnimation...)
     */
    public static ObjectAnimator ofAnimation(AnimatableModel model, int... animationIndexes) {
        ModelAnimation[] animations = new ModelAnimation[animationIndexes.length];
        for (int i = 0; i < animationIndexes.length; i++) {
            animations[i] = model.getAnimation(animationIndexes[i]);
        }
        return ofAnimation(model, animations);
    }

    /**
     * 为目标{@link ModelAnimation}内部构造并返回一个{@link ObjectAnimator}和{@link AnimatableModel}。
     * <b> setAutoCancel(true)对不同动画的新调用不起作用。</b>
     * 此方法默认将This应用于返回的ObjectAnimator:
     * <ul>
     * <li>持续时间值到max {@link ModelAnimation#getDuration()}以便
     *匹配原来的动画速度。</li>
     * <li>插入器为{@link LinearInterpolator}以匹配自然动画
     李*插值。</li>
     * </ul>
     * <p>不要忘记调用{@link ObjectAnimator#start()}</p>
     * @param model      AnimatableModel
     * @param animations ModelAnimation
     * @return ObjectAnimator
     */
    public static ObjectAnimator ofAnimation(AnimatableModel model, ModelAnimation... animations) {
        android.animation.PropertyValuesHolder[] propertyValuesHolders = new android.animation.PropertyValuesHolder[animations.length];
        long duration = 0;
        for (int i = 0; i < animations.length; i++) {
            duration = Math.max(duration, animations[i].getDurationMillis());
            propertyValuesHolders[i] = PropertyValuesHolder.ofAnimationTime(animations[i], 0, animations[i].getDuration());
        }
        ObjectAnimator objectAnimator = ofPropertyValuesHolder(model, propertyValuesHolders);
        objectAnimator.setDuration(duration);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
                for (ModelAnimation animation : animations) {
                    animation.setTimePosition(0);
                }
            }
        });
        objectAnimator.setAutoCancel(true);
        return objectAnimator;
    }

    /**
     * 创建动画对象
     * <b>setAutoCancel(true) 不起作用</b>
     * <p>不要忘记调用 {@link ObjectAnimator#start()}</p>
     * @param model         The targeted model to animate
     * @param animationName 动画的字符串名称。
     *                      <br>该名称应与中定义和导出的名称对应模型。
     *                      <br>通常是在3D创建软件中定义的动作名称。
     *                      {@link ModelAnimation#getName()}
     * @param times         经过的时间(从0到{@link ModelAnimation#getDuration()})
     *                      {@link ModelAnimation}将在两者之间进行动画。
     * @return ObjectAnimator
     * @see #ofAnimationTime(AnimatableModel, ModelAnimation, float...)
     * @see ModelAnimation#getName()
     */
    public static ObjectAnimator ofAnimationTime(AnimatableModel model
            , String animationName, float... times) {
        return ofAnimationTime(model, getAnimationByName(model, animationName)
                , times);
    }

    /**
     * 创建动画对象
     * <b>setAutoCancel(true) 不起作用</b>
     * <p>不要忘记调用 {@link ObjectAnimator#start()}</p>
     * @param animationIndex 索引
     * @param times          从0到{@link ModelAnimation#getDuration()}经过的时间，{@link ModelAnimation}将在两者之间进行动画。
     * @see #ofAnimationTime(AnimatableModel, ModelAnimation, float...)
     */
    public static ObjectAnimator ofAnimationTime(AnimatableModel model, int animationIndex
            , float... times) {
        return ofAnimationTime(model, model.getAnimation(animationIndex)
                , times);
    }

    /**
     * 创建动画对象
     * <b>setAutoCancel(true) 不起作用</b>
     * <p>不要忘记调用 {@link ObjectAnimator#start()}</p>
     * @param animation 动画
     * @param times          从0到{@link ModelAnimation#getDuration()}经过的时间，{@link ModelAnimation}将在两者之间进行动画。
     * @return The constructed ObjectAnimator
     */
    public static ObjectAnimator ofAnimationTime(AnimatableModel model
            , ModelAnimation animation, float... times) {
        return ofPropertyValuesHolder(model, animation
                , PropertyValuesHolder.ofAnimationTime(animation, times));
    }

    /**
     * 创建动画对象
     * <b>setAutoCancel(true) 不起作用</b>
     * <p>不要忘记调用 {@link ObjectAnimator#start()}</p>
     * @param animationName 动画名称
     * @param frames        帧数
     * @see #ofAnimationFrame(AnimatableModel, ModelAnimation, int...)
     * @see ModelAnimation#getName()
     */
    public static ObjectAnimator ofAnimationFrame(AnimatableModel model, String animationName, int... frames) {
        return ofAnimationFrame(model, getAnimationByName(model, animationName), frames);
    }

    /**
     * 创建动画对象
     * <b>setAutoCancel(true) 不起作用</b>
     * <p>不要忘记调用 {@link ObjectAnimator#start()}</p>
     * @see #ofAnimationFrame(AnimatableModel, ModelAnimation, int...)
     */
    public static ObjectAnimator ofAnimationFrame(AnimatableModel model, int animationIndex, int... frames) {
        return ofAnimationFrame(model, model.getAnimation(animationIndex), frames);
    }

    /**
     * 创建动画对象
     * <b>setAutoCancel(true) 不起作用</b>
     * <p>不要忘记调用 {@link ObjectAnimator#start()}</p>
     * @return ObjectAnimator
     * @see #ofAnimationTime(AnimatableModel, ModelAnimation, float...)
     */
    public static ObjectAnimator ofAnimationFrame(AnimatableModel model
            , ModelAnimation animation, int... frames) {
        return ofPropertyValuesHolder(model, animation
                , PropertyValuesHolder.ofAnimationFrame(animation, frames));
    }

    /**
     * 创建动画对象
     * <b>setAutoCancel(true) 不起作用</b>
     * <p>不要忘记调用 {@link ObjectAnimator#start()}</p>
     * @return ObjectAnimator
     * @see #ofAnimationFraction(AnimatableModel, ModelAnimation, float...)
     * @see ModelAnimation#getName()
     */
    public static ObjectAnimator ofAnimationFraction(AnimatableModel model, String animationName, float... fractions) {
        return ofAnimationFraction(model, getAnimationByName(model, animationName), fractions);
    }

    /**
     * 创建动画对象
     * <b>setAutoCancel(true) 不起作用</b>
     * <p>不要忘记调用 {@link ObjectAnimator#start()}</p>
     * @return ObjectAnimator
     * @see #ofAnimationFraction(AnimatableModel, ModelAnimation, float...)
     */
    public static ObjectAnimator ofAnimationFraction(AnimatableModel model, int animationIndex, float... fractions) {
        return ofAnimationFraction(model, model.getAnimation(animationIndex), fractions);
    }

    /**
     * 创建动画对象
     * <b>setAutoCancel(true) 不起作用</b>
     * <p>不要忘记调用 {@link ObjectAnimator#start()}</p>
     * @return ObjectAnimator
     * @see #ofAnimationTime(AnimatableModel, ModelAnimation, float...)
     */
    public static ObjectAnimator ofAnimationFraction(AnimatableModel model
            , ModelAnimation animation, float... fractions) {
        return ofPropertyValuesHolder(model, animation
                , PropertyValuesHolder.ofAnimationFraction(animation, fractions));
    }

    private static ObjectAnimator ofPropertyValuesHolder(AnimatableModel model, ModelAnimation animation, android.animation.PropertyValuesHolder value) {
        ObjectAnimator objectAnimator = ofPropertyValuesHolder(model, value);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
                animation.setTimePosition(0);
            }
        });
        objectAnimator.setDuration(animation.getDurationMillis());
        objectAnimator.setAutoCancel(true);
        return objectAnimator;
    }

    /**
     * 创建动画对象
     * <b>setAutoCancel(true) 不起作用</b>
     * <p>不要忘记调用 {@link ObjectAnimator#start()}</p>
     * @return ObjectAnimator
     */
    public static ObjectAnimator ofPropertyValuesHolder(AnimatableModel model, android.animation.PropertyValuesHolder... values) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(model, values);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        return objectAnimator;
    }

    /**
     * 根据名称获取关联的动画，如果给定名称中不存在动画，则为空。
     * <p>
     *     该名称应与模型中定义和导出的名称对应。
     *     <br>通常是在3D创建软件中定义的动作名称。
     * </p>
     *
     * @param name 对象的字符串名称的弱引用
     *            如果没有指定，返回<code>String.valueOf(animation.getIndex())</code>>
     */
    private static ModelAnimation getAnimationByName(AnimatableModel model, String name) {
        for (int i = 0; i < model.getAnimationCount(); i++) {
            ModelAnimation animation = model.getAnimation(i);
            if (TextUtils.equals(animation.getName(), name)) {
                return model.getAnimation(i);
            }
        }
        return null;
    }

    /**
     * 该类保存有关属性和该属性的值的信息
     * 动画播放过程中用到
     * <pre>
     * ObjectAnimator.hasSameTargetAndProperties(Animator anim) {
     *      PropertyValuesHolder[] theirValues = ((ObjectAnimator) anim).getValues();
     *      if (((ObjectAnimator) anim).getTarget() == getTarget() &&
     *              mValues.length == theirValues.length) {
     *          for (int i = 0; i < mValues.length; ++i) {
     *              PropertyValuesHolder pvhMine = mValues[i];
     *              PropertyValuesHolder pvhTheirs = theirValues[i];
     *              if (pvhMine.getPropertyName() == null ||
     *                      !pvhMine.getPropertyName().equals(pvhTheirs.getPropertyName())) {
     *                  return false;
     *              }
     *          }
     *          return true;
     *      }
     *  }
     * </pre>
     *
     * @see ObjectAnimator
     */

    public static class PropertyValuesHolder {

        /**
         * 为目标{@link ModelAnimation}构造并返回PropertyValuesHolder。
         * 此方法默认将This应用于返回的ObjectAnimator:
         * <ul>
         * <li>持续时间值要{@link ModelAnimation#getDuration()}以便匹配原来的动画速度。</li>
         * <li>插入器为{@link LinearInterpolator}以匹配自然动画的插值。</li>
         * </ul>
         * @param animation Animation
         * @return PropertyValuesHolder
         */
        public static android.animation.PropertyValuesHolder ofAnimation(ModelAnimation animation) {
            return ofAnimationTime(animation, 0, animation.getDuration());
        }

        /**
         * 构造并返回一个指定动画时间的PropertyValuesHolder
         * @param times         运行时间(从0到{@link ModelAnimation#getDuration()}，
         *                      动画{@link ModelAnimation}将在两者之间进行动画。
         * @see #ofAnimationTime(ModelAnimation, float...)
         */
        public static android.animation.PropertyValuesHolder ofAnimationTime(String animationName, float... times) {
            return android.animation.PropertyValuesHolder.ofFloat(new AnimationProperty<>(animationName, ModelAnimation.TIME_POSITION), times);
        }

        /**
         * 创建PropertyValuesHolder
         * @param times         运行时间(从0到{@link ModelAnimation#getDuration()}，
         *                      动画{@link ModelAnimation}将在两者之间进行动画。
         * @return PropertyValuesHolder
         */
        public static android.animation.PropertyValuesHolder ofAnimationTime(ModelAnimation animation, float... times) {
            return android.animation.PropertyValuesHolder.ofFloat(new AnimationProperty<>(animation, ModelAnimation.TIME_POSITION), times);
        }

        /**
         * 构造并返回一个指定动画时间的PropertyValuesHolder
         * @return PropertyValuesHolder
         * @see #ofAnimationFrame(String, int...)
         */
        public static android.animation.PropertyValuesHolder ofAnimationFrame(String animationName, int... frames) {
            return android.animation.PropertyValuesHolder.ofInt(new AnimationProperty<>(animationName, ModelAnimation.FRAME_POSITION), frames);
        }

        /**
         * 构造并返回一个指定动画时间的PropertyValuesHolder
         * @return PropertyValuesHolder
         */
        public static android.animation.PropertyValuesHolder ofAnimationFrame(ModelAnimation animation, int... frames) {
            return android.animation.PropertyValuesHolder.ofInt(new AnimationProperty<>(animation, ModelAnimation.FRAME_POSITION), frames);
        }


        /**
         * 构造并返回一个指定动画时间的PropertyValuesHolder
         * @return PropertyValuesHolder
         * @see #ofAnimationFraction(ModelAnimation, float...)
         */
        public static android.animation.PropertyValuesHolder ofAnimationFraction(String animationName, float... fractions) {
            return android.animation.PropertyValuesHolder.ofFloat(new AnimationProperty<>(animationName, ModelAnimation.FRACTION_POSITION), fractions);
        }

        /**
         * 构造并返回一个指定动画时间的PropertyValuesHolder
         * @return PropertyValuesHolder
         */
        public static android.animation.PropertyValuesHolder ofAnimationFraction(ModelAnimation animation, float... fractions) {
            return android.animation.PropertyValuesHolder.ofFloat(new AnimationProperty<>(animation, ModelAnimation.FRACTION_POSITION), fractions);
        }

        /**
         * Internal class to manage a sub Renderable Animation property
         */
        private static class AnimationProperty<T> extends Property<AnimatableModel, T> {

            WeakReference<ModelAnimation> animation;
            String animationName = null;
            Property<ModelAnimation, T> property;

            public AnimationProperty(ModelAnimation animation, Property<ModelAnimation, T> property) {
                super(property.getType(), "animation[" + animation.getName() + "]." + property.getName());
                this.property = property;
                this.animation = new WeakReference<>(animation);
            }

            public AnimationProperty(String animationName, Property<ModelAnimation, T> property) {
                super(property.getType(), "animation[" + animationName + "]." + property.getName());
                this.property = property;
                this.animationName = animationName;
            }

            @Override
            public void set(AnimatableModel object, T value) {
                property.set(getAnimation(object), value);
            }

            @Override
            public T get(AnimatableModel object) {
                return property.get(getAnimation(object));
            }

            private ModelAnimation getAnimation(AnimatableModel model) {
                if (animation == null && animation.get() == null) {
                    animation = new WeakReference<>(getAnimationByName(model, animationName));
                }
                return animation.get();
            }
        }
    }
}
