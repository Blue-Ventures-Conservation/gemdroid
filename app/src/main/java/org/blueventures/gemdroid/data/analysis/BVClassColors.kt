package org.blueventures.gemdroid.data.analysis

import androidx.compose.ui.graphics.Color
import org.blueventures.gemdroid.ui.theme.blend3Way

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
}