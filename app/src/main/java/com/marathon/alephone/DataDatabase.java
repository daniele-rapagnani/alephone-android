package com.marathon.alephone;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DataEntry.class}, version = 4, exportSchema = false)
public abstract class DataDatabase extends RoomDatabase {
    public abstract DataEntryDao dataEntryDao();
}
