package org.blueventures.gemdroid.data.analysis

import androidx.compose.ui.graphics.Color
import org.blueventures.gemdroid.ui.theme.blend3Way
import kotlin.enums.enumEntries

object BVClassColors {
    fun makeColorPalette(classes: List<String>): List<Color> {
        val bv = getBVPalette(classes)
        return if (bv != null) {
            bv
        } else {
            val pal = mutableListOf<Color>()
            val size = classes.size
            for (i in 0 until size) {
                pal.add(Color(blend3Way(BVGreen, BVRed, BVDarkBlue, i, size)))
            }
            pal
        }
    }

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

    // BV class colors
    val BVDarkGreen = Color(0xFF005000)
    val BVGreen = Color(0xFF008F00)
    val BVLimeGreen = Color(0xFF32CD32)
    val BVLightGreen = Color(0xFF90EE90)
    val BVRed = Color(0xFFFF0000)
    val BVOrange = Color(0xFFFFa500)
    val BVBurlywood = Color(0xFFDEB887)
    val BVYellow = Color(0xFFFFFF00)
    val BVDarkBlue = Color(0xFF0000DB)

    enum class BVClass(val num: Int, val color: Color, val names: List<String>) {
        CCM(1, BVDarkGreen, listOf("Closed-Canopy Mangrove")),
        OCMI(2, BVGreen, listOf("Open-Canopy Mangrove", "Open-Canopy Mangrove I", "Open-Canopy Mangrove 1")),
        OCMII(3, BVLimeGreen, listOf("Open-Canopy Mangrove II", "Open-Canopy Mangrove 2")),
        OCMIII(4, BVLightGreen, listOf("Open-Canopy Mangrove III", "Open-Canopy Mangrove 3")),
        TF(5, BVRed, listOf("Terrestrial Forest")),
        OTV(6, BVOrange, listOf("Other Vegetation", "Other Vegetation I", "Other Vegetation 1", "Other Terrestrial Vegetation", "Other Terrestrial Vegetation I", "Other Terrestrial Vegetation 1")),
        OV(7, BVBurlywood, listOf("Freshwater Vegetation", "Other Vegetation II", "Other Vegetation 2", "Other Terrestrial Vegetation II", "Other Terrestrial Vegetation 2")),
        BE(8, BVYellow, listOf("Barren Exposed", "Barren/Exposed")),
        RW(9, BVDarkBlue, listOf("Residual Water"));

        companion object {
            fun fromName(name: String): BVClass? {
                val sane = sanitize(name)
                enumEntries<BVClass>().forEach { bv ->
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
}