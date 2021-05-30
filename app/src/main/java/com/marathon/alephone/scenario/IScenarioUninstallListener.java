package com.marathon.alephone.scenario;

import java.io.File;

public interface IScenarioUninstallListener {
    void onDataUninstallStarted(ScenarioEntry scenario);
    void onDataUninstallDone(ScenarioEntry scenario);
    void onDataUninstallError(ScenarioEntry scenario, String error);
}
