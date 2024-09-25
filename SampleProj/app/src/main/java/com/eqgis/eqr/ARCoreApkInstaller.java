package com.eqgis.eqr;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.eqgis.ar.ARPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * ARCoreAPK安装器
 * <p>
 *     从assets目录安装ARCore，当前对应使用ARCore_v1.45
 * </p>
 * @author tanyx 2024/9/24
 * @version 1.0
 * <br/>SampleCode:<br/>
 * <code>
 *
 * </code>
 **/
public class ARCoreApkInstaller {
    private final String apkPackName = "com.google.ar.core";
    private String apkName;
    private Context mContext;
    private Thread subThread;

    public ARCoreApkInstaller() {
    }

    public ARCoreApkInstaller(Context context, String name) {
        mContext = context;
        apkName = name;
    }



    class installTask implements Runnable {
        @Override
        public void run() {
            if (!ARPlugin.isARApkReady(mContext)) {

                AssetManager assets = mContext.getAssets();
                try
                {
                    //获取assets资源目录下的apk,为了避免被编译压缩，修改后缀名。
                    InputStream stream = assets.open(apkName);
                    if(stream==null){
                        Log.e(ARCoreApkInstaller.class.getSimpleName(), "The apk about ARCore was null.");
                        return;
                    }
                    String absolutePath = mContext.getCacheDir().getAbsolutePath();
                    String folder = absolutePath+ File.separator + "apktemp" + File.separator;
                    File f=new File(folder);
                    if(!f.exists()){
                        f.mkdir();
                    }
                    String apkPath = absolutePath + File.separator + "apktemp" + File.separator + "tmp.apk";
                    File file = new File(apkPath);
                    //创建apk文件
                    file.createNewFile();
                    //将资源中的文件重写到sdcard中
                    //<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
                    writeStreamToFile(stream, file);
                    //安装apk
                    //<uses-permission android:name="android.permission.INSTALL_PACKAGES" />
                    installApk(apkPath);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void writeStreamToFile(InputStream stream, File file)
    {
        try
        {
            OutputStream output = null;
            try
            {
                output = new FileOutputStream(file);
            }
            catch (FileNotFoundException e1)
            {
                // TODO Auto-generated catch block
//                e1.printStackTrace();
                Log.w(ARCoreApkInstaller.class.getSimpleName(), "writeStreamToFile: "+e1);
            }
            try
            {
                try
                {
                    final byte[] buffer = new byte[1024];
                    int read;

                    while ((read = stream.read(buffer)) != -1)
                        output.write(buffer, 0, read);

                    output.flush();
                }
                finally
                {
                    output.close();
                }
            }
            catch (Exception e)
            {
                Log.w(ARCoreApkInstaller.class.getSimpleName(), "writeStreamToFile: "+e);
            }
        }
        finally
        {
            try
            {
                stream.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void installApk(String apkPath)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File apkFile = new File(apkPath);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            Uri uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileprovider", apkFile);
//            intent.setDataAndType(uri, "application/vnd.android.package-archive");
//        } else {
//            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
//        }
        Uri apkUri = Uri.fromFile(apkFile);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mContext.startActivity(intent);
    }


    public ARCoreApkInstaller install() {
        if (subThread != null && subThread.isAlive()) {
            return this;
        }
        subThread = new Thread(new installTask());
        subThread.start();
        return this;
    }

    public void uninstall() {
        Uri packageURI = Uri.parse("package:" + apkPackName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        mContext.startActivity(uninstallIntent);
    }


    public boolean hasInstalled() {
        final PackageManager packageManager = mContext.getPackageManager();
        List<PackageInfo> installedPackInfoList = packageManager.getInstalledPackages(0);
        for (int i = 0; installedPackInfoList != null && i < installedPackInfoList.size(); i++) {
            PackageInfo installedPackInfo = installedPackInfoList.get(i);
            if (installedPackInfo != null && TextUtils.equals(apkPackName, installedPackInfo.packageName)) {
                String newApkPath = mContext.getCacheDir() + File.separator + "apktemp" + File.separator + "tmp.apk";
                copyApkFromAssets(mContext, apkName, newApkPath);
                PackageInfo assetPackInfo = packageManager.getPackageArchiveInfo(newApkPath, PackageManager.GET_ACTIVITIES);
                if (assetPackInfo != null) {
                    String assetVersionName = assetPackInfo.versionName;
                    int assetVersionCode = assetPackInfo.versionCode;
                    if (!TextUtils.equals(assetVersionName, installedPackInfo.versionName) || installedPackInfo.versionCode < assetVersionCode) {
                        return false;
                    } else {
                        return true;
                    }
                }


                return true;
            }
        }
        return false;
    }

    public boolean copyApkFromAssets(Context context, String fileName, String path) {
        boolean copyIsFinish = false;
        try {
            InputStream is = context.getAssets().open(fileName);
            File file = new File(path);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();
            copyIsFinish = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return copyIsFinish;
    }
}