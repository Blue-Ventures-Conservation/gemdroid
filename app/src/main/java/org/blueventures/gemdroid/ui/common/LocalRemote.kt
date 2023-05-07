package org.blueventures.gemdroid.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.github.zibnix.droidbones.api.ApiResult

@Composable
fun <T> LocalRemote(
    getLocal: ((Result<T>) -> Unit) -> Unit,
    getRemote: ((ApiResult<T>) -> Unit) -> Unit,
    save: (T) -> Unit,
    errorHandler: (Int?, String?) -> Pair<String?, Boolean> = { _, _ -> Pair(null, true) }, // returns a message to display and whether or not the request should be retried
    success: @Composable (T) -> Unit,
) {
    val (local, setLocal) = remember { mutableStateOf<Result<T>?>(null) }
    val (remote, setRemote) = remember { mutableStateOf<ApiResult<T>?>(null) }

    when {
        local == null -> {
            Progress()
            getLocal(setLocal)
        }
        local.isSuccess -> {
            success(local.getOrNull()!!)
        }
        remote == null -> {
            PleaseWait()
            LaunchedEffect(key1 = true) {
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
            val dat = remote.data!!
            save(dat)
            success(dat)
        }
    }
}