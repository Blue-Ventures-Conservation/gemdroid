package org.blueventures.gemdroid.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

val SkyBlue = Color(0xFF25AADD)
val LightGreen = Color(0xFF25DDAA)
val MildRed = Color(0xFFDD4425)
val MidnightBlue = Color(0xFF010135)
val DarkSlate = Color(0xFF313B4C)
val DarkGray = Color(0xFF222936)

fun g2R2B(index: Int, size: Int): String {
    return blend3Way(LightGreen, MildRed, SkyBlue, index, size).hexColor()
}

fun blend3Way(from: Color, mid: Color, to: Color, index: Int, size: Int): Int {
    return when (size) {
        1 -> from.toArgb()
        2 -> {
            when (index) {
                0 -> from.toArgb()
                else -> to.toArgb()
            }
        }
        else -> {
            val odd = size%2
            val even = when (odd) {
                0 -> 1
                else -> 0
            }
            val half = size/2
            val bumpHalf = half + even + odd // this adjustment keeps colors evenly spread, preferring to insert new colors between from and mid
            when {
                (index + 1 >= bumpHalf) -> blend(mid, to, (index - half), (size - half))
                else -> blend(from, mid, index, bumpHalf)
            }
        }
    }
}

fun blend(from: Color, to: Color, index: Int, size: Int): Int {
    return when (size) {
        1 -> from.toArgb()
        else -> {
            val ratio = index.toFloat() * (1.0F / (size.toFloat() - 1.0F))
            val out = FloatArray(3)
            ColorUtils.blendHSL(colorToHSL(from), colorToHSL(to), ratio, out)
            return ColorUtils.HSLToColor(out)
        }
    }
}

fun colorToHSL(color: Color): FloatArray {
    val out = FloatArray(3)
    ColorUtils.colorToHSL(color.toArgb(), out)
    return out
}

fun Int.hexColor(): String {
    return String.format("#%06X", 0xFFFFFF and this)
}