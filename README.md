# AlephOne for Android

## What is this?
This is a port of the [AlephOne](https://alephone.lhowon.org/) engine to Android.
Yes, this means you can now play the Marathon saga on your Android phone! :robot:

## How to run
Download the APK of the desired chapter from the [releases](https://github.com/daniele-rapagnani/alephone-android/releases) page from your phone and install it. 
Your phone may complain about the APKs signature as it is not registered with the Google Play Store, simply ignore the warnings. Enjoy!

## Known Issues

* Marathon 2/Infinity take a lot of time to start (on some phones even a minute or so), it looks like the app is frozen but it's not. After that the game goes on smoothly.
* If you put the app in background the screen is not redrawn unless you start moving.

## How to play

The game supports playing with both a bluetooth keyboard and a bluetooth joystick!
In both cases you can configure your input from the preferences screen.

If you want you can also play using touch controls.
Here's a description of how touch controls work, the outer rectangle represents the screen,
the small rectangles represent areas of the screen.

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

## Contributing

### Dependencies
All dependencies are included in this project. 
The only requirement is that you must use the __Android NDK 18b__ or lower to build the project because of a bug in newer versions. If you don't do this the game will crash on startup complaining about not finding the main `.so` library.

You can still build the project with the latest NDK version but you'll need to patch it.
You can find the patch [here](https://android-review.googlesource.com/changes/platform%2Fndk~1318770/revisions/1/patch?zip.).
For further information on the issue take a look at [this issue](https://github.com/android/ndk/issues/929).

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

This will build all three chapters of the game.
If you want just one you can build the corresponding flavor:

```sh
./gradlew assembleMarathon2Release
```
