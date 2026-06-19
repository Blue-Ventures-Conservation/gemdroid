package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.AppBarUpdate
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col

object UploadOrCreate {
    @Composable
    fun Screen(appBar: AppBar, upload: Click, create: Click) {
        appBar.Update(AppBarUpdate(stringResource(R.string.classification_reference_areas)))

        Col.Dash(stringResource(R.string.do_you_have_classification_reference_area_shapefiles)) {
            Col.DashboardButton(stringResource(R.string.yes_i_already_have_cra_shapefile_s)) {
                upload()
            }
            Col.DashboardButton(stringResource(R.string.no_and_i_d_like_to_create_my_cras_using_this_app)) {
                create()
            }
        }
    }
}