package com.google.sceneform;

/**
 * 自定义的深度图
 * <p>
 *     包含深度数据（早期的ARCore采用2个byte表示一个深度，后来改用了4个byte）
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
