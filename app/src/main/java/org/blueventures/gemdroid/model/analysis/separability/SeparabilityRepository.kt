package org.blueventures.gemdroid.model.analysis.separability

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class SeparabilityRepository(
    private val datasource: SeparabilityDatasource = SeparabilityDatasource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun loadCRAs(roiDir: File) = flow {
        emit(datasource.loadCRAs(roiDir))
    }.flowOn(ioDispatcher)
}