package org.blueventures.gemdroid.model.settings

import android.content.Context
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.model.settings.SettingsDatasource.Companion.forceLandsat

class SettingsViewModel(repo: SettingsRepository = SettingsRepository()): ApiViewModel(repo) {
    fun getState(context: Context, callback: (State) -> Unit) = read(context, { prefs ->
        State(
            forceLandsat.read(prefs)
        )
    }, callback)

    fun setForceLandsat(context: Context, force: Boolean) = write(context, forceLandsat.key, force)
}