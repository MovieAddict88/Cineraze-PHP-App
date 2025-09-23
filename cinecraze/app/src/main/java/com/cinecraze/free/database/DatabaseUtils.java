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
        
        entity.setTitle(entry.getTitle());
        entity.setSubCategory(entry.getSubCategory());
        entity.setCountry(entry.getCountry());
        entity.setDescription(entry.getDescription());
        entity.setPoster(entry.getPoster());
        entity.setThumbnail(entry.getThumbnail());
        entity.setRating(entry.getRatingString());
        entity.setDuration(entry.getDuration());
        entity.setYear(entry.getYearString());
        entity.setMainCategory(mainCategory);
        
        return entity;
    }
    
    /**
     * Convert EntryWithDetails database POJO to Entry API model
     */
    public static Entry entityToEntry(EntryWithDetails entityWithDetails) {
        Entry entry = new Entry();
        EntryEntity entity = entityWithDetails.entry;

        // Use proper setter methods
        entry.setTitle(entity.getTitle());
        entry.setSubCategory(entity.getSubCategory());
        entry.setMainCategory(entity.getMainCategory());
        entry.setCountry(entity.getCountry());
        entry.setDescription(entity.getDescription());
        entry.setPoster(entity.getPoster());
        entry.setThumbnail(entity.getThumbnail());
        entry.setRating(entity.getRating());
        entry.setDuration(entity.getDuration());
        entry.setYear(entity.getYear());

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
        entry.setRelated(new ArrayList<>()); // Related entries are not handled in this version

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