TOP_PATH := $(call my-dir)

include $(CLEAR_VARS)
include $(TOP_PATH)/SDL2/Android.mk

include $(CLEAR_VARS)
LOCAL_CFLAGS := -w
include $(TOP_PATH)/SDL_ttf/Android.mk