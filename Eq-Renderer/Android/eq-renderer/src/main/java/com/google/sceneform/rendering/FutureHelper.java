package com.google.sceneform.rendering;

import android.util.Log;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** 用于打印错误信息 */
@SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"}) // CompletableFuture
class FutureHelper {
  private  FutureHelper() {}


  /**
   * 用于exceptionally中打印错误信息
   */
  static <T> CompletableFuture<T> logOnException(
      final String tag, final CompletableFuture<T> input, final String errorMsg) {
    input.exceptionally(
        throwable -> {
          Log.e(tag, errorMsg, throwable);
          throw new CompletionException(throwable);
        });
    return input;
  }
}
