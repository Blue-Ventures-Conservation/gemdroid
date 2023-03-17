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
    fun getRemoteCRAs(callback: (Result<List<String>>) -> Unit) = flow {
        emit(datasource.getRemoteCRAs(callback))
    }.flowOn(ioDispatcher)

    fun validateLocalCRA(roiDir: File, files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?) = flow {
        emit(datasource.validateLocalCRA(roiDir, files, names, remoteCRAs, previous))
    }.flowOn(ioDispatcher)

    fun getCRAFields(cont: CRAFile, hist: CRAFile?, callback: (Result<Fields>) -> Unit) = flow {
        emit(datasource.getCRAFields(cont, hist, callback))
    }.flowOn(ioDispatcher)

    fun uploadCRAs(c1: CRAFile, c2: CRAFile, callback: (Result<Unit>) -> Unit) = flow {
        emit(datasource.uploadCRAs(c1, c2, callback))
    }.flowOn(ioDispatcher)

    fun uploadCRA(cra: CRAFile, callback: (Result<Unit>) -> Unit) = flow {
        emit(datasource.uploadCRA(cra, callback))
    }.flowOn(ioDispatcher)

    fun ingestCRATable(key: String) = flow {
        emit(datasource.ingestCRA(key))
    }.flowOn(ioDispatcher)

    fun awaitCRAIngestion(name: String, key: String) = flow {
        emit(datasource.awaitCRAIngestion(name, key))
    }.flowOn(ioDispatcher)

    fun uploadFields(cra: CRAFile, callback: (Result<Unit>) -> Unit) = flow {
        emit(datasource.uploadFields(cra, callback))
    }.flowOn(ioDispatcher)

    fun uploadFields(cont: CRAFile, hist: CRAFile, callback: (Result<Unit>) -> Unit) = flow {
        emit(datasource.uploadFields(cont, hist, callback))
    }.flowOn(ioDispatcher)

    fun saveCRAs(roiDir: File, cra: CRA) = flow {
        emit(datasource.saveCRAs(roiDir, cra))
    }.flowOn(ioDispatcher)
}