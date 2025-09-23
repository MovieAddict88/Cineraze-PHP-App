package com.cinecraze.free;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.cinecraze.free.database.CineCrazeDatabase;
import com.cinecraze.free.database.dao.EntryDao;
import com.cinecraze.free.database.dao.EpisodeDao;
import com.cinecraze.free.database.dao.SeasonDao;
import com.cinecraze.free.database.dao.ServerDao;
import com.cinecraze.free.database.entities.EntryEntity;
import com.cinecraze.free.database.entities.EpisodeEntity;
import com.cinecraze.free.database.entities.SeasonEntity;
import com.cinecraze.free.database.entities.ServerEntity;
import com.cinecraze.free.database.pojos.EntryWithDetails;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    private CineCrazeDatabase db;
    private EntryDao entryDao;
    private ServerDao serverDao;
    private SeasonDao seasonDao;
    private EpisodeDao episodeDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, CineCrazeDatabase.class).build();
        entryDao = db.entryDao();
        serverDao = db.serverDao();
        seasonDao = db.seasonDao();
        episodeDao = db.episodeDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void writeEntryAndReadWithDetails() throws Exception {
        // Create and insert an entry
        EntryEntity entry = new EntryEntity();
        entry.setTitle("Test Movie");
        long entryId = entryDao.insertAndGetId(entry);

        // Create and insert a server for the movie
        ServerEntity movieServer = new ServerEntity();
        movieServer.setName("Movie Server");
        movieServer.setEntryId((int)entryId);
        serverDao.insert(movieServer);

        // Create and insert a season
        SeasonEntity season = new SeasonEntity();
        season.setSeasonNumber(1);
        season.setEntryId((int)entryId);
        long seasonId = seasonDao.insertAndGetId(season);

        // Create and insert an episode
        EpisodeEntity episode = new EpisodeEntity();
        episode.setEpisodeNumber(1);
        episode.setSeasonId((int)seasonId);
        long episodeId = episodeDao.insertAndGetId(episode);

        // Create and insert a server for the episode
        ServerEntity episodeServer = new ServerEntity();
        episodeServer.setName("Episode Server");
        episodeServer.setEpisodeId((int)episodeId);
        serverDao.insert(episodeServer);

        // Read the entry with details
        EntryWithDetails entryWithDetails = entryDao.getEntryWithDetails((int)entryId);

        // Assertions
        assertNotNull(entryWithDetails);
        assertEquals("Test Movie", entryWithDetails.entry.getTitle());

        // Check movie server
        assertNotNull(entryWithDetails.servers);
        assertEquals(1, entryWithDetails.servers.size());
        assertEquals("Movie Server", entryWithDetails.servers.get(0).getName());

        // Check season
        assertNotNull(entryWithDetails.seasons);
        assertEquals(1, entryWithDetails.seasons.size());
        assertEquals(1, entryWithDetails.seasons.get(0).season.getSeasonNumber());

        // Check episode
        assertNotNull(entryWithDetails.seasons.get(0).episodes);
        assertEquals(1, entryWithDetails.seasons.get(0).episodes.size());
        assertEquals(1, entryWithDetails.seasons.get(0).episodes.get(0).episode.getEpisodeNumber());

        // Check episode server
        assertNotNull(entryWithDetails.seasons.get(0).episodes.get(0).servers);
        assertEquals(1, entryWithDetails.seasons.get(0).episodes.get(0).servers.size());
        assertEquals("Episode Server", entryWithDetails.seasons.get(0).episodes.get(0).servers.get(0).getName());
    }
}
