package org.blueventures.gemdroid.model.settings

import org.blueventures.gemdroid.model.api.ApiRepository

class SettingsRepository(
    private val datasource: SettingsDatasource = SettingsDatasource(),
): ApiRepository() {
}