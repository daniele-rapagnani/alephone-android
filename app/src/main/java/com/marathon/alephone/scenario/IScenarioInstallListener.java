package com.marathon.alephone.scenario;

import java.io.File;

public interface IScenarioInstallListener {
    void onDataInstallStarted(File location, int totalSteps, String hash);
    void onDataInstallProgress(int stepDone, int totalSteps);
    void onDataInstallDone(File location, long size, String hash);
    void onDataInstallError(String error);
}
