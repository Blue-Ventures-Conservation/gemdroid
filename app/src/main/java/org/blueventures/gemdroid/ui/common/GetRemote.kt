package org.blueventures.gemdroid.ui.common

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.zibnix.droidbones.NoStack
import com.github.zibnix.droidbones.api.ApiResult
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.Expires
import org.blueventures.gemdroid.data.staleCheck
import java.net.HttpURLConnection

/**
 * Functions in this file will load from the network when loading locally fails.
 *
 * This means that even though there may be locally cached data, when the API data changes, that local file load
 * can fail and we'll just fetch the updated format from the server without showing errors to the user.
 */
object GetRemote {
    @Composable
    fun <T> Save(
        getLocal: ((Result<T>) -> Unit) -> Unit,
        getRemote: ((ApiResult<T>) -> Unit) -> Unit,
        save: (T) -> Unit,
        checkExpires: Boolean = false,
        // returns a message to display and whether or not the request should be retried
        errorHandler: RemoteErrHandler = { _, _, _ -> Pair(null, true) },
        screen: @Composable (T) -> Unit) {
        Display(getLocal, getRemote, checkExpires, errorHandler, screen) { dat ->
            save(dat)
            screen(dat)
        }
    }

    @Composable
    fun <T> AwaitSave(
        getLocal: ((Result<T>) -> Unit) -> Unit,
        getRemote: ((ApiResult<T>) -> Unit) -> Unit,
        save: (T, (Result<Unit>) -> Unit) -> Unit,
        checkExpires: Boolean = false,
        // returns a message to display and whether or not the request should be retried
        errorHandler: RemoteErrHandler = { _, _, _ -> Pair(null, true) },
        saveFail: ((T) -> Unit)? = null,
        screen: @Composable (T) -> Unit
    ) {
        val (saved, setSaved) = remember { mutableStateOf<T?>(null) }

        when (saved) {
            null -> {
                Display(getLocal, getRemote, checkExpires, errorHandler, { dat ->
                    screen(dat)
                }) { dat ->
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
        checkExpires: Boolean = false,
        // returns a message to display and whether or not the request should be retried
        errorHandler: RemoteErrHandler = { _, _, _ -> Pair(null, true) },
        setLocal: @Composable (T) -> Unit,
        setRemote: @Composable (T) -> Unit,
    ) {
        val (localResult, setLocalResult) = remember { mutableStateOf<Result<T>?>(null) }
        val (remoteResult, setRemoteResult) = remember { mutableStateOf<ApiResult<T>?>(null) }

        Orient.Unspecified()

        when {
            localResult == null -> {
                Progress()
                getLocal(setLocalResult)
            }
            localResult.isSuccess -> {
                val data = localResult.getOrNull()!!
                if (checkExpires && data is Expires && staleCheck(data)) {
                    setLocalResult(Result.failure(NoStack(R.string.expired)))
                } else {
                    setLocal(localResult.getOrNull()!!)
                }
            }
            remoteResult == null -> {
                // Forcing portrait here is a cheap hack to prevent resending these request on screen rotation.
                // Really the model should keep requests alive across screen rotations.
                // That would mean no longer canceling the coroutine job in ApiViewModel.
                // It would also likely require reestablishing the connection to the UI by calling the latest
                // callback function which would have to be provided to the model after rotation, possibly as
                // a property on the viewmodel.
                // In addition, showing the correct screen state (spinner) after rotation might require a bit
                // more tracking of state indicating the request is outstanding and active.
                Orient.Portrait()

                PleaseWait()
                Effect.Once {
                    getRemote(setRemoteResult)
                }
            }
            remoteResult is ApiResult.Error -> {
                if (remoteResult.code == HttpURLConnection.HTTP_FORBIDDEN) {
                    BasicMessage(message = stringResource(R.string.access_email_rationale)) { context ->
                        context.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:") // Only email apps handle this.
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("bv.gemapp@gmail.com"))
                            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.access_email_subject))
                            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.access_email_body))
                        })
                    }
                } else {
                    val p = errorHandler(LocalContext.current, remoteResult.code, remoteResult.message)

                    if (p.second) {
                        RefreshableError(p.first) { stop ->
                            getRemote { result ->
                                stop()
                                setRemoteResult(result)
                            }
                        }
                    } else {
                        BasicMessage(message = p.first ?: stringResource(R.string.unrecoverable_error))
                    }
                }
            }
            else -> {
                val data = remoteResult.data!!
                setLocalResult(Result.success(data))
                setRemote(data)
            }
        }
    }
}