package com.eqgis.sceneform.rendering;

import android.os.Handler;
import android.os.Message;
import android.os.Process;

import com.hunantv.imgo.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

class EngineHandler extends android.os.HandlerThread {
  private Handler workHandler = null;

  private volatile List<WorkMessageProxy> messageProxyList;

  private Runnable mLoadDataRunnable;

  public EngineHandler() {
    super("Filament_Engine_Handler", Process.THREAD_PRIORITY_DISPLAY);
    start();
    workHandler = new Handler(getLooper()) {
      @Override
      public void handleMessage(Message msg) {
        if (messageProxyList != null) {
          for (WorkMessageProxy workMessageProxy : messageProxyList) {
            workMessageProxy.handleMessage(msg);
          }
        }
      }
    };
  }


  public void post(Runnable run) {
    workHandler.post(run);
  }

  public void postLoadDataRunnable(Runnable runnable) {
    if (mLoadDataRunnable != null) {
      LogUtil.d("testwxy", "removeCallbacks(mLoadDataRunnable):" + mLoadDataRunnable);
      workHandler.removeCallbacks(mLoadDataRunnable);
    }
    mLoadDataRunnable = runnable;
    workHandler.post(runnable);
  }

  public void postAtFrontOfQueue(Runnable runnable) {
    workHandler.postAtFrontOfQueue(runnable);
  }

  public void postDelayed(Runnable runnable, long delay) {
    workHandler.postDelayed(runnable, delay);
  }

  public void postAtTime(Runnable runnable, long time) {
    workHandler.postAtTime(runnable, time);
  }

  public void addMessageProxy(WorkMessageProxy proxy) {
    initMessageProxyList();
    messageProxyList.add(proxy);
  }

  public void removeMessageProxy(WorkMessageProxy proxy) {
    initMessageProxyList();
    messageProxyList.remove(proxy);
  }

  private void initMessageProxyList() {
    if (messageProxyList == null) {
      messageProxyList = new ArrayList<>();
    }
  }

  public Handler getHandler() {
    return workHandler;
  }

  public interface WorkMessageProxy {
    void handleMessage(Message msg);
  }

}
