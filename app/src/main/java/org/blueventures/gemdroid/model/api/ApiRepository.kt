package org.blueventures.gemdroid.model.api

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

open class ApiRepository(
    private val datasource: ApiDatasource = ApiDatasource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    fun getIdToken() = flow {
        emit(datasource.getIdToken())
    }.flowOn(ioDispatcher)
}