package com.cinecraze.free.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "seasons",
        foreignKeys = @ForeignKey(entity = EntryEntity.class,
                                  parentColumns = "id",
                                  childColumns = "entry_id",
                                  onDelete = ForeignKey.CASCADE))
public class SeasonEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "season_number")
    private int seasonNumber;

    @ColumnInfo(name = "season_poster")
    private String seasonPoster;

    @ColumnInfo(name = "entry_id", index = true)
    private int entryId;

    // Constructor
    public SeasonEntity() {}

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSeasonNumber() { return seasonNumber; }
    public void setSeasonNumber(int seasonNumber) { this.seasonNumber = seasonNumber; }

    public String getSeasonPoster() { return seasonPoster; }
    public void setSeasonPoster(String seasonPoster) { this.seasonPoster = seasonPoster; }

    public int getEntryId() { return entryId; }
    public void setEntryId(int entryId) { this.entryId = entryId; }
}
