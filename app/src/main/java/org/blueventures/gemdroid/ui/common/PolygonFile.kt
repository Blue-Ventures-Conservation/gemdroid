package org.blueventures.gemdroid.ui.common

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.zibnix.droidbones.localized
import org.blueventures.gemdroid.R
import java.io.InputStream

typealias StreamValidator<T> = (List<Uri>, (Result<T>) -> Unit) -> Unit

object PolygonFile {
    data class Streams(val streams: List<InputStream?>, val names: List<String?>)

    @Composable
    fun <T> Screen(title: String, validator: StreamValidator<T>, failure: (String) -> Unit, success: (T) -> Unit) {
        val ctx = LocalContext.current
        val resultHandler: (Result<T>?) -> Unit = { result ->
            if (result != null) {
                when {
                    result.isSuccess -> success(result.getOrNull()!!)
                    else -> failure(result.exceptionOrNull()!!.localized(ctx))
                }
            }
        }

        Result(title, validator, resultHandler) {}
    }

    @Composable
    fun <T> Result(title: String, validator: StreamValidator<T>, result: (Result<T>) -> Unit, progress: () -> Unit = {}) {
        val (uris, setUris) = remember { mutableStateOf<List<Uri>?>(null) }

        when (uris) {
            null -> GetUris(title, setUris)
            else -> Validation(validator, uris, result, setUris, progress)
        }
    }

    @Composable
    fun <T> Validation(validator: StreamValidator<T>, uris: List<Uri>, result: (Result<T>) -> Unit, setUris: (List<Uri>?) -> Unit, progress: () -> Unit = {}) {
        Progress()
        Effect.Once {
            validator(uris) { res ->
                if (res.isFailure) {
                    setUris(null)
                }
                result(res)
            }
            progress()
        }
    }

    @Composable
    fun GetUris(title: String, setUris: (List<Uri>?) -> Unit) {
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments(), setUris)

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.you_should_select_all_of),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.shp_file_extensions),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.or_you_can_select_a_zip),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(0.dp))
        Butt.Text(stringResource(R.string.select_polygon_file)) {
            launcher.launch(arrayOf("application/zip", "application/octet-stream", "x-gis/x-shapefile", "application/text"))
        }
    }
}