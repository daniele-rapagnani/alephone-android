package com.marathon.alephone;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DataSelectorActivity extends AppCompatActivity {
    private final static int ADD_FOLDER_RC = 1;

    private final static int INSTALL_NOT_ID = 1;

    private DataSelectorAdapter dataSelAdp;
    private DataDatabase dataDb;
    private Executor dbExecutor;
    private Executor installExecutor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_selector);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "AlephOne",
                    "AlephOne Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            channel.setDescription("AlephOne notification channel");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        this.dataDb = Room.databaseBuilder(
            getApplicationContext(),
            DataDatabase.class,
            "alephone"
        )
            .fallbackToDestructiveMigration()
            .build()
        ;

        this.dataSelAdp = new DataSelectorAdapter(this);
        RecyclerView rv = (RecyclerView)findViewById(R.id.data_recycler);
        rv.setAdapter(this.dataSelAdp);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
            rv.getContext(),
            layoutManager.getOrientation()
        );

        rv.addItemDecoration(dividerItemDecoration);

        this.dbExecutor = Executors.newSingleThreadExecutor();
        this.installExecutor = Executors.newSingleThreadExecutor();

        final LiveData<List<DataEntry>> entries = dataDb.dataEntryDao().getAll();
        entries.observe(this, new Observer<List<DataEntry>>() {
            @Override
            public void onChanged(final List<DataEntry> dataEntries) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dataSelAdp.setDataEntries(dataEntries);
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.add_folder:
                startAddFolder();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch(requestCode) {
            case ADD_FOLDER_RC:
                onFolderSelected(data);
                return;
        }
    }

    protected void onFolderSelected(Intent data) {
       installData(data.getData());
    }

    protected void startAddFolder() {
        Intent in = new Intent();
        in.setAction(Intent.ACTION_OPEN_DOCUMENT);
        in.setType("application/zip");
        startActivityForResult(in, ADD_FOLDER_RC);
    }

    protected void installData(final Uri uri) {
        this.installExecutor.execute(new Runnable() {
            @Override
            public void run() {
                DataInstaller di = new DataInstaller(uri, getApplicationContext());

                final NotificationCompat.Builder nb = new NotificationCompat.Builder(
                    DataSelectorActivity.this,
                    "AlephOne"
                )
                    .setSmallIcon(android.R.drawable.arrow_down_float)
                    .setContentTitle("Installing")
                    .setContentText("Installing data")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setNotificationSilent()
                ;

                final NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(DataSelectorActivity.this)
                ;

                nb.setProgress(0, 0, true);
                notificationManager.notify(INSTALL_NOT_ID, nb.build());

                di.install(new IDataInstallListener() {
                    @Override
                    public void onDataInstallStarted(File location, int totalSteps, String hash) {
                        nb.setProgress(totalSteps, 0, false);
                        notificationManager.notify(INSTALL_NOT_ID, nb.build());
                    }

                    @Override
                    public void onDataInstallProgress(int stepDone, int totalSteps) {
                        nb.setProgress(totalSteps, stepDone, false);
                        notificationManager.notify(INSTALL_NOT_ID, nb.build());
                    }

                    @Override
                    public void onDataInstallDone(File location, String hash) {
                        nb.setProgress(0, 0, true);
                        notificationManager.notify(INSTALL_NOT_ID, nb.build());

                        DataScanner ds = new DataScanner(location);
                        DataEntry de = ds.scan();
                        de.packageHash = hash;

                        addDataEntry(de);

                        notificationManager.cancel(INSTALL_NOT_ID);
                    }

                    @Override
                    public void onDataInstallError(final String error) {
                        nb.setProgress(0, 0, false);
                        notificationManager.cancel(INSTALL_NOT_ID);

                        showError("Can't install data", String.format(
                            "Error while installing data: %s",
                            error
                        ));
                    }
                });
            }
        });
    }

    protected void addDataEntry(final DataEntry de) {
        this.dbExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<DataEntry> entries = dataDb.dataEntryDao().findByHash(de.packageHash);

                if (entries.isEmpty()) {
                    dataDb.dataEntryDao().insertAll(de);
                }
            }
        });
    }

    private void showError(final String title, final String text)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(DataSelectorActivity.this)
                    .setTitle(title)
                    .setMessage(text)
                    .setCancelable(true)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .show()
                ;
            }
        });
    }
}
