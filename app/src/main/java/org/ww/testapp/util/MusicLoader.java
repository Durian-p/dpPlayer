package org.ww.testapp.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.Collator;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import org.ww.testapp.entity.Music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MusicLoader {
    private static final String TAG = "MusicFinder";

    public static List<Music> findLocalMusic(Context context) {
        List<Music> musicList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = makeSongCursor(contentResolver, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int durationColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int pathColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

                do {
                    long id = cursor.getLong(idColumnIndex);
                    String title = cursor.getString(titleColumnIndex);
                    String artist = cursor.getString(artistColumnIndex);
                    String album = cursor.getString(albumColumnIndex);
                    int duration = cursor.getInt(durationColumnIndex);
                    String path = cursor.getString(pathColumnIndex);
                    Bitmap albumArt = getAlbumArt(context, path); // 获取专辑封面

                    Music music = new Music();
                    music.setId(id);
                    music.setTitle(title);
                    music.setArtist(artist);
                    music.setAlbum(album);
                    music.setDuration(duration);
                    music.setPath(path);
                    music.setAlbumArt(albumArt); // 将专辑封面添加到 Music 实例中

                    // Filter out invalid or too short duration songs
                    if (!TextUtils.isEmpty(title) && duration > 60000) {
                        musicList.add(music);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error finding local music", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        // 根据首字母进行排序
        musicList.sort(new Comparator<Music>()
        {
            @Override
            public int compare(Music music1, Music music2)
            {
                return SortUtil.compare(music1.getTitle(), music2.getTitle());
            }
        });
        return musicList;
    }

    private static Bitmap getAlbumArt(Context context, String audioFilePath) throws IOException
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(audioFilePath);
        byte[] albumArt = retriever.getEmbeddedPicture();
        retriever.release();
        if (albumArt != null) {
            return BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
        } else {
            // 未找到专辑封面
            return null;
        }
    }

    private static Cursor makeSongCursor(ContentResolver contentResolver, String selection, String[] selectionArgs) {
        String selectionStatement = "duration>6 AND is_music=1 AND title != ''";

        if (!TextUtils.isEmpty(selection)) {
            selectionStatement = selectionStatement + " AND " + selection;
        }

        return contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA
                },
                selectionStatement,
                selectionArgs,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }
}

