package org.blueventures.gemdroid.ui.common.maps

import android.Manifest
import android.content.Context
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.intPreferencesKey
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapType
import kotlinx.coroutines.Job
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.data.URLs
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.ui.common.AppBar
import org.blueventures.gemdroid.ui.common.Click
import org.blueventures.gemdroid.ui.common.RequestPermission

object Maps {
    interface Storage {
        fun getMapType(context: Context, callback: (MapType) -> Unit): Job
        fun setMapType(context: Context, mapType: MapType): Job

        companion object {
            val mapTypeKey = intPreferencesKey("map_type_key")

            fun fromViewModel(viewModel: ApiViewModel) = object : Storage {
                override fun getMapType(context: Context, callback: (MapType) -> Unit) = viewModel.read(context, mapTypeKey, MapType.HYBRID.value) { callback(convert(it)) }
                override fun setMapType(context: Context, mapType: MapType) = viewModel.write(context, mapTypeKey, mapType.value)
            }

            private fun convert(from: Int): MapType {
                for (t in MapType.values()) {
                    if (t.value == from) {
                        return t
                    }
                }

                return MapType.HYBRID
            }
        }
    }

    @Composable
    fun NoLayers(
        appBar: AppBar,
        title: String,
        attemptGps: Boolean = false,
        center: LatLng? = null,
        storage: Storage? = null,
        draw: Draw.Model? = null,
        poly: Poly.Model? = null,
        floating: @Composable BoxScope.() -> Unit = {},
    ) {
        Screen<URLs>(appBar, title, attemptGps, center, storage, null, draw, poly, floating)
    }

    /**
     * The layers argument to Screen should be Layers that outlive composition, such as static fields on an object for example.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun <T : URLs> Screen(
        appBar: AppBar,
        title: String,
        attemptGps: Boolean = false,
        center: LatLng? = null,
        storage: Storage? = null,
        layers: Layers.Model<T>? = null,
        draw: Draw.Model? = null,
        poly: Poly.Model? = null,
        floating: @Composable BoxScope.() -> Unit = {},
    ) {
        if (attemptGps) {
            RequestPermission(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                rationale = stringResource(R.string.gps_rationale),
                description = stringResource(R.string.gps_rationale_description),
                optional = true
            ) { granted ->
                Compose.Screen(appBar, title, granted, center, storage, layers, draw, poly, floating)
            }
        } else {
            Compose.Screen(appBar, title, false, center, storage, layers, draw, poly, floating)
        }
    }

    @Composable
    fun BoxScope.MapActionButton(click: Click, align: Alignment = Alignment.BottomEnd, content: @Composable () -> Unit) {
        FloatingActionButton(click, modifier = Modifier
            .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 48.dp)
            .align(align),
            content = content,
        )
    }
}