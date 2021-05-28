package com.marathon.alephone.processors;

import android.content.Intent;

import java.util.HashMap;

public class FileProcessingRequest {
    public final String processingName;
    public final Object requestData;
    public final Intent existingResult;

    public FileProcessingRequest(String processingName, Object requestData, Intent existingResult) {
        this.processingName = processingName;
        this.requestData = requestData;
        this.existingResult = existingResult;
    }

    public FileProcessingRequest(String processingName, Object requestData) {
        this(processingName, requestData, null);
    }
}
