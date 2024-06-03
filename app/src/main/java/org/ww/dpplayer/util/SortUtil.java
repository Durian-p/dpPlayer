package org.ww.dpplayer.util;

import android.icu.text.Collator;

import java.util.Locale;

public class SortUtil
{
    public static int compare(String s1, String s2)
    {
        // 使用默认本地语言环境的 Collator
        Collator collator = Collator.getInstance(Locale.getDefault());
        //return collator.compare(s1,s2);

        // 中文文字转换为对应拼音再进行比较
        String pinyin1 = ChineseToPinyin.getPinyin(s1);
        String pinyin2 = ChineseToPinyin.getPinyin(s2);

        // 其他字符优先级比中文文字和英文字母优先级低
        boolean isX1 = !(Character.isLetter(s1.charAt(0)) || isChinese(s1.charAt(0)));
        boolean isX2 = !(Character.isLetter(s2.charAt(0)) || isChinese(s2.charAt(0)));

        if (isX1 && !isX2)
            return 1;
        if (!isX1 && isX2)
            return -1;
        return collator.compare(pinyin1, pinyin2);


//        if (!isLetter1 && !isLetter2) {
//            // 同为其他字符时，比较unicode码值大小
//            return s1.compareTo(s2);
//        } else if (!isLetter1) {
//            // music1 首字母为非字母字符，优先级更低
//            return 1;
//        } else if (!isLetter2) {
//            // music2 首字母为非字母字符，优先级更低
//            return -1;
//        } else {
//            // 同为中文时，比较二者转化为拼音字符串的大小
//            return collator.compare(pinyin1, pinyin2);
//        }
    }


    private static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }
}
