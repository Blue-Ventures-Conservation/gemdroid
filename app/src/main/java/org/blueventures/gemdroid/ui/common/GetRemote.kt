package org.blueventures.gemdroid.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.github.zibnix.droidbones.api.ApiResult

object GetRemote {
    @Composable
    fun <T> Save(
        getLocal: ((Result<T>) -> Unit) -> Unit,
        getRemote: ((ApiResult<T>) -> Unit) -> Unit,
        save: (T) -> Unit,
        // returns a message to display and whether or not the request should be retried
        errorHandler: (Int?, String?) -> Pair<String?, Boolean> = { _, _ -> Pair(null, true) },
        screen: @Composable (T) -> Unit) {
        Screen(getLocal, getRemote, errorHandler) { dat ->
            save(dat)
            screen(dat)
        }
    }

    @Composable
    fun <T> AwaitSave(
        getLocal: ((Result<T>) -> Unit) -> Unit,
        getRemote: ((ApiResult<T>) -> Unit) -> Unit,
        save: (T, (Result<Unit>) -> Unit) -> Unit,
        // returns a message to display and whether or not the request should be retried
        errorHandler: (Int?, String?) -> Pair<String?, Boolean> = { _, _ -> Pair(null, true) },
        saveFail: ((T) -> Unit)? = null,
        screen: @Composable (T) -> Unit
    ) {
        val (saved, setSaved) = remember { mutableStateOf<T?>(null) }

        when (saved) {
            null -> {
                Screen(getLocal, getRemote, errorHandler) { dat ->
                    save(dat) { result ->
                        when {
                            result.isSuccess -> setSaved(dat)
                            result.isFailure -> {
                                saveFail?.invoke(dat) ?: setSaved(dat)
                            }
                        }
                    }
                }
            }
            else -> {
                screen(saved)
            }
        }
    }

    @Composable
    fun <T> Screen(
        getLocal: ((Result<T>) -> Unit) -> Unit,
        getRemote: ((ApiResult<T>) -> Unit) -> Unit,
        // returns a message to display and whether or not the request should be retried
        errorHandler: (Int?, String?) -> Pair<String?, Boolean> = { _, _ -> Pair(null, true) },
        screen: @Composable (T) -> Unit,
    ) {
        val (local, setLocal) = remember { mutableStateOf<Result<T>?>(null) }
        val (remote, setRemote) = remember { mutableStateOf<ApiResult<T>?>(null) }

        when {
            local == null -> {
                Progress()
                getLocal(setLocal)
            }
            local.isSuccess -> {
                screen(local.getOrNull()!!)
            }
            remote == null -> {
                PleaseWait()
                Effect.Once {
                    getRemote(setRemote)
                }
            }
            remote is ApiResult.Error -> {
                val p = errorHandler(remote.code, remote.message)

                if (p.second) {
                    RefreshableError(p.first) { stopRefresh ->
                        getRemote { result ->
                            stopRefresh()
                            setRemote(result)
                        }
                    }
                } else {
                    BasicMessage(message = p.first ?: "Unrecoverable error, please press Back.")
                }
            }
            else -> {
                screen(remote.data!!)
            }
        }
    }
}