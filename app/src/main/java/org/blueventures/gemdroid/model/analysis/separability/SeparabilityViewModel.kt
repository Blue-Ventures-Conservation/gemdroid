package org.blueventures.gemdroid.model.analysis.separability

import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.model.api.ApiViewModel
import java.io.File

class SeparabilityViewModel(private val repo: SeparabilityRepository = SeparabilityRepository()): ApiViewModel(repo) {
    var roiDir = File("")

    fun loadCRAs(callback: (Result<CRA>) -> Unit) {
        withToken({ repo.loadCRAs(roiDir, callback) }, callback)
    }
}