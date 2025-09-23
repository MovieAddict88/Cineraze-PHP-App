package com.cinecraze.free.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "entries")
public class EntryEntity {
    
    @PrimaryKey
    private int id;
    
    @ColumnInfo(name = "title")
    private String title;
    
    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "description")
    private String description;
    
    @ColumnInfo(name = "poster")
    private String poster;
    
    @ColumnInfo(name = "thumbnail")
    private String thumbnail;
    
    @ColumnInfo(name = "rating")
    private String rating;
    
    @ColumnInfo(name = "duration")
    private String duration;
    
    @ColumnInfo(name = "year")
    private String year;
    
    @ColumnInfo(name = "parental_rating")
    private String parentalRating;

    @ColumnInfo(name = "main_category")
    private String mainCategory;
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }
    
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    
    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }
    
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    
    public String getParentalRating() { return parentalRating; }
    public void setParentalRating(String parentalRating) { this.parentalRating = parentalRating; }

    public String getMainCategory() { return mainCategory; }
    public void setMainCategory(String mainCategory) { this.mainCategory = mainCategory; }
}