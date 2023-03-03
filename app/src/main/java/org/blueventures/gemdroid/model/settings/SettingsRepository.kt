package org.blueventures.gemdroid.model.settings

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class SettingsRepository(
    private val datasource: SettingsDatasource = SettingsDatasource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
}