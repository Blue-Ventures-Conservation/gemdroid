package org.blueventures.gemdroid.model.analysis.classification

import org.blueventures.gemdroid.model.api.ApiViewModel

class ClassificationViewModel(
    private val repo: ClassificationRepository = ClassificationRepository()
): ApiViewModel(repo) {
}