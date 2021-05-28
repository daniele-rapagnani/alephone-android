package com.marathon.alephone.processors;

import android.app.Activity;
import android.content.Intent;

import com.marathon.alephone.NotificationsManager;

import java.util.concurrent.Executor;

public abstract class SAFFileLongProcessor extends SAFFileProcessor {
    private static int startNotId = 0;
    private static int nextNotId = 1;

    private final NotificationsManager notManager;
    private final Executor executor;
    private int notId = -1;

    public static void setStartingNotificationId(int start) {
        SAFFileLongProcessor.startNotId = start;
    }

    public static int getStartingNotificationId() {
        return SAFFileLongProcessor.startNotId;
    }

    public SAFFileLongProcessor(
        Activity activity,
        Type type,
        NotificationsManager notManager,
        Executor executor
    ) {
        super(activity, type);
        this.notManager = notManager;
        this.executor = executor;
    }

    public SAFFileLongProcessor(
        Activity activity,
        Type type,
        NotificationsManager notManager,
        Executor executor,
        String mime
    ) {
        super(activity, type, mime);
        this.notManager = notManager;
        this.executor = executor;
    }

    protected abstract String getNotificationTitle();
    protected abstract String getNotificationText();
    protected abstract int getNotificationIcon();

    @Override
    public void onProcessFile(final Intent data, final Object requestData) {
        this.notId = SAFFileLongProcessor.getStartingNotificationId() + SAFFileLongProcessor.nextNotId++;

        final NotificationsManager.ProgressNotification prog =
            this.notManager.createProgressNotification(
                this.notId,
                getNotificationTitle(),
                getNotificationText(),
                getNotificationIcon()
            )
        ;

        prog.showIndeterminate();

        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                SAFFileLongProcessor.this.onRun(data, prog, requestData);
            }
        });
    }

    protected abstract void onRun(
        final Intent data,
        final NotificationsManager.ProgressNotification progNot,
        final Object requestData
    );
}
