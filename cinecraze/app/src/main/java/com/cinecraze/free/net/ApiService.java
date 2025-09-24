package com.cinecraze.free.net;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("content.php")
    Call<JsonObject> getContent(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("type") String type,
            @Query("sort") String sort
    );
}