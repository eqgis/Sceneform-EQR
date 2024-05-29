package com.eqgis.ar;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * AR增强图像数据库
 * <p>存储图片数据集，与2D图片识别配合使用，采集并保存到图片数据库中的图片越多，可识别的图片也越多。</p>
 * @author tanyx
 */
public class ARAugmentedImageDatabase {
    com.google.ar.core.AugmentedImageDatabase coredatabase = null;
    com.huawei.hiar.ARAugmentedImageDatabase hwdatabase = null;

    ARAugmentedImageDatabase( com.google.ar.core.AugmentedImageDatabase coreobj,com.huawei.hiar.ARAugmentedImageDatabase hwobj ){
        if (coreobj==null && hwobj==null){
            throw new IllegalArgumentException();
        }
        coredatabase = coreobj;
        hwdatabase = hwobj;
    }

    /**
     * 构造函数
     * @param session {@link ARSession} ARSeesion对象
     */
    public ARAugmentedImageDatabase(ARSession session) {
        if (session.coreSession!=null){
            coredatabase = new com.google.ar.core.AugmentedImageDatabase(session.coreSession);
        }else{
            hwdatabase = new com.huawei.hiar.ARAugmentedImageDatabase(session.hwSession);
        }
    }

    /**
     * 使用图片数据流创建图片数据库，图片流通过ARAugmentedImageDatabase.serialize()获取数据。
     * @param session ARSession对象
     * @param inputStream 图片数据流，可以从ARAugmentedImageDatabase.serialize()获取数据。
     * @return
     * @throws IOException
     */
    public static  ARAugmentedImageDatabase deserialize(ARSession session, InputStream inputStream) throws IOException{
        if (session.coreSession!=null){
            com.google.ar.core.AugmentedImageDatabase p=com.google.ar.core.AugmentedImageDatabase.deserialize(session.coreSession,inputStream);
            if (p==null)return null;
            return new ARAugmentedImageDatabase(p , null);
        }else{
            com.huawei.hiar.ARAugmentedImageDatabase p =com.huawei.hiar.ARAugmentedImageDatabase.deserialize(session.hwSession,inputStream);
            if (p==null)return null;
            return new ARAugmentedImageDatabase(null,p);
        }
    }

    /**
     * 添加一个已知尺寸（以米为单位的物理宽度）的Bitmap图片到数据库中，并输入该图像的物理宽度（以米为单位的物理宽度），添加成功后，返回图片在数据库中存储的索引。
     * @param name 图片在图像库中使用的名称。
     * @param bitmap 图片Bitmap对象。
     * @param widthInMeters 图片实际宽度，以米为单位。
     * @return 图片存储索引值。
     */
    public int addImage(String name, Bitmap bitmap, float widthInMeters) {
        if (coredatabase!=null){
            return coredatabase.addImage(name,bitmap,widthInMeters);
        }else {
            return hwdatabase.addImage(name,bitmap,widthInMeters);
        }
    }

    /**
     * 添加一张Bitmap图片到数据库中，添加成功后，返回图片在数据库中存储的索引。
     * @param name 图片在图像库中使用的名称。
     * @param bitmap 图片Bitmap对象。
     * @return 图片存储索引值。
     */
    public int addImage(String name, Bitmap bitmap) {
        if (coredatabase!=null){
            return coredatabase.addImage(name,bitmap );
        }else {
            return hwdatabase.addImage(name,bitmap );
        }
    }

    /**
     * 获取数据库中保存的图片数目，需要在算法启动（调用ARSession.update）以后才能正常使用。
     * @return 图片存储张数。
     */
    public int getNumImages() {
        if (coredatabase!=null){
            return coredatabase.getNumImages();
        }else {
            return hwdatabase.getNumImages();
        }
    }

    /**
     * 获取数据库中图片的数据流，需要在算法启动（调用ARSession.update）以后才能正常使用。
     * @param outputStream 获取到的图片流数据。
     * @throws IOException
     */
    public void serialize(OutputStream outputStream) throws IOException {
        if (coredatabase!=null){
            coredatabase.serialize(outputStream);
        }else {
            hwdatabase.serialize(outputStream);
        }
    }

}
