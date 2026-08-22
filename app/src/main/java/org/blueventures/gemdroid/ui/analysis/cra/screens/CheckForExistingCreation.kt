package org.blueventures.gemdroid.ui.analysis.cra.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.GeojsonPolygonFeatureCollection
import org.blueventures.gemdroid.model.analysis.cra.CRAViewModel
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.Col
import org.blueventures.gemdroid.ui.common.Progress

object CheckForExistingCreation {
    @Composable
    fun Screen(viewModel: CRAViewModel, next: Click) {
        val (existingResult, setExistingResult) = remember { mutableStateOf<Result<GeojsonPolygonFeatureCollection>?>(null) }
        when {
            existingResult == null -> {
                Progress()
                viewModel.loadLocallyCreatedCRAFile(setExistingResult)
            }
            existingResult.isFailure || existingResult.getOrNull()!!.features.isEmpty() -> {
                Progress()
                next()
            }
            else -> {
                val (deleteRequest, setDeleteRequest) = remember { mutableStateOf<Unit?>(null) }
                when {
                    deleteRequest == null -> {
                        Col.Dash(stringResource(R.string.there_are_already_cras_created_for_this_project)) {
                            Col.DashboardButton(stringResource(R.string.continue_where_i_left_off)) {
                                val fc = existingResult.getOrNull()!!
                                fc.fixFloats()
                                viewModel.continueExisting(fc)
                                next()
                            }
                            Col.DashboardButton(stringResource(R.string.start_over)) {
                                setDeleteRequest(Unit)
                            }
                        }
                    }
                    else -> {
                        Progress()
                        viewModel.deleteLocallyCreatedCRAFile {
                            next()
                        }
                    }
                }
            }
        }
    }
}