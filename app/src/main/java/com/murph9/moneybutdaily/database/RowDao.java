package com.murph9.moneybutdaily.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.murph9.moneybutdaily.model.Row;

import java.util.List;

@Dao
public interface RowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Row row);

    @Update
    void update(Row row);

    @Delete
    void delete(Row row);

    @Query("DELETE FROM rows")
    void deleteAll();

    @Query("SELECT * from rows WHERE Line = :lineId ORDER BY Line ASC LIMIT 1")
    Row getSingle(int lineId);

    @Query("SELECT * from rows ORDER BY Line ASC")
    LiveData<List<Row>> getAllRows();
}
