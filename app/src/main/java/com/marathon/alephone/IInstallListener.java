package com.marathon.alephone;

import java.io.File;

public interface IInstallListener {
    void onDataInstallStarted(File location, int totalSteps, String hash);
    void onDataInstallProgress(int stepDone, int totalSteps);
    void onDataInstallDone(File location, long size, String hash);
    void onDataInstallError(String error);
}
