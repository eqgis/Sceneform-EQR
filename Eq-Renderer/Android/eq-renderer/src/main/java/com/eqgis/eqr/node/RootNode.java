package com.eqgis.eqr.node;

import androidx.annotation.Nullable;

import com.eqgis.exception.NotSupportException;
import com.eqgis.sceneform.AnchorNode;
import com.eqgis.sceneform.NodeParent;
import com.eqgis.sceneform.Scene;
import com.eqgis.sceneform.rendering.Light;
import com.eqgis.sceneform.rendering.Renderable;
import com.eqgis.sceneform.rendering.RenderableInstance;
import com.eqgis.ar.ARAnchor;

/**
 * 根节点
 **/
public class RootNode extends AnchorNode {

    /**
     * 构造函数
     */
    public RootNode() {
        setName("RootNode");
    }

    /**
     * 构造函数
     * @param anchor AR锚
     */
    public RootNode(ARAnchor anchor) {
        super(anchor);
        setName("RootNode");
    }

    /**
     * 设置锚点信息
     * @param anchor AR锚
     */
    @Override
    public void setAnchor(@Nullable ARAnchor anchor) {
        if (super.getAnchor() != null){
            super.getAnchor().detach();
        }
        super.setAnchor(anchor);
    }

    /**
     * 根节点不可设置渲染对象
     */
    @Override
    public RenderableInstance setRenderable(@Nullable Renderable renderable) {
        throw new NotSupportException();
    }

    /**
     * 根节点不可设置光源信息
     */
    @Override
    public void setLight(@Nullable Light light) {
        throw new NotSupportException();
    }


    @Override
    public void setParent(@Nullable NodeParent parent) {
        if (!(parent instanceof Scene)){
            //仅支持scene作为rootNode的父节点
            throw new IllegalArgumentException("only support scene");
        }
        super.setParent(parent);
    }
}
