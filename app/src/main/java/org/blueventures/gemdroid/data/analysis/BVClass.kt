package org.blueventures.gemdroid.data.analysis

import androidx.compose.ui.graphics.Color
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVBurlywood
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVDarkBlue
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVDarkGreen
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVGreen
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVLightGreen
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVLimeGreen
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVOrange
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVRed
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVYellow
import kotlin.enums.enumEntries

enum class BVClass(val number: Int, val color: Color, val names: List<String>) {
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