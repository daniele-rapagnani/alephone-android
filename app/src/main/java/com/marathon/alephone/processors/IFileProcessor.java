package com.marathon.alephone.processors;

import android.content.Intent;

import androidx.annotation.Nullable;

public interface IFileProcessor {
    boolean onProcessingRequested(FileProcessingRequest request);
    boolean handleResult(int requestCode, int resultCode, @Nullable Intent data);
    void onProcessFile(Intent data, Object requestData);
}
