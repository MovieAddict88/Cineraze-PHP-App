package com.cinecraze.free.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "episodes",
        foreignKeys = @ForeignKey(entity = SeasonEntity.class,
                                  parentColumns = "id",
                                  childColumns = "season_id",
                                  onDelete = ForeignKey.CASCADE))
public class EpisodeEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "episode_number")
    private int episodeNumber;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "duration")
    private String duration;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "thumbnail")
    private String thumbnail;

    @ColumnInfo(name = "season_id", index = true)
    private int seasonId;

    // Constructor
    public EpisodeEntity() {}

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEpisodeNumber() { return episodeNumber; }
    public void setEpisodeNumber(int episodeNumber) { this.episodeNumber = episodeNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public int getSeasonId() { return seasonId; }
    public void setSeasonId(int seasonId) { this.seasonId = seasonId; }
}
