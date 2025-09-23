package com.cinecraze.free.database.pojos;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.cinecraze.free.database.entities.EpisodeEntity;
import com.cinecraze.free.database.entities.ServerEntity;

import java.util.List;

public class EpisodeWithServers {
    @Embedded
    public EpisodeEntity episode;

    @Relation(
         parentColumn = "id",
         entityColumn = "episode_id"
    )
    public List<ServerEntity> servers;
}
