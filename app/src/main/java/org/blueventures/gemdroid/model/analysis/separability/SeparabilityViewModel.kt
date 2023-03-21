package org.blueventures.gemdroid.model.analysis.separability

import kotlinx.coroutines.Job
import org.blueventures.gemdroid.data.CRA
import org.blueventures.gemdroid.model.api.ApiViewModel
import java.io.File

class SeparabilityViewModel(private val repo: SeparabilityRepository = SeparabilityRepository()): ApiViewModel(repo) {
    var roiDir = File("")

    private var loadCRAsJob: Job? = null

    fun loadCRAs(callback: (Result<CRA>) -> Unit) {
        if (loadCRAsJob != null) {
            resultWithToken({ loadCRAsJob =  it }, repo.loadCRAs(roiDir)) { result ->
                loadCRAsJob = null
                callback(result)
            }
        }
    }
}