LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := SDL2
LOCAL_SRC_FILES := $(LOCAL_PATH)/../../../../../distribution/SDL2/lib/${BUILD_TYPE}/${APP_ABI}/libSDL2.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/../../../../../distribution/SDL2/include/SDL
include $(PREBUILT_SHARED_LIBRARY)