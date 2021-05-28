package com.marathon.alephone.processors;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public abstract class SAFFileProcessor implements IFileProcessor {
    private static int startId = 0;
    private static int nextId = 1;

    private final Activity activity;
    private final Type type;
    private String mime = "application/octet-stream";
    private int id = -1;
    private HashMap<Integer, Object> requestsData = new HashMap<>();

    public enum Type {
        CREATE,
        OPEN,
        OPEN_DIR
    }

    public static void setStartingId(int start) {
        SAFFileProcessor.startId = start;
    }

    public static int getStartingId() {
        return SAFFileProcessor.startId;
    }

    public SAFFileProcessor(Activity activity, Type type) {
        this.activity = activity;
        this.type = type;
    }

    public SAFFileProcessor(Activity activity, Type type, String mime) {
        this(activity, type);
        this.mime = mime;
    }

    protected Intent createIntent() {
        String action;

        switch(this.type) {
            case CREATE:
                action = Intent.ACTION_CREATE_DOCUMENT;
                break;

            case OPEN_DIR:
                action = Intent.ACTION_OPEN_DOCUMENT_TREE;
                break;

            default:
                action = Intent.ACTION_OPEN_DOCUMENT;
                break;
        }

        Intent intent = new Intent(action);
        intent.setType(this.mime);

        if (this.type == Type.CREATE) {
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            intent.putExtra(
                Intent.EXTRA_TITLE,
                getDefaultFilename()
            );
        }

        return intent;
    }

    protected String getDefaultFilename() {
        Date now = Calendar.getInstance().getTime();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-mm-dd-hh-mm-ss");
        return String.format("%s", sf.format(now));
    }

    public String getMime() {
        return mime;
    }

    @Override
    public boolean onProcessingRequested(FileProcessingRequest request) {
        if (request.processingName != getName()) {
            return false;
        }

        if (request.existingResult != null) {
            onProcessFile(request.existingResult, request.requestData);
            return true;
        }

        this.id = SAFFileProcessor.getStartingId() + SAFFileProcessor.nextId++;
        this.requestsData.put(this.id, request.requestData);

        Intent intent = createIntent();

        this.activity.startActivityForResult(
            intent,
            this.id
        );

        return true;
    }

    protected <T> T getRequestData() {
        if (!this.requestsData.containsKey(this.id)) {
            return null;
        }

        return (T)this.requestsData.get(this.id);
    }

    protected <T> T popRequestData() {
        if (!this.requestsData.containsKey(this.id)) {
            return null;
        }

        T requestData = (T)this.requestsData.get(this.id);
        this.requestsData.remove(this.id);

        return requestData;
    }

    @Override
    public boolean handleResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return false;
        }

        if (requestCode != this.id) {
            return false;
        }

        onProcessFile(data, popRequestData());
        return true;
    }

    protected Activity getActivity() {
        return activity;
    }

    protected abstract String getName();
}
