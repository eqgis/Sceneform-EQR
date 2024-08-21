package com.google.sceneform.rendering;

import android.media.Image;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.google.sceneform.utilities.BufferHelper;
import com.google.sceneform.CustomDepthImage;
import com.google.android.filament.Texture;
import com.google.sceneform.utilities.AndroidPreconditions;
import com.google.sceneform.utilities.Preconditions;

import java.nio.ByteBuffer;

/**
 * 深度图纹理
 * <pre>
 *     AR模式下获取 DepthImage 或 RawDepthImage
 * </pre>
 */
public class DepthTexture {
    @Nullable private final Texture filamentTexture;
    private final Handler handler = new Handler(Looper.myLooper());

    /**
     * 构造函数
     * <pre>
     *      调用这个构造函数会创建一个新的Filament Texture，用于从DepthImage输入数据。
     * </pre>
     *
     * @param width int
     * @param height int
     */
    public DepthTexture(int width, int height) {
        filamentTexture = new Texture.Builder()
                .width(width)
                .height(height)
                .sampler(Texture.Sampler.SAMPLER_2D)
                .format(Texture.InternalFormat.RG8)
                .levels(1)
                .build(EngineInstance.getEngine().getFilamentEngine());

        ResourceManager.getInstance()
                .getDepthTextureCleanupRegistry()
                .register(this, new CleanupCallback(filamentTexture));
    }
    /**
     * 构造函数
     * <pre>
     *      调用这个构造函数会创建一个新的Filament Texture，用于从DepthImage输入数据。
     * </pre>
     *
     * @param width int
     * @param height int
     */
    public DepthTexture(int width, int height,Texture.InternalFormat internalFormat) {
        filamentTexture = new Texture.Builder()
                .width(width)
                .height(height)
                .sampler(Texture.Sampler.SAMPLER_2D)
                .format(/*Texture.InternalFormat.RG8*/internalFormat)
                .levels(1)
                .build(EngineInstance.getEngine().getFilamentEngine());

        ResourceManager.getInstance()
                .getDepthTextureCleanupRegistry()
                .register(this, new CleanupCallback(filamentTexture));
    }

    /**
     * 获取filament纹理对象
     * @return 纹理对象
     */
    public Texture getFilamentTexture() {
        return Preconditions.checkNotNull(filamentTexture);
    }

    /**
     * 更新深度图
     * @param depthImage {@link Image} 安卓Image对象
     */
    public void updateDepthTexture(Image depthImage) {
        if (filamentTexture == null) {
            return;
        }

        IEngine engine = EngineInstance.getEngine();

        Image.Plane plane = depthImage.getPlanes()[0];

        ByteBuffer buffer = plane.getBuffer();
        ByteBuffer clonedBuffer = BufferHelper.cloneByteBuffer(buffer);

        Texture.PixelBufferDescriptor pixelBufferDescriptor = new Texture.PixelBufferDescriptor(
                clonedBuffer,
                Texture.Format.RG,
                Texture.Type.UBYTE,
                1,
                0,
                0,
                0,
                handler,
                null
        );

        filamentTexture.setImage(
                engine.getFilamentEngine(),
                0,
                pixelBufferDescriptor
        );
        buffer.clear();
        clonedBuffer.clear();
    }

    /**
     * 更新深度图对象
     * @author Ikkyu 2022/01/24
     * @param depthImage 自定义的深度图数据对象
     * @param format 纹理格式
     */
    public void updateDepthTexture(CustomDepthImage depthImage,
                                   Texture.Format format) {
        if (filamentTexture == null) {
            return;
        }

        IEngine engine = EngineInstance.getEngine();

        //get bytes by CustomDepthImage
        byte[] bytes = depthImage.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        ByteBuffer clonedBuffer = BufferHelper.cloneByteBuffer(buffer);

        Texture.PixelBufferDescriptor pixelBufferDescriptor = new Texture.PixelBufferDescriptor(
                clonedBuffer,
                /*Texture.Format.RG*/format,
                Texture.Type.UBYTE,
                1,
                0,
                0,
                0,
                handler,
                null
        );

        filamentTexture.setImage(
                engine.getFilamentEngine(),
                0,
                pixelBufferDescriptor
        );
        buffer.clear();
        clonedBuffer.clear();
    }

    /**
     * Cleanup回调
     */
    private static final class CleanupCallback implements Runnable {
        @Nullable private final Texture filamentTexture;

        CleanupCallback(@Nullable Texture filamentTexture) {
            this.filamentTexture = filamentTexture;
        }

        @Override
        public void run() {
            AndroidPreconditions.checkUiThread();

            IEngine engine = EngineInstance.getEngine();
            if (engine == null || !engine.isValid()) {
                return;
            }
            if (filamentTexture != null) {
                engine.destroyTexture(filamentTexture);
            }
        }
    }
}
