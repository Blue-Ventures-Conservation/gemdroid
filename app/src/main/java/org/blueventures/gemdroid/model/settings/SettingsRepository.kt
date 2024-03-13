package org.blueventures.gemdroid.model.settings

import org.blueventures.gemdroid.model.api.ApiRepository

class SettingsRepository(
    datasource: SettingsDatasource = SettingsDatasource(),
): ApiRepository(datasource)