LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := seetanet-prebuilt
LOCAL_SRC_FILES := $(LOCAL_PATH)/jniLibs/$(TARGET_ARCH_ABI)/libseetanet2.so
# LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include/seetanet/
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := seetafacedetector-prebuilt
LOCAL_SRC_FILES := $(LOCAL_PATH)/jniLibs/$(TARGET_ARCH_ABI)/libSeetaFaceDetector2.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := seetafacelandmarker-prebuilt
LOCAL_SRC_FILES := $(LOCAL_PATH)/jniLibs/$(TARGET_ARCH_ABI)/libSeetaFaceLandmarker2.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := seetafacerecognizer-prebuilt
LOCAL_SRC_FILES := $(LOCAL_PATH)/jniLibs/$(TARGET_ARCH_ABI)/libSeetaFaceRecognizer2.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := opencv3-prebuilt
LOCAL_SRC_FILES := $(LOCAL_PATH)/jniLibs/$(TARGET_ARCH_ABI)/libopencv_java3.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := facedetect

# MY_CPP_LIST += $(wildcard $(LOCAL_PATH)/*.cpp)
# LOCAL_SRC_FILES := $(MY_CPP_LIST:$(LOCAL_PATH)/%=%)
LOCAL_SRC_FILES := facedetect.cpp

LOCAL_C_INCLUDES += $(LOCAL_PATH)/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/seeta/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/seetanet/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/opencv/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/opencv2/

LOCAL_LDFLAGS += -L$(LOCAL_PATH)/lib -fuse-ld=bfd

LOCAL_LDLIBS += -llog -lz

LOCAL_CFLAGS += -mfpu=neon-vfpv4 -funsafe-math-optimizations -ftree-vectorize  -ffast-math

LOCAL_SHARED_LIBRARIES += seetanet-prebuilt
LOCAL_SHARED_LIBRARIES += seetafacedetector-prebuilt
LOCAL_SHARED_LIBRARIES += seetafacelandmarker-prebuilt
LOCAL_SHARED_LIBRARIES += opencv3-prebuilt

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := facerecognize
LOCAL_SRC_FILES := facerecognize.cpp

LOCAL_C_INCLUDES += $(LOCAL_PATH)/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/seeta/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/seetanet/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/opencv/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/opencv2/

LOCAL_LDFLAGS += -L$(LOCAL_PATH)/lib -fuse-ld=bfd

LOCAL_LDLIBS += -llog -lz

LOCAL_CFLAGS += -mfpu=neon-vfpv4 -funsafe-math-optimizations -ftree-vectorize  -ffast-math

LOCAL_SHARED_LIBRARIES += seetanet-prebuilt
LOCAL_SHARED_LIBRARIES += seetafacedetector-prebuilt
LOCAL_SHARED_LIBRARIES += seetafacelandmarker-prebuilt
LOCAL_SHARED_LIBRARIES += seetafacerecognizer-prebuilt
LOCAL_SHARED_LIBRARIES += opencv3-prebuilt

include $(BUILD_SHARED_LIBRARY)
