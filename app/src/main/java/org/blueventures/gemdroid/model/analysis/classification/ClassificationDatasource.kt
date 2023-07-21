package org.blueventures.gemdroid.model.analysis.classification

import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.model.api.ApiDatasource

class ClassificationDatasource(
    private val api: Api.Service = Api.Service.instance(),
): ApiDatasource(api) {
    companion object {
        const val classificationDir = "classification"
        const val contLCTilesDir = "cont_lc_tiles" // land cover tiles
        const val histLCTilesDir = "hist_lc_tiles" // land cover tiles
        const val ccomClassifyFile = "ccom_classify.json"
        const val hcomClassifyFile = "hcom_classify.json"
    }
}