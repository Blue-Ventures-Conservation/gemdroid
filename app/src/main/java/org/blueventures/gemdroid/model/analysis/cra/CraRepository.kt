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
    fun getRemoteCRAs(callback: (Result<List<String>>) -> Unit) = datasource.getRemoteCRAs(callback)
    fun getCRAFields(cont: CRAFile, hist: CRAFile?, callback: (Result<Fields>) -> Unit) = datasource.getCRAFields(cont, hist, callback)
    fun uploadCRAs(c1: CRAFile, c2: CRAFile, callback: (Result<Unit>) -> Unit) = datasource.uploadCRAs(c1, c2, callback)
    fun uploadCRA(cra: CRAFile, callback: (Result<Unit>) -> Unit) = datasource.uploadCRA(cra, callback)
    fun uploadFields(cra: CRAFile, callback: (Result<Unit>) -> Unit) = datasource.uploadFields(cra, callback)
    fun uploadFields(cont: CRAFile, hist: CRAFile, callback: (Result<Unit>) -> Unit) = datasource.uploadFields(cont, hist, callback)

    fun validateLocalCRA(roiDir: File, files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?) = flow {
        emit(datasource.validateLocalCRA(roiDir, files, names, remoteCRAs, previous))
    }.flowOn(ioDispatcher)

    fun ingestCRATable(key: String) = flow {
        emit(datasource.ingestCRA(key))
    }.flowOn(ioDispatcher)

    fun awaitCRAIngestion(name: String, key: String) = flow {
        emit(datasource.awaitCRAIngestion(name, key))
    }.flowOn(ioDispatcher)

    fun saveCRAs(roiDir: File, cra: CRA) = flow {
        emit(datasource.saveCRAs(roiDir, cra))
    }.flowOn(ioDispatcher)
}