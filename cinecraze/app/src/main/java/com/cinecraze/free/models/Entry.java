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

    // This field will be populated manually after parsing
    private transient String mainCategory;

    @SerializedName("Description")
    private String description;

    @SerializedName("Poster")
    private String poster;

    @SerializedName("Thumbnail")
    private String thumbnail;

    @SerializedName("Rating")
    private Object rating; // Can be float, int, or String

    @SerializedName("parentalRating")
    private String parentalRating;

    @SerializedName("Duration")
    private String duration;

    @SerializedName("Year")
    private Object year; // Can be int or String

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

    public String getMainCategory() {
        return mainCategory;
    }

    public void setMainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
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
        if (rating instanceof Number) {
            return ((Number) rating).floatValue();
        } else if (rating instanceof String) {
            try {
                return Float.parseFloat((String) rating);
            } catch (NumberFormatException e) {
                return 0.0f;
            }
        }
        return 0.0f;
    }

    public String getRatingString() {
        if (rating instanceof String) {
            return (String) rating;
        } else if (rating instanceof Number) {
            return String.valueOf(rating);
        }
        return "0";
    }

    public void setRating(Object rating) {
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
        if (year instanceof Number) {
            return ((Number) year).intValue();
        } else if (year instanceof String) {
            try {
                return Integer.parseInt((String) year);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public String getYearString() {
        if (year instanceof String) {
            return (String) year;
        } else if (year instanceof Number) {
            return String.valueOf(year);
        }
        return "0";
    }

    public void setYear(Object year) {
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
