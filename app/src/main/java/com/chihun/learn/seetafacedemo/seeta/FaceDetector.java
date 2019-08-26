package com.chihun.learn.seetafacedemo.seeta;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.chihun.learn.seetafacedemo.MyApp;

public class FaceDetector {

    private static final String TAG = FaceDetector.class.getSimpleName();
    private Context mContext;

    static {
        System.loadLibrary("facedetect");
    }

    private FaceDetector() {
        mContext = MyApp.getInstance();
    }
    private static class InstanceHolder{
        private final static FaceDetector INSTANCE = new FaceDetector();
    }
    public static FaceDetector getInstance(){
        return InstanceHolder.INSTANCE;
    }

    /**
     * 初始化引擎，加载模式文件
     */
    public void loadEngine(String detectModelFile, String markerModelFile) {
        if (null == detectModelFile || "".equals(detectModelFile)) {
            Log.w(TAG, "detectModelFile file path is invalid!");
            return;
        }
        if (null == markerModelFile || "".equals(markerModelFile)) {
            Log.w(TAG, "markerModelFile file path is invalid!");
            return;
        }
        initFaceDetection(detectModelFile, markerModelFile);
    }

    public void loadEngine() {
        if (null == mContext) {
            Log.w(TAG, "please call initial first!");
            return;
        }
        loadEngine(getPath("fd_2_00.dat", mContext), getPath("pd_2_00_pts81.dat", mContext));
    }

    /**
     * 检测图片
     * @param rgbaddr 图片数据内存地址
     * @return 识别结果
     */
    public void detect(long rgbaddr) {
        long start = System.currentTimeMillis();
        applyFaceDetection(rgbaddr);
        Log.d(TAG, "spend time: " + (System.currentTimeMillis() - start));
    }

    //该函数主要用来完成载入外部模型文件时，获取文件的路径加文件名
    public static String getPath(String file, Context context) {
        AssetManager assetManager = context.getAssets();
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(assetManager.open(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            File outFile = new File(context.getFilesDir(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            return outFile.getAbsolutePath();
        } catch (IOException ex) {
            Log.i("FileUtil", "Failed to upload a file");
        }
        return "";
    }

    /**
     * 释放引擎
     */
    public void releaseEngine() {
        releaseFaceDetection();
    }

    //人脸检测的三个native函数
    private native int initFaceDetection(String detectModelFile, String markerModelFile);
    private native void applyFaceDetection(long addr);
    private native int releaseFaceDetection();
}
