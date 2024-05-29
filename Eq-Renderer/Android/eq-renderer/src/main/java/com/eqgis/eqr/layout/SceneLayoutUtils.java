/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eqgis.eqr.layout;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.eqgis.ar.ARConfig;
import com.eqgis.ar.ARSession;
import com.eqgis.ar.exceptions.ARSessionException;

class SceneLayoutUtils {
  private static final String TAG = "SessionCreator";
  private static Handler handler = new Handler();
  /**
   * Creates and shows a Toast containing an error message. If there was an exception passed in it
   * will be appended to the toast. The error will also be written to the Log
   */
  static void displayError(
          final Context context, final String errorMsg,  final Throwable problem) {
    final String tag = context.getClass().getSimpleName();
    final String toastText;
    if (problem != null && problem.getMessage() != null) {
      Log.e(tag, errorMsg, problem);
      toastText = errorMsg + ": " + problem.getMessage();
    } else if (problem != null) {
      Log.e(tag, errorMsg, problem);
      toastText = errorMsg;
    } else {
      Log.e(tag, errorMsg);
      toastText = errorMsg;
    }
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
      }
    },0);
  }

  public static Object[] createArSession(Activity activity, ARConfig.PlaneFindingMode mode) throws ARSessionException {

    // if we have the camera permission, create the session
    ARSession session =  null;//, EnumSet.of(Session.Feature.SHARED_CAMERA));
    try {
      session = new ARSession(activity);
    }catch (ARSessionException e){
      throw new ARSessionException(e);
    }

    //sharedSession = new Session(activity, EnumSet.of(Session.Feature.SHARED_CAMERA));

    // IMPORTANT!!!  ArSceneView needs to use the non-blocking update mode.
    ARConfig config = new ARConfig(session);
    config.setUpdateMode(ARConfig.UpdateMode.LATEST_CAMERA_IMAGE);

    //@updated by tanyx_ikkyu 2021/08/04
//    config.setLightEstimationMode(Config.LightEstimationMode.DISABLED);

    //config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
    if (mode != null){
      config.setPlaneFindingMode(mode);
    }
//    config.setFocusMode(ARConfig.FocusMode.AUTO_FOCUS);
    config.setFocusMode(ARConfig.FocusMode.FIXED_FOCUS);//固定焦距，不采用自动对焦

    if (session.isDepthModeSupported()) {
      config.openDepth();
    }
    session.configure(config);

    return new Object[]{session,config};
  }
}
