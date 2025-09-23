package com.cinecraze.free.models.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Episode {
    @SerializedName("id")
    private int id;

    @SerializedName("type")
    private String type;

    @SerializedName("Episode")
    private int episode;

    @SerializedName("Title")
    private String title;

    @SerializedName("Description")
    private String description;

    @SerializedName("Thumbnail")
    private String thumbnail;

    @SerializedName("Duration")
    private String duration;

    @SerializedName("Servers")
    private List<Server> servers;

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public int getEpisode() {
        return episode;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getDuration() {
        return duration;
    }

    public List<Server> getServers() {
        return servers;
    }
}
