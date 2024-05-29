package com.eqgis.eqr.track;

import com.eqgis.ar.ARAugmentedImage;

import java.util.Collection;

/**
 * 图片识别监听事件
 * @author tanyx 2023/7/4
 * @version 1.0
 * <br/>SampleCode:<br/>
 * <code>
 *
 * </code>
 **/
public interface ARImageTrackListener {
    void onImageChanged(Collection<ARAugmentedImage> images);
}
