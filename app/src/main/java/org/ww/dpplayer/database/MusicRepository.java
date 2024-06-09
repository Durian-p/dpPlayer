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
    private static final int MAX_PLAY_HISTORY_SIZE = 100;

    private static MusicRepository instance;
    private final DatabaseHelper dbHelper;
    SQLiteDatabase db;
    private final Context context;
    private MusicRepositoryListener listener;

    private MusicRepository(Context context) {
        this.context = context;
        dbHelper = DatabaseHelper.getInstance(context.getApplicationContext());
        db = dbHelper.getWritableDatabase();
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
        void onHistoryMusicChanged();
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
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, music.getTitle());
        values.put(DatabaseHelper.COLUMN_ALBUM, music.getAlbum());
        values.put(DatabaseHelper.COLUMN_ARTIST, music.getArtist());
        values.put(DatabaseHelper.COLUMN_DURATION, music.getDuration());
        values.put(DatabaseHelper.COLUMN_PATH, music.getPath());
        values.put(DatabaseHelper.COLUMN_ADDED_AT, System.currentTimeMillis());
        long id = db.insert(DatabaseHelper.TABLE_LOCAL_MUSIC, null, values);
//        db.close();
        return id;
    }

    public void addLocalMusics(List<Music> musicList) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (Music music : musicList) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_SONG_ID, music.getId());
            values.put(DatabaseHelper.COLUMN_TITLE, music.getTitle());
            values.put(DatabaseHelper.COLUMN_ALBUM, music.getAlbum());
            values.put(DatabaseHelper.COLUMN_ARTIST, music.getArtist());
            values.put(DatabaseHelper.COLUMN_DURATION, music.getDuration());
            values.put(DatabaseHelper.COLUMN_PATH, music.getPath());
            values.put(DatabaseHelper.COLUMN_ADDED_AT, System.currentTimeMillis());
            db.insert(DatabaseHelper.TABLE_LOCAL_MUSIC, null, values);
        }
//        db.close();
    }

    public int updateLocalMusic(Music music) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, music.getTitle());
        values.put(DatabaseHelper.COLUMN_ALBUM, music.getAlbum());
        values.put(DatabaseHelper.COLUMN_ARTIST, music.getArtist());
        values.put(DatabaseHelper.COLUMN_DURATION, music.getDuration());
        values.put(DatabaseHelper.COLUMN_PATH, music.getPath());
        int rows = db.update(DatabaseHelper.TABLE_LOCAL_MUSIC, values, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(music.getId())});
//        db.close();
        return rows;
    }

    public void deleteLocalMusic(long id) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_LOCAL_MUSIC, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(id)});
        db.delete(DatabaseHelper.TABLE_PLAY_HISTORY, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(id)});
        db.delete(DatabaseHelper.TABLE_HEART_MUSIC, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(id)});
        db.delete(DatabaseHelper.TABLE_PLAYLIST_SONGS, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(id)});
//        db.close();
    }

    public Music getLocalMusicById(long id) {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Music music = null;
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCAL_MUSIC, null, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                music = new Music();
                music.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
                loadMusic(cursor, music);
            }
//            cursor.close();
        }
//        db.close();
        return music;
    }

    public List<Music> getAllLocalMusic() {
        List<Music> musicList = new ArrayList<>();
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_LOCAL_MUSIC, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Music music = new Music();
                music.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
                loadMusic(cursor, music);
                musicList.add(music);
            }
//            cursor.close();
        }
//        db.close();
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
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SONG_ID, id);
        db.insert(DatabaseHelper.TABLE_HEART_MUSIC, null, values);
//        db.close();
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
//            cursor.close();
        }
//        db.close();
        return musicList;
    }

    public Music getHeartMusicBySongId(Long id) {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
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
//                cursor.close();
            }
        }
//        db.close();
        return music;
    }

    public void deleteHeartMusic(long id) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_HEART_MUSIC, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(id)});
//        db.close();
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
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLAYLIST_NAME, playlist.getName());
        values.put(DatabaseHelper.COLUMN_PLAYLIST_COVER, playlist.getCoverAsByteArray());
        long id = db.insert(DatabaseHelper.TABLE_PLAYLIST, null, values);
        for (long sid : playlist.getMusicIdList()) {
            if (getLocalMusicById(sid) == null)
                continue;
            addMusicToMlist(id, sid);
        }
//        db.close();
        notifyMusicListsChanged();
        return id;
    }

    public int updateMusicList(MusicList playlist) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLAYLIST_NAME, playlist.getName());
        values.put(DatabaseHelper.COLUMN_PLAYLIST_COVER, playlist.getCoverAsByteArray());
        int rows = db.update(DatabaseHelper.TABLE_PLAYLIST, values, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(playlist.getId())});
//        db.close();
        notifyMusicListsChanged();
        return rows;
    }

    public void deleteMusicList(long id) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_PLAYLIST, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.delete(DatabaseHelper.TABLE_PLAYLIST_SONGS, DatabaseHelper.COLUMN_PLAYLIST_ID + " = ?", new String[]{String.valueOf(id)});
//        db.close();
        notifyMusicListsChanged();
    }

    public List<MusicList> getAllMusicLists() {
        List<MusicList> playlistList = new ArrayList<>();
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
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
//            cursor.close();
        }
//        db.close();
        return playlistList;
    }

    public MusicList getMusicListById(long id) {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
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
//                    cursor2.close();
                }
            }
//            cursor.close();
        }
//        db.close();
        return playlist;
    }

    public MusicList getMusicListByName(String playlistName) {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
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
//                    cursor2.close();
                }
            }
//            cursor.close();
        }
//        db.close();
        return playlist;
    }

    public List<Long> getMusicIdsByPlaylistId(long playlistId) {
        List<Long> musicIds = new ArrayList<>();
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLIST_SONGS, null, DatabaseHelper.COLUMN_PLAYLIST_ID + " = ?", new String[]{String.valueOf(playlistId)}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                musicIds.add(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
            }
//            cursor.close();
        }
//        db.close();
        return musicIds;
    }

    public List<Music> getMusicInMlistByMlistId(long id)
    {
        List<Music> musics = new ArrayList<>();
        List<Long> musicIds = getMusicIdsByPlaylistId(id);
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
        for (Long musicId : musicIds) {
            Cursor cursor = db.query(DatabaseHelper.TABLE_LOCAL_MUSIC, null, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(musicId)}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    Music music = new Music();
                    music.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
                    loadMusic(cursor, music);
                    musics.add(music);
                }
//                cursor.close();
            }
        }
//        db.close();
        return musics;
    }

    public boolean addMusicToMlist(long playlistId, long songId) {
        if (getLocalMusicById(songId) == null) {
            throw new IllegalArgumentException("Song ID not found in local music table");
        }
        ContentValues values = new ContentValues();
        // 检查歌曲是否存在
        if (isMusicInMlist(songId, playlistId)) {
            return false;
        }
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        values.put(DatabaseHelper.COLUMN_PLAYLIST_ID, playlistId);
        values.put(DatabaseHelper.COLUMN_SONG_ID, songId);
        db.insert(DatabaseHelper.TABLE_PLAYLIST_SONGS, null, values);
//        db.close();
        notifyMusicListsChanged();
        return true;
    }

    public boolean isMusicInMlist(long songId, long playlistId)
    {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLIST_SONGS, null, DatabaseHelper.COLUMN_PLAYLIST_ID + " = ? AND " + DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(playlistId), String.valueOf(songId)}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                cursor.close();
                return true;
            }
//            cursor.close();
        }
//        db.close();
        return false;
    }

    public boolean removeMusicFromPlaylist(long playlistId, long songId) {
        try
        {
//            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(DatabaseHelper.TABLE_PLAYLIST_SONGS, DatabaseHelper.COLUMN_PLAYLIST_ID + " = ? AND " + DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(playlistId), String.valueOf(songId)});
//            db.close();
            notifyMusicListsChanged();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public ArrayList<Music> getMusicsByAlbum(String album)
    {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
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
//            cursor.close();
        }
//        db.close();
        return musicList;
    }

    public ArrayList<Music> getMusicsByArtist(String artist)
    {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
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
//            cursor.close();
        }
//        db.close();
        return musicList;
    }

    // 历史记录
    public void addPlayHistory(long songId) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // 播放计数加一
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLAY_COUNT, DatabaseHelper.COLUMN_PLAY_COUNT + 1);
        db.update(DatabaseHelper.TABLE_LOCAL_MUSIC, values, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(songId)});

        // Delete existing record for the song
        db.delete(DatabaseHelper.TABLE_PLAY_HISTORY, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(songId)});

        // Add new play record
        values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SONG_ID, songId);
        values.put(DatabaseHelper.COLUMN_PLAYED_AT, System.currentTimeMillis());
        db.insert(DatabaseHelper.TABLE_PLAY_HISTORY, null, values);

        // Trim history to maintain only the latest 100 records
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAY_HISTORY, new String[]{DatabaseHelper.COLUMN_ID}, null, null, null, null, DatabaseHelper.COLUMN_PLAYED_AT + " DESC", MAX_PLAY_HISTORY_SIZE + ", 100");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                db.delete(DatabaseHelper.TABLE_PLAY_HISTORY, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            }
//            cursor.close();
        }

//        db.close();
        if (listener != null)
            listener.onHistoryMusicChanged();
    }

    public List<Music> getPlayHistory() {
        List<Music> musicList = new ArrayList<>();
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_LOCAL_MUSIC + " lm INNER JOIN " + DatabaseHelper.TABLE_PLAY_HISTORY + " ph ON lm." + DatabaseHelper.COLUMN_SONG_ID + " = ph." + DatabaseHelper.COLUMN_SONG_ID + " ORDER BY ph." + DatabaseHelper.COLUMN_PLAYED_AT + " DESC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Music music = new Music();
                music.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
                loadMusic(cursor, music);
                musicList.add(music);
            }
//            cursor.close();
        }
//        db.close();
        return musicList;
    }

    // 获取按照播放次数排名的前一百首歌曲
    public List<Music> getTop100SongsByPlayCount() {
        List<Music> musicList = new ArrayList<>();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_LOCAL_MUSIC +
                " ORDER BY " + DatabaseHelper.COLUMN_PLAY_COUNT + " DESC LIMIT 100";
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
        return musicList;
    }

    // 获取按照添加时间由近到远的15首歌
    public List<Music> getLatest15Songs() {
        List<Music> musicList = new ArrayList<>();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_LOCAL_MUSIC +
                " ORDER BY " + DatabaseHelper.COLUMN_ADDED_AT + " DESC LIMIT 15";
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
        return musicList;
    }

    // 获取最近听的10首歌
    public List<Music> getRecent10Songs() {
        List<Music> musicList = new ArrayList<>();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_PLAY_HISTORY +
                " ORDER BY " + DatabaseHelper.COLUMN_PLAYED_AT + " DESC LIMIT 10";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Music music = getLocalMusicById(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
                musicList.add(music);
            }
        }
        return musicList;
    }

}
