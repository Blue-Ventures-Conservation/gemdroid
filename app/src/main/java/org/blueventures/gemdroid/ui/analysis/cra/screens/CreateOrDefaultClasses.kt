package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col

object CreateOrDefaultClasses {
    @Composable
    fun Screen(useDefault: Click, create: Click) {
        Col.Dash(stringResource(R.string.use_recommended_or_create)) {
            Col.DashboardButton(stringResource(R.string.use_recommended_classes)) {
                useDefault()
            }
            Col.DashboardButton(stringResource(R.string.create_my_own_classes)) {
                create()
            }
        }
    }
}