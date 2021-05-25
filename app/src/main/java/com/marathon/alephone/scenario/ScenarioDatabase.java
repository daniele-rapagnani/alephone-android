package com.marathon.alephone.scenario;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

@Database(entities = {ScenarioEntry.class}, version = 7, exportSchema = false)
@TypeConverters({ Converters.class })
public abstract class ScenarioDatabase extends RoomDatabase {
    public abstract ScenarioDao scenarioDao();
}
