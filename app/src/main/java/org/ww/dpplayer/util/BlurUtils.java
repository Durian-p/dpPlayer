package org.ww.dpplayer.util;
import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

public class BlurUtils {

    public static Bitmap blurEdge(Context context, Bitmap original, float radius, float edgeWidthRatio) {
        if (original == null) return null;

        // 计算边缘宽度
        int edgeWidth = (int) (Math.min(original.getWidth(), original.getHeight()) * edgeWidthRatio);

        // 创建输出位图
        Bitmap output = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());

        // 初始化RenderScript
        RenderScript rs = RenderScript.create(context);

        // 创建输入和输出的Allocation
        Allocation input = Allocation.createFromBitmap(rs, original);
        Allocation outputAlloc = Allocation.createFromBitmap(rs, output);

        // 创建模糊脚本
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        // 设置模糊半径
        blurScript.setRadius(radius);

        // 对边缘进行模糊处理
        // 注意：这里的逻辑简化了边缘的处理，实际应用中可能需要更复杂的剪裁逻辑来精确控制边缘模糊区域
        for (int y = 0; y < edgeWidth; y++) { // 处理顶部边缘
            blurScript.setInput(input);
            blurScript.forEach(outputAlloc);
            outputAlloc.copyTo(output);
        }
        for (int y = original.getHeight() - edgeWidth; y < original.getHeight(); y++) { // 处理底部边缘
            blurScript.setInput(input);
            blurScript.forEach(outputAlloc);
            outputAlloc.copyTo(output);
        }
        for (int x = 0; x < edgeWidth; x++) { // 处理左侧边缘
            blurScript.setInput(input);
            blurScript.forEach(outputAlloc);
            outputAlloc.copyTo(output);
        }
        for (int x = original.getWidth() - edgeWidth; x < original.getWidth(); x++) { // 处理右侧边缘
            blurScript.setInput(input);
            blurScript.forEach(outputAlloc);
            outputAlloc.copyTo(output);
        }

        // 清理资源
        input.destroy();
        outputAlloc.destroy();
        blurScript.destroy();
        rs.destroy();

        return output;
    }
}
