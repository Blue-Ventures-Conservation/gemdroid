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

        // rotation resend these requests, because this is poor design with compose
        // and yet, I should be free to automatically schedule things on other threads
        // that ultimately affect the UI without the user's constant interaction, so here we are
        Orient.Portrait()

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
                PleaseWait()
                Effect.Once {
                    getRemote(setRemoteResult)
                }
            }
            remoteResult is ApiResult.Error -> {
                if (remoteResult.code == HttpURLConnection.HTTP_FORBIDDEN) {
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
                setRemote(remoteResult.data!!)
            }
        }
    }
}