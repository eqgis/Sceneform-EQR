package com.eqgis.eqr.utils;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * XML元素节点
 * <p>与{@link XmlNodeParser}结合，用于解析XML</p>
 * <pre>SampleCode:
 * </pre>
 *
 * @author tanyx 2022/8/8
 * @version 1.0
 **/
public class XmlNode {
    private XmlNode parent;
    private String name;
    private String value;
    private ArrayList<XmlNode> children;
    private boolean root;

    private XmlNode(){
    }

    public XmlNode(XmlNode parent) {
        init(parent);
    }


    XmlNode(XmlNodeParser xmlNodeParser, Node node) {
        init(null);
        xmlNodeParser.addChild(this,node);
    }

    /**
     * 判断是否是根节点
     * @return boolean
     */
    public boolean isRoot(){
        return root;
    }

    /**
     * 判断子集是否为空
     * @return
     */
    public boolean isChildEmpty(){
        if (children == null || children.size() == 0){
            return true;
        }return false;
    }

    /**
     * 获取parent
     * @return
     */
    public XmlNode getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ArrayList<XmlNode> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<XmlNode> children) {
        this.children = children;
    }

    //<editor-fold> 内部方法
    private void init(XmlNode p) {
        //若parent为null，则作为根节点
        if (p == null){
            this.parent = new XmlNode();
            root = true;
        }else {
            this.parent = p;
            root = false;
        }
        //在父节点的子集中添加this
        if (this.parent.children == null){
            this.parent.children = new ArrayList<XmlNode>();
        }
        this.parent.children.add(this);
    }

    //</editor-fold>
}
