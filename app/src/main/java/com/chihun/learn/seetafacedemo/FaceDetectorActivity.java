package com.chihun.learn.seetafacedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import com.chihun.learn.seetafacedemo.seeta.FaceDetector;

public class FaceDetectorActivity extends AppCompatActivity {

    private static final String TAG = FaceDetectorActivity.class.getSimpleName();

    private CameraBridgeViewBase cameraBridgeViewBase;
    private Mat mRgba;
    private Mat mGray;

    private BaseLoaderCallback _baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    cameraBridgeViewBase.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    private CameraBridgeViewBase.CvCameraViewListener2 mCvCameraViewListener2 = new CameraBridgeViewBase.CvCameraViewListener2() {

        private FaceDetector mFaceDetect = FaceDetector.getInstance();

        @Override
        public void onCameraViewStarted(int width, int height) {
            Log.d(TAG, "onCameraViewStarted()");
            mRgba = new Mat(height, width, CvType.CV_8UC4);
            mGray = new Mat(height, width, CvType.CV_8UC1);
            //在这里调用所有需要提前初始化的native方法
            mFaceDetect.loadEngine();
        }

        @Override
        public void onCameraViewStopped() {
            mRgba.release();
            mGray.release();

            //在这里析构和释放所有之前初始化和分配内存的native方法
            mFaceDetect.releaseEngine();
        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            mRgba = inputFrame.rgba();
            mGray = inputFrame.gray();

            //在这里调用处理每一张frame的native方法 记得在方法中传入的是long型的
            mFaceDetect.detect(mRgba.getNativeObjAddr());
            return mRgba;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_face_detector);

        initView();
    }

    private void initView() {
        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(mCvCameraViewListener2);

        //在这里设置图像的大小，在手机中图像需要横屏状态，图片太大的话会卡顿
        cameraBridgeViewBase.setMaxFrameSize(640, 480);

        cameraBridgeViewBase.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                finish();
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        disableCamera();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, _baseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            _baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        disableCamera();
    }

    public void disableCamera() {
        if (cameraBridgeViewBase != null)
            cameraBridgeViewBase.disableView();
    }
}
