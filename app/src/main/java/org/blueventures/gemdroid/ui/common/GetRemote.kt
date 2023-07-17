package org.blueventures.gemdroid.ui.common

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.api.ApiResult
import org.blueventures.gemdroid.R
import java.net.HttpURLConnection

object GetRemote {
    @Composable
    fun <T> Save(
        getLocal: ((Result<T>) -> Unit) -> Unit,
        getRemote: ((ApiResult<T>) -> Unit) -> Unit,
        save: (T) -> Unit,
        // returns a message to display and whether or not the request should be retried
        errorHandler: (Int?, String?) -> Pair<String?, Boolean> = { _, _ -> Pair(null, true) },
        screen: @Composable (T) -> Unit) {
        Display(getLocal, getRemote, errorHandler) { dat ->
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
                Display(getLocal, getRemote, errorHandler) { dat ->
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
    fun <T> Display(
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
                if (remote.code == HttpURLConnection.HTTP_FORBIDDEN) {
                    BasicMessage(message = stringResource(R.string.access_email_rationale)) { context ->
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:") // Only email apps handle this.
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("bv.gemapp@gmail.com"))
                            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.access_email_subject))
                            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.access_email_body))
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            val chooser = Intent.createChooser(intent, null)
                            context.startActivity(chooser)
                        }
                    }
                } else {
                    val p = errorHandler(remote.code, remote.message)

                    if (p.second) {
                        RefreshableError(p.first) { stopRefresh ->
                            getRemote { result ->
                                stopRefresh()
                                setRemote(result)
                            }
                        }
                    } else {
                        BasicMessage(message = p.first ?: stringResource(R.string.unrecoverable_error))
                    }
                }
            }
            else -> {
                screen(remote.data!!)
            }
        }
    }
}