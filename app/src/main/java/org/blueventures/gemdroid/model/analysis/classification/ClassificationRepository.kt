package org.blueventures.gemdroid.model.analysis.classification

import org.blueventures.gemdroid.model.api.ApiRepository

class ClassificationRepository(
    datasource: ClassificationDatasource = ClassificationDatasource(),
): ApiRepository(datasource)