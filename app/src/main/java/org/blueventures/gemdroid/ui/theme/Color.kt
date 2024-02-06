package org.blueventures.gemdroid.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

val SkyBlue = Color(0xFF25AADD)
val LightGreen = Color(0xFF11DD11)
val Caution = Color(0xFFFFE968)
val MildRed = Color(0xFFDD4425)
val LightGrey = Color(0xFF909090)
val MidnightBlue = Color(0xFF010135)
val DarkSlate = Color(0xFF313B4C)
val DarkGray = Color(0xFF222936)
val OffWhite = Color(0xFFFFFBFE)

fun g2R2BHex(index: Int, size: Int): String {
    return g2R2B(index, size).toHexString()
}

fun g2R2B(index: Int, size: Int): Color {
    return Color(blend3Way(LightGreen, MildRed, SkyBlue, index, size))
}

fun blend3Way(from: Color, mid: Color, to: Color, index: Int, size: Int, alpha: Int = 0xFF): Int {
    return ColorUtils.setAlphaComponent(when (size) {
        1 -> from.toArgb()
        2 -> {
            when (index) {
                0 -> from.toArgb()
                else -> to.toArgb()
            }
        }
        else -> {
            val half = size/2
            when {
                (index >= half) -> blend(mid, to, (index - half), (size - half))
                else -> blend(from, mid, index, half + 1)
            }
        }
    }, alpha)
}

fun blend(from: Color, to: Color, index: Int, size: Int, alpha: Int = 0xFF): Int {
    return ColorUtils.setAlphaComponent(when (size) {
        1 -> from.toArgb()
        else -> {
            val ratio = index.toFloat() * (1.0F / (size.toFloat() - 1.0F))
            val out = hsl()
            ColorUtils.blendHSL(colorToHSL(from), colorToHSL(to), ratio, out)
            ColorUtils.HSLToColor(out)
        }
    }, alpha)
}

fun colorToHSL(color: Color): FloatArray {
    val out = hsl()
    ColorUtils.colorToHSL(color.toArgb(), out)
    return out
}

private fun hsl(): FloatArray {
    return FloatArray(3)
}

fun Color.toHexString() = toArgb().hexColor()

private fun Int.hexColor() = String.format("#%06X", 0xFFFFFF and this)