# AlephOne for Android

## What is this?
This is a port of the [AlephOne](https://alephone.lhowon.org/) engine to Android.
Yes, this means you can now pay the Marathon saga on your Android phone! :robot:

## Dependencies
All dependencies are included in this project. 
The only requirement is that you must use the __Android NDK 18b__ or lower to build the project because of a bug in newer versions.

## Building
Just clone the repository with:

```sh
git clone --recursive git@github.com:daniele-rapagnani/alephone-android.git
```

Then import the project in Android Studio or run the assemble task with:

```sh
# Use assembleRelease instead if you want the release build
./gradlew assembleDebug
```
