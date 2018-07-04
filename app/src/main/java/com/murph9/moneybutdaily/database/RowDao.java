package com.murph9.moneybutdaily.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.murph9.moneybutdaily.model.Row;

import java.util.List;

@Dao
public interface RowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Row row);

    @Query("DELETE FROM rows")
    void deleteAll();

    @Query("SELECT * from rows ORDER BY Line ASC")
    LiveData<List<Row>> getAllRows();
}
