package org.blueventures.gemdroid.model.settings

import com.github.zibnix.droidbones.mvvm.IOViewModel

class SettingsViewModel(private val repo: SettingsRepository = SettingsRepository()): IOViewModel(repo) {
}