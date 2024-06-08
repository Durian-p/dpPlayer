package org.ww.dpplayer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "music.db";
    private static final int DATABASE_VERSION = 2; // Update database version to 2

    // Table names
    public static final String TABLE_LOCAL_MUSIC = "local_music";
    public static final String TABLE_HEART_MUSIC = "heart_music";
    public static final String TABLE_PLAYLIST = "playlist";
    public static final String TABLE_PLAYLIST_SONGS = "playlist_songs";
    public static final String TABLE_PLAY_HISTORY = "play_history"; // New table for play history

    // Common columns
    public static final String COLUMN_ID = "id";

    // Local music table columns
    public static final String COLUMN_SONG_ID = "song_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_ALBUM = "album";
    public static final String COLUMN_ARTIST = "artist";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_PATH = "path";
    public static final String COLUMN_PLAY_COUNT = "play_count"; // New column for play count
    public static final String COLUMN_ADDED_AT = "added_at"; // New column for added time

    // Playlist table columns
    public static final String COLUMN_PLAYLIST_NAME = "name";
    public static final String COLUMN_PLAYLIST_COVER = "cover";

    // Playlist songs table columns
    public static final String COLUMN_PLAYLIST_ID = "playlist_id";

    // Play history table columns
    public static final String COLUMN_PLAYED_AT = "played_at"; // New column for timestamp

    // Create table statements
    private static final String CREATE_TABLE_LOCAL_MUSIC = "CREATE TABLE " + TABLE_LOCAL_MUSIC + " (" +
            COLUMN_SONG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TITLE + " TEXT, " +
            COLUMN_ALBUM + " TEXT, " +
            COLUMN_ARTIST + " TEXT, " +
            COLUMN_DURATION + " INTEGER, " +
            COLUMN_PATH + " TEXT, " +
            COLUMN_PLAY_COUNT + " INTEGER DEFAULT 0, " + // Default value for play count
            COLUMN_ADDED_AT + " INTEGER" + ")"; // Added time

    private static final String CREATE_TABLE_HEART_MUSIC = "CREATE TABLE " + TABLE_HEART_MUSIC + " (" +
            COLUMN_SONG_ID + " INTEGER, " +
            "FOREIGN KEY(" + COLUMN_SONG_ID + ") REFERENCES " + TABLE_LOCAL_MUSIC + "(" + COLUMN_SONG_ID + ") ON DELETE CASCADE" + ")";

    private static final String CREATE_TABLE_PLAYLIST = "CREATE TABLE " + TABLE_PLAYLIST + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PLAYLIST_NAME + " TEXT, " +
            COLUMN_PLAYLIST_COVER + " BLOB" + ")";

    private static final String CREATE_TABLE_PLAYLIST_SONGS = "CREATE TABLE " + TABLE_PLAYLIST_SONGS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PLAYLIST_ID + " INTEGER, " +
            COLUMN_SONG_ID + " INTEGER, " +
            "FOREIGN KEY(" + COLUMN_PLAYLIST_ID + ") REFERENCES " + TABLE_PLAYLIST + "(" + COLUMN_ID + ") ON DELETE CASCADE, " +
            "FOREIGN KEY(" + COLUMN_SONG_ID + ") REFERENCES " + TABLE_LOCAL_MUSIC + "(" + COLUMN_SONG_ID + ") ON DELETE CASCADE" + ")";

    private static final String CREATE_TABLE_PLAY_HISTORY = "CREATE TABLE " + TABLE_PLAY_HISTORY + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_SONG_ID + " INTEGER, " +
            COLUMN_PLAYED_AT + " INTEGER, " +
            "FOREIGN KEY(" + COLUMN_SONG_ID + ") REFERENCES " + TABLE_LOCAL_MUSIC + "(" + COLUMN_SONG_ID + ") ON DELETE CASCADE" + ")";

    // Singleton instance
    private static DatabaseHelper instance;
    private SQLiteDatabase database;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LOCAL_MUSIC);
        db.execSQL(CREATE_TABLE_HEART_MUSIC);
        db.execSQL(CREATE_TABLE_PLAYLIST);
        db.execSQL(CREATE_TABLE_PLAYLIST_SONGS);
        db.execSQL(CREATE_TABLE_PLAY_HISTORY); // Create play history table
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add new columns for play count and added time
            db.execSQL("ALTER TABLE " + TABLE_LOCAL_MUSIC + " ADD COLUMN " + COLUMN_PLAY_COUNT + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_LOCAL_MUSIC + " ADD COLUMN " + COLUMN_ADDED_AT + " INTEGER");
        }
    }

    public synchronized SQLiteDatabase getDatabase() {
        if (database == null || !database.isOpen()) {
            database = getWritableDatabase();
        }
        return database;
    }
}
