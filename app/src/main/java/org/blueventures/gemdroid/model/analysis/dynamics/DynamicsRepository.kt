package org.blueventures.gemdroid.model.analysis.dynamics

import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File
import java.io.InputStream

class DynamicsRepository(
    private val datasource: DynamicsDatasource = DynamicsDatasource(),
): ApiRepository(datasource) {
    fun validateShapefile(dynamicsDir: File, files: List<InputStream?>, names: List<String?>) = goFlow { datasource.validateShapefile(dynamicsDir, files, names) }
}