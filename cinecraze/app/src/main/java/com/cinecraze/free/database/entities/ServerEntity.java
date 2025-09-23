package com.cinecraze.free.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "servers",
        foreignKeys = {
            @ForeignKey(entity = EntryEntity.class,
                        parentColumns = "id",
                        childColumns = "entry_id",
                        onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = EpisodeEntity.class,
                        parentColumns = "id",
                        childColumns = "episode_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class ServerEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "license")
    private String license;

    @ColumnInfo(name = "drm")
    private boolean drm;

    @ColumnInfo(name = "entry_id", index = true)
    private Integer entryId;

    @ColumnInfo(name = "episode_id", index = true)
    private Integer episodeId;

    // Constructor
    public ServerEntity() {}

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }

    public boolean isDrm() { return drm; }
    public void setDrm(boolean drm) { this.drm = drm; }

    public Integer getEntryId() { return entryId; }
    public void setEntryId(Integer entryId) { this.entryId = entryId; }

    public Integer getEpisodeId() { return episodeId; }
    public void setEpisodeId(Integer episodeId) { this.episodeId = episodeId; }
}
