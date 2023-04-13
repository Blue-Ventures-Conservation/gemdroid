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
            RefreshableError { stopRefresh ->
                getRemote { result ->
                    stopRefresh()
                    setRemote(result)
                }
            }
        }
        else -> {
            val dat = remote.data!!
            save(dat)
            success(dat)
        }
    }
}