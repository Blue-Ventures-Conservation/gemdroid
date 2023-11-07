package org.blueventures.gemdroid.ui.common.polygons.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Collect
import org.blueventures.gemdroid.ui.common.Nav
import org.blueventures.gemdroid.ui.common.SnackFun
import org.blueventures.gemdroid.ui.common.polygons.Polygons

object NamePolygon {
    interface Model: Polygons.AppBarTitler {
        var name: String

        val polygonType: Int

        fun validatePolygonName(): Boolean
    }

    @Composable
    fun Screen(model: Model, appBar: AppBar, snack: SnackFun, back: Click, next: Click) {
        Nav.Wrap({
            model.name = ""
            back()
        }) {
            appBar.Update(AppBarUpdate(model.appBarTitle(stringResource(model.title))))
            val err = stringResource(R.string.please_enter_unique_non_special_name)
            Collect.Text(header = stringResource(R.string.name_your_polygon).format(stringResource(model.polygonType)), label = stringResource(R.string.please_enter_name), initial = model.name, snack, { name ->
                model.name = name
                if (model.validatePolygonName()) null else err
            }, next)
        }
    }
}