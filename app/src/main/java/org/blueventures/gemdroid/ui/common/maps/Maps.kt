package org.blueventures.gemdroid.ui.common.maps

import android.Manifest
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.URLs
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.RequestPermission

object Maps {
    /**
     * The layers argument to Screen should be Layers that outlive composition, such as static fields on an object for example.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun <T : URLs> Screen(
        floating: @Composable BoxScope.() -> Unit = {},
        attemptGps: Boolean = false,
        tiles: Tiles.Model<T>? = null,
        draw: Draw.Model? = null,
        poly: Poly.Model? = null,
    ) {
        if (attemptGps) {
            RequestPermission(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                rationale = stringResource(R.string.gps_rationale),
                description = stringResource(R.string.gps_rationale_description),
                optional = true
            ) { granted ->
                Compose.Screen(granted, tiles, draw, poly, floating)
            }
        } else {
            Compose.Screen(false, tiles, draw, poly, floating)
        }
    }

    @Composable
    fun BoxScope.MapActionButton(click: Click, content: @Composable () -> Unit) {
        FloatingActionButton(click, modifier = Modifier
            .padding(bottom = 64.dp, end = 24.dp)
            .align(Alignment.BottomEnd),
            content = content,
        )
    }
}