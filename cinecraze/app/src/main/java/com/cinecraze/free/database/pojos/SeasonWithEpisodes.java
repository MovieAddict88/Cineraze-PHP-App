package com.cinecraze.free.database.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.cinecraze.free.database.entities.SeasonEntity;

import java.util.List;

public class SeasonWithEpisodes {
    @Embedded
    public SeasonEntity season;

    @Relation(
         entity = com.cinecraze.free.database.entities.EpisodeEntity.class,
         parentColumn = "id",
         entityColumn = "season_id"
    )
    public List<EpisodeWithServers> episodes;
}
