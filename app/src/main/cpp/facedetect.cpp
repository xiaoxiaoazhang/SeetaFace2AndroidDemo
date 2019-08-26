#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <sstream>
#include <seeta/FaceDetector.h>
#include <seeta/FaceLandmarker.h>
#include <seeta/Struct_cv.h>
#include <seeta/Struct.h>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <array>
#include <map>
#include <iostream>

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , "Seeta", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN , "Seeta", __VA_ARGS__)

static seeta::FaceDetector *FD;
static seeta::FaceLandmarker *FL;

extern "C"
JNIEXPORT jint JNICALL
Java_com_chihun_learn_seetafacedemo_seeta_FaceDetector_initFaceDetection(JNIEnv *env, jobject instance,
                                                              jstring detectModelFile_, jstring markerModelFile_) {
    const char *detectModelFile = env->GetStringUTFChars(detectModelFile_, 0);
    const char *markerModelFile = env->GetStringUTFChars(markerModelFile_, 0);
    seeta::ModelSetting::Device device = seeta::ModelSetting::AUTO;

    int id = 0;
    seeta::ModelSetting FD_model( detectModelFile, device, id );
    seeta::ModelSetting FL_model( markerModelFile, device, id );

    FD = new seeta::FaceDetector(FD_model);
    FL = new seeta::FaceLandmarker(FL_model);

    FD->set(seeta::FaceDetector::PROPERTY_VIDEO_STABLE, 1);
    FD->set(seeta::FaceDetector::PROPERTY_THRESHOLD1, 0.65f);

    int res = EXIT_SUCCESS;
    env->ReleaseStringUTFChars(detectModelFile_, detectModelFile);
    env->ReleaseStringUTFChars(markerModelFile_, markerModelFile);
    return (jint)res;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_chihun_learn_seetafacedemo_seeta_FaceDetector_applyFaceDetection(JNIEnv *env, jobject instance, jlong addr) {

    // TODO
    cv::Mat &img = *(cv::Mat *) addr;
    cv::Mat rgb_img;
    cv::cvtColor(img, rgb_img, cv::COLOR_RGBA2BGR);

    seeta::cv::ImageData simage = rgb_img;

    if(NULL == FD) {
        LOGW("FD is NULL");
        return;
    }

    auto faces = FD->detect(simage);
    LOGD("faces size: %d", faces.size);
    for (int i = 0; i < faces.size; ++i)
    {
        auto &face = faces.data[i];
        auto points = FL->mark(simage, face.pos);

        cv::rectangle(img, cv::Rect(face.pos.x, face.pos.y, face.pos.width, face.pos.height), CV_RGB(128, 128, 255), 3);
        for (auto &point : points)
        {
            cv::circle(img, cv::Point(point.x, point.y), 2, CV_RGB(128, 255, 128), -1);
        }
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_chihun_learn_seetafacedemo_seeta_FaceDetector_releaseFaceDetection(JNIEnv *env, jobject instance) {
    delete FD;
    delete FL;
    int ret = EXIT_SUCCESS;
    return (jint)ret;
}