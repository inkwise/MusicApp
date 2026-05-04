package com.inkwise.music.ui.theme

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

// =====================================================================
// 方案B：自定义取色算法（基于 Salt Player 颜色直方图统计）
// =====================================================================

/**
 * 从封面 Bitmap 提取主导颜色。
 * 1. 缩小到 100x100 减少计算量
 * 2. 量化颜色后统计直方图
 * 3. 按频率排序，跳过太暗/太亮/太灰的颜色
 * 4. 返回最优主题色
 */
fun extractDominantColor(bitmap: Bitmap): Int {
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false)

    val colorCount = mutableMapOf<Int, Int>()
    for (x in 0 until scaledBitmap.width) {
        for (y in 0 until scaledBitmap.height) {
            val pixel = scaledBitmap.getPixel(x, y)
            val quantizedColor = quantizeColor(pixel)
            colorCount[quantizedColor] = (colorCount[quantizedColor] ?: 0) + 1
        }
    }

    val sortedColors = colorCount.entries
        .sortedByDescending { it.value }
        .map { it.key }

    val dominantColor = sortedColors.firstOrNull { color ->
        val hsl = rgbToHsl(color)
        hsl[2] in 0.15f..0.85f  // 亮度在 15%-85% 之间
                && hsl[1] > 0.1f     // 饱和度大于 10%
    } ?: sortedColors.first()

    scaledBitmap.recycle()
    return dominantColor
}

/**
 * 颜色量化：将 RGB 各通道低 4 位置零，减少颜色数量以便统计
 */
fun quantizeColor(color: Int): Int {
    val r = ((color shr 16) and 0xFF) and 0xF0
    val g = ((color shr 8) and 0xFF) and 0xF0
    val b = (color and 0xFF) and 0xF0
    return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
}

/**
 * RGB 转 HSL，返回 [h, s, l]，各分量范围 0..1
 */
fun rgbToHsl(color: Int): FloatArray {
    val r = ((color shr 16) and 0xFF) / 255f
    val g = ((color shr 8) and 0xFF) / 255f
    val b = (color and 0xFF) / 255f

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val l = (max + min) / 2f

    val s = if (max == min) 0f else {
        if (l > 0.5f) (max - min) / (2f - max - min)
        else (max - min) / (max + min)
    }

    var h = if (max == min) 0f else {
        when (max) {
            r -> ((g - b) / (max - min)) + (if (g < b) 6f else 0f)
            g -> ((b - r) / (max - min)) + 2f
            else -> ((r - g) / (max - min)) + 4f
        }
    }
    h /= 6f
    return floatArrayOf(h, s, l)
}

// =====================================================================
// 颜色变体工具
// =====================================================================

/** 暗化颜色，factor 范围 0..1 */
fun darkenColor(color: Color, factor: Float): Color {
    val r = (color.red * (1f - factor))
    val g = (color.green * (1f - factor))
    val b = (color.blue * (1f - factor))
    return Color(r, g, b, color.alpha)
}

/** 亮化颜色，factor 范围 0..1 */
fun lightenColor(color: Color, factor: Float): Color {
    val r = color.red + ((1f - color.red) * factor)
    val g = color.green + ((1f - color.green) * factor)
    val b = color.blue + ((1f - color.blue) * factor)
    return Color(r, g, b, color.alpha)
}

/**
 * 感知亮度判断：使用加权公式计算颜色是否偏暗。
 * luminance = 0.299*R + 0.587*G + 0.114*B
 * 返回值 < 0.5 表示深色背景，适合白色文字。
 */
fun isColorDark(color: Color): Boolean {
    val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    return luminance < 0.5f
}

// =====================================================================
// 保留原有工具函数（兼容现有调用）
// =====================================================================

fun Color.toSoftBackground(): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hsl)

    hsl[1] = (hsl[1] * 0.15f).coerceAtMost(0.2f)
    hsl[2] = 0.95f

    return Color(ColorUtils.HSLToColor(hsl))
}

fun harmonizeToPlayerBackground(colorInt: Int): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(colorInt, hsl)

    val saturation = hsl[1]
    val lightness = hsl[2]

    if (lightness > 0.6f) {
        hsl[1] = (saturation * 0.4f).coerceAtMost(0.5f)
        hsl[2] = lightness * 0.96f
    } else {
        hsl[1] = (saturation * 0.3f).coerceAtMost(0.4f)
        hsl[2] = 0.88f
    }

    return Color(ColorUtils.HSLToColor(hsl))
}
