package com.cinecraze.free.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import com.cinecraze.free.database.entities.EpisodeEntity;

import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cinecraze.free.database.entities.EpisodeEntity;

@Dao
public interface EpisodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EpisodeEntity episode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAndGetId(EpisodeEntity episode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(EpisodeEntity... episodes);

    @Query("DELETE FROM episodes")
    void deleteAll();
}
