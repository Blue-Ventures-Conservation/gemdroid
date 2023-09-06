package org.blueventures.gemdroid.model.api

import com.github.zibnix.droidbones.mvvm.IORepository

open class ApiRepository(
    private val datasource: ApiDatasource = ApiDatasource(),
): IORepository() {
    fun getIdToken() = goFlow { datasource.getIdToken() }
    fun getTasksResults(vararg names: String) = goFlow { datasource.getTasksResults(*names) }
    fun uriFromStorage(path: String) = goFlow { datasource.uriFromStorage(path) }
}