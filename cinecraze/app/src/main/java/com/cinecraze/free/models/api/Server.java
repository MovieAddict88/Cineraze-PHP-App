package com.cinecraze.free.models.api;

import com.google.gson.annotations.SerializedName;

public class Server {
    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
