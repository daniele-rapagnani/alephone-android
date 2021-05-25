package com.marathon.alephone.scenario;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ScenarioUninstaller {
    private final ScenarioEntry scenario;

    public ScenarioUninstaller(ScenarioEntry scenario) {
        this.scenario = scenario;
    }

    public void uninstall(IScenarioUninstallListener listener) {
        listener.onDataUninstallStarted(this.scenario);

        File scenDir = new File(this.scenario.path);

        if (!scenDir.exists() && scenDir.isDirectory()) {
            listener.onDataUninstallDone(this.scenario);
            return;
        }

        try {
            FileUtils.deleteDirectory(scenDir);
        } catch (IOException e) {
            listener.onDataUninstallError(this.scenario, e.getLocalizedMessage());
        }

        listener.onDataUninstallDone(this.scenario);
    }
}
