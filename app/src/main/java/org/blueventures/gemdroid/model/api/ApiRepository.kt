package org.blueventures.gemdroid.model.api

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.github.zibnix.droidbones.api.ApiResult
import com.github.zibnix.droidbones.mvvm.IORepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
    fun renameFile(file: File, newName: String) = goFlow { datasource.renameFile(file, newName) }
    fun zipFile(files: List<File>, out: File) = goFlow { datasource.zipFile(files, out) }

    fun <T> read(context: Context, callback: (Preferences) -> T) = context.dataStore.data.catch {
        emit(emptyPreferences())
    }.map(callback).flowOn(ioDispatcher)

    fun <T> read(context: Context, default: KeyValue<T>) = context.dataStore.data.catch {
        emit(emptyPreferences())
    }.map { prefs ->
        default.read(prefs)
    }.flowOn(ioDispatcher)

    fun <T> write(context: Context, key: Preferences.Key<T>, value: T) = goFlow {
        try {
            context.dataStore.edit { prefs ->
                prefs[key] = value
            }
        } catch (_: Exception) {}
    }
}

data class KeyValue<T>(val key: Preferences.Key<T>, val default: T) {
    fun read(prefs: Preferences) = prefs[key] ?: default
}