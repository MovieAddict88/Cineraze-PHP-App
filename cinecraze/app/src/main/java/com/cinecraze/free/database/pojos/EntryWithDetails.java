package com.cinecraze.free.database.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.cinecraze.free.database.entities.EntryEntity;
import com.cinecraze.free.database.entities.ServerEntity;

import java.util.List;

public class EntryWithDetails {
    @Embedded
    public EntryEntity entry;

    @Relation(
         parentColumn = "id",
         entityColumn = "entry_id"
    )
    public List<ServerEntity> servers;

    @Relation(
         entity = com.cinecraze.free.database.entities.SeasonEntity.class,
         parentColumn = "id",
         entityColumn = "entry_id"
    )
    public List<SeasonWithEpisodes> seasons;
}
