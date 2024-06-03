package org.ww.dpplayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.util.MusicLoader;

import java.util.ArrayList;
import java.util.List;

public class MusicRepository {
    private static MusicRepository instance;
    private final DatabaseHelper dbHelper;
    private MusicRepositoryListener listener;

    private MusicRepository(Context context) {
        dbHelper = new DatabaseHelper(context.getApplicationContext());
    }

    public static synchronized MusicRepository getInstance() {
        if (instance == null) {
            return null;
        }
        return instance;
    }

    public static synchronized MusicRepository initInstance(Context context) {
        if (instance == null) {
            instance = new MusicRepository(context);
        }
        return instance;
    }

    public interface MusicRepositoryListener {
        void onHeartMusicChanged();
    }

    public void setMusicRepositoryListener(MusicRepositoryListener listener) {
        this.listener = listener;
    }

    public boolean toggleHeartSong(Music music) {
        if (getHeartMusicBySongId(music.getId()) == null) {
            addHeartMusicNoCheck(music);
            notifyHeartMusicChanged();
            return true;
        } else {
            deleteHeartMusic(music.getId());
            notifyHeartMusicChanged();
            return false;
        }
    }

    private void notifyHeartMusicChanged() {
        if (listener != null) {
            listener.onHeartMusicChanged();
        }
    }

    public boolean addHeartMusic(Music music) {
        long id = music.getId();
        if (getHeartMusicBySongId(id) != null)
            return false;
        addHeartMusicNoCheck(music);
        notifyHeartMusicChanged();
        return true;
    }

    public long addHeartMusicNoCheck(Music music) {
        long id = music.getId();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SONG_ID, music.getId());
        values.put(DatabaseHelper.COLUMN_TITLE, music.getTitle());
        values.put(DatabaseHelper.COLUMN_ALBUM, music.getAlbum());
        values.put(DatabaseHelper.COLUMN_ARTIST, music.getArtist());
        values.put(DatabaseHelper.COLUMN_DURATION, music.getDuration());
        values.put(DatabaseHelper.COLUMN_PATH, music.getPath());
        db.insert(DatabaseHelper.TABLE_HEART_MUSIC, null, values);
        db.close();
        return id;
    }

    public List<Music> getAllHeartMusic() {
        List<Music> musicList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_HEART_MUSIC, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Music music = new Music();
                music.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
                loadMusic(cursor, music);
                musicList.add(music);
            }
            cursor.close();
        }
        db.close();
        return musicList;
    }

    public Music getHeartMusicBySongId(Long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Music music = null;
        if (id != null) {
            Cursor cursor = db.query(DatabaseHelper.TABLE_HEART_MUSIC, null, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    music = new Music();
                    music.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
                    loadMusic(cursor, music);
                }
                cursor.close();
            }
        }
        db.close();
        return music;
    }

    public void deleteHeartMusic(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_HEART_MUSIC, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    private void loadMusic(Cursor cursor, Music music) {
        music.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)));
        music.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALBUM)));
        music.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ARTIST)));
        music.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DURATION)));
        music.setPath(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PATH)));
        music.setAlbumArt(MusicLoader.getAlbumArt(music.getPath()));
    }
}
