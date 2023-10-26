package org.blueventures.gemdroid.model.settings

import com.github.zibnix.droidbones.mvvm.IORepository

class SettingsRepository(
    private val datasource: SettingsDatasource = SettingsDatasource(),
): IORepository() {
}