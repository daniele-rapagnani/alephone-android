package com.marathon.alephone.processors;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.marathon.alephone.AlertUtils;
import com.marathon.alephone.NotificationsManager;
import com.marathon.alephone.R;
import com.marathon.alephone.scenario.ScenarioEntry;
import com.marathon.alephone.scenario.ScenarioExporter;

import java.util.concurrent.Executor;

public class ScenarioDataExportProcessor extends SAFFileLongProcessor {
    public static class Data {
        public final ScenarioEntry scenario;

        public Data(ScenarioEntry scenario) {
            this.scenario = scenario;
        }
    }

    public static final String NAME = "com.marathon.alephone.SCENARIO_DATA_EXPORT_PROCESSOR";

    public ScenarioDataExportProcessor(
        Activity activity,
        NotificationsManager notManager,
        Executor executor
    ) {
        super(activity, Type.CREATE, notManager, executor, "application/zip");
    }

    @Override
    protected String getNotificationTitle() {
        return getActivity().getString(R.string.scenario_export_notification_title);
    }

    @Override
    protected String getNotificationText() {
        return getActivity().getString(R.string.scenario_export_notification_text);
    }

    @Override
    protected int getNotificationIcon() {
        return android.R.drawable.ic_popup_sync;
    }

    @Override
    protected String getDefaultFilename() {
        @NonNull Data data = getRequestData();
        return String.format(
            getActivity().getString(R.string.scenario_export_default_filename),
            data.scenario.scenarioName,
            super.getDefaultFilename()
        );
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

        final Data exportData = (Data)requestData;
        final ScenarioExporter sex = new ScenarioExporter(exportData.scenario, getActivity());

        sex.export(data.getData(), new ScenarioExporter.IScenarioExportListener() {
            @Override
            public void onExportSuccess(Uri dst, ScenarioEntry scenario) {
                progNot.close();

                AlertUtils.showInfo(
                    getActivity(),
                    getActivity().getString(R.string.scenario_export_success_dialog_title),
                    getActivity().getString(R.string.scenario_export_success_dialog_text)
                );
            }

            @Override
            public void onExportError(ScenarioEntry scenario, String error) {
                progNot.close();

                AlertUtils.showError(
                    getActivity(),
                    getActivity().getString(R.string.scenario_export_error_dialog_title),
                    error
                );
            }
        });
    }

    @Override
    protected String getName() {
        return ScenarioDataExportProcessor.NAME;
    }
}
