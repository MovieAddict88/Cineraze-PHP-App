package com.cinecraze.free.repository;

import android.content.Context;
import android.util.Log;

import com.cinecraze.free.models.ApiResponse;
import com.cinecraze.free.models.Category;
import com.cinecraze.free.models.Entry;
import com.cinecraze.free.net.ApiService;
import com.cinecraze.free.net.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataRepository {

    private static final String TAG = "DataRepository";
    public static final int DEFAULT_PAGE_SIZE = 20;

    private ApiService apiService;

    // This callback is kept for compatibility with BaseFragment's pull-to-refresh
    public interface DataCallback {
        void onSuccess(List<Entry> entries);
        void onError(String error);
    }

    public interface PaginatedDataCallback {
        void onSuccess(List<Entry> entries, boolean hasMorePages, int totalCount);
        void onError(String error);
    }

    public DataRepository(Context context) {
        apiService = RetrofitClient.getClient(context).create(ApiService.class);
    }

    private void fetchData(int page, int pageSize, String sort, String type, PaginatedDataCallback callback) {
        Log.d(TAG, "Fetching data from API: page=" + page + ", limit=" + pageSize + ", sort=" + sort + ", type=" + type);
        apiService.getContent(page, pageSize, sort, type).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    List<Entry> allEntries = new ArrayList<>();

                    if (apiResponse.getCategories() != null) {
                        for (Category category : apiResponse.getCategories()) {
                            if (category.getEntries() != null) {
                                for (Entry entry : category.getEntries()) {
                                    entry.setMainCategory(category.getMainCategory());
                                }
                                allEntries.addAll(category.getEntries());
                            }
                        }
                    }

                    boolean hasMorePages = apiResponse.getPagination().getPage() < apiResponse.getPagination().getTotalPages();
                    int totalCount = apiResponse.getPagination().getTotalItems();

                    Log.d(TAG, "Successfully fetched page " + apiResponse.getPagination().getPage() + ". Total items: " + totalCount);
                    callback.onSuccess(allEntries, hasMorePages, totalCount);

                } else {
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                    Log.e(TAG, "API call failed with code: " + response.code() + " and body: " + errorBody);
                    callback.onError("API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                callback.onError("Network Error: " + t.getMessage());
            }
        });
    }

    public void getPaginatedData(int page, int pageSize, PaginatedDataCallback callback) {
        // UI pages are 0-based, API is 1-based
        fetchData(page + 1, pageSize, "newest", "all", callback);
    }

    public void getPaginatedDataByCategory(String category, int page, int pageSize, PaginatedDataCallback callback) {
        String type;
        switch (category) {
            case "Movies": type = "movie"; break;
            case "TV Series": type = "series"; break;
            case "Live TV": type = "live"; break;
            default: type = "all"; break;
        }
        // UI pages are 0-based, API is 1-based
        fetchData(page + 1, pageSize, "newest", type, callback);
    }

    public void searchPaginated(String searchQuery, int page, int pageSize, PaginatedDataCallback callback) {
        Log.w(TAG, "Search functionality is not supported by the current API endpoint.");
        callback.onSuccess(new ArrayList<>(), false, 0);
    }

    public void forceRefreshData(final PaginatedDataCallback callback) {
        Log.d(TAG, "Force refresh triggered. Fetching page 1.");
        getPaginatedData(0, DEFAULT_PAGE_SIZE, callback);
    }

    public void getPaginatedFilteredData(String genre, String country, String year, int page, int pageSize, PaginatedDataCallback callback) {
        Log.w(TAG, "Filtering by genre/country/year is not supported by the current API.");
        callback.onSuccess(new ArrayList<>(), false, 0);
    }

    public List<String> getUniqueGenres() {
        Log.w(TAG, "getUniqueGenres is not supported and will return an empty list.");
        return new ArrayList<>();
    }

    public List<String> getUniqueCountries() {
        Log.w(TAG, "getUniqueCountries is not supported and will return an empty list.");
        return new ArrayList<>();
    }

    public List<String> getUniqueYears() {
        Log.w(TAG, "getUniqueYears is not supported and will return an empty list.");
        return new ArrayList<>();
    }

    public void getTopRatedEntries(int count, DataCallback callback) {
        // Fetch page 1, sorted by rating, with a limit of `count`
        fetchData(1, count, "rating", "all", new PaginatedDataCallback() {
            @Override
            public void onSuccess(List<Entry> entries, boolean hasMorePages, int totalCount) {
                callback.onSuccess(entries);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
}