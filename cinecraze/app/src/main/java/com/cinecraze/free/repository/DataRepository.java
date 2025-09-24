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
import com.cinecraze.free.models.Entry;
import com.cinecraze.free.models.Playlist;
import com.cinecraze.free.models.PlaylistsVersion;
import com.cinecraze.free.net.ApiService;
import com.cinecraze.free.net.RetrofitClient;

import java.util.ArrayList;
import android.os.Handler;
import android.os.Looper;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataRepository {

    private static final String TAG = "DataRepository";
    private static final String CACHE_KEY_PLAYLIST = "playlist_data";
    private static final String CACHE_KEY_PLAYLIST_VERSION = "playlist_version";
    private static final long CACHE_EXPIRY_HOURS = 24; // Cache expires after 24 hours
    public static final int DEFAULT_PAGE_SIZE = 20; // Default items per page

    private CineCrazeDatabase database;
    private ApiService apiService;
    private Handler mainHandler;


    public interface DataCallback {
        void onSuccess(List<Entry> entries);
        void onError(String error);
    }

    public interface PaginatedDataCallback {
        void onSuccess(List<Entry> entries, boolean hasMorePages, int totalCount);
        void onError(String error);
    }

    public interface ContentCallback {
        void onSuccess(ApiResponse apiResponse);
        void onError(String error);
    }

    public DataRepository(Context context) {
        database = CineCrazeDatabase.getInstance(context);
        apiService = RetrofitClient.getClient().create(ApiService.class);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Expose cache validity so UI can decide whether to prompt before downloading
     */
    public boolean hasValidCache() {
        try {
            CacheMetadataEntity metadata = database.cacheMetadataDao().getMetadata(CACHE_KEY_PLAYLIST);
            return metadata != null && isCacheValid(metadata.getLastUpdated());
        } catch (Exception e) {
            Log.e(TAG, "Error checking cache validity: " + e.getMessage(), e);
            return false;
        }
    }

    private void cacheContent(ApiResponse apiResponse, boolean clearExistingData) {
        database.runInTransaction(() -> {
            if (clearExistingData) {
                database.serverDao().deleteAll();
                database.episodeDao().deleteAll();
                database.seasonDao().deleteAll();
                database.entryDao().deleteAll();
            }

            if (apiResponse.getCategories() != null) {
                for (Category category : apiResponse.getCategories()) {
                    if (category != null && category.getEntries() != null) {
                        String mainCategory = category.getMainCategory();
                        for (Entry entry : category.getEntries()) {
                            if (entry != null) {
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
            }

            CacheMetadataEntity metadata = new CacheMetadataEntity(
                    CACHE_KEY_PLAYLIST,
                    System.currentTimeMillis(),
                    String.valueOf(apiResponse.getPagination().getPage())
            );
            database.cacheMetadataDao().insert(metadata);
        });
    }


    /**
     * Get paginated data from cache
     */
    public void getPaginatedData(int page, int pageSize, PaginatedDataCallback callback) {
        try {
            int offset = page * pageSize;
            List<EntryWithDetails> entities = database.entryDao().getEntriesWithDetailsPaged(pageSize, offset);
            List<Entry> entries = DatabaseUtils.entitiesToEntries(entities);
            int totalCount = database.entryDao().getEntriesCount();
            boolean hasMorePages = (offset + pageSize) < totalCount;

            Log.d(TAG, "Loaded page " + page + " with " + entries.size() + " items. Total: " + totalCount + ", HasMore: " + hasMorePages);
            callback.onSuccess(entries, hasMorePages, totalCount);
        } catch (Exception e) {
            Log.e(TAG, "Error loading paginated data: " + e.getMessage(), e);
            callback.onError("Error loading page: " + e.getMessage());
        }
    }

    /**
     * Get paginated data by category
     */
    public void getPaginatedDataByCategory(String category, int page, int pageSize, PaginatedDataCallback callback) {
        try {
            int offset = page * pageSize;
            List<EntryWithDetails> entities = database.entryDao().getEntriesWithDetailsByCategoryPaged(category, pageSize, offset);
            List<Entry> entries = DatabaseUtils.entitiesToEntries(entities);
            int totalCount = database.entryDao().getEntriesCountByCategory(category);
            boolean hasMorePages = (offset + pageSize) < totalCount;

            Log.d(TAG, "Loaded category '" + category + "' page " + page + " with " + entries.size() + " items. Total: " + totalCount);
            callback.onSuccess(entries, hasMorePages, totalCount);
        } catch (Exception e) {
            Log.e(TAG, "Error loading paginated category data: " + e.getMessage(), e);
            callback.onError("Error loading category page: " + e.getMessage());
        }
    }

    /**
     * Search with pagination
     */
    public void searchPaginated(String searchQuery, int page, int pageSize, PaginatedDataCallback callback) {
        try {
            int offset = page * pageSize;
            List<EntryWithDetails> entities = database.entryDao().searchWithDetailsByTitlePaged(searchQuery, pageSize, offset);
            List<Entry> entries = DatabaseUtils.entitiesToEntries(entities);
            int totalCount = database.entryDao().getSearchResultsCount(searchQuery);
            boolean hasMorePages = (offset + pageSize) < totalCount;

            Log.d(TAG, "Search '" + searchQuery + "' page " + page + " with " + entries.size() + " results. Total: " + totalCount);
            callback.onSuccess(entries, hasMorePages, totalCount);
        } catch (Exception e) {
            Log.e(TAG, "Error searching with pagination: " + e.getMessage(), e);
            callback.onError("Error searching: " + e.getMessage());
        }
    }

    /**
     * Get entries by category from cache
     */
    public List<Entry> getEntriesByCategory(String category) {
        List<EntryWithDetails> entities = database.entryDao().getEntriesWithDetailsByCategory(category);
        return DatabaseUtils.entitiesToEntries(entities);
    }

    /**
     * Search entries by title from cache
     */
    public List<Entry> searchByTitle(String title) {
        List<EntryWithDetails> entities = database.entryDao().searchWithDetailsByTitle(title);
        return DatabaseUtils.entitiesToEntries(entities);
    }

    /**
     * Get all cached entries
     */
    public List<Entry> getAllCachedEntries() {
        List<EntryWithDetails> entities = database.entryDao().getEntriesWithDetails();
        return DatabaseUtils.entitiesToEntries(entities);
    }

    public Entry getEntryWithDetails(int entryId) {
        EntryWithDetails entryWithDetails = database.entryDao().getEntryWithDetails(entryId);
        if (entryWithDetails != null) {
            return DatabaseUtils.entityToEntry(entryWithDetails);
        }
        return null;
    }

    /**
     * Get total count of cached entries
     */
    public int getTotalEntriesCount() {
        return database.entryDao().getEntriesCount();
    }

    /**
     * Get unique genres from cached data
     */
    public List<String> getUniqueGenres() {
        try {
            List<String> genres = database.entryDao().getUniqueGenres();
            // Filter out null and empty values
            List<String> filteredGenres = new ArrayList<>();
            for (String genre : genres) {
                if (genre != null && !genre.trim().isEmpty() && !genre.equalsIgnoreCase("null")) {
                    filteredGenres.add(genre.trim());
                }
            }
            return filteredGenres;
        } catch (Exception e) {
            Log.e(TAG, "Error getting unique genres: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get unique countries from cached data
     */
    public List<String> getUniqueCountries() {
        try {
            List<String> countries = database.entryDao().getUniqueCountries();
            // Filter out null and empty values
            List<String> filteredCountries = new ArrayList<>();
            for (String country : countries) {
                if (country != null && !country.trim().isEmpty() && !country.equalsIgnoreCase("null")) {
                    filteredCountries.add(country.trim());
                }
            }
            return filteredCountries;
        } catch (Exception e) {
            Log.e(TAG, "Error getting unique countries: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get unique years from cached data
     */
    public List<String> getUniqueYears() {
        try {
            List<String> years = database.entryDao().getUniqueYears();
            // Filter out null, empty, and zero values
            List<String> filteredYears = new ArrayList<>();
            for (String year : years) {
                if (year != null && !year.trim().isEmpty() && !year.equalsIgnoreCase("null") && !year.equals("0")) {
                    filteredYears.add(year.trim());
                }
            }
            return filteredYears;
        } catch (Exception e) {
            Log.e(TAG, "Error getting unique years: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get paginated data filtered by genre, country, and year
     */
    public void getPaginatedFilteredData(String genre, String country, String year, int page, int pageSize, PaginatedDataCallback callback) {
        try {
            int offset = page * pageSize;
            List<EntryWithDetails> entities = database.entryDao().getEntriesWithDetailsFilteredPaged(
                genre == null || genre.isEmpty() ? null : genre,
                country == null || country.isEmpty() ? null : country,
                year == null || year.isEmpty() ? null : year,
                pageSize, offset
            );
            List<Entry> entries = DatabaseUtils.entitiesToEntries(entities);
            int totalCount = database.entryDao().getEntriesFilteredCount(
                genre == null || genre.isEmpty() ? null : genre,
                country == null || country.isEmpty() ? null : country,
                year == null || year.isEmpty() ? null : year
            );
            boolean hasMorePages = (offset + pageSize) < totalCount;

            Log.d(TAG, "Loaded filtered page " + page + " with " + entries.size() + " items. Total: " + totalCount);
            callback.onSuccess(entries, hasMorePages, totalCount);
        } catch (Exception e) {
            Log.e(TAG, "Error loading filtered paginated data: " + e.getMessage(), e);
            callback.onError("Error loading filtered page: " + e.getMessage());
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

    /**
     * Check if cache is still valid
     */
    private boolean isCacheValid(long lastUpdated) {
        long currentTime = System.currentTimeMillis();
        long cacheAge = currentTime - lastUpdated;
        long expiryTime = TimeUnit.HOURS.toMillis(CACHE_EXPIRY_HOURS);
        return cacheAge < expiryTime;
    }

    public void fetchContent(int page, int limit, String type, String sort, ContentCallback callback) {
        apiService.getContent(page, limit, type, sort).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch content: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                callback.onError("Failed to fetch content: " + t.getMessage());
            }
        });
    }

    public void ensureDataAvailable(DataCallback callback) {
        if (hasValidCache()) {
            callback.onSuccess(new ArrayList<>());
        } else {
            fetchContent(1, DEFAULT_PAGE_SIZE, "all", "newest", new ContentCallback() {
                @Override
                public void onSuccess(ApiResponse apiResponse) {
                    cacheContent(apiResponse, true);
                    callback.onSuccess(new ArrayList<>());
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        }
    }

    /**
     * Load data from local cache
     */
}