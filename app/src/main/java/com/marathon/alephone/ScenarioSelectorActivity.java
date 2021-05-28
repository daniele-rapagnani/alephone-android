package com.marathon.alephone;

import android.content.Intent;
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

import com.marathon.alephone.processors.FileProcessingRequest;
import com.marathon.alephone.processors.IFileProcessor;
import com.marathon.alephone.processors.PluginsInstallProcessor;
import com.marathon.alephone.processors.ScenarioDataExportProcessor;
import com.marathon.alephone.processors.ScenarioDataImportProcessor;
import com.marathon.alephone.processors.ScenarioInstallProcessor;
import com.marathon.alephone.scenario.IScenarioUninstallListener;
import com.marathon.alephone.scenario.ScenarioDownloader;
import com.marathon.alephone.scenario.ScenarioEntry;
import com.marathon.alephone.scenario.ScenarioManager;
import com.marathon.alephone.scenario.ScenarioSelectorAdapter;
import com.marathon.alephone.scenario.ScenarioUninstaller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ScenarioSelectorActivity
    extends AppCompatActivity
    implements ScenarioDownloader.IScenarioDownloaderListener
{
    private final static String SCENARIO_DOWNLOAD_SHOWN_KEY = "scenario_download_shown";

    private ScenarioSelectorAdapter dataSelAdp;
    private Executor installExecutor;
    private ScenarioManager scenarioManager;
    private NotificationsManager notificationsManager;
    private ScenarioDownloader scenarioDownloader;
    private List<IFileProcessor> fileProcessors = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scenario_list);

        this.notificationsManager = new NotificationsManager(this);
        this.scenarioDownloader = new ScenarioDownloader(this);
        this.scenarioManager = new ScenarioManager("alephone", this);

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

        this.installExecutor = Executors.newSingleThreadExecutor();

        final LiveData<List<ScenarioEntry>> entries = this.scenarioManager.getDAO().getAll();
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

        this.fileProcessors.add(new ScenarioInstallProcessor(
            this,
            this.notificationsManager,
            this.installExecutor,
            this.scenarioManager
        ));

        this.fileProcessors.add(new ScenarioDataImportProcessor(
            this,
            this.notificationsManager,
            this.installExecutor
        ));

        this.fileProcessors.add(new ScenarioDataExportProcessor(
            this,
            this.notificationsManager,
            this.installExecutor
        ));

        this.fileProcessors.add(new PluginsInstallProcessor(
            this,
            this.notificationsManager,
            this.installExecutor
        ));
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
                startFileProcessing(new FileProcessingRequest(
                    ScenarioInstallProcessor.NAME,
                    new ScenarioInstallProcessor.Data(false)
                ));
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

                            AlertUtils.showOnetimeInfo(
                                SCENARIO_DOWNLOAD_SHOWN_KEY,
                                ScenarioSelectorActivity.this,
                                getString(R.string.scenario_download_title),
                                getString(R.string.scenario_download_text)
                            );
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
        for (IFileProcessor fp : this.fileProcessors) {
            if (fp.handleResult(requestCode, resultCode, data)) {
                return;
            }
        }
    }

    public void onPlayScenario(final ScenarioEntry de) {
        de.lastPlayed = new Date();
        this.scenarioManager.updateScenarioEntry(de);

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
                                    1000,
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
                                    scenarioManager.deleteScenarioEntry(scenario);
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

    public boolean startFileProcessing(FileProcessingRequest request) {
        for (IFileProcessor fp : this.fileProcessors) {
            if (fp.onProcessingRequested(request)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onScenarioDownloadCompleted(Uri file) {
        Intent i = new Intent();
        i.setData(file);

        FileProcessingRequest request = new FileProcessingRequest(
            ScenarioInstallProcessor.NAME,
            new ScenarioInstallProcessor.Data(true),
            i
        );

        startFileProcessing(request);
    }

    @Override
    public void onScenarioDownloadError(String reason) {
        AlertUtils.showError(
            this,
            getString(R.string.scenario_download_error_title),
            String.format(getString(R.string.scenario_download_error_text), reason)
        );
    }
}
