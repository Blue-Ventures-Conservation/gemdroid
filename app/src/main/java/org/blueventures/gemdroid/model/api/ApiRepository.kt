package org.blueventures.gemdroid.model.api

open class ApiRepository(
    private val datasource: ApiDatasource = ApiDatasource(),
) {
    fun getIdToken(callback: (Result<Unit>) -> Unit) {
        datasource.getIdToken(callback)
    }
}