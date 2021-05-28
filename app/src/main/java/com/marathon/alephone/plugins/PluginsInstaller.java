package com.marathon.alephone.plugins;

import android.app.Activity;
import android.net.Uri;

import com.marathon.alephone.IInstallListener;
import com.marathon.alephone.R;
import com.marathon.alephone.scenario.ScenarioEntry;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PluginsInstaller {
    public interface IPluginInstallListener {
        void onPluginsInstallStarted();
        void onPluginsInstallDone(List<Plugin> plugins);
        void onPluginsInstallError(String error);
    }

    private final Activity activity;

    public PluginsInstaller(Activity activity) {
        this.activity = activity;
    }

    public void install(Uri file, ScenarioEntry scenario, IPluginInstallListener listener) {
        PluginsScanner scanner = new PluginsScanner(this.activity);
        List<Plugin> plugins = scanner.scan(file);

        if (plugins == null || plugins.isEmpty()) {
            listener.onPluginsInstallError(this.activity.getString(R.string.no_plugins_install_error));
            return;
        }

        try {
            File pluginsDir = new File(scenario.rootPath, "Plugins");

            listener.onPluginsInstallStarted();

            if (!pluginsDir.exists()) {
                if (!pluginsDir.mkdir()) {
                    listener.onPluginsInstallError(
                        this.activity.getString(R.string.plugins_install_error_mkdir_failed)
                    );
                    return;
                }
            }

            InputStream is = this.activity.getContentResolver().openInputStream(file);
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze = null;

            byte[] buffer = new byte[1024 * 8];

            while ((ze = zis.getNextEntry()) != null) {
                for (Plugin p : plugins) {
                    if (FilenameUtils.directoryContains(p.path, ze.getName())) {
                        File parent = new File(p.path);
                        File cur = new File(ze.getName());
                        File fo = new File(pluginsDir, parent.toURI().relativize(cur.toURI()).getPath());

                        if (ze.isDirectory()) {
                            if (!fo.exists() && !fo.mkdirs()) {
                                listener.onPluginsInstallError(
                                    this.activity.getString(R.string.plugins_install_error_mkdir_file_failed)
                                    + fo.getAbsolutePath()
                                );

                                return;
                            }

                            continue;
                        }

                        FileOutputStream fouts = new FileOutputStream(fo);

                        int count = 0;

                        while ((count = zis.read(buffer)) != -1) {
                            fouts.write(buffer, 0, count);
                        }

                        fouts.close();
                    }
                }

                zis.closeEntry();
            }

            listener.onPluginsInstallDone(plugins);
            zis.close();
        } catch (IOException e) {
            listener.onPluginsInstallError(e.getLocalizedMessage());
        }
    }
}
