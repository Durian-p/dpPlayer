package org.ww.dpplayer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "music.db";
    private static final int DATABASE_VERSION = 2; // 更新数据库版本

    // Table names
    public static final String TABLE_LOCAL_MUSIC = "local_music";
    public static final String TABLE_HEART_MUSIC = "heart_music";
    public static final String TABLE_PLAYLIST = "playlist";
    public static final String TABLE_PLAYLIST_SONGS = "playlist_songs";

    // Common columns
    public static final String COLUMN_ID = "id";

    // Local music table columns
    public static final String COLUMN_SONG_ID = "song_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_ALBUM = "album";
    public static final String COLUMN_ARTIST = "artist";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_PATH = "path";

    // Playlist table columns
    public static final String COLUMN_PLAYLIST_NAME = "name";
    public static final String COLUMN_PLAYLIST_COVER = "cover";

    // Playlist songs table columns
    public static final String COLUMN_PLAYLIST_ID = "playlist_id";

    // Create table statements
    private static final String CREATE_TABLE_LOCAL_MUSIC = "CREATE TABLE " + TABLE_LOCAL_MUSIC + " (" +
            COLUMN_SONG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TITLE + " TEXT, " +
            COLUMN_ALBUM + " TEXT, " +
            COLUMN_ARTIST + " TEXT, " +
            COLUMN_DURATION + " INTEGER, " +
            COLUMN_PATH + " TEXT" + ")";

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

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LOCAL_MUSIC);
        db.execSQL(CREATE_TABLE_HEART_MUSIC);
        db.execSQL(CREATE_TABLE_PLAYLIST);
        db.execSQL(CREATE_TABLE_PLAYLIST_SONGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_TABLE_LOCAL_MUSIC);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HEART_MUSIC);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_SONGS);
            db.execSQL(CREATE_TABLE_HEART_MUSIC);
            db.execSQL(CREATE_TABLE_PLAYLIST_SONGS);
        }
    }
}
