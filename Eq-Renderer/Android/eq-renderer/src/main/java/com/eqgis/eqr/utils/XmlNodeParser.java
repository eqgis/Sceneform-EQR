package com.eqgis.eqr.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * XML解析器
 * <p>用于解析XML</p>
 * <pre>SampleCode:
 *             XmlNode xmlNode = new XmlNodeParser().parse(file);
 *             ArrayList<XmlNode> xmlNodeList = xmlNode.getXmlNode("xml-key");
 * </pre>
 *
 * @author tanyx 2022/8/8
 * @version 1.0
 **/
public class XmlNodeParser {

    private DocumentBuilderFactory dbf;

    /**
     * 构造函数
     */
    public XmlNodeParser() {
        this.dbf = DocumentBuilderFactory.newInstance();
    }

    /**
     * 解析XML文件
     * @param xmlFile
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public XmlNode parse(File xmlFile) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilder db = dbf.newDocumentBuilder();

        //将xml文件解析
        Document document = db.parse(xmlFile);

        //desc-根节点中直接取主节点出来
        XmlNode mainNode = new XmlNode(this,document).getChildren().get(0).getChildren().get(0);
        return mainNode;
    }

    /**
     * 解析XML字符串
     * @param string xml字符串
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public XmlNode parse(String string) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder db = dbf.newDocumentBuilder();

        //将xml字符串解析为document
        ByteArrayInputStream inputStream = new ByteArrayInputStream(string.getBytes());
        Document document = db.parse(inputStream);
        inputStream.close();

        //desc-根节点中直接取主节点出来
        XmlNode mainNode = new XmlNode(this,document).getChildren().get(0).getChildren().get(0);
        return mainNode;
    }
    /**
     * 解析XML字符串
     * @param inputStream 输入流
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public XmlNode parse(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder db = dbf.newDocumentBuilder();

        //将xml字符串解析为document
        Document document = db.parse(inputStream);
        inputStream.close();

        //desc-根节点中直接取主节点出来
        XmlNode mainNode = new XmlNode(this,document).getChildren().get(0).getChildren().get(0);
        return mainNode;
    }


    /**
     * 通过指定名称在父节点中查找指定节点
     * <p>通常用于找到指定对象，然后通过{@link XmlNode#getParent()}获取其父节点。</p>
     * @param parentXmlNode 父节点
     * @param name 名称
     * @return 子节点
     */
    public ArrayList<XmlNode> findXmlNode(XmlNode parentXmlNode,String name){
        ArrayList<XmlNode> result = new ArrayList<XmlNode>();
        return findChild(parentXmlNode,result,name);
    }


    //<editor-fold> 子类通过重写以下方法，提高效率
    /**
     * 通过指定名称在父节点中查找指定节点
     * <p>本类的子类可重写本方法，实现在查询子节点时就读取出相关属性，以便提升效率</p>
     * @param node
     * @param result
     * @param name
     * @return
     */
    private ArrayList<XmlNode> findChild(XmlNode node, ArrayList<XmlNode> result, String name) {
        if (!node.isChildEmpty()){
            ArrayList<XmlNode> children = node.getChildren();
            //子节点不为空
            for (XmlNode child : children) {
                if (name.equals(child.getName())){
                    //根据名称找到该节点，将该节点放入结果集
                    result.add(child);
                }else {
                    //同时向下继续检索（备注：此处若在父节点位置找到了指定名称的节点，则不在该父节点向下检索）
                    findChild(child,result,name);
                }
            }
        }
        return result;
    }

    /**
     * 添加子节点
     * <p>本类的子类可重写本方法，实现在添加子节点时就读取出相关属性，以便提升效率</p>
     * @param parent
     * @param node
     */
    protected void addChild(XmlNode parent, Node node){
        if ("#text".equals(node.getNodeName())){
            //将text类型直接作为父节点的值，例如“<Version>1.0</Version>”这样的示例，直接将“1.0”作为“Version”的值
            String value = node.getNodeValue();
            if ("\n".equals(value)){
                parent.setValue(null);
            }else {
                parent.setValue(value);
            }
            return;
        }
        XmlNode xmlNode = new XmlNode(parent);
        xmlNode.setName(node.getNodeName());
        xmlNode.setValue(node.getNodeValue());

        NodeList childNodes = node.getChildNodes();
        if (childNodes!=null && childNodes.getLength()!=0){
            for (int i = 0; i < childNodes.getLength(); i++) {
                addChild(xmlNode,childNodes.item(i));
            }
        }
    }
    //</editor-fold>
}
