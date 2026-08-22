package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col

object CreateOrDefaultClasses {
    @Composable
    fun Screen(viewModel: CRAViewModel, useDefault: Click, create: Click, ) {
        val context = LocalContext.current
        Col.Dash(stringResource(R.string.use_recommended_or_create)) {
            Col.DashboardButton(stringResource(R.string.use_recommended_classes)) {
                viewModel.setCRAClasses(context, emptyList())
                useDefault()
            }
            Col.DashboardButton(stringResource(R.string.create_my_own_classes)) {
                create()
            }
        }
    }
}