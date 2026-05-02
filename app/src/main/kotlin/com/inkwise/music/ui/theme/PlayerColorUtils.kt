package com.inkwise.music.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

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
