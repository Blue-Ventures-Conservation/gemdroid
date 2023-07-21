package org.blueventures.gemdroid.model.analysis.classification

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import org.blueventures.gemdroid.data.ROI
import org.blueventures.gemdroid.model.analysis.classification.separability.SeparabilityViewModel
import org.blueventures.gemdroid.model.analysis.cra.CRAAwaiter
import org.blueventures.gemdroid.model.api.ApiViewModel
import java.io.File

class ClassificationViewModel(
    private val repo: ClassificationRepository = ClassificationRepository()
): ApiViewModel(repo) {
    lateinit var sepViewModel: SeparabilityViewModel
    lateinit var craAwaiter: CRAAwaiter

    var roiDir = File("")
        set(value) {
            field = value
            sepViewModel.roiDir = value
        }

    var roi = ROI()
        set(value) {
            field = value
            sepViewModel.roi = value
        }

    fun init(activity: ComponentActivity, awaiter: CRAAwaiter) {
        sepViewModel = activity.viewModels<SeparabilityViewModel>().value
        sepViewModel.init(awaiter)
        craAwaiter = awaiter
    }
}