package com.cinecraze.free.net;

import android.content.Context;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://moviefcs.infinityfreeapp.com/api/";

    // Making getClient accept a context to create a cache directory
    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            // Create a cache
            int cacheSize = 10 * 1024 * 1024; // 10 MiB
            Cache cache = new Cache(new File(context.getCacheDir(), "http-cache"), cacheSize);

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // This interceptor will be used to add cache control headers
            Interceptor networkInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .header("Cache-Control", "public, max-age=" + (60 * 5)) // 5 minutes
                            .build();
                }
            };

            Interceptor customInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("User-Agent", "Mozilla/5.0 (Android) CineCraze/1.0")
                            .header("Accept", "application/json")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(customInterceptor)
                    .addNetworkInterceptor(networkInterceptor) // Use addNetworkInterceptor for response headers
                    .addInterceptor(loggingInterceptor)
                    .cache(cache) // Add the cache
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
