package com.marathon.alephone.scenario;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import com.marathon.alephone.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class ScenarioDownloader {
    public interface IScenarioDownloaderListener {
        void onScenarioDownloadCompleted(Uri file);
        void onScenarioDownloadError(String error);
    };

    private static HashMap<String, Integer> URLS = new LinkedHashMap<>();

    private HashMap<Long, IScenarioDownloaderListener> runningDownloads = new LinkedHashMap<>();
    private final Activity activity;

    static {
        URLS.put("Marathon", R.string.marathon_url);
        URLS.put("Marathon 2", R.string.marathon2_url);
        URLS.put("Marathon Infinity", R.string.marathoninf_url);
        URLS.put("Marathon EVIL", R.string.evil_url);
        URLS.put("Tempus Irae", R.string.tempusirae_url);
        URLS.put("Marathon RED", R.string.red_url);
        URLS.put("Eternal X", R.string.eternal_url);
        URLS.put("Rubicon X", R.string.rubiconx_url);
        URLS.put("Marathon Phoenix SE", R.string.phoenix_url);
    }

    public ScenarioDownloader(Activity activity) {
        this.activity = activity;

        this.activity.registerReceiver(
            onComplete,
            new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        );
    }

    public Set<String> getSupportedScenarios() {
        return URLS.keySet();
    }

    public boolean startScenarioDownload(String name, IScenarioDownloaderListener listener) {
        if (!URLS.containsKey(name)) {
            return false;
        }

        DownloadManager downMan = (DownloadManager)
            this.activity.getSystemService(Context.DOWNLOAD_SERVICE)
        ;

        if (downMan == null) {
            return false;
        }

        Uri downloadUri = Uri.parse(this.activity.getString(URLS.get(name)));
        DownloadManager.Request req = new DownloadManager.Request(downloadUri);
        req.setTitle(String.format(this.activity.getString(R.string.scenario_file_download_title), name));
        req.setMimeType("application/zip");
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        req.setDestinationInExternalFilesDir(this.activity, null, String.format("%s.zip", name));

        runningDownloads.put(downMan.enqueue(req), listener);

        return true;
    }

    private BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (!ScenarioDownloader.this.runningDownloads.containsKey(completeDownloadId)) {
                return;
            }

            DownloadManager downMan = (DownloadManager)
                ScenarioDownloader.this.activity.getSystemService(Context.DOWNLOAD_SERVICE)
            ;

            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                IScenarioDownloaderListener listener = ScenarioDownloader.this.runningDownloads.get(completeDownloadId);

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(completeDownloadId);

                Cursor c = downMan.query(query);
                if (c.moveToFirst()) {
                    int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int status = c.getInt(columnIndex);

                    switch (status) {
                        case DownloadManager.STATUS_SUCCESSFUL:
                            listener.onScenarioDownloadCompleted(downMan.getUriForDownloadedFile(completeDownloadId));
                            break;

                        case DownloadManager.STATUS_FAILED:
                            int error = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                            listener.onScenarioDownloadError(String.format("%d", error));
                            break;

                        default:
                            listener.onScenarioDownloadError(ScenarioDownloader.this.activity.getString(R.string.generic_error));
                            break;
                    }
                }

                c.close();
            }
        }
    };
}
