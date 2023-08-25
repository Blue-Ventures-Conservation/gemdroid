package org.blueventures.gemdroid.ui.common

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import java.io.InputStream

typealias StreamValidator<T> = (Shapefile.Streams, (Result<T>?) -> Unit) -> Job

object Shapefile {
    data class Streams(val streams: List<InputStream?>, val names: List<String?>)

    @Composable
    fun <T> Screen(title: String, validator: StreamValidator<T>, failure: (String) -> Unit, success: (T) -> Unit) {
        val (streams, setStreams) = remember { mutableStateOf<Streams?>(null) }
        val (validation, setValidation) = remember { mutableStateOf<Result<T>?>(null) }

        when (streams) {
            null -> GetStreams(title, setStreams)
            else -> {
                Progress()
                when {
                    validation == null -> validator(streams, setValidation)
                    validation.isFailure -> {
                        failure(validation.exceptionOrNull()!!.localized(LocalContext.current))
                        setValidation(null)
                        setStreams(null)
                    }
                    validation.isSuccess -> success(validation.getOrNull()!!)
                }
            }
        }
    }

    @Composable
    fun GetStreams(title: String, setStreams: (Streams?) -> Unit) {
        val context = LocalContext.current.applicationContext
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { files ->
            val strms = mutableListOf<InputStream?>()
            val names = mutableListOf<String?>()
            files.forEach { uri ->
                strms.add(context.contentResolver.openInputStream(uri))
                names.add(contentDisplayName(context, uri))
            }
            setStreams(Streams(strms, names))
        }

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
            Text(modifier = Modifier.fillMaxWidth(), text = stringResource(R.string.you_should_select_all_of), fontSize = 16.sp, textAlign = TextAlign.Center)
            Text(modifier = Modifier.fillMaxWidth(), text = stringResource(R.string.shp_file_extensions), fontSize = 16.sp, textAlign = TextAlign.Center)
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.or_you_can_select_a_zip),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(0.dp))
        Butt.Text(stringResource(R.string.select_shapefile)) {
            launcher.launch(arrayOf("*/*"))
        }
    }

    private fun contentDisplayName(context: Context, uri: Uri): String? {
        var name: String? = null

        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.let { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                name = cursor.getString(nameIndex)
                cursor.close()
            }
        }

        return name
    }
}