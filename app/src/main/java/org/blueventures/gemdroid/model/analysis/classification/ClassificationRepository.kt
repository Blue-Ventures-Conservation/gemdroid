package org.blueventures.gemdroid.model.analysis.classification

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.blueventures.gemdroid.model.api.ApiRepository

class ClassificationRepository(
    private val datasource: ClassificationDatasource = ClassificationDatasource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): ApiRepository(datasource = datasource) {
}