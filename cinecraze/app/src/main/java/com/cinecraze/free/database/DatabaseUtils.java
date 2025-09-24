package com.cinecraze.free.database;

import com.cinecraze.free.database.entities.EntryEntity;
import com.cinecraze.free.database.pojos.EntryWithDetails;
import com.cinecraze.free.database.pojos.EpisodeWithServers;
import com.cinecraze.free.database.pojos.SeasonWithEpisodes;
import com.cinecraze.free.models.Entry;
import com.cinecraze.free.models.Episode;
import com.cinecraze.free.models.Server;
import com.cinecraze.free.models.Season;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtils {
    
    private static final Gson gson = new Gson();
    
    /**
     * Convert Entry API model to EntryEntity database entity
     */
    public static EntryEntity entryToEntity(Entry entry, String mainCategory) {
        EntryEntity entity = new EntryEntity();
        
        entity.setId(entry.getId());
        entity.setTitle(entry.getTitle());
        entity.setDescription(entry.getDescription());
        entity.setPoster(entry.getPoster());
        entity.setThumbnail(entry.getThumbnail());
        entity.setRating(String.valueOf(entry.getRating()));
        entity.setDuration(entry.getDuration());
        entity.setYear(String.valueOf(entry.getYear()));
        entity.setMainCategory(mainCategory);
        entity.setContentType(entry.getType());
        entity.setParentalRating(entry.getParentalRating());
        
        return entity;
    }
    
    /**
     * Convert EntryWithDetails database POJO to Entry API model
     */
    public static Entry entityToEntry(EntryWithDetails entityWithDetails) {
        Entry entry = new Entry();
        EntryEntity entity = entityWithDetails.entry;

        // Use proper setter methods
        entry.setId(entity.getId());
        entry.setTitle(entity.getTitle());
        entry.setDescription(entity.getDescription());
        entry.setPoster(entity.getPoster());
        entry.setThumbnail(entity.getThumbnail());
        try {
            entry.setRating(Float.parseFloat(entity.getRating()));
        } catch (NumberFormatException e) {
            entry.setRating(0.0f);
        }
        entry.setDuration(entity.getDuration());
        try {
            entry.setYear(Integer.parseInt(entity.getYear()));
        } catch (NumberFormatException e) {
            entry.setYear(0);
        }
        entry.setType(entity.getContentType());
        entry.setParentalRating(entity.getParentalRating());

        // Convert related entities
        List<Server> servers = new ArrayList<>();
        if (entityWithDetails.servers != null) {
            for (com.cinecraze.free.database.entities.ServerEntity serverEntity : entityWithDetails.servers) {
                Server server = new Server();
                server.setName(serverEntity.getName());
                server.setUrl(serverEntity.getUrl());
                server.setLicense(serverEntity.getLicense());
                server.setDrm(serverEntity.isDrm());
                servers.add(server);
            }
        }
        entry.setServers(servers);

        List<Season> seasons = new ArrayList<>();
        if (entityWithDetails.seasons != null) {
            for (SeasonWithEpisodes seasonWithEpisodes : entityWithDetails.seasons) {
                Season season = new Season();
                season.setSeason(seasonWithEpisodes.season.getSeasonNumber());
                season.setSeasonPoster(seasonWithEpisodes.season.getSeasonPoster());

                List<Episode> episodes = new ArrayList<>();
                if (seasonWithEpisodes.episodes != null) {
                    for (EpisodeWithServers episodeWithServers : seasonWithEpisodes.episodes) {
                        Episode episode = new Episode();
                        episode.setEpisode(episodeWithServers.episode.getEpisodeNumber());
                        episode.setTitle(episodeWithServers.episode.getTitle());
                        episode.setDuration(episodeWithServers.episode.getDuration());
                        episode.setDescription(episodeWithServers.episode.getDescription());
                        episode.setThumbnail(episodeWithServers.episode.getThumbnail());

                        List<Server> episodeServers = new ArrayList<>();
                        if (episodeWithServers.servers != null) {
                            for (com.cinecraze.free.database.entities.ServerEntity serverEntity : episodeWithServers.servers) {
                                Server server = new Server();
                                server.setName(serverEntity.getName());
                                server.setUrl(serverEntity.getUrl());
                                server.setLicense(serverEntity.getLicense());
                                server.setDrm(serverEntity.isDrm());
                                episodeServers.add(server);
                            }
                        }
                        episode.setServers(episodeServers);
                        episodes.add(episode);
                    }
                }
                season.setEpisodes(episodes);
                seasons.add(season);
            }
        }
        entry.setSeasons(seasons);

        return entry;
    }

    /**
     * Convert list of EntryWithDetails to list of Entry
     */
    public static List<Entry> entitiesToEntries(List<EntryWithDetails> entitiesWithDetails) {
        List<Entry> entries = new ArrayList<>();
        for (EntryWithDetails entityWithDetails : entitiesWithDetails) {
            entries.add(entityToEntry(entityWithDetails));
        }
        return entries;
    }
}