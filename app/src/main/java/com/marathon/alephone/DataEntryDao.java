package com.marathon.alephone;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DataEntryDao {
    @Query("SELECT * FROM data_entry")
    LiveData<List<DataEntry>> getAll();

    @Query("SELECT * FROM data_entry WHERE path = :path")
    List<DataEntry> findByPath(String path);

    @Query("SELECT * FROM data_entry WHERE package_hash = :hash")
    List<DataEntry> findByHash(String hash);

    @Update
    void update(DataEntry entry);

    @Insert
    void insertAll(DataEntry... entries);

    @Delete
    void delete(DataEntry entry);

    @Query("SELECT COUNT(*) FROM data_entry")
    int getCount();
}
