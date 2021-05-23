package com.marathon.alephone;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "data_entry")
public class DataEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "path")
    public String path;

    @ColumnInfo(name = "scenario_name")
    public String scenarioName;

    @ColumnInfo(name = "package_hash")
    public String packageHash;

    @ColumnInfo(name = "arguments")
    public String arguments;
}
