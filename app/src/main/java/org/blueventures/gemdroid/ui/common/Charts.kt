package org.blueventures.gemdroid.ui.common

import com.anychart.AnyChartView
import com.anychart.core.SeparateChart
import org.blueventures.gemdroid.model.Licenses

object Charts {
    fun prep(chart: AnyChartView, charter: () -> SeparateChart): SeparateChart {
        chart.setLicenceKey(Licenses.anychart)
        return chart(charter)
    }

    fun chart(charter: () -> SeparateChart): SeparateChart {
        val c = charter()
        c.credits().text("")
        return c
    }
}