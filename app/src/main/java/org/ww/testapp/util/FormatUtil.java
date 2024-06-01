package org.ww.testapp.util;

import android.annotation.SuppressLint;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

@SuppressLint("SimpleDateFormat")
/**
 * 进行一些转换工作
 */
public class FormatUtil {

    /**
     * 格式化时间
     *
     * @param time 时间值 (00:00 -23:59:59)
     * @return 时间
     */
    public static String formatTime(long time) {
        long temp = time;
        if (temp == 0L) {
            return "00:00";
        }
        temp /= 1000;
        long s = (temp % 60); // s秒
        long m = (temp / 60 % 60); //m分
        long h = (temp / 60 / 60 % 24); //h小时
        if (h > 0) {
            return String.format("%02d:%02d:%02d", h, m, s);
        } else {
            return String.format("%02d:%02d", m, s);
        }
    }

    /**
     * 格式化播放次数
     *
     */
    public static String formatPlayCount(long count) {
        if (count < 10000) {
            return String.valueOf(count);
        } else {
            return String.format("%d.%d万", count / 10000, (count / 1000) % 10);
        }
    }

    /**
     * 格式化时间
     *
     * @param time 时间值 (00:00 -23:59:59)
     * @return 时间
     */
    public static String formatDate(long time) {
        long duration = System.currentTimeMillis() - time;
        if (duration < 60 * 1000) {
            return duration / 1000 + "秒前";
        } else if (duration < 60 * 1000 * 60) {
            return duration / 1000 / 60 + "分钟前";
        } else if (duration < 60 * 1000 * 60 * 24) {
            return duration / 1000 / 60 / 60 + "小时前";
        } else {
            SimpleDateFormat dfs = new SimpleDateFormat("yyyy年MM月dd日");
            return dfs.format(new Date(time));
        }
    }

    /**
     * 格式化时间
     *
     * @param time 时间值 (00:00 -23:59:59)
     * @return 时间
     */
    public static String formatDate(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date parse = dateFormat.parse(time);
            long duration = System.currentTimeMillis() - parse.getTime();
            if (duration < 60 * 1000) {
                return duration / 1000 + "秒前";
            } else if (duration < 60 * 1000 * 60) {
                return duration / 1000 / 60 + "分钟前";
            } else if (duration < 60 * 1000 * 60 * 24) {
                return duration / 1000 / 60 / 60 + "小时前";
            } else {
                return time;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return time;
        }
    }

    /**
     * 格式化文件大小
     *
     * @param size 文件大小值
     * @return 文件大小
     */
    public static String formatSize(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        if (size < 1024) {
            return df.format(size) + "B";
        } else if (size < 1048576) {
            return df.format(size / 1024.0) + "KB";
        } else if (size < 1073741824) {
            return df.format(size / 1048576.0) + "MB";
        } else {
            return df.format(size / 1073741824.0) + "GB";
        }
    }

    /**
     * 对乱码的处理
     *
     * @param s 原字符串
     * @return GBK处理后的数据
     */
    public static String formatGBKStr(String s) {
        try {
            return new String(s.getBytes("ISO-8859-1"), "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return s;
        }
    }

    public static String getTimeDifference(String starTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        String endTime = dateFormat.format(new Date());
        try {
            Date parse = dateFormat.parse(starTime);
            Date parse1 = dateFormat.parse(endTime);

            long diff = parse1.getTime() - parse.getTime();

            long day = diff / (24 * 60 * 60 * 1000);
            long hour = diff / (60 * 60 * 1000) - day * 24;
            long min = diff / (60 * 1000) - day * 24 * 60 - hour * 60;
            long s = diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60;

            if (day > 15) {
                return dateFormat1.format(parse);
            } else if (day > 0) {
                return day + "天前";
            } else if (hour > 0) {
                return hour + "小时前";
            } else if (min > 0) {
                return min + "分钟前";
            } else {
                return s + "秒前";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return starTime;
        }
    }

    /**
     * 毫秒转化成时间
     *
     * @return 时间
     */
    public static String distime(long time) {
        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        return dfs.format(date);
    }

    private static final SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当前时间
     * @return 时间 yyyy-MM-dd HH:mm:ss
     */
    public static String getChatDateTime(long time) {
        return dfs.format(new Date(time));
    }

    /**
     * 根据字符串获取当前时间
     * @return 时间 yyyy-MM-dd HH:mm:ss
     */
    public static long getChatParseDateTime(String time) {
        try {
            return dfs.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String formatDate(Date date, String pattern) {
        if (date == null) throw new IllegalArgumentException("date is null");
        if (pattern == null) throw new IllegalArgumentException("pattern is null");

        SimpleDateFormat formatter = formatFor(pattern);
        return formatter.format(date);
    }

    private static final ThreadLocal<SoftReference<Map<String, SimpleDateFormat>>> THREADLOCAL_FORMATS =
    new ThreadLocal<SoftReference<Map<String, SimpleDateFormat>>>() {
        @Override
        protected SoftReference<Map<String, SimpleDateFormat>> initialValue() {
            return new SoftReference<>(new HashMap<>());
        }
    };

    private static SimpleDateFormat formatFor(String pattern) {
        SoftReference<Map<String, SimpleDateFormat>> ref = THREADLOCAL_FORMATS.get();
        Map<String, SimpleDateFormat> formats = ref.get();
        if (formats == null) {
            formats = new HashMap<>();
            THREADLOCAL_FORMATS.set(new SoftReference<>(formats));
        }

        SimpleDateFormat format = formats.get(pattern);
        if (format == null) {
            format = new SimpleDateFormat(pattern, Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            formats.put(pattern, format);
        }

        return format;
    }
}
