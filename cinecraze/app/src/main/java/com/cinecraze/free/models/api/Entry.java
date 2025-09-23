package com.cinecraze.free.models.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Entry {
    @SerializedName("id")
    private int id;

    @SerializedName("type")
    private String type;

    @SerializedName("Title")
    private String title;

    @SerializedName("Description")
    private String description;

    @SerializedName("Poster")
    private String poster;

    @SerializedName("Thumbnail")
    private String thumbnail;

    @SerializedName("Rating")
    private float rating;

    @SerializedName("Year")
    private int year;

    @SerializedName("parentalRating")
    private String parentalRating;

    @SerializedName("Duration")
    private String duration;

    @SerializedName("Servers")
    private List<Server> servers;

    @SerializedName("Seasons")
    private List<Season> seasons;

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPoster() {
        return poster;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public float getRating() {
        return rating;
    }

    public int getYear() {
        return year;
    }

    public String getParentalRating() {
        return parentalRating;
    }

    public String getDuration() {
        return duration;
    }

    public List<Server> getServers() {
        return servers;
    }

    public List<Season> getSeasons() {
        return seasons;
    }
}
