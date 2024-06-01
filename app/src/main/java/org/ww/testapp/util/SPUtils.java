package org.ww.testapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import org.ww.testapp.R;
import org.ww.testapp.ui.main.MainActivity;

import java.util.Set;

/**
 * 作者：yonglong on 2016/8/12 16:03
 * 邮箱：643872807@qq.com
 * 版本：2.5
 * 内部存儲工具類
 */
public class SPUtils
{
    /**
     * 第一次进入应用
     */
    public static final String SP_KEY_FIRST_COMING = "first_coming";
    public static final String SP_KEY_FIRST_INIT_DATABASE = "first_init_database";
    public static final String SP_KEY_NOTICE_CODE = "notice_code";

    /**
     * 音乐接口
     */
    public static final String SP_KEY_PLATER_API_URL = "music_api";
    public static final String SP_KEY_NETEASE_API_URL = "netease_api";
    public static final String SP_KEY_NETEASE_UID = "netease_uid";
    /**
     * 桌面歌词锁定
     */
    public static final String SP_KEY_FLOAT_LYRIC_LOCK = "float_lyric_lock";
    public static final String SP_KEY_THEME_MODE = "theme_mode";
    public static final String SP_KEY_USER_NAME = "user_name";
    public static final String SP_KEY_PASSWORD = "pass_word";

    private static final String MUSIC_ID = "music_id";
    private static final String PLAY_POSITION = "play_position";
    private static final String PLAY_MODE = "play_mode";
    private static final String SPLASH_URL = "splash_url";
    private static final String WIFI_MODE = "wifi_mode";
    private static final String LYRIC_MODE = "lyric_mode";
    private static final String NIGHT_MODE = "night_mode";
    private static final String POSITION = "position";
    private static final String DESKTOP_LYRIC_SIZE = "desktop_lyric_size";
    private static final String DESKTOP_LYRIC_COLOR = "desktop_lyric_color";
    public static final String QQ_OPEN_ID = "qq_open_id";
    public static final String QQ_ACCESS_TOKEN = "qq_access_token";
    public static final String QQ_EXPIRES_IN = "expires_in";

    public static int getPlayPosition(Context context) {
        return getAnyByKey(context, PLAY_POSITION, -1);
    }

    public static void setPlayPosition(Context context, int position) {
        putAnyCommit(context, PLAY_POSITION, position);
    }


    public static String getCurrentSongId(Context context) {
        return getAnyByKey(context, MUSIC_ID, "");
    }

    public static void saveCurrentSongId(Context context, String mid) {
        putAnyCommit(context, MUSIC_ID, mid);
    }

    public static long getPosition(Context context) {
        return getAnyByKey(context, POSITION, 0L);
    }

    public static void savePosition(Context context, long id) {
        putAnyCommit(context, POSITION, id);
    }

    public static int getPlayMode(Context context) {
        return getAnyByKey(context, PLAY_MODE, 0);
    }

    public static void savePlayMode(Context context, int mode) {
        putAnyCommit(context, PLAY_MODE, mode);
    }

    public static String getSplashUrl(Context context) {
        return getAnyByKey(context, SPLASH_URL, "");
    }

    public static void saveSplashUrl(Context context, String url) {
        putAnyCommit(context, SPLASH_URL, url);
    }

    public static boolean getWifiMode(Context context) {
        return getAnyByKey(context, context.getString(R.string.setting_key_mobile_wifi), false);
    }

    public static void saveWifiMode(Context context, boolean enable) {
        putAnyCommit(context, context.getString(R.string.setting_key_mobile_wifi), enable);
    }

    public static boolean isShowLyricView(Context context) {
        return getAnyByKey(context, context.getString(R.string.setting_key_mobile_wifi), false);
    }

    public static void showLyricView(Context context, boolean enable) {
        putAnyCommit(context, context.getString(R.string.setting_key_mobile_wifi), enable);
    }


    public static boolean isNightMode(Context context) {
        return getAnyByKey(context, NIGHT_MODE, false);
    }

    public static void saveNightMode(Context context, boolean on) {
        putAnyCommit(context, NIGHT_MODE, on);
    }


    public static int getFontSize(Context context) {
        return getAnyByKey(context, DESKTOP_LYRIC_SIZE, 30);
    }

    public static void saveFontSize(Context context, int size) {
        putAnyCommit(context, DESKTOP_LYRIC_SIZE, size);
    }


    public static void saveFontColor(Context context, int color) {
        putAnyCommit(context, DESKTOP_LYRIC_COLOR, color);
    }

    public static int getFontColor(Context context) {
        return getAnyByKey(context, DESKTOP_LYRIC_COLOR, Color.RED);
    }



    /**
     * -------------------------------------------------------
     * <p>底层操作
     * -------------------------------------------------------
     */
    public static boolean getAnyByKey(Context context, String key, boolean defValue) {
        return getPreferences(context).getBoolean(key, defValue);
    }

    public static void putAnyCommit(Context context, String key, boolean value) {
        getPreferences(context).edit().putBoolean(key, value).apply();
    }

    public static float getAnyByKey(Context context, String key, float defValue) {
        return getPreferences(context).getFloat(key, defValue);
    }

    public static void putAnyCommit(Context context, String key, float value) {
        getPreferences(context).edit().putFloat(key, value).apply();
    }

    public static int getAnyByKey(Context context, String key, int defValue) {
        return getPreferences(context).getInt(key, defValue);
    }

    public static void putAnyCommit(Context context, String key, int value) {
        getPreferences(context).edit().putInt(key, value).apply();
    }

    public static long getAnyByKey(Context context, String key, long defValue) {
        return getPreferences(context).getLong(key, defValue);
    }

    public static void putAnyCommit(Context context, String key, long value) {
        getPreferences(context).edit().putLong(key, value).apply();
    }

    public static void putAnyCommit(Context context, String key, Set<String> defValue) {
        getPreferences(context).edit().putStringSet(key, defValue).apply();
    }

    public static String getAnyByKey(Context context, String key, @Nullable String defValue) {
        return getPreferences(context).getString(key, defValue);
    }

    public static Set<String> getAnyByKey(Context context, String key, Set<String> defValue) {
        return getPreferences(context).getStringSet(key, defValue);
    }

    public static void putAnyCommit(Context context, String key, @Nullable String value) {
        getPreferences(context).edit().putString(key, value).apply();
    }

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
