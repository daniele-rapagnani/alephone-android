package com.marathon.alephone.scenario;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScenarioDao {
    @Query("SELECT * FROM scenarios ORDER BY last_played DESC")
    LiveData<List<ScenarioEntry>> getAll();

    @Query("SELECT * FROM scenarios WHERE path = :path")
    List<ScenarioEntry> findByPath(String path);

    @Query("SELECT * FROM scenarios WHERE package_hash = :hash")
    List<ScenarioEntry> findByHash(String hash);

    @Update
    void update(ScenarioEntry entry);

    @Insert
    void insertAll(ScenarioEntry... entries);

    @Delete
    void delete(ScenarioEntry entry);

    @Query("SELECT COUNT(*) FROM scenarios")
    int getCount();
}
