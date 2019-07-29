# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.7)

# Allow setting properties for libraries created buy
# subdirectories
cmake_policy(SET CMP0079 NEW)

project(alephone C CXX)

# SDL2
add_library(SDL SHARED IMPORTED)
set_target_properties(
        SDL PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL2/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libSDL2.so
)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL2/include/SDL)

# SDL2main
add_library(SDLmain SHARED IMPORTED)
set_target_properties(
        SDLmain PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL2/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libSDL2main.a
)

# SDL2_image
add_library(SDL_image SHARED IMPORTED)
set_target_properties(
        SDL_image PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_image/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libSDL2_image.so
)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_image/include)

# PNG
add_library(png STATIC IMPORTED)
set_target_properties(
        png PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_image/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libpng.a
)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/png/include)

# JPEG
add_library(jpeg STATIC IMPORTED)
set_target_properties(
        jpeg PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_image/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libjpeg.a
)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/jpeg /include)

# WEBP
add_library(webp STATIC IMPORTED)
set_target_properties(
        webp PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_image/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libwebp.a
)

# WEBP DECODER
add_library(webpdecoder STATIC IMPORTED)
set_target_properties(
        webpdecoder PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_image/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libwebpdecoder_static.a
)

# SDL2_mixer
add_library(SDL_mixer SHARED IMPORTED)
set_target_properties(
        SDL_mixer PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_mixer/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libSDL2_mixer.so
)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_mixer/include)

# FLAC
add_library(FLAC STATIC IMPORTED)
set_target_properties(
        FLAC PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_mixer/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libFLAC.a
)

# FLAC
add_library(modplug STATIC IMPORTED)
set_target_properties(
        modplug PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_mixer/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libmodplug.a
)

# mpg123
add_library(mpg123 SHARED IMPORTED)
set_target_properties(
        mpg123 PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_mixer/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libmpg123.so
)

# ogg
add_library(ogg STATIC IMPORTED)
set_target_properties(
        ogg PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_mixer/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libogg.a
)

# timidity
add_library(timidity STATIC IMPORTED)
set_target_properties(
        timidity PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_mixer/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libtimidity.a
)

# timidity
add_library(hidapi SHARED IMPORTED)
set_target_properties(
        hidapi PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/hidapi/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libhidapi.so
)

# SDL2_ttf
add_library(SDL_ttf SHARED IMPORTED)
set_target_properties(
        SDL_ttf PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_ttf/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libSDL2_ttf.so
)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_ttf/include)

# SDL2_net
add_library(SDL_net SHARED IMPORTED)
set_target_properties(
        SDL_net PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_net/lib/${CMAKE_BUILD_TYPE}/${CMAKE_ANDROID_ARCH_ABI}/libSDL2_net.so
)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/SDL_net/include)

# Boost
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/boost_1_70_0)

###
# Project settings
###

#add_compile_definitions(HAVE_OPENGL)
add_compile_definitions(HAVE_UNISTD_H)
add_compile_definitions(HAVE_LUA)
add_compile_definitions(HAVE_MEMMOVE)
add_compile_definitions(SDL)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/alephone)

target_link_libraries(main SDL SDL_image SDL_mixer SDL_net png jpeg webp webpdecoder FLAC modplug mpg123 ogg timidity SDL_ttf SDLmain hidapi z)