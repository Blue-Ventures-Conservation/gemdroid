package org.blueventures.gemdroid.model.settings

import android.content.Context
import org.blueventures.gemdroid.model.api.ApiViewModel
import org.blueventures.gemdroid.model.settings.SettingsDatasource.Companion.forceLandsat
import org.blueventures.gemdroid.model.settings.SettingsDatasource.Companion.welcomeShown

class SettingsViewModel(repo: SettingsRepository = SettingsRepository()): ApiViewModel(repo) {
    fun getState(context: Context, callback: (State) -> Unit) = read(context, { prefs ->
        State(
            forceLandsat.read(prefs),
        )
    }, callback)

    fun getWelcomeShown(context: Context, callback: (Boolean) -> Unit) = read(context, welcomeShown.key, welcomeShown.default, callback)

    fun setForceLandsat(context: Context, force: Boolean) = write(context, forceLandsat.key, force)
    fun setWelcomeShown(context: Context, shown: Boolean) = write(context, welcomeShown.key, shown)
}