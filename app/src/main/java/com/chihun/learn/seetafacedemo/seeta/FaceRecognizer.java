package com.chihun.learn.seetafacedemo.seeta;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.chihun.learn.seetafacedemo.MyApp;

public class FaceRecognizer {
    private static final String TAG = FaceRecognizer.class.getSimpleName();

    private static final String BASE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "seeta";

    static {
        System.loadLibrary("facerecognize");
    }

    private FaceRecognizer() {

    }
    private static class InstanceHolder{
        private final static FaceRecognizer INSTANCE = new FaceRecognizer();
    }
    public static FaceRecognizer getInstance(){
        return InstanceHolder.INSTANCE;
    }

    /**
     * 初始化引擎，加载模式文件
     */
    public void loadEngine(String detectModelFile, String markerModelFile, String recognizeModelFile) {
        if (null == detectModelFile || "".equals(detectModelFile)) {
            Log.w(TAG, "detectModelFile file path is invalid!");
            return;
        }
        if (null == markerModelFile || "".equals(markerModelFile)) {
            Log.w(TAG, "markerModelFile file path is invalid!");
            return;
        }
        if (null == recognizeModelFile || "".equals(recognizeModelFile)) {
            Log.w(TAG, "recognizeModelFile file path is invalid!");
            return;
        }
        initNativeEngine(detectModelFile, markerModelFile, recognizeModelFile);
    }

    public void loadEngine() {
        loadEngine(getPath("fd_2_00.dat"), getPath("pd_2_00_pts5.dat"), getPath("fr_2_10.dat"));
    }

    public void registerFace() {
        List<String> list = FaceRecognizer.getAssetsPath("image");
        if (null == list || list.isEmpty()) {
            Log.w(TAG, "face list is empty!");
            return;
        }
        nativeRegisterFace(list);
    }

    /**
     * 检测图片
     * @param rgbaddr 图片数据内存地址
     * @return 识别结果
     */
    public void recognize(long rgbaddr) {
        long start = System.currentTimeMillis();
        nativeRecognition(rgbaddr);
        Log.d(TAG, "spend time: " + (System.currentTimeMillis() - start));
    }

    //该函数主要用来完成载入外部模型文件时，获取文件的路径加文件名
    public static String getPath(String file) {
        String sdcardModelPath = isSdcardAssetFileExist("model", file);
        if (null != sdcardModelPath) {
            return sdcardModelPath;
        } else {
            Context context = MyApp.getInstance();
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
                Log.i(TAG, "Failed to upload a file");
            } finally {
                if (null != inputStream) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return "";
        }
    }

    public static List<String> getAssetsPath(String dir) {
        Context context = MyApp.getInstance();
        AssetManager assetManager = context.getAssets();
        List<String> list = null;
        String[] fileNames = null;
        try {
            fileNames = assetManager.list(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (null != fileNames && fileNames.length > 0) {
            list = new ArrayList<>(fileNames.length);
            for (String fileName : fileNames) {
                Log.d(TAG, "fileName: " + fileName);
                BufferedInputStream inputStream = null;
                try {
                    inputStream = new BufferedInputStream(assetManager.open(dir + File.separator + fileName));
                    byte[] data = new byte[inputStream.available()];
                    inputStream.read(data);
                    inputStream.close();
                    File outFile = new File(context.getFilesDir(), fileName);
                    FileOutputStream os = new FileOutputStream(outFile);
                    os.write(data);
                    os.close();
                    list.add(outFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (null != inputStream) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            Log.d(TAG, "copy file finish!!!");
        } else {
            Log.d(TAG, "no file!!!");
        }
        return list;
    }

    public final static String isSdcardAssetFileExist(String folder, String fileName) {
        String dir = BASE_DIR;
        if (!TextUtils.isEmpty(folder)) {
            dir += File.separator + folder;
        }
        File file = new File(dir, fileName);
        return file.exists() ? file.getAbsolutePath() : null;
    }

    /**
     * 释放引擎
     */
    public void releaseEngine() {
        releaseNativeEngine();
    }

    //人脸检测的三个native函数
    private native int initNativeEngine(String detectModelFile, String markerModelFile, String recognizeModelFile);
    private native int nativeRegisterFace(List<String> facePaths);
    private native void nativeRecognition(long addr);
    private native int releaseNativeEngine();
}
