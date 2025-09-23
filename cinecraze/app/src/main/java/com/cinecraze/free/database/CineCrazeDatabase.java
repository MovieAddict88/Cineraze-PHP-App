package com.cinecraze.free.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.cinecraze.free.database.dao.CacheMetadataDao;
import com.cinecraze.free.database.dao.DownloadItemDao;
import com.cinecraze.free.database.dao.EntryDao;
import com.cinecraze.free.database.dao.EpisodeDao;
import com.cinecraze.free.database.dao.SeasonDao;
import com.cinecraze.free.database.dao.ServerDao;
import com.cinecraze.free.database.entities.CacheMetadataEntity;
import com.cinecraze.free.database.entities.DownloadItemEntity;
import com.cinecraze.free.database.entities.EntryEntity;
import com.cinecraze.free.database.entities.EpisodeEntity;
import com.cinecraze.free.database.entities.SeasonEntity;
import com.cinecraze.free.database.entities.ServerEntity;

@Database(
    entities = {EntryEntity.class, CacheMetadataEntity.class, DownloadItemEntity.class, ServerEntity.class, SeasonEntity.class, EpisodeEntity.class},
    version = 4,
    exportSchema = false
)
public abstract class CineCrazeDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "cinecraze_database";
    private static CineCrazeDatabase instance;
    
    public abstract EntryDao entryDao();
    public abstract CacheMetadataDao cacheMetadataDao();
    public abstract DownloadItemDao downloadItemDao();
    public abstract ServerDao serverDao();
    public abstract SeasonDao seasonDao();
    public abstract EpisodeDao episodeDao();
    
    public static synchronized CineCrazeDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                CineCrazeDatabase.class,
                DATABASE_NAME
            )
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .allowMainThreadQueries() // For simplicity, but ideally use background threads
            .build();
        }
        return instance;
    }

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create new tables
            database.execSQL("CREATE TABLE IF NOT EXISTS `servers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `url` TEXT, `license` TEXT, `drm` INTEGER NOT NULL, `entry_id` INTEGER, `episode_id` INTEGER, FOREIGN KEY(`entry_id`) REFERENCES `entries`(`id`) ON DELETE CASCADE, FOREIGN KEY(`episode_id`) REFERENCES `episodes`(`id`) ON DELETE CASCADE )");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_servers_entry_id` ON `servers` (`entry_id`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_servers_episode_id` ON `servers` (`episode_id`)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `seasons` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `season_number` INTEGER NOT NULL, `season_poster` TEXT, `entry_id` INTEGER NOT NULL, FOREIGN KEY(`entry_id`) REFERENCES `entries`(`id`) ON DELETE CASCADE )");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_seasons_entry_id` ON `seasons` (`entry_id`)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `episodes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `episode_number` INTEGER NOT NULL, `title` TEXT, `duration` TEXT, `description` TEXT, `thumbnail` TEXT, `season_id` INTEGER NOT NULL, FOREIGN KEY(`season_id`) REFERENCES `seasons`(`id`) ON DELETE CASCADE )");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_episodes_season_id` ON `episodes` (`season_id`)");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create a new temporary table without the JSON columns
            database.execSQL("CREATE TABLE `entries_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT, `sub_category` TEXT, `country` TEXT, `description` TEXT, `poster` TEXT, `thumbnail` TEXT, `rating` TEXT, `duration` TEXT, `year` TEXT, `main_category` TEXT)");

            // Copy the data from the old table to the new table
            database.execSQL("INSERT INTO `entries_new` (id, title, sub_category, country, description, poster, thumbnail, rating, duration, year, main_category) SELECT id, title, sub_category, country, description, poster, thumbnail, rating, duration, year, main_category FROM `entries`");

            // Drop the old table
            database.execSQL("DROP TABLE `entries`");

            // Rename the new table to the original table name
            database.execSQL("ALTER TABLE `entries_new` RENAME TO `entries`");
        }
    };
}