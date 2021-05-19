package com.marathon.alephone;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;

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

    protected void updateScenarioPath()
    {
        File extPath = Environment.getExternalStorageDirectory();

        if (!extPath.canRead())
        {
            return;
        }

        File scenarioSpec = new File(extPath, "AlephOneScenario.txt");

        if (!scenarioSpec.canRead())
        {
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(scenarioSpec);

            byte[] data = new byte[(int)scenarioSpec.length()];
            fis.read(data);
            fis.close();

            String path = new String(data, "UTF-8").trim();

            if (path.startsWith("#"))
            {
                return;
            }

            File scenarioPath = new File(path);

            if (scenarioPath.canRead() && scenarioPath.isDirectory())
            {
                setScenarioPath(path);
            }
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.am = getResources().getAssets();
        setAssetManager(am);
        updateScenarioPath();
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
