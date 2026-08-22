package org.blueventures.gemdroid.data.analysis

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.blueventures.gemdroid.R
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

enum class BVClass(val number: Int, val color: Color, val enNames: List<String>) {
    CCM(1, BVDarkGreen, listOf("Closed-Canopy Mangrove")),
    OCMI(2, BVGreen, listOf("Open-Canopy Mangrove", "Open-Canopy Mangrove I", "Open-Canopy Mangrove 1")),
    OCMII(3, BVLimeGreen, listOf("Open-Canopy Mangrove II", "Open-Canopy Mangrove 2")),
    OCMIII(4, BVLightGreen, listOf("Open-Canopy Mangrove III", "Open-Canopy Mangrove 3")),
    TF(5, BVRed, listOf("Terrestrial Forest")),
    OTV(6, BVOrange, listOf("Other Vegetation", "Other Vegetation I", "Other Vegetation 1", "Other Terrestrial Vegetation", "Other Terrestrial Vegetation I", "Other Terrestrial Vegetation 1")),
    OV(7, BVBurlywood, listOf("Freshwater Vegetation", "Other Vegetation II", "Other Vegetation 2", "Other Terrestrial Vegetation II", "Other Terrestrial Vegetation 2")),
    BE(8, BVYellow, listOf("Barren Exposed", "Barren/Exposed")),
    RW(9, BVDarkBlue, listOf("Residual Water"));

    fun localizedName(context: Context): String {
        return when(this) {
            CCM -> context.getString(R.string.closed_canopy_mangrove)
            OCMI -> context.getString(R.string.open_canopy_mangrove)
            OCMII -> context.getString(R.string.open_canopy_mangrove_ii)
            OCMIII -> context.getString(R.string.open_canopy_mangrove_iii)
            TF -> context.getString(R.string.terrestrial_forest)
            OTV -> context.getString(R.string.other_vegetation)
            OV -> context.getString(R.string.freshwater_vegetation)
            BE -> context.getString(R.string.barren_exposed)
            RW -> context.getString(R.string.residual_water)
        }
    }

    fun toCRAClass(context: Context) = CRAClass(number, localizedName(context))
    
    companion object {
        fun fromName(name: String): BVClass? {
            val sane = sanitize(name)
            enumEntries<BVClass>().forEach { bv ->
                for (nm in bv.enNames) {
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