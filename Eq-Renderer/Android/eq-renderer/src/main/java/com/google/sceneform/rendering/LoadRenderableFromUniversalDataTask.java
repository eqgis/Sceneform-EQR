package com.google.sceneform.rendering;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.google.sceneform.utilities.SceneformBufferUtils;
import com.google.sceneform.utilities.UrlResolverUtils;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

/** 通用数据加载任务，并初始化可渲染对象的任务 */
@SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"}) // CompletableFuture
public class LoadRenderableFromUniversalDataTask<T extends Renderable> {
    private static final String TAG = LoadRenderableFromUniversalDataTask.class.getSimpleName();
    private final T renderable;
    private final IUniversalData universalData;

    LoadRenderableFromUniversalDataTask(
            T renderable, Context context, Uri sourceUri, @Nullable Function<String, Uri> urlResolver) {
        this.renderable = renderable;
        IRenderableInternalData data = renderable.getRenderableData();
        if (data instanceof IUniversalData) {
            this.universalData =
                    (IUniversalData) data;
        } else {
            throw new IllegalStateException("Expected task type " + TAG);
        }
        this.universalData.setUrlResolver(
                missingPath -> UrlResolverUtils.getUriFromMissingResource(sourceUri, missingPath, urlResolver));
        this.universalData.setContext(context.getApplicationContext());
        this.renderable.getId().update();
    }

    /** Returns {@link CompletableFuture} for a new {@link Renderable}. */
    @SuppressWarnings({"AndroidApiChecker"})
    public CompletableFuture<T> downloadAndProcessRenderable(
            Callable<InputStream> inputStreamCreator) {

        return CompletableFuture.supplyAsync(
                        // Download byte buffer via thread pool
                        () -> {
                            try {
                                return SceneformBufferUtils.inputStreamCallableToByteArray(inputStreamCreator);
                            } catch (Exception e) {
                                throw new CompletionException(e);
                            }
                        },
                        ThreadPools.getThreadPoolExecutor())
                .thenApplyAsync(
                        (byte[] bytes) -> {
                            this.universalData.setData(bytes);
                            return renderable;
                        },
                        ThreadPools.getMainExecutor());
    }

    /**
     * 通用数据接口
     */
    public interface IUniversalData {
        void setContext(Context context);
        void setData(byte[] bytes);
        void setUrlResolver(Function<String, Uri> urlResolver);
    }
}
