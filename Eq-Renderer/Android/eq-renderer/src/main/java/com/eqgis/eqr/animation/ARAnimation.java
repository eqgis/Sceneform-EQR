package com.eqgis.eqr.animation;

import android.animation.ObjectAnimator;

import com.eqgis.sceneform.Node;


/**
 * AR动画
 * */
public class ARAnimation {
    protected Node mNode;
    protected boolean isLoad = false;
    protected ObjectAnimator m_ObjectAnimator;
    protected ARAnimatorListener mARAnimatorListener;

    /**
     * 构造方法
     * */
    protected ARAnimation(){
    }

    /**
     * 构造方法
     * */
    public ARAnimation(Node node){
        mNode = node;
    }

    /**
     * 动画播放
     * */
    public void play(){
        if (m_ObjectAnimator!=null){
            m_ObjectAnimator.start();
        }
        isLoad = true;
    }

    /**
     * 动画暂停
     * */
    public void pause(){
        if (m_ObjectAnimator!=null){
            m_ObjectAnimator.pause();
        }
        isLoad = false;
    }

    /**
     * 恢复播放
     */
    public void resume(){
        if (m_ObjectAnimator!=null){
            m_ObjectAnimator.resume();
        }
        isLoad = true;
    }

    /**
     * 停止播放
     */
    public void stop(){
        if (m_ObjectAnimator!=null){
            m_ObjectAnimator.cancel();
        }
        isLoad = false;
    }

    /**
     * 获取AR元素
     * @return
     */
    protected Node getNode() {
        return mNode;
    }

    /**
     * 设置AR元素
     * @param e
     */
    protected void setNode(Node e) {
        this.mNode = e;
    }

    /**
     * 获取属性动画
     * @return
     */
    protected ObjectAnimator getObjectAnimator() {
        return m_ObjectAnimator;
    }

    /**
     * 设置属性动画
     * @param animator
     */
    protected void setObjectAnimator(ObjectAnimator animator) {
        this.m_ObjectAnimator = animator;
    }

    public ARAnimatorListener getARAnimatorListener() {
        return mARAnimatorListener;
    }

    /**
     * 设置动画监听
     * @param ARAnimatorListener
     */
    public void setARAnimatorListener(ARAnimatorListener ARAnimatorListener) {
        mARAnimatorListener = ARAnimatorListener;
    }

    public interface ARAnimatorListener{
        //todo 可扩展
        public void onAnimationStart();
        public void onAnimationEnd();
    }
}
