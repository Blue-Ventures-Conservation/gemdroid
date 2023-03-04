package org.blueventures.gemdroid.model.separability

import com.github.zibnix.droidbones.mvvm.BaseViewModel
import org.blueventures.gemdroid.data.CRA
import java.io.File

class SeparabilityViewModel(private val repo: SeparabilityRepository = SeparabilityRepository()): BaseViewModel() {
    var roiDir = File("")

    fun loadCRAs(callback: (Result<CRA>) -> Unit) = scoped { repo.loadCRAs(roiDir).collect(callback) }
}