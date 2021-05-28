package com.marathon.alephone.processors;

import android.app.Activity;
import android.content.Intent;

import com.marathon.alephone.AlertUtils;
import com.marathon.alephone.NotificationsManager;
import com.marathon.alephone.R;
import com.marathon.alephone.plugins.Plugin;
import com.marathon.alephone.plugins.PluginsInstaller;
import com.marathon.alephone.scenario.ScenarioEntry;

import java.util.List;
import java.util.concurrent.Executor;

public class PluginsInstallProcessor extends SAFFileLongProcessor {
    public static class Data {
        public final ScenarioEntry scenario;
        public Data(ScenarioEntry scenario) {
            this.scenario = scenario;
        }
    }

    public static final String NAME = "com.marathon.alephone.PLUGINS_IMPORT_PROCESS";

    public PluginsInstallProcessor(
        Activity activity,
        NotificationsManager notManager,
        Executor executor
    ) {
        super(activity, SAFFileProcessor.Type.OPEN, notManager, executor, "application/zip");
    }

    @Override
    protected String getNotificationTitle() {
        return getActivity().getString(R.string.plugins_install_notification_title);
    }

    @Override
    protected String getNotificationText() {
        return getActivity().getString(R.string.plugins_install_notification_text);
    }

    @Override
    protected int getNotificationIcon() {
        return android.R.drawable.ic_popup_sync;
    }

    @Override
    protected void onRun(
        final Intent data,
        final NotificationsManager.ProgressNotification progNot,
        final Object requestData
    ) {
        if (requestData == null) {
            return;
        }

        final Data installData = (Data)requestData;
        final PluginsInstaller pii = new PluginsInstaller(getActivity());

        pii.install(data.getData(), installData.scenario, new PluginsInstaller.IPluginInstallListener() {
            @Override
            public void onPluginsInstallStarted() { }

            @Override
            public void onPluginsInstallDone(List<Plugin> plugins) {
                progNot.close();

                String[] pluginsNames = new String[plugins.size()];

                for (int i = 0; i < plugins.size(); i++) {
                    pluginsNames[i] = String.format("%s:%s", plugins.get(i).name, plugins.get(i).version);
                }

                AlertUtils.showInfo(
                    getActivity(),
                    getActivity().getString(R.string.plugins_install_success_dialog_title),
                    String.format(
                        getActivity().getString(R.string.plugins_install_success_dialog_text),
                        String.join(", ", pluginsNames)
                    )
                );
            }

            @Override
            public void onPluginsInstallError(String error) {
                progNot.close();

                AlertUtils.showError(
                    getActivity(),
                    getActivity().getString(R.string.plugins_install_error_dialog_title),
                    error
                );
            }
        });
    }

    @Override
    protected String getName() {
        return PluginsInstallProcessor.NAME;
    }
}
