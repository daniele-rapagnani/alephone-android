package com.marathon.alephone.processors;

import android.app.Activity;
import android.content.Intent;

import com.marathon.alephone.AlertUtils;
import com.marathon.alephone.NotificationsManager;
import com.marathon.alephone.R;
import com.marathon.alephone.IInstallListener;
import com.marathon.alephone.scenario.IScenarioManager;
import com.marathon.alephone.scenario.ScenarioEntry;
import com.marathon.alephone.scenario.ScenarioInstaller;
import com.marathon.alephone.scenario.ScenarioScanner;

import java.io.File;
import java.util.concurrent.Executor;

public class ScenarioInstallProcessor extends SAFFileLongProcessor {
    public static class Data {
        final boolean deleteAfterInstall;

        public Data(boolean deleteAfterInstall) {
            this.deleteAfterInstall = deleteAfterInstall;
        }
    }

    public static final String NAME = "com.marathon.alephone.SCENARIO_INSTALL_PROCESSOR";
    private final static String SCENARIO_ADD_SHOWN_KEY = "scenario_ADD_shown";

    private final IScenarioManager scenarioManager;

    public ScenarioInstallProcessor(
        Activity activity,
        NotificationsManager notManager,
        Executor executor,
        IScenarioManager scenarioManager
    ) {
        super(activity, Type.OPEN, notManager, executor, "application/zip");
        this.scenarioManager = scenarioManager;
    }

    @Override
    protected String getNotificationTitle() {
        return getActivity().getString(R.string.scenario_install_notification_title);
    }

    @Override
    protected String getNotificationText() {
        return getActivity().getString(R.string.scenario_install_notification_text);
    }

    @Override
    protected int getNotificationIcon() {
        return android.R.drawable.arrow_down_float;
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

        AlertUtils.showOnetimeInfo(
            SCENARIO_ADD_SHOWN_KEY,
            getActivity(),
            getActivity().getString(R.string.scenario_add_title),
            getActivity().getString(R.string.scenario_add_text)
        );

        ScenarioInstaller di = new ScenarioInstaller(data.getData(), getActivity());
        Data installData = (Data)requestData;

        di.install(new IInstallListener() {
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
                progNot.showIndeterminate();

                ScenarioScanner ds = new ScenarioScanner(location);
                ScenarioEntry de = ds.scan();
                de.packageHash = hash;
                de.size = size;

                scenarioManager.addScenarioEntry(de);

                progNot.close();
            }

            @Override
            public void onDataInstallError(final String error) {
                progNot.close();

                AlertUtils.showError(
                    getActivity(),
                    getActivity().getString(R.string.scenario_install_error_dialog_title),
                    error
                );
            }
        }, installData.deleteAfterInstall);
    }

    @Override
    protected String getName() {
        return ScenarioInstallProcessor.NAME;
    }
}
