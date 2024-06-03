package org.ww.testapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.ww.testapp.entity.Music;
import org.ww.testapp.util.MusicLoader;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MusicRepository
{
    private DatabaseHelper dbHelper;

    public MusicRepository(Context context)
    {
        dbHelper = new DatabaseHelper(context);
    }

    // 触发收藏按钮,返回歌曲是否为收藏
    public boolean toggleHeartSong(Music music)
    {
        if (getHeartMusicBySongId(music.getId()) == null)
        {
            addHeartMusicNoCheck(music);
            return true;
        }
        else
        {
            deleteHeartMusic(music.getId());
            return false;
        }
    }

    // 添加歌曲到收藏，返回是否执行插入
    public boolean addHeartMusic(Music music)
    {
        long id = music.getId();
        if (getHeartMusicBySongId(id) != null)
            return false;
        addHeartMusicNoCheck(music);
        return true;
    }

    public long addHeartMusicNoCheck(Music music)
    {
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

    // 获取所有收藏的歌曲
    public List<Music> getAllHeartMusic()
    {
        List<Music> musicList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_HEART_MUSIC, null, null, null, null, null, null);
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
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

    public Music getHeartMusicBySongId(Long id)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Music music = null;
        if (id != null)
        {
            Cursor cursor = db.query(DatabaseHelper.TABLE_HEART_MUSIC, null, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor != null)
            {
                if (cursor.moveToNext())
                {
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

    // 删除收藏的歌曲
    public void deleteHeartMusic(long id)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_HEART_MUSIC, DatabaseHelper.COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // 新建歌单
    public long createPlaylist(String name, Bitmap cover)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLAYLIST_NAME, name);
        if (cover != null)
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            cover.compress(Bitmap.CompressFormat.PNG, 100, stream);
            values.put(DatabaseHelper.COLUMN_PLAYLIST_COVER, stream.toByteArray());
        }
        long id = db.insert(DatabaseHelper.TABLE_PLAYLIST, null, values);
        db.close();
        return id;
    }

    // 获取所有歌单
    public List<String> getAllPlaylists()
    {
        List<String> playlistList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLIST, null, null, null, null, null, null);
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                playlistList.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAYLIST_NAME)));
            }
            cursor.close();
        }
        db.close();
        return playlistList;
    }

    // 获取歌单封面
    public Bitmap getPlaylistCover(long playlistId)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLAYLIST, new String[]{DatabaseHelper.COLUMN_PLAYLIST_COVER}, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(playlistId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst())
        {
            byte[] coverBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAYLIST_COVER));
            cursor.close();
            if (coverBytes != null)
            {
                return BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.length);
            }
        }
        db.close();
        return null;
    }

    // 添加歌曲到歌单
    public long addSongToPlaylist(long playlistId, long songId)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLAYLIST_ID, playlistId);
        values.put(DatabaseHelper.COLUMN_SONG_ID, songId);
        long id = db.insert(DatabaseHelper.TABLE_PLAYLIST_SONGS, null, values);
        db.close();
        return id;
    }

    // 获取歌单中的所有歌曲
    public List<Music> getSongsInPlaylist(long playlistId)
    {
        List<Music> musicList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_HEART_MUSIC + " m INNER JOIN " + DatabaseHelper.TABLE_PLAYLIST_SONGS + " ps ON m." + DatabaseHelper.COLUMN_ID + " = ps." + DatabaseHelper.COLUMN_SONG_ID + " WHERE ps." + DatabaseHelper.COLUMN_PLAYLIST_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(playlistId)});
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                Music music = new Music();
                music.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
                loadMusic(cursor, music);
                musicList.add(music);
            }
            cursor.close();
        }
        db.close();
        return musicList;
    }

    private void loadMusic(Cursor cursor, Music music)
    {
        music.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)));
        music.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALBUM)));
        music.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ARTIST)));
        music.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DURATION)));
        music.setPath(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PATH)));
        music.setAlbumArt(MusicLoader.getAlbumArt(music.getPath()));
    }

    // 删除歌单
    public void deletePlaylist(long id)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_PLAYLIST, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.delete(DatabaseHelper.TABLE_PLAYLIST_SONGS, DatabaseHelper.COLUMN_PLAYLIST_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
