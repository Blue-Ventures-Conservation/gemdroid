package org.blueventures.gemdroid.model.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import org.blueventures.gemdroid.BuildConfig
import org.blueventures.gemdroid.model.api.ApiDatasource
import org.blueventures.gemdroid.model.api.KeyValue

class SettingsDatasource: ApiDatasource() {
    companion object {
        private val forceLandsatKey = booleanPreferencesKey("force_landsat_key")
        private const val forceLandsatDefault = true
        val forceLandsat = KeyValue(forceLandsatKey, forceLandsatDefault)

        const val versionString = BuildConfig.VERSION_NAME
    }
}

data class State(
    val forceLandsat: Boolean,
)