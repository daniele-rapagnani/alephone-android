package com.marathon.alephone.scenario;

import android.content.Context;

import androidx.room.Room;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ScenarioManager implements IScenarioManager {
    private Executor dbExecutor;
    private ScenarioDatabase dataDb;

    public ScenarioManager(String name, Context context) {
        this.dbExecutor = Executors.newSingleThreadExecutor();

        this.dataDb = Room.databaseBuilder(
            context,
            ScenarioDatabase.class,
            name
        )
            .fallbackToDestructiveMigration()
            .build()
        ;
    }

    public void addScenarioEntry(final ScenarioEntry de) {
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

    public void deleteScenarioEntry(final ScenarioEntry de) {
        this.dbExecutor.execute(new Runnable() {
            @Override
            public void run() {
                dataDb.scenarioDao().delete(de);
            }
        });
    }

    public void updateScenarioEntry(final ScenarioEntry de) {
        this.dbExecutor.execute(new Runnable() {
            @Override
            public void run() {
                dataDb.scenarioDao().update(de);
            }
        });
    }

    public ScenarioDao getDAO() {
        return this.dataDb.scenarioDao();
    }
}
