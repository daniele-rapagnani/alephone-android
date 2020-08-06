package com.marathon.alephone;

import android.content.res.AssetManager;
import android.os.Bundle;

import org.libsdl.app.SDLActivity;

public class MainActivity extends SDLActivity {
    private AssetManager am = null;

    public static native void setAssetManager(AssetManager mgr);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.am = getResources().getAssets();
        setAssetManager(am);
    }

    @Override
    protected String[] getLibraries() {
        return new String[] {
                "hidapi",
                "SDL2",
                "SDL2_image",
                "SDL2_mixer",
                // "SDL2_net",
                "SDL2_ttf",
                "main"
        };
    }
}
