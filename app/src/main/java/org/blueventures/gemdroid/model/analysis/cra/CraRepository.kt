package org.blueventures.gemdroid.model.analysis.cra

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File
import java.io.InputStream

class CraRepository(
    private val datasource: CraDatasource = CraDatasource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): ApiRepository(datasource) {
    fun getRemoteCRAs() = flow {
        emit(datasource.getRemoteCRAs())
    }.flowOn(ioDispatcher)

    fun validateLocalCRA(roiDir: File, files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?) = flow {
        emit(datasource.validateLocalCRA(roiDir, files, names, remoteCRAs, previous))
    }.flowOn(ioDispatcher)

    fun getCRAFields(cont: CRAFile, hist: CRAFile?) = flow {
        emit(datasource.getCRAFields(cont, hist))
    }.flowOn(ioDispatcher)

    fun uploadCRA(cra: CRAFile) = flow {
        emit(datasource.uploadCRA(cra))
    }.flowOn(ioDispatcher)

    fun uploadCRAs(c1: CRAFile, c2: CRAFile) = flow {
        emit(datasource.uploadCRAs(c1, c2))
    }.flowOn(ioDispatcher)

    fun ingestCRA(cra: CRAFile) = flow {
        emit(datasource.ingestCRA(cra))
    }.flowOn(ioDispatcher)

    fun ingestCRAs(c1: CRAFile, c2: CRAFile) = flow {
        emit(datasource.ingestCRAs(c1, c2))
    }.flowOn(ioDispatcher)

    fun uploadFields(cra: CRAFile) = flow {
        emit(datasource.uploadFields(cra))
    }.flowOn(ioDispatcher)

    fun uploadFields(c1: CRAFile, c2: CRAFile) = flow {
        emit(datasource.uploadFields(c1, c2))
    }.flowOn(ioDispatcher)

    fun saveCRAs(roiDir: File, cra: CRA) = flow {
        emit(datasource.saveCRAs(roiDir, cra))
    }.flowOn(ioDispatcher)
}