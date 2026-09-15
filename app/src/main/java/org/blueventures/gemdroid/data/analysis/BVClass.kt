package org.blueventures.gemdroid.data.analysis

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVDarkBlue
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVDarkGreen
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVGreen
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVLightGreen
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVOrange
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVRed
import org.blueventures.gemdroid.data.analysis.BVClassColors.BVYellow
import kotlin.enums.enumEntries

enum class BVClass(val number: Int, val color: Color, val enNames: List<String>) {
    CCMI(1, BVDarkGreen, listOf("Closed-Canopy Mangrove", "Closed-Canopy Mangrove I", "Closed-Canopy Mangrove 1")),
    CCMII(2, BVGreen, listOf("Closed-Canopy Mangrove II", "Closed-Canopy Mangrove 2")),
    OCMI(3, BVGreen, listOf("Open-Canopy Mangrove", "Open-Canopy Mangrove I", "Open-Canopy Mangrove 1")),
    OCMII(4, BVLightGreen, listOf("Open-Canopy Mangrove II", "Open-Canopy Mangrove 2")),
    TF(5, BVRed, listOf("Terrestrial Forest")),
    OV(6, BVOrange, listOf("Other Vegetation", "Freshwater Vegetation", "Other Vegetation I", "Other Vegetation 1", "Other Terrestrial Vegetation", "Other Terrestrial Vegetation I", "Other Terrestrial Vegetation 1")),
    BE(8, BVYellow, listOf("Barren Exposed", "Barren/Exposed", "Barren / Exposed")),
    RW(9, BVDarkBlue, listOf("Residual Water"));

    data class Subclass(val name: String, val description: String)
    data class LocalizedInfo(val name: String, val subclasses: List<Subclass>, val description: String)
    fun localizedInfo(context: Context): LocalizedInfo {
        return when(this) {
            CCMI -> LocalizedInfo(
                context.getString(R.string.closed_canopy_mangrove),
                emptyList(),
                context.getString(R.string.ccmi_description)
            )
            CCMII -> LocalizedInfo(
                context.getString(R.string.closed_canopy_mangrove_ii),
                emptyList(),
                context.getString(R.string.ccmii_description)
            )
            OCMI -> LocalizedInfo(
                context.getString(R.string.open_canopy_mangrove),
                emptyList(),
                context.getString(R.string.ocmi_description)
            )
            OCMII -> LocalizedInfo(
                context.getString(R.string.open_canopy_mangrove_ii),
                emptyList(),
                context.getString(R.string.ocmii_description)
            )
            TF -> LocalizedInfo(
                context.getString(R.string.terrestrial_forest),
                emptyList(),
                context.getString(R.string.terrestrial_forest_description)
            )
            OV -> LocalizedInfo(
                context.getString(R.string.other_vegetation),
                listOf(
                    Subclass(context.getString(R.string.shrubland), context.getString(R.string.shrubland_description)),
                    Subclass(context.getString(R.string.cropland), context.getString(R.string.cropland_description)),
                    Subclass(context.getString(R.string.non_mangrove_intertidal_vegetation), context.getString(R.string.non_mangrove_intertidal_vegetation_description))
                ),
                ""
            )
            BE -> LocalizedInfo(
                context.getString(R.string.barren_exposed),
                listOf(
                    Subclass(context.getString(R.string.exposed_soil), context.getString(R.string.exposed_soil_description)),
                    Subclass(context.getString(R.string.exposed_mud), context.getString(R.string.exposed_mud_description)),
                    Subclass(context.getString(R.string.inactive_aquaculture_ponds), "")
                ),
                ""
            )
            RW -> LocalizedInfo(
                context.getString(R.string.residual_water),
                listOf(
                    Subclass(context.getString(R.string.clear_water), context.getString(R.string.clear_water_description)),
                    Subclass(context.getString(R.string.turbid_water), context.getString(R.string.turbid_water_description)),
                    Subclass(context.getString(R.string.very_turbid_water), context.getString(R.string.very_turbid_water_description)),
                    Subclass(context.getString(R.string.active_aquaculture_ponds), "")
                ),
                ""
            )
        }
    }

    fun toCRAClass(context: Context) = CRAClass(number, localizedInfo(context).name)
    
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