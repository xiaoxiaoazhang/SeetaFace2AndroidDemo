#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <sstream>
#include <seeta/FaceEngine.h>
#include <seeta/Struct_cv.h>
#include <seeta/Struct.h>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <array>
#include <map>
#include <iostream>

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , "Seeta", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN , "Seeta", __VA_ARGS__)

static seeta::FaceEngine *FE;
static std::map<int64_t, std::string> GalleryIndexMap;
// recognization threshold
static float threshold = 0.1;

extern "C"
JNIEXPORT jint JNICALL
Java_com_chihun_learn_seetafacedemo_seeta_FaceRecognizer_initNativeEngine(JNIEnv *env, jobject instance,
                                                              jstring detectModelFile_
                                                              , jstring markerModelFile_
                                                              , jstring recognizeModelFile_) {
    const char *detectModelFile = env->GetStringUTFChars(detectModelFile_, 0);
    const char *markerModelFile = env->GetStringUTFChars(markerModelFile_, 0);
    const char *recognizeModelFile = env->GetStringUTFChars(recognizeModelFile_, 0);

    seeta::ModelSetting::Device device = seeta::ModelSetting::AUTO;
    int id = 0;
    seeta::ModelSetting FD_model(detectModelFile, device, id );
    seeta::ModelSetting PD_model(markerModelFile, device, id );
    seeta::ModelSetting FR_model(recognizeModelFile, device, id );
    FE = new seeta::FaceEngine(FD_model, PD_model, FR_model, 2, 16 );

    //set face detector's min face size
    FE->FD.set(seeta::FaceDetector::PROPERTY_MIN_FACE_SIZE, 80 );
    FE->FD.set(seeta::FaceDetector::PROPERTY_VIDEO_STABLE, 1);
    //set face detect threshold
    FE->FD.set(seeta::FaceDetector::PROPERTY_THRESHOLD1, 0.60f);

    int res = EXIT_SUCCESS;
    env->ReleaseStringUTFChars(detectModelFile_, detectModelFile);
    env->ReleaseStringUTFChars(markerModelFile_, markerModelFile);
    env->ReleaseStringUTFChars(recognizeModelFile_, recognizeModelFile);
    return (jint)res;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_chihun_learn_seetafacedemo_seeta_FaceRecognizer_nativeRegisterFace(JNIEnv *env, jobject instance, jobject faceList) {

    jclass jArrayList = env->GetObjectClass(faceList);
    jmethodID jArrayList_get = env->GetMethodID(jArrayList, "get", "(I)Ljava/lang/Object;");
    jmethodID jArrayList_size = env->GetMethodID(jArrayList, "size", "()I");
    jint len = env->CallIntMethod(faceList, jArrayList_size);
    LOGD("face len: %d", len);
    GalleryIndexMap.clear();
    std::vector<int64_t> GalleryIndex(len);
    for (int i = 0; i < len; i++) {
        jstring filepath_ = (jstring) env->CallObjectMethod(faceList, jArrayList_get, i);
        const char *filepath = env->GetStringUTFChars(filepath_, 0);
        LOGD("filepath: %s", filepath);

        int64_t &index = GalleryIndex[i];
        seeta::cv::ImageData image = cv::imread( filepath );

        if(NULL == FE) {
            LOGW("FE is NULL");
            return;
        }
        auto id = FE->Register( image );
        LOGD("Registered id = %d, index=%d", id, index);
        GalleryIndexMap.insert( std::make_pair( index, filepath ) );
        env->ReleaseStringUTFChars(filepath_, filepath);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_chihun_learn_seetafacedemo_seeta_FaceRecognizer_nativeRecognition(JNIEnv *env, jobject instance, jlong addr) {

    if(NULL == FE) {
        LOGW("FE is NULL");
        return;
    }
    // TODO
    cv::Mat &frame = *(cv::Mat *) addr;
    cv::Mat rgb_img;
    cv::cvtColor(frame, rgb_img, cv::COLOR_RGBA2BGR);

    seeta::cv::ImageData image = rgb_img;

    // Detect all faces
    std::vector<SeetaFaceInfo> faces = FE->DetectFaces( image );

    for( SeetaFaceInfo &face : faces )
    {
        // Query top 1
        int64_t index = -1;
        float similarity = 0;

        auto points = FE->DetectPoints(image, face);

        auto queried = FE->QueryTop( image, points.data(), 1, &index, &similarity );

        cv::rectangle( frame, cv::Rect( face.pos.x, face.pos.y, face.pos.width, face.pos.height ), CV_RGB( 128, 128, 255 ), 3 );
        for (int i = 0; i < 5; ++i)
        {
            auto &point = points[i];
            cv::circle(frame, cv::Point(point.x, point.y), 2, CV_RGB(128, 255, 128), -1);
        }

        // no face queried from database
        if (queried < 1) continue;

        // similarity greater than threshold, means recognized
        LOGW("similarity: %f", similarity);
        if( similarity > threshold )
        {
            cv::putText( frame, GalleryIndexMap[index], cv::Point( face.pos.x, face.pos.y - 5 ), CV_FONT_HERSHEY_COMPLEX, 1, CV_RGB( 255, 128, 128 ) );
        }
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_chihun_learn_seetafacedemo_seeta_FaceRecognizer_releaseNativeEngine(JNIEnv *env, jobject instance) {
    delete FE;
    int ret = EXIT_SUCCESS;
    return (jint)ret;
}