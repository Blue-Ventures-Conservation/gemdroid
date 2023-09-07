package org.blueventures.gemdroid.model.api

import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.mvvm.IORepository
import org.blueventures.gemdroid.api.Api
import org.blueventures.gemdroid.data.Serializer
import java.io.File

open class ApiRepository(
    private val datasource: ApiDatasource = ApiDatasource(),
): IORepository() {
    fun getIdToken() = goFlow { datasource.getIdToken() }
    fun uriFromStorage(path: String) = goFlow { datasource.uriFromStorage(path) }
    fun <I, O> getRemote(body: I, call: suspend (Api.Service, I) -> ApiResult<O>) = goFlow { datasource.getRemote(body, call) }
    fun <T, S : Serializer<T>> loadFile(file: File, serializer: S) = goFlow { datasource.loadFile(file, serializer) }
    fun <T, S : Serializer<T>> saveFile(file: File, data: T, serializer: S) = goFlow { datasource.saveFile(file, data, serializer) }
    fun deleteFile(file: File) = goFlow { datasource.deleteFile(file) }
}