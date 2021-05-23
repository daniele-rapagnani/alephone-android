package com.marathon.alephone;

import android.app.ActionBar;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import org.libsdl.app.SDLActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class MainActivity extends SDLActivity {
    private AssetManager am = null;

    public static native void setAssetManager(AssetManager mgr);
    public static native void setScenarioPath(String path);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        
        ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }

        this.am = getResources().getAssets();
        setAssetManager(am);

        Bundle b = getIntent().getExtras();

        if (b != null && b.getString("scenarioPath") != null) {
            setScenarioPath(b.getString("scenarioPath"));
        }
    }

    @Override
    protected String[] getLibraries() {
        return new String[] {
            "SDL2",
            "SDL2_image",
            "SDL2_mixer",
            "SDL2_net",
            "SDL2_ttf",
            "main"
        };
    }
}
