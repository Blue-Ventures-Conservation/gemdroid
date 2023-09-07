package org.blueventures.gemdroid.model.analysis.classification.separability

import org.blueventures.gemdroid.model.api.ApiRepository

class SeparabilityRepository(
    datasource: SeparabilityDatasource = SeparabilityDatasource(),
): ApiRepository(datasource)