package org.blueventures.gemdroid.ui.common

import com.anychart.AnyChartView
import com.anychart.charts.Cartesian
import org.blueventures.gemdroid.model.Licenses

object Charts {
    fun prep(chart: AnyChartView, cart: () -> Cartesian): Cartesian {
        chart.setLicenceKey(Licenses.anychart)
        return cartesian(cart)
    }

    fun cartesian(cart: () -> Cartesian): Cartesian {
        val cartesian = cart()
        cartesian.credits().text("")
        return cartesian
    }
}