package com.marathon.alephone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.marathon.alephone.scenario.IScenarioInstallListener;
import com.marathon.alephone.scenario.IScenarioUninstallListener;
import com.marathon.alephone.scenario.ScenarioDatabase;
import com.marathon.alephone.scenario.ScenarioDownloader;
import com.marathon.alephone.scenario.ScenarioEntry;
import com.marathon.alephone.scenario.ScenarioExporter;
import com.marathon.alephone.scenario.ScenarioInstaller;
import com.marathon.alephone.scenario.ScenarioScanner;
import com.marathon.alephone.scenario.ScenarioSelectorAdapter;
import com.marathon.alephone.scenario.ScenarioUninstaller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ScenarioSelectorActivity
    extends AppCompatActivity
    implements ScenarioDownloader.IScenarioDownloaderListener
{
    private final static int ADD_SCENARIO_RC = 1;
    private final static int EXPORT_SCENARIO_DATA_RC = 2;
    private final static int IMPORT_SCENARIO_DATA_RC = 3;

    private final static int INSTALL_NOT_ID = 1;
    private final static int UNINSTALL_NOT_ID = 2;
    private final static int EXPORT_DATA_NOT_ID = 3;
    private final static int IMPORT_DATA_NOT_ID = 4;

    private final static String SCENARIOSEL_PREFS = "scenariosel";
    private final static String SCENARIO_DOWNLOAD_SHOWN_KEY = "scenario_download_shown";
    private final static String SCENARIO_ADD_SHOWN_KEY = "scenario_ADD_shown";

    private ScenarioSelectorAdapter dataSelAdp;
    private ScenarioDatabase dataDb;
    private Executor dbExecutor;
    private Executor installExecutor;
    private NotificationsManager notificationsManager;
    private ScenarioDownloader scenarioDownloader;
    private List<Uri> deleteAfterInstall = new LinkedList<>();

    private ScenarioEntry exportDataScenario = null;
    private ScenarioEntry importDataScenario = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scenario_list);

        this.notificationsManager = new NotificationsManager(this);
        this.scenarioDownloader = new ScenarioDownloader(this);

        this.dataDb = Room.databaseBuilder(
            getApplicationContext(),
            ScenarioDatabase.class,
            "alephone"
        )
            .fallbackToDestructiveMigration()
            .build()
        ;

        this.dataSelAdp = new ScenarioSelectorAdapter(this);
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

        final LiveData<List<ScenarioEntry>> entries = dataDb.scenarioDao().getAll();
        entries.observe(this, new Observer<List<ScenarioEntry>>() {
            @Override
            public void onChanged(final List<ScenarioEntry> dataEntries) {
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
            case R.id.add_scenario:
                startAddFolder();
                return true;

            case R.id.download_scenario:
                List<String> scenarios = new ArrayList<>();

                for (String s : this.scenarioDownloader.getSupportedScenarios()) {
                    boolean found = false;

                    for (ScenarioEntry se : this.dataSelAdp.getScenarioEntries()) {
                        if (se.scenarioName.equals(s)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        scenarios.add(s);
                    }
                }

                if (scenarios.isEmpty()) {
                    AlertUtils.showInfo(
                        this,
                        getString(R.string.scenario_already_installed_title),
                        getString(R.string.scenario_already_installed_text)
                    );

                    return true;
                }

                AlertUtils.showChoice(
                    this,
                    getString(R.string.scenario_download_choose),
                    scenarios,
                    new AlertUtils.ChoiceListener<String>() {
                        @Override
                        public void success(String choice) {
                            scenarioDownloader.startScenarioDownload(choice, ScenarioSelectorActivity.this);

                            if (!getPrefBool(SCENARIO_DOWNLOAD_SHOWN_KEY, false)) {
                                AlertUtils.showInfo(
                                        ScenarioSelectorActivity.this,
                                        getString(R.string.scenario_download_title),
                                        getString(R.string.scenario_download_text)
                                );

                                setPrefBool(SCENARIO_DOWNLOAD_SHOWN_KEY, true);
                            }
                        }

                        @Override
                        public void canceled() { }
                    }
                );

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
            case ADD_SCENARIO_RC:
                onScenarioFileSelected(data);
                return;

            case EXPORT_SCENARIO_DATA_RC:
                onScenarioExportFileSelected(data, this.exportDataScenario);
                return;

            case IMPORT_SCENARIO_DATA_RC:
                onScenarioImportFileSelected(data, this.importDataScenario);
                return;
        }
    }

    protected void onScenarioFileSelected(Intent data) {
        installData(data.getData());

        if (!getPrefBool(SCENARIO_ADD_SHOWN_KEY, false)) {
            AlertUtils.showInfo(
                ScenarioSelectorActivity.this,
                getString(R.string.scenario_add_title),
                getString(R.string.scenario_add_text)
            );

            setPrefBool(SCENARIO_ADD_SHOWN_KEY, true);
        }
    }

    protected void startAddFolder() {
        Intent in = new Intent();
        in.setAction(Intent.ACTION_OPEN_DOCUMENT);
        in.setType("application/zip");
        startActivityForResult(in, ADD_SCENARIO_RC);
    }

    protected void installData(final Uri uri) {
        this.installExecutor.execute(new Runnable() {
            @Override
            public void run() {
                ScenarioInstaller di = new ScenarioInstaller(uri, getApplicationContext());

                final NotificationsManager.ProgressNotification progNot =
                    notificationsManager.createProgressNotification(
                        INSTALL_NOT_ID,
                        getString(R.string.scenario_install_notification_title),
                        getString(R.string.scenario_install_notification_text),
                        android.R.drawable.arrow_down_float
                    )
                ;

                progNot.showIndeterminate();

                di.install(new IScenarioInstallListener() {
                    @Override
                    public void onDataInstallStarted(File location, int totalSteps, String hash) {
                        progNot.showWithProgress(0, totalSteps);
                    }

                    @Override
                    public void onDataInstallProgress(int stepDone, int totalSteps) {
                        progNot.showWithProgress(stepDone, totalSteps);
                    }

                    @Override
                    public void onDataInstallDone(File location, long size, String hash) {
                        progNot.showIndeterminate();

                        ScenarioScanner ds = new ScenarioScanner(location);
                        ScenarioEntry de = ds.scan();
                        de.packageHash = hash;
                        de.size = size;

                        addScenarioEntry(de);

                        processDeleteAfterInstall();

                        progNot.close();
                    }

                    @Override
                    public void onDataInstallError(final String error) {
                        processDeleteAfterInstall();

                        progNot.close();

                        AlertUtils.showError(
                            ScenarioSelectorActivity.this,
                            "Can't install data",
                            String.format(
                                "Error while installing data: %s",
                                error
                            )
                        );
                    }
                });
            }
        });
    }

    public void onPlayScenario(final ScenarioEntry de) {
        de.lastPlayed = new Date();
        updateScenarioEntry(de);

        Intent i = new Intent(this, AlephOneActivity.class);
        i.putExtra("scenarioPath", de.rootPath);
        this.startActivity(i);
    }

    public void onUninstall(final ScenarioEntry de) {
        AlertUtils.showYesNo(
            this,
            getString(R.string.scenario_uninstall_alert_title),
            getString(R.string.scenario_uninstall_alert_text),
            new AlertUtils.YesNoListener() {
                @Override
                public void yes() {
                    installExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            ScenarioUninstaller su = new ScenarioUninstaller(de);

                            final NotificationsManager.ProgressNotification pm =
                                notificationsManager.createProgressNotification(
                                    UNINSTALL_NOT_ID,
                                    getString(R.string.scenario_uninstall_notification_title),
                                    String.format(getString(R.string.scenario_uninstall_notification_text), de.scenarioName),
                                    android.R.drawable.ic_menu_delete
                                )
                            ;

                            su.uninstall(new IScenarioUninstallListener() {
                                @Override
                                public void onDataUninstallStarted(ScenarioEntry scenario) {
                                    pm.showIndeterminate();
                                }

                                @Override
                                public void onDataUninstallDone(ScenarioEntry scenario) {
                                    pm.close();
                                    deleteScenarioEntry(scenario);
                                }

                                @Override
                                public void onDataUninstallError(ScenarioEntry scenario, String error) {
                                    AlertUtils.showError(
                                        ScenarioSelectorActivity.this,
                                        getString(R.string.scenario_uninstall_error_title),
                                        error
                                    );

                                    pm.close();
                                }
                            });
                        }
                    });
                }

                @Override
                public void no() { }
            }
        );
    }

    protected void addScenarioEntry(final ScenarioEntry de) {
        this.dbExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<ScenarioEntry> entries = dataDb.scenarioDao().findByHash(de.packageHash);

                if (entries.isEmpty()) {
                    dataDb.scenarioDao().insertAll(de);
                }
            }
        });
    }

    protected void deleteScenarioEntry(final ScenarioEntry de) {
        this.dbExecutor.execute(new Runnable() {
            @Override
            public void run() {
                dataDb.scenarioDao().delete(de);
            }
        });
    }

    protected void updateScenarioEntry(final ScenarioEntry de) {
        this.dbExecutor.execute(new Runnable() {
            @Override
            public void run() {
                dataDb.scenarioDao().update(de);
            }
        });
    }

    public void onScenarioExportFileSelected(final Intent data, ScenarioEntry se) {
        final ScenarioExporter sex = new ScenarioExporter(se, this);

        final NotificationsManager.ProgressNotification prog =
            this.notificationsManager.createProgressNotification(
                EXPORT_DATA_NOT_ID,
                "Exporting data",
                "Exporting scenario data",
                android.R.drawable.ic_popup_sync
            )
        ;

        prog.showIndeterminate();

        this.installExecutor.execute(new Runnable() {
            @Override
            public void run() {
                sex.export(data.getData(), new ScenarioExporter.IScenarioExportListener() {
                    @Override
                    public void onExportSuccess(Uri dst, ScenarioEntry scenario) {
                        prog.close();
                        exportDataScenario = null;

                        AlertUtils.showInfo(
                            ScenarioSelectorActivity.this,
                            "Success",
                            "Scenario data exported successfully"
                        );
                    }

                    @Override
                    public void onExportError(ScenarioEntry scenario, String error) {
                        prog.close();
                        exportDataScenario = null;

                        AlertUtils.showError(
                            ScenarioSelectorActivity.this,
                            "Error exporting data",
                            error
                        );
                    }
                });
            }
        });
    }

    public void onExportScenarioData(ScenarioEntry s) {
        if (this.exportDataScenario != null) {
            return;
        }

        Date now = Calendar.getInstance().getTime();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-mm-dd-hh-mm-ss");

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        intent.putExtra(
            Intent.EXTRA_TITLE,
            String.format("%s_backup_%s.zip", s.scenarioName, sf.format(now))
        );

        this.exportDataScenario = s;
        startActivityForResult(intent, EXPORT_SCENARIO_DATA_RC);
    }

    public void onScenarioImportFileSelected(final Intent data, final ScenarioEntry se) {
        ScenarioExporter.Manifest manifest =
            ScenarioExporter.readManifest(data.getData(), this)
        ;

        if (manifest == null) {
            AlertUtils.showError(
                this,
                "Import error",
                "The file you select is not valid scenario data"
            );

            this.importDataScenario = null;

            return;
        }

        if (!manifest.scenario.equals(se.scenarioName)) {
            AlertUtils.showError(
                    this,
                    "Import error",
                    String.format(
                        "The file you selected is for '%s', it can't be imported in '%s'",
                        manifest.scenario,
                        se.scenarioName
                    )
            );

            this.importDataScenario = null;

            return;
        }

        this.installExecutor.execute(new Runnable() {
            @Override
            public void run() {
                ScenarioInstaller di = new ScenarioInstaller(
                    data.getData(),
                    ScenarioSelectorActivity.this
                );

                final NotificationsManager.ProgressNotification progNot =
                    notificationsManager.createProgressNotification(
                        IMPORT_DATA_NOT_ID,
                        "Importing",
                        "Importing scenario data",
                        android.R.drawable.ic_popup_sync
                    )
                ;

                progNot.showIndeterminate();

                di.installData(
                    new IScenarioInstallListener() {
                        @Override
                        public void onDataInstallStarted(File location, int totalSteps, String hash) {
                            progNot.showWithProgress(0, totalSteps);
                        }

                        @Override
                        public void onDataInstallProgress(int stepDone, int totalSteps) {
                            progNot.showWithProgress(stepDone, totalSteps);
                        }

                        @Override
                        public void onDataInstallDone(File location, long size, String hash) {
                            progNot.close();

                            ScenarioSelectorActivity.this.importDataScenario = null;

                            AlertUtils.showInfo(
                                ScenarioSelectorActivity.this,
                                "Import succeeded",
                                "Data imported correctly"
                            );
                        }

                        @Override
                        public void onDataInstallError(String error) {
                            progNot.close();

                            ScenarioSelectorActivity.this.importDataScenario = null;

                            AlertUtils.showError(
                                ScenarioSelectorActivity.this,
                                "Import error",
                                error
                            );
                        }
                    },
                    new File(se.rootPath),
                    se.packageHash,
                    Arrays.asList(ScenarioExporter.MANIFEST_NAME)
                );
            }
        });
    }

    public void onImportScenarioData(ScenarioEntry s) {
        if (this.importDataScenario != null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/zip");

        this.importDataScenario = s;
        startActivityForResult(intent, IMPORT_SCENARIO_DATA_RC);
    }

    @Override
    public void onScenarioDownloadCompleted(Uri file) {
        installData(file);
        deleteAfterInstall.add(file);
    }

    @Override
    public void onScenarioDownloadError(String reason) {
        AlertUtils.showError(
            this,
            getString(R.string.scenario_download_error_title),
            String.format(getString(R.string.scenario_download_error_text), reason)
        );
    }

    protected void processDeleteAfterInstall() {
        for (Uri u : this.deleteAfterInstall) {
            getContentResolver().delete(u, null, null);
        }
    }

    protected boolean getPrefBool(String key, boolean def) {
        SharedPreferences pref = getSharedPreferences(SCENARIOSEL_PREFS, Context.MODE_PRIVATE);
        return pref.getBoolean(key, def);
    }

    protected void setPrefBool(String key, boolean val) {
        SharedPreferences pref = getSharedPreferences(SCENARIOSEL_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor e = pref.edit();
        e.putBoolean(key, val);
        e.commit();
    }
}
