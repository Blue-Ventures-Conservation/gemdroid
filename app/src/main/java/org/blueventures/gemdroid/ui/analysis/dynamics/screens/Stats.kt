package org.blueventures.gemdroid.ui.analysis.dynamics.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anychart.chart.common.dataentry.DataEntry
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.PolygonDrawer.Companion.hectareInMeters
import org.blueventures.gemdroid.data.PolygonDrawer.Companion.hectares
import org.blueventures.gemdroid.data.analysis.BVClassColors.makeColorPalette
import org.blueventures.gemdroid.data.analysis.dynamics.ClassDynamics
import org.blueventures.gemdroid.data.analysis.dynamics.Conversion
import org.blueventures.gemdroid.model.analysis.dynamics.DynamicsViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Charts
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Info
import org.blueventures.gemdroid.ui.theme.toHexString
import kotlin.math.round

object Stats {
    @Composable
    fun Screen(viewModel: DynamicsViewModel, appBar: AppBar) {
        appBar.Update(AppBarUpdate(viewModel.roi.appBarTitle(stringResource(R.string.dynamics))))

        val classColors = mutableMapOf<String, String>()
        val classNames = mutableListOf<String>()
        for (classDynamics in viewModel.analysisRegion.allClasses) {
            classNames.add(classDynamics.name)
        }

        val pal = makeColorPalette(classNames)
        for ((i, className) in classNames.withIndex()) {
            classColors[className] = pal[i].toHexString()
        }

        Col.MidPad(arrange = Arrangement.Top, scroll = true) {
            Info.Block {
                val className = viewModel.analysisClass.name
                val conversions = viewModel.analysisClass.conversions

                Info.Header(viewModel.analysisRegion.name)
                ClassBlock(className, viewModel.analysisClass)
                Chart(stringResource(R.string.conversion_to_other_classes).format(className), conversions.from, classColors)
                Spacer(modifier = Modifier.size(16.dp))
                Chart(stringResource(R.string.other_class_conversions_to).format(className), conversions.to, classColors)
            }
        }
    }

    @Composable
    fun ClassBlock(className: String, cd: ClassDynamics) {
        StatsRow(ClassLabel(R.string.total_hist_area, className), cd.histArea)
        StatsRow(ClassLabel(R.string.total_cont_area, className), cd.contArea)
        DetailsRow(ClassLabel(R.string.percent_change_of, className), percentChange(cd.contArea, cd.histArea))
        PercentRow(ClassLabel(R.string.loss_fmt, className), cd.loss, cd.histArea)
        PercentRow(ClassLabel(R.string.persistence_fmt, className), cd.persistence, cd.histArea)
        PercentRow(ClassLabel(R.string.gain_fmt, className), cd.gain, cd.histArea)
    }

    @Composable
    fun PercentRow(label: String, area: Double, historicalArea: Double) {
        DetailsRow(label, hectares(toHectares(area)) + " [" + percent(area, historicalArea) + "]")
    }

    private fun percent(num: Double, denom: Double) = (round((num/denom) * 1e4)/1e2).toString() + "%"
    private fun percentChange(cont: Double, hist: Double) = percent(cont - hist, hist)

    @Composable
    fun StatsRow(label: String, area: Double) {
        DetailsRow(label, hectares(toHectares(area)))
    }

    @Composable
    fun DetailsRow(label: String, value: String) {
        Info.Block {
            Info.SubHeader(label, false)
            Info.Row {
                Info.Txt(stringResource(R.string.area))
                Info.Txt(value)
            }
        }
    }

    @Composable
    private fun ClassLabel(@StringRes fmt: Int, targetClass: String) = stringResource(fmt).format(targetClass)

    private fun toHectares(meters: Double) = (meters/hectareInMeters).toInt()

    @Composable
    fun Chart(title: String, conversions: List<Conversion>, classColors: Map<String, String>) {
        val data = mutableListOf<DataEntry>()
        for (conv in conversions) {
            data.add(ConversionEntry(conv.name, classColors[conv.name] ?: "", toHectares(conv.area)))
        }
        Charts.BarChart(title, stringResource(R.string.other_classes), stringResource(R.string.area_ha), data, yUnit = "ha", fill = false)
    }

    class ConversionEntry(x: String, color: String, value: Number) : DataEntry() {
        init {
            setValue("x", x)
            setValue("fill", color)
            setValue("value", value)
        }
    }
}