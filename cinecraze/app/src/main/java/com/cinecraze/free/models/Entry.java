package com.cinecraze.free.models;

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

    @SerializedName("parentalRating")
    private String parentalRating;

    @SerializedName("Duration")
    private String duration;

    @SerializedName("Year")
    private int year;

    @SerializedName("Servers")
    private List<Server> servers;

    @SerializedName("Seasons")
    private List<Season> seasons;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getParentalRating() {
        return parentalRating;
    }

    public void setParentalRating(String parentalRating) {
        this.parentalRating = parentalRating;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public List<Season> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<Season> seasons) {
        this.seasons = seasons;
    }

    public String getImageUrl() {
        return poster != null ? poster : thumbnail;
    }
}
