package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import org.blueventures.gemdroid.R

@ExperimentalPermissionsApi
@Composable
fun RequestPermission(
    permission: String,
    rationale: String,
    description: String,
    optional: Boolean = false,
    content: @Composable (Boolean) -> Unit
) {
    val perm = rememberPermissionState(permission)
    var rationaleShown by remember { mutableStateOf(false) }

    if (perm.status.isGranted) {
        content(true)
    } else {
        if (rationaleShown && optional && !perm.status.shouldShowRationale) {
            content(false)
        } else {
            Col.BigPad {
                Spacer(modifier = Modifier.height(0.dp))
                Text(text = if (perm.status.shouldShowRationale) {
                    rationaleShown = true
                    rationale
                } else { description }, fontSize = 24.sp, textAlign = TextAlign.Center)
                Butt.Text(stringResource(R.string.grant_permission)) {
                    perm.launchPermissionRequest()
                }
            }
        }
    }
}