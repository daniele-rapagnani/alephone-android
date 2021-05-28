package com.marathon.alephone.scenario;

import android.content.Context;
import android.net.Uri;

import com.marathon.alephone.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ScenarioExporter {
    public interface IScenarioExportListener {
        void onExportSuccess(Uri dst, ScenarioEntry scenario);
        void onExportError(ScenarioEntry scenario, String error);
    }

    public static class Manifest {
        public final String scenario;
        public final int version;

        public Manifest(String scenario, int version) {
            this.scenario = scenario;
            this.version = version;
        }

        public Manifest(JSONObject obj) throws JSONException {
            this.scenario = obj.getString(MANIFEST_SCENARIO_KEY);
            this.version = obj.getInt(MANIFEST_VERSION_KEY);
        }
    }

    public final static String MANIFEST_NAME = "manifest.json";
    private final static String MANIFEST_SCENARIO_KEY = "scenario";
    private final static String MANIFEST_VERSION_KEY = "version";
    private final static int MANIFEST_VERSION = 1;

    private final ScenarioEntry scenario;
    private final Context context;

    public ScenarioExporter(ScenarioEntry scenario, Context context) {
        this.scenario = scenario;
        this.context = context;
    }

    public static Manifest readManifest(Uri dst, Context context) {
        Manifest manifest = null;

        try {
            InputStream is = context.getContentResolver().openInputStream(dst);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));

            ZipEntry ze;

            while ((ze = zis.getNextEntry()) != null) {
                if (ze.getName().equals(MANIFEST_NAME)) {
                    byte[] buffer = new byte[1024];
                    int length = 0;
                    String string = "";

                    while ((length = zis.read(buffer, 0, buffer.length)) > 0) {
                        string += new String(buffer, 0, length, "UTF8");
                    }

                    manifest = new Manifest(new JSONObject(string));
                }

                zis.closeEntry();

                if (manifest != null) {
                    break;
                }
            }

            zis.close();
            is.close();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return manifest;
    }

    public void export(Uri dst, IScenarioExportListener listener) {
        List<File> files = getFilesToExport();

        if (files.isEmpty()) {
            listener.onExportError(
                this.scenario,
                this.context.getString(R.string.scenario_data_export_error_nothing)
            );

            return;
        }

        File rootPath = new File(this.scenario.rootPath);

        try {
            OutputStream os = this.context.getContentResolver().openOutputStream(dst);
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
            os.close();
        } catch (IOException | JSONException e) {
            listener.onExportError(this.scenario, e.getLocalizedMessage());
        }

        listener.onExportSuccess(dst, this.scenario);
    }

    private void writeManifest(ZipOutputStream zos) throws JSONException, IOException {
        JSONObject manifest = new JSONObject();
        manifest.put(MANIFEST_SCENARIO_KEY, this.scenario.scenarioName);
        manifest.put(MANIFEST_VERSION_KEY, MANIFEST_VERSION);

        String manifestString = manifest.toString();
        zos.putNextEntry(new ZipEntry(MANIFEST_NAME));
        zos.write(manifestString.getBytes(), 0, manifestString.getBytes().length);
        zos.closeEntry();
    }

    private List<File> getFilesToExport() {
        List<String> dirs = new ArrayList<>(Arrays.asList(
            "Saved Games",
            "Quick Saves",
            "Recordings",
            "Screenshots"
        ));
        List<File> result = new ArrayList<>();

        ScenarioScanner sc = new ScenarioScanner(new File(this.scenario.rootPath));
        Node preferencesFile = sc.findXPath("//stringset[@index=\"129\"]/string[@index=\"4\"]/text()");

        if (preferencesFile != null) {
            dirs.add(preferencesFile.getNodeValue());
        } else {
            dirs.add("Aleph One Preferences");
        }

        for (String dir : dirs) {
            File f = new File(this.scenario.rootPath, dir);
            listRecursive(f, result);
        }

        return result;
    }

    private void listRecursive(File root, List<File> output) {
        if (!root.exists()) {
            return;
        }

        if (!root.isDirectory()) {
            output.add(root);
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
