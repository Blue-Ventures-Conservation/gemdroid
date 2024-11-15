package org.blueventures.gemdroid.ui.roi.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col

object CopiedPolygon {
    @Composable
    fun Screen(appBar: AppBar, keep: Click, edit: Click) {
        appBar.Update(AppBarUpdate(stringResource(R.string.create_coarse_boundary)))
        Col.Dash(stringResource(R.string.would_you_like_to_keep_the_copied_boundary)) {
            Col.DashboardButton(stringResource(R.string.keep_the_copied_boundary), keep)
            Col.DashboardButton(stringResource(R.string.edit_replace_the_boundary), edit)
        }
    }
}