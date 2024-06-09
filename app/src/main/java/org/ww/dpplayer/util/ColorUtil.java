package org.ww.dpplayer.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.core.graphics.ColorUtils;

public class ColorUtil
{
    public static int getAverageColorWithBlackOverlay(Bitmap bitmap, boolean top) {
        int averageColor = getAverageColor(bitmap, top); // 先获取平均颜色

        // 计算透明度的黑色颜色值
        int overlayColor = Color.argb((int)(255 * 0.8), 0, 0, 0); // 60%透明度的黑色

        // 结合平均颜色与黑色滤镜。这里简单地使用了叠加（overlay）的概念，但实际的混合效果可能需要更复杂的计算
        // 注意：此处的叠加逻辑是直接将两种颜色的ARGB值进行简单相加，这可能不是所有情况下的期望效果。
        // 对于更准确的颜色滤镜应用，可能需要使用PorterDuff模式或其他图像处理技术。

        return ColorUtils.compositeColors(averageColor, overlayColor);
    }

    public static int getAverageColor(Bitmap bitmap, boolean top) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width];
        int y = top ? 0 : height - 1;
        bitmap.getPixels(pixels, 0, width, 0, y, width, 1);

        int red = 0, green = 0, blue = 0;
        for (int pixel : pixels) {
            red += Color.red(pixel);
            green += Color.green(pixel);
            blue += Color.blue(pixel);
        }

        int count = pixels.length;
        return Color.rgb(red / count , green / count, blue / count);
    }

    public static int getAverageColor(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int red = 0, green = 0, blue = 0;
        for (int pixel : pixels) {
            red += Color.red(pixel);
            green += Color.green(pixel);
            blue += Color.blue(pixel);
        }
        return Color.rgb(red / pixels.length , green / pixels.length, blue / pixels.length);
    }

    public static boolean isDarkColor(int color) {
        // 计算输入颜色的相对亮度
        double relativeLuminance = Color.luminance(color);

        // 简单判断，如果亮度高于某个阈值（这里取中间值0.5作为示例），则认为颜色较亮，返回黑色；否则返回白色
        // 这里的阈值可以根据实际情况调整以适应不同的需求
        return relativeLuminance < 0.5;
    }

}
