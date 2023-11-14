package org.blueventures.gemdroid.model.settings

import org.blueventures.gemdroid.model.api.ApiViewModel

class SettingsViewModel(private val repo: SettingsRepository = SettingsRepository()): ApiViewModel(repo) {
}