# AlephOne for Android

## What is this?
This is a port of the [AlephOne](https://alephone.lhowon.org/) engine to Android.
Yes, this means you can now pay the Marathon saga on your Android phone! :robot:

## How to run
Install the APK and then copy the game's data files to `/sdcard/alephone`.

If the game complains about not finding the data files, make sure you have given the app's storage access in your phone's app settings.

You can get the marathon series data files from: 
- [Marathon](https://github.com/Aleph-One-Marathon/data-marathon)
- [Marathon 2](https://github.com/Aleph-One-Marathon/data-marathon-2)
- [Marathon Infinity](https://github.com/Aleph-One-Marathon/data-marathon-infinity)

## How to play

Controls are explained below, the rectangle represents the screen:

```
 ________________________________________________________________________________________
|                         |  Swipe Left/Right Changes Weapon  |                          |
|      Second Trigger     |                                   |                          |
|                         |      Swipe Down Toggles Map       |                          |
|_________________________|___________________________________|__________________________|
|                         |                                   |                          |
|      First Trigger      |            Action                 |       Digital Stick      |
|                         |                                   |                          |
|_________________________|___________________________________|__________________________|
```

## Building

### Dependencies
All dependencies are included in this project. 
The only requirement is that you must use the __Android NDK 18b__ or lower to build the project because of a bug in newer versions.

### Compiling
Just clone the repository with:

```sh
git clone --recursive git@github.com:daniele-rapagnani/alephone-android.git
```

Then import the project in Android Studio or run the assemble task with:

```sh
# Use assembleRelease instead if you want the release build
./gradlew assembleDebug
```
