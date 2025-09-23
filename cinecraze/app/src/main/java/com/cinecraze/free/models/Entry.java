package com.cinecraze.free.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Entry {

    @SerializedName("id")
    private int id;

    @SerializedName("type")
    private String type;

    private String mainCategory;

    @SerializedName("Title")
    private String title;

    @SerializedName("Description")
    private String description;

    @SerializedName("Poster")
    private String poster;

    @SerializedName("Thumbnail")
    private String thumbnail;

    @SerializedName("Rating")
    private Object rating;

    @SerializedName("Year")
    private Object year;

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
        if (rating instanceof Number) {
            return ((Number) rating).floatValue();
        }
        return 0.0f;
    }

    public int getYear() {
        if (year instanceof Number) {
            return ((Number) year).intValue();
        }
        return 0;
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

    public void setId(int id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setRating(Object rating) {
        this.rating = rating;
    }

    public void setYear(Object year) {
        this.year = year;
    }

    public void setParentalRating(String parentalRating) {
        this.parentalRating = parentalRating;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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

    public String getMainCategory() {
        return type;
    }

    public void setMainCategory(String mainCategory) {
        this.type = mainCategory;
    }
}
