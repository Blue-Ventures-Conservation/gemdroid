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
val Clear = Color(0x00000000)

// BV class colors
val BVDarkGreen = Color(0xFF006400)
val BVGreen = Color(0xFF008000)
val BVLimeGreen = Color(0xFF32CD32)
val BVLightGreen = Color(0xFF90EE90)
val BVRed = Color(0xFFFF0000)
val BVOrange = Color(0xFFFFa500)
val BVBurlywood = Color(0xFFDEB887)
val BVYellow = Color(0xFFFFFF00)
val BVDarkBlue = Color(0xFF00008B)

fun Color.toHexString() = toArgb().hexColor()

fun makeColorPalette(classes: List<String>): List<Color> {
    val bv = getBVPalette(classes)
    return if (bv != null) {
        bv
    } else {
        val pal = mutableListOf<Color>()
        val size = classes.size
        for (i in 0 until size) {
            pal.add(Color(blend3Way(LightGreen, MildRed, SkyBlue, i, size)))
        }
        pal
    }
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

private fun blend(from: Color, to: Color, index: Int, size: Int, alpha: Int = 0xFF): Int {
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

private fun colorToHSL(color: Color): FloatArray {
    val out = hsl()
    ColorUtils.colorToHSL(color.toArgb(), out)
    return out
}

private fun hsl(): FloatArray {
    return FloatArray(3)
}

private fun Int.hexColor() = String.format("#%06X", 0xFFFFFF and this)

private fun getBVPalette(classes: List<String>): List<Color>? {
    val cmap = mutableMapOf<BVClass, Int>()

    val pal = mutableListOf<Color>()
    for (className in classes) {
        val bv = BVClass.fromName(className) ?: return null

        pal.add(bv.color)
        cmap[bv] = pal.size - 1
    }

    if (cmap.contains(BVClass.OCMII) && !cmap.contains(BVClass.OCMIII)) {
        val pos = cmap[BVClass.OCMII]!!
        pal[pos] = BVClass.OCMIII.color
    }

    return pal
}

private enum class BVClass(val num: Int, val color: Color, val names: List<String>) {
    CCM(1, BVDarkGreen, listOf("Closed-Canopy Mangrove")),
    OCMI(2, BVGreen, listOf("Open-Canopy Mangrove I", "Open-Canopy Mangrove 1")),
    OCMII(3, BVLimeGreen, listOf("Open-Canopy Mangrove II", "Open-Canopy Mangrove 2")),
    OCMIII(4, BVLightGreen, listOf("Open-Canopy Mangrove III", "Open-Canopy Mangrove 3")),
    TF(5, BVRed, listOf("Terrestrial Forest")),
    OTV(6, BVOrange, listOf("Other Terrestrial Vegetation", "Other Terrestrial Vegetation 1")),
    OV(7, BVBurlywood, listOf("Other Vegetation", "Freshwater Vegetation", "Other Terrestrial Vegetation 2")),
    BE(8, BVYellow, listOf("Barren Exposed", "Barren/Exposed")),
    RW(9, BVDarkBlue, listOf("Residual Water"));

    companion object {
        fun fromName(name: String): BVClass? {
            val sane = sanitize(name)
            enumValues<BVClass>().forEach { bv ->
                for (nm in bv.names) {
                    if (sane == sanitize(nm)) {
                        return bv
                    }
                }
            }

            return null
        }

        private fun sanitize(name: String) = name.lowercase().replace("-", " ").replace("/", " ")
    }
}