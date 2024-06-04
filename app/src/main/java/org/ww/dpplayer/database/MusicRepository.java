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

    // 新增播放列表
    public long addMusicList(MusicList playlist) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLAYLIST_NAME, playlist.getName());
        values.put(DatabaseHelper.COLUMN_PLAYLIST_COVER, playlist.getCoverAsByteArray());
        long id = db.insert(DatabaseHelper.TABLE_PLAYLIST, null, values);
        db.close();
        return id;
    }

    // 更新播放列表
    public int updateMusicList(MusicList playlist) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLAYLIST_NAME, playlist.getName());
        values.put(DatabaseHelper.COLUMN_PLAYLIST_COVER, playlist.getCoverAsByteArray());
        int rows = db.update(DatabaseHelper.TABLE_PLAYLIST, values, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(playlist.getId())});
        db.close();
        return rows;
    }

    // 删除播放列表
    public void deleteMusicList(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_PLAYLIST, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.delete(DatabaseHelper.TABLE_PLAYLIST_SONGS, DatabaseHelper.COLUMN_PLAYLIST_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // 获取所有播放列表
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
                playlistList.add(playlist);
            }
            cursor.close();
        }
        db.close();
        return playlistList;
    }

    // 根据 ID 获取播放列表
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
                        playlist.getMusicIdList().add(cursor2.getString(cursor2.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
                    }
                    cursor2.close();
                }
            cursor.close();
}
        }

        db.close();
        return playlist;
    }

    // 根据歌单名获取播放列表
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
                if (cursor2 != null)
                {
                    while (cursor2.moveToNext()) {
                        playlist.getMusicIdList().add(cursor2.getString(cursor2.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
                    }
                    cursor2.close();
                }
            }
            cursor.close();
        }
        db.close();
        return playlist;
    }


    // 获取指定播放列表中的所有歌曲 ID
    public List<String> getMusicIdsByPlaylistId(long playlistId) {
        List<String> musicIds = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLIST_SONGS, null, DatabaseHelper.COLUMN_PLAYLIST_ID + " = ?", new String[]{String.valueOf(playlistId)}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                musicIds.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SONG_ID)));
            }
            cursor.close();
        }
        db.close();
        return musicIds;
    }

    // 向播放列表中添加歌曲
    public void addMusicToPlaylist(long playlistId, long songId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLAYLIST_ID, playlistId);
        values.put(DatabaseHelper.COLUMN_SONG_ID, songId);
        db.insert(DatabaseHelper.TABLE_PLAYLIST_SONGS, null, values);
        db.close();
    }

    // 从播放列表中删除歌曲
    public void removeMusicFromPlaylist(long playlistId, long songId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_PLAYLIST_SONGS, DatabaseHelper.COLUMN_PLAYLIST_ID + " = ? AND " + DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(playlistId), String.valueOf(songId)});
        db.close();
    }
}
