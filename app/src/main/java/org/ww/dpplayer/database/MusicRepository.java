package org.ww.dpplayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.ww.dpplayer.entity.Music;
import org.ww.dpplayer.entity.MusicList;
import org.ww.dpplayer.util.MusicLoader;

import java.util.ArrayList;
import java.util.List;

public class MusicRepository {
    private static MusicRepository instance;
    private final DatabaseHelper dbHelper;
    private final Context context;
    private MusicRepositoryListener listener;

    private MusicRepository(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context.getApplicationContext());
        List<Music> musicList = MusicLoader.findLocalMusic(context);
        addLocalMusics(musicList);
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
        void onMusicListsChanged();
    }

    public void setMusicRepositoryListener(MusicRepositoryListener listener) {
        this.listener = listener;
    }

    private void notifyHeartMusicChanged() {
        if (listener != null) {
            listener.onHeartMusicChanged();
        }
    }

    private void notifyMusicListsChanged() {
        if (listener != null) {
            listener.onMusicListsChanged();
        }
    }





    // Local Music CRUD operations
    public long addLocalMusic(Music music) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, music.getTitle());
        values.put(DatabaseHelper.COLUMN_ALBUM, music.getAlbum());
        values.put(DatabaseHelper.COLUMN_ARTIST, music.getArtist());
        values.put(DatabaseHelper.COLUMN_DURATION, music.getDuration());
        values.put(DatabaseHelper.COLUMN_PATH, music.getPath());
        long id = db.insert(DatabaseHelper.TABLE_LOCAL_MUSIC, null, values);
        db.close();
        return id;
    }

    public void addLocalMusics(List<Music> musicList) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (Music music : musicList) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_SONG_ID, music.getId());
            values.put(DatabaseHelper.COLUMN_TITLE, music.getTitle());
            values.put(DatabaseHelper.COLUMN_ALBUM, music.getAlbum());
            values.put(DatabaseHelper.COLUMN_ARTIST, music.getArtist());
            values.put(DatabaseHelper.COLUMN_DURATION, music.getDuration());
            values.put(DatabaseHelper.COLUMN_PATH, music.getPath());
            db.insert(DatabaseHelper.TABLE_LOCAL_MUSIC, null, values);
        }
        db.close();
    }

    public int updateLocalMusic(Music music) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, music.getTitle());
        values.put(DatabaseHelper.COLUMN_ALBUM, music.getAlbum());
        values.put(DatabaseHelper.COLUMN_ARTIST, music.getArtist());
        values.put(DatabaseHelper.COLUMN_DURATION, music.getDuration());
        values.put(DatabaseHelper.COLUMN_PATH, music.getPath());
        int rows = db.update(DatabaseHelper.TABLE_LOCAL_MUSIC, values, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(music.getId())});
        db.close();
        return rows;
    }

    public void deleteLocalMusic(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_LOCAL_MUSIC, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Music getLocalMusicById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Music music = null;
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCAL_MUSIC, null, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                music = new Music();
                music.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
                loadMusic(cursor, music);
            }
            cursor.close();
        }
        db.close();
        return music;
    }

    public List<Music> getAllLocalMusic() {
        List<Music> musicList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCAL_MUSIC, null, null, null, null, null, null);
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

    // Heart Music operations
    public boolean toggleHeartSong(Music music) {
        if (getLocalMusicById(music.getId()) == null)
            return false;
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
        values.put(DatabaseHelper.COLUMN_SONG_ID, id);
        db.insert(DatabaseHelper.TABLE_HEART_MUSIC, null, values);
        db.close();
        return id;
    }

    public List<Music> getAllHeartMusic() {
        List<Music> musicList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_LOCAL_MUSIC + " lm INNER JOIN " + DatabaseHelper.TABLE_HEART_MUSIC + " hm ON lm." + DatabaseHelper.COLUMN_SONG_ID + " = hm." + DatabaseHelper.COLUMN_SONG_ID;
        Cursor cursor = db.rawQuery(query, null);
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
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_LOCAL_MUSIC + " lm INNER JOIN " + DatabaseHelper.TABLE_HEART_MUSIC + " hm ON lm." + DatabaseHelper.COLUMN_SONG_ID + " = hm." + DatabaseHelper.COLUMN_SONG_ID + " WHERE hm." + DatabaseHelper.COLUMN_SONG_ID + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
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

    // Playlist operations
    public long addMusicList(MusicList playlist) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLAYLIST_NAME, playlist.getName());
        values.put(DatabaseHelper.COLUMN_PLAYLIST_COVER, playlist.getCoverAsByteArray());
        long id = db.insert(DatabaseHelper.TABLE_PLAYLIST, null, values);
        for (long sid : playlist.getMusicIdList()) {
            if (getLocalMusicById(sid) == null)
                continue;
            addMusicToPlaylist(id, sid);
        }
        db.close();
        notifyMusicListsChanged();
        return id;
    }

    public int updateMusicList(MusicList playlist) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLAYLIST_NAME, playlist.getName());
        values.put(DatabaseHelper.COLUMN_PLAYLIST_COVER, playlist.getCoverAsByteArray());
        int rows = db.update(DatabaseHelper.TABLE_PLAYLIST, values, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(playlist.getId())});
        db.close();
        notifyMusicListsChanged();
        return rows;
    }

    public void deleteMusicList(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_PLAYLIST, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.delete(DatabaseHelper.TABLE_PLAYLIST_SONGS, DatabaseHelper.COLUMN_PLAYLIST_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        notifyMusicListsChanged();
    }

    public List<MusicList> getAllMusicLists() {
        List<MusicList> playlistList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLIST, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                MusicList playlist = new MusicList();
                playlist.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
                playlist.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAYLIST_NAME)));
                playlist.setCoverFromByteArray(cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAYLIST_COVER)));
                playlist.setMusicIdList(getMusicIdsByPlaylistId(playlist.getId()));
                playlistList.add(playlist);
            }
            cursor.close();
        }
        db.close();
        return playlistList;
    }

    public MusicList getMusicListById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        MusicList playlist = null;
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLIST, null, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                playlist = new MusicList();
                playlist.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
                playlist.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAYLIST_NAME)));
                playlist.setCoverFromByteArray(cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAYLIST_COVER)));
                Cursor cursor2 = db.query(DatabaseHelper.TABLE_PLAYLIST_SONGS, null, DatabaseHelper.COLUMN_PLAYLIST_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
                if (cursor2 != null) {
                    while (cursor2.moveToNext()) {
                        playlist.getMusicIdList().add(cursor2.getLong(cursor2.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
                    }
                    cursor2.close();
                }
            }
            cursor.close();
        }
        db.close();
        return playlist;
    }

    public MusicList getMusicListByName(String playlistName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        MusicList playlist = null;
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLIST, null, DatabaseHelper.COLUMN_PLAYLIST_NAME + " = ?", new String[]{playlistName}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                playlist = new MusicList();
                playlist.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
                playlist.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAYLIST_NAME)));
                playlist.setCoverFromByteArray(cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAYLIST_COVER)));
                Cursor cursor2 = db.query(DatabaseHelper.TABLE_PLAYLIST_SONGS, null, DatabaseHelper.COLUMN_PLAYLIST_ID + " = ?", new String[]{String.valueOf(playlist.getId())}, null, null, null);
                if (cursor2 != null) {
                    while (cursor2.moveToNext()) {
                        playlist.getMusicIdList().add(cursor2.getLong(cursor2.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
                    }
                    cursor2.close();
                }
            }
            cursor.close();
        }
        db.close();
        return playlist;
    }

    public List<Long> getMusicIdsByPlaylistId(long playlistId) {
        List<Long> musicIds = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLIST_SONGS, null, DatabaseHelper.COLUMN_PLAYLIST_ID + " = ?", new String[]{String.valueOf(playlistId)}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                musicIds.add(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
            }
            cursor.close();
        }
        db.close();
        return musicIds;
    }

    public List<Music> getMusicInMlistByMlistId(long id)
    {
        List<Music> musics = new ArrayList<>();
        List<Long> musicIds = getMusicIdsByPlaylistId(id);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        for (Long musicId : musicIds) {
            Cursor cursor = db.query(DatabaseHelper.TABLE_LOCAL_MUSIC, null, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(musicId)}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    Music music = new Music();
                    music.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
                    loadMusic(cursor, music);
                    musics.add(music);
                }
                cursor.close();
            }
        }
        db.close();
        return musics;
    }

    public boolean addMusicToPlaylist(long playlistId, long songId) {
        if (getLocalMusicById(songId) == null) {
            throw new IllegalArgumentException("Song ID not found in local music table");
        }
        ContentValues values = new ContentValues();
        // 检查歌曲是否存在
        if (isMusicInMlist(songId, playlistId)) {
            return false;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        values.put(DatabaseHelper.COLUMN_PLAYLIST_ID, playlistId);
        values.put(DatabaseHelper.COLUMN_SONG_ID, songId);
        db.insert(DatabaseHelper.TABLE_PLAYLIST_SONGS, null, values);
        db.close();
        return true;
    }

    public boolean isMusicInMlist(long songId, long playlistId)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLIST_SONGS, null, DatabaseHelper.COLUMN_PLAYLIST_ID + " = ? AND " + DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(playlistId), String.valueOf(songId)}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public void removeMusicFromPlaylist(long playlistId, long songId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_PLAYLIST_SONGS, DatabaseHelper.COLUMN_PLAYLIST_ID + " = ? AND " + DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(playlistId), String.valueOf(songId)});
        db.close();
    }

    public ArrayList<Music> getMusicsByAlbum(String album)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<Music> musicList = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCAL_MUSIC, null, DatabaseHelper.COLUMN_ALBUM + " = ?", new String[]{album}, null, null, null);
        if (cursor != null)
        {
            while (cursor.moveToNext()) {
                Music music = new Music();
                music.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
                loadMusic(cursor, music);
                musicList.add(music);
            }
        }
        return musicList;
    }

    public ArrayList<Music> getMusicsByArtist(String artist)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<Music> musicList = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCAL_MUSIC, null, DatabaseHelper.COLUMN_ARTIST + " = ?", new String[]{artist}, null, null, null);
        if (cursor != null)
        {
            while (cursor.moveToNext()) {
                Music music = new Music();
                music.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
                loadMusic(cursor, music);
                musicList.add(music);
            }
        }
        return musicList;
    }

}
