package org.blueventures.gemdroid.model.analysis.classification

import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.model.api.ApiDatasource

class ClassificationDatasource(
    private val api: Api.Service = Api.Service.instance(),
): ApiDatasource(api) {
}