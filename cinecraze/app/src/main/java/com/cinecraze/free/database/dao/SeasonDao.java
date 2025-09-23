package com.cinecraze.free.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import com.cinecraze.free.database.entities.SeasonEntity;

import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cinecraze.free.database.entities.SeasonEntity;

@Dao
public interface SeasonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SeasonEntity season);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAndGetId(SeasonEntity season);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(SeasonEntity... seasons);

    @Query("DELETE FROM seasons")
    void deleteAll();
}
