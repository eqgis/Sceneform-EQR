package com.eqgis.sceneform.rendering;

import android.media.Image;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.eqgis.sceneform.utilities.BufferHelper;
import com.eqgis.sceneform.CustomDepthImage;
import com.google.android.filament.Texture;
import com.eqgis.sceneform.utilities.AndroidPreconditions;
import com.eqgis.sceneform.utilities.Preconditions;

import java.nio.ByteBuffer;

/**
 * <pre>
 *     The DepthTexture class holds a special Texture to store
 *     information from a DepthImage or RawDepthImage to implement the occlusion of
 *     virtual objects behind real world objects.
 * </pre>
 */
public class DepthTexture {
    @Nullable private final Texture filamentTexture;
    private final Handler handler = new Handler(Looper.myLooper());

    /**
     * <pre>
     *      A call to this constructor creates a new Filament Texture which is
     *      later used to feed in data from a DepthImage.
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
     * <pre>
     *      A call to this constructor creates a new Filament Texture which is
     *      later used to feed in data from a DepthImage.
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
    public Texture getFilamentTexture() {
        return Preconditions.checkNotNull(filamentTexture);
    }
    /**
     * <pre>
     *     This is the most important function of this class.
     *     The Filament Texture is updated based on the newest
     *     DepthImage. To solve a problem with a to early
     *     released DepthImage the ByteBuffer which holds all
     *     necessary data is cloned. The cloned ByteBuffer is unaffected
     *     of a released DepthImage and therefore produces not
     *     a flickering result.
     * </pre>
     *
     * @param depthImage {@link Image}
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
     * update texture
     * @author Ikkyu 2022/01/24
     * @param depthImage
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
     * Cleanup filament objects after garbage collection
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
