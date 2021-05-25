package com.marathon.alephone.scenario;

import android.content.Context;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ScenarioExporter {
    public interface IScenarioExportListener {
        void onExportSuccess(Uri dst, ScenarioEntry scenario);
        void onExportError(ScenarioEntry scenario, String error);
    }

    private final static String MANIFEST_NAME = "manifest.json";
    private final static String MANIFEST_SCENARIO_KEY = "scenario";

    private final ScenarioEntry scenario;
    private final Context context;

    public ScenarioExporter(ScenarioEntry scenario, Context context) {
        this.scenario = scenario;
        this.context = context;
    }

    public void export(Uri dst, IScenarioExportListener listener) {
        List<File> files = getFilesToExport();

        if (files.isEmpty()) {
            listener.onExportError(this.scenario, "Nothing to export");
            return;
        }

        File rootPath = new File(this.scenario.rootPath);

        try {
            OutputStream os = context.getContentResolver().openOutputStream(dst);
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os));

            writeManifest(zos);

            for (File f : files) {
                String rel = rootPath.toURI().relativize(f.toURI()).getPath();
                zos.putNextEntry(new ZipEntry(rel));

                FileInputStream fis = new FileInputStream(f);

                int length = 0;

                byte[] buffer = new byte[1024];
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }

                zos.closeEntry();
                fis.close();
            }

            zos.finish();
            zos.close();
        } catch (IOException | JSONException e) {
            listener.onExportError(this.scenario, e.getLocalizedMessage());
        }

        listener.onExportSuccess(dst, this.scenario);
    }

    private void writeManifest(ZipOutputStream zos) throws JSONException, IOException {
        JSONObject manifest = new JSONObject();
        manifest.put(MANIFEST_SCENARIO_KEY, this.scenario.scenarioName);

        String manifestString = manifest.toString();
        zos.putNextEntry(new ZipEntry(MANIFEST_NAME));
        zos.write(manifestString.getBytes(), 0, manifestString.getBytes().length);
        zos.closeEntry();
    }

    private List<File> getFilesToExport() {
        String[] dirs = new String[] { "Saved Games", "Quick Saves" };
        List<File> result = new ArrayList<>();

        for (String dir : dirs) {
            File f = new File(this.scenario.rootPath, dir);

            if (f.exists() && f.isDirectory()) {
                listRecursive(f, result);
            }
        }

        return result;
    }

    private void listRecursive(File root, List<File> output) {
        if (!root.isDirectory() || !root.exists()) {
            return;
        }

        File[] files = root.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                listRecursive(file, output);
                continue;
            }

            output.add(file);
        }
    }
}
