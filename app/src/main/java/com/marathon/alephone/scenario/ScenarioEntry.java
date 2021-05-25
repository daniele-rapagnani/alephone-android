package com.marathon.alephone.scenario;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "scenarios")
public class ScenarioEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "path")
    public String path;

    @ColumnInfo(name = "root_path")
    public String rootPath;

    @ColumnInfo(name = "size")
    public long size;

    @ColumnInfo(name = "scenario_name")
    public String scenarioName;

    @ColumnInfo(name = "package_hash")
    public String packageHash;

    @ColumnInfo(name = "arguments")
    public String arguments;

    @ColumnInfo(name = "last_played")
    public Date lastPlayed;
}
