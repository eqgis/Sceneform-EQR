package com.google.sceneform.utilities;

import java.nio.ByteBuffer;

/**
 * Buffer助手
 */
public class BufferHelper {
    /**
     * 克隆ByteBuffer
     * @param original {@link ByteBuffer}
     * @return {@link ByteBuffer}
     */
    public static ByteBuffer cloneByteBuffer(ByteBuffer original) {
        ByteBuffer clone = ByteBuffer.allocate(original.capacity());
        original.rewind();//copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }
}
