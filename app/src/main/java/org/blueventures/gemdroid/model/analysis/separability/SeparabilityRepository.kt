package org.blueventures.gemdroid.model.analysis.separability

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File

class SeparabilityRepository(
    private val datasource: SeparabilityDatasource = SeparabilityDatasource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): ApiRepository(datasource) {
    fun loadCRAs(roiDir: File, callback: (Result<CRA>) -> Unit) = datasource.loadCRAs(roiDir, callback)
}