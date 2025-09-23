package com.cinecraze.free.repository;

import android.content.Context;
import android.util.Log;

import com.cinecraze.free.database.CineCrazeDatabase;
import com.cinecraze.free.database.DatabaseUtils;
import com.cinecraze.free.database.entities.CacheMetadataEntity;
import com.cinecraze.free.database.entities.EntryEntity;
import com.cinecraze.free.database.pojos.EntryWithDetails;
import com.cinecraze.free.database.entities.EpisodeEntity;
import com.cinecraze.free.database.entities.SeasonEntity;
import com.cinecraze.free.database.entities.ServerEntity;
import com.cinecraze.free.models.Category;
import com.cinecraze.free.models.CineData;
import com.cinecraze.free.models.Entry;
import com.cinecraze.free.net.ApiService;
import com.cinecraze.free.net.RetrofitClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataRepository {

    private static final String TAG = "DataRepository";
    private static final String CACHE_KEY_API_DATA = "api_data";
    private static final int API_PAGE_LIMIT = 50;

    private CineCrazeDatabase database;
    private ApiService apiService;

    public interface DataCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface PaginatedDataCallback {
        void onSuccess(List<Entry> entries, boolean hasMorePages, int totalCount);
        void onError(String error);
    }

    public DataRepository(Context context) {
        database = CineCrazeDatabase.getInstance(context);
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    public void syncRemoteData(DataCallback callback) {
        List<CineData> allData = new ArrayList<>();
        fetchAllPages(1, allData, new DataCallback() {
            @Override
            public void onSuccess() {
                cacheApiData(allData);
                callback.onSuccess();
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private void fetchAllPages(int page, List<CineData> allData, DataCallback callback) {
        apiService.getContent(page, API_PAGE_LIMIT, "all", "newest").enqueue(new Callback<CineData>() {
            @Override
            public void onResponse(Call<CineData> call, Response<CineData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CineData cineData = response.body();
                    allData.add(cineData);
                    if (cineData.getPagination().getPage() < cineData.getPagination().getTotalPages()) {
                        fetchAllPages(page + 1, allData, callback);
                    } else {
                        callback.onSuccess();
                    }
                } else {
                    callback.onError("API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CineData> call, Throwable t) {
                callback.onError("Network Error: " + t.getMessage());
            }
        });
    }

    private void cacheApiData(List<CineData> allData) {
        database.runInTransaction(() -> {
            database.serverDao().deleteAll();
            database.episodeDao().deleteAll();
            database.seasonDao().deleteAll();
            database.entryDao().deleteAll();

            Set<Integer> entryIds = new HashSet<>();
            for (CineData cineData : allData) {
                for (Category category : cineData.getCategories()) {
                    String mainCategory = category.getMainCategory();
                    for (Entry entry : category.getEntries()) {
                        if (entry != null && entryIds.add(entry.getId())) {
                            EntryEntity entryEntity = DatabaseUtils.entryToEntity(entry, mainCategory);
                            long entryId = database.entryDao().insertAndGetId(entryEntity);

                            if (entry.getServers() != null) {
                                for (com.cinecraze.free.models.Server server : entry.getServers()) {
                                    ServerEntity serverEntity = new ServerEntity();
                                    serverEntity.setName(server.getName());
                                    serverEntity.setUrl(server.getUrl());
                                    serverEntity.setEntryId((int) entryId);
                                    database.serverDao().insert(serverEntity);
                                }
                            }

                            if (entry.getSeasons() != null) {
                                for (com.cinecraze.free.models.Season season : entry.getSeasons()) {
                                    SeasonEntity seasonEntity = new SeasonEntity();
                                    seasonEntity.setSeasonNumber(season.getSeason());
                                    seasonEntity.setSeasonPoster(season.getSeasonPoster());
                                    seasonEntity.setEntryId((int) entryId);
                                    long seasonId = database.seasonDao().insertAndGetId(seasonEntity);

                                    if (season.getEpisodes() != null) {
                                        for (com.cinecraze.free.models.Episode episode : season.getEpisodes()) {
                                            EpisodeEntity episodeEntity = new EpisodeEntity();
                                            episodeEntity.setEpisodeNumber(episode.getEpisode());
                                            episodeEntity.setTitle(episode.getTitle());
                                            episodeEntity.setDuration(episode.getDuration());
                                            episodeEntity.setDescription(episode.getDescription());
                                            episodeEntity.setThumbnail(episode.getThumbnail());
                                            episodeEntity.setSeasonId((int) seasonId);
                                            long episodeId = database.episodeDao().insertAndGetId(episodeEntity);

                                            if (episode.getServers() != null) {
                                                for (com.cinecraze.free.models.Server server : episode.getServers()) {
                                                    ServerEntity serverEntity = new ServerEntity();
                                                    serverEntity.setName(server.getName());
                                                    serverEntity.setUrl(server.getUrl());
                                                    serverEntity.setEpisodeId((int) episodeId);
                                                    database.serverDao().insert(serverEntity);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            CacheMetadataEntity metadata = new CacheMetadataEntity(
                    CACHE_KEY_API_DATA,
                    System.currentTimeMillis(),
                    "1"
            );
            database.cacheMetadataDao().insert(metadata);
        });
    }

    public void getPaginatedData(int page, int pageSize, PaginatedDataCallback callback) {
        try {
            int offset = (page - 1) * pageSize;
            List<EntryWithDetails> entities = database.entryDao().getEntriesWithDetailsPaged(pageSize, offset);
            List<Entry> entries = DatabaseUtils.entitiesToEntries(entities);
            int totalCount = database.entryDao().getEntriesCount();
            boolean hasMorePages = (offset + pageSize) < totalCount;
            callback.onSuccess(entries, hasMorePages, totalCount);
        } catch (Exception e) {
            callback.onError("Error loading page: " + e.getMessage());
        }
    }

    public List<Entry> getTopRatedEntries(int count) {
        try {
            List<EntryWithDetails> entities = database.entryDao().getTopRatedEntriesWithDetails(count);
            return DatabaseUtils.entitiesToEntries(entities);
        } catch (Exception e) {
            Log.e(TAG, "Error getting top rated entries: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public void getPaginatedDataByCategory(String category, int page, int pageSize, PaginatedDataCallback callback) {
        try {
            int offset = (page - 1) * pageSize;
            List<EntryWithDetails> entities = database.entryDao().getEntriesWithDetailsByCategoryPaged(category, pageSize, offset);
            List<Entry> entries = DatabaseUtils.entitiesToEntries(entities);
            int totalCount = database.entryDao().getEntriesCountByCategory(category);
            boolean hasMorePages = (offset + pageSize) < totalCount;
            callback.onSuccess(entries, hasMorePages, totalCount);
        } catch (Exception e) {
            callback.onError("Error loading category page: " + e.getMessage());
        }
    }

    public void searchPaginated(String searchQuery, int page, int pageSize, PaginatedDataCallback callback) {
        try {
            int offset = (page - 1) * pageSize;
            List<EntryWithDetails> entities = database.entryDao().searchWithDetailsByTitlePaged(searchQuery, pageSize, offset);
            List<Entry> entries = DatabaseUtils.entitiesToEntries(entities);
            int totalCount = database.entryDao().getSearchResultsCount(searchQuery);
            boolean hasMorePages = (offset + pageSize) < totalCount;
            callback.onSuccess(entries, hasMorePages, totalCount);
        } catch (Exception e) {
            callback.onError("Error searching: " + e.getMessage());
        }
    }

    public List<String> getUniqueGenres() {
        try {
            return database.entryDao().getUniqueGenres();
        } catch (Exception e) {
            Log.e(TAG, "Error getting unique genres: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<String> getUniqueYears() {
        try {
            return database.entryDao().getUniqueYears();
        } catch (Exception e) {
            Log.e(TAG, "Error getting unique years: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public void getPaginatedFilteredData(String genre, String year, int page, int pageSize, PaginatedDataCallback callback) {
        try {
            int offset = (page - 1) * pageSize;
            List<EntryWithDetails> entities = database.entryDao().getEntriesWithDetailsFilteredPaged(
                genre, year, pageSize, offset
            );
            List<Entry> entries = DatabaseUtils.entitiesToEntries(entities);
            int totalCount = database.entryDao().getEntriesFilteredCount(genre, year);
            boolean hasMorePages = (offset + pageSize) < totalCount;
            callback.onSuccess(entries, hasMorePages, totalCount);
        } catch (Exception e) {
            callback.onError("Error loading filtered page: " + e.getMessage());
        }
    }

    public Entry getEntryWithDetails(int entryId) {
        EntryWithDetails entryWithDetails = database.entryDao().getEntryWithDetails(entryId);
        if (entryWithDetails != null) {
            return DatabaseUtils.entityToEntry(entryWithDetails);
        }
        return null;
    }
}