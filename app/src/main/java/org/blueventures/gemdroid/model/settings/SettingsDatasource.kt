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

        private val welcomeShownKey = booleanPreferencesKey("welcome_shown_key")
        private const val welcomeShownDefault = false
        val welcomeShown = KeyValue(welcomeShownKey, welcomeShownDefault)

        const val versionString = BuildConfig.VERSION_NAME
        const val bvUri = "https://blueventures.org/"
        const val userManualUri = "https://docs.google.com/document/d/1dD9Lqv61NUfSxZ5SQouypVtePro6xCVXEvhJj2G3DfI/"
        const val publicationUri = "https://blueventures.org/publications/the-google-earth-engine-mangrove-mapping-methodology-geemmm/"
        const val desktopUri = "https://github.com/Blue-Ventures-Conservation/GEM/"
    }
}

data class State(
    val forceLandsat: Boolean,
)