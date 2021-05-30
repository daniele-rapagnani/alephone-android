package com.marathon.alephone.processors;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.marathon.alephone.AlertUtils;
import com.marathon.alephone.NotificationsManager;
import com.marathon.alephone.R;
import com.marathon.alephone.IInstallListener;
import com.marathon.alephone.ScenarioSelectorActivity;
import com.marathon.alephone.scenario.ScenarioEntry;
import com.marathon.alephone.scenario.ScenarioExporter;
import com.marathon.alephone.scenario.ScenarioInstaller;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Executor;

public class ScenarioDataImportProcessor extends SAFFileLongProcessor {
    public static class Data {
        public final ScenarioEntry scenario;

        public Data(ScenarioEntry scenario) {
            this.scenario = scenario;
        }
    }

    public static final String NAME = "com.marathon.alephone.SCENARIO_DATA_IMPORT_PROCESSOR";

    public ScenarioDataImportProcessor(
        Activity activity,
        NotificationsManager notManager,
        Executor executor
    ) {
        super(activity, Type.OPEN, notManager, executor, "application/zip");
    }

    @Override
    protected String getNotificationTitle() {
        return getActivity().getString(R.string.scenario_import_notification_title);
    }

    @Override
    protected String getNotificationText() {
        return getActivity().getString(R.string.scenario_import_notification_text);
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

        final Data importData = (Data)requestData;

        ScenarioExporter.Manifest manifest =
            ScenarioExporter.readManifest(data.getData(), getActivity())
        ;

        if (manifest == null) {
            AlertUtils.showError(
                getActivity(),
                getActivity().getString(R.string.scenario_import_error_dialog_title),
                getActivity().getString(R.string.scenario_import_error_dialog_text_invalid)
            );

            progNot.close();

            return;
        }

        if (!manifest.scenario.equals(importData.scenario.scenarioName)) {
            AlertUtils.showError(
                getActivity(),
                getActivity().getString(R.string.scenario_import_error_dialog_title),
                String.format(
                    getActivity().getString(R.string.scenario_import_error_dialog_text_mismatch),
                    manifest.scenario,
                    importData.scenario.scenarioName
                )
            );

            progNot.close();

            return;
        }

        final ScenarioInstaller di = new ScenarioInstaller(
            data.getData(),
            getActivity()
        );

        AlertUtils.showToast(
            getActivity(),
            getActivity().getString(R.string.toast_scenario_data_importing)
        );

        di.installData(
            new IInstallListener() {
                @Override
                public void onDataInstallStarted(File location, int totalSteps, String hash) {
                    progNot.showWithProgress(0, totalSteps);
                }

                @Override
                public void onDataInstallProgress(int stepDone, int totalSteps) {
                    progNot.showWithProgress(stepDone, totalSteps);
                }

                @Override
                public void onDataInstallDone(File location, long size, String hash) {
                    progNot.close();

                    AlertUtils.showInfo(
                        getActivity(),
                        getActivity().getString(R.string.scenario_import_success_dialog_title),
                        getActivity().getString(R.string.scenario_import_success_dialog_text)
                    );
                }

                @Override
                public void onDataInstallError(String error) {
                    progNot.close();

                    AlertUtils.showError(
                        getActivity(),
                        getActivity().getString(R.string.scenario_import_error_dialog_title),
                        error
                    );
                }
            },
            new File(importData.scenario.rootPath),
            importData.scenario.packageHash,
            Arrays.asList(ScenarioExporter.MANIFEST_NAME),
            false
        );
    }

    @Override
    protected String getName() {
        return ScenarioDataImportProcessor.NAME;
    }
}
