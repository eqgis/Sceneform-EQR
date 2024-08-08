package com.eqgis.sceneform;

/**
 * CustomDepthImage
 * <p>
 *     JavaBean
 * </p>
 * @author Ikkyu 2022/01/22
 */
public class CustomDepthImage{
    private byte[] bytes;//data
    private int width;
    private int height;

    public CustomDepthImage(byte[] bytes, int width, int height) {
        this.bytes = bytes;
        this.width = width;
        this.height = height;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
