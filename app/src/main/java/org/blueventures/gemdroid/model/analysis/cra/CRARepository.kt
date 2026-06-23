package org.blueventures.gemdroid.model.analysis.cra

import org.blueventures.gemdroid.data.ContemporaryAndHistoricalCRAs
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File
import java.io.InputStream

class CRARepository(
    private val datasource: CRADatasource = CRADatasource(),
): ApiRepository(datasource) {
    fun getRemoteCRAs() = goFlow { datasource.getRemoteCRAs() }
    fun validateLocalCRA(roiDir: File, files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?, overwrite: Boolean) = goFlow { datasource.validateLocalCRA(roiDir, files, names, remoteCRAs, previous, overwrite) }
    fun getCRAFields(hist: CRAFile?, cont: CRAFile) = goFlow { datasource.getCRAFields(hist, cont) }
    fun uploadCRA(cra: CRAFile) = goFlow { datasource.uploadCRA(cra) }
    fun uploadCRAs(c1: CRAFile, c2: CRAFile) = goFlow { datasource.uploadCRAs(c1, c2) }
    fun ingestCRA(cra: CRAFile) = goFlow { datasource.ingestCRA(cra) }
    fun ingestCRAs(roiDir: File, c1: CRAFile, c2: CRAFile) = goFlow { datasource.ingestCRAs(roiDir, c1, c2) }
    fun uploadFields(cra: CRAFile) = goFlow { datasource.uploadFields(cra) }
    fun uploadFields(c1: CRAFile, c2: CRAFile) = goFlow { datasource.uploadFields(c1, c2) }
    fun saveCRAs(roiDir: File, cras: ContemporaryAndHistoricalCRAs) = goFlow { datasource.saveCRAs(roiDir, cras) }
    fun loadCRAs(roiDir: File) = goFlow { datasource.loadCRAs(roiDir) }
    fun shouldAwaitCRAs(roiDir: File) = goFlow { datasource.shouldAwaitCRAs(roiDir) }
    fun awaitCRAs(roiDir: File, cras: ContemporaryAndHistoricalCRAs) = goFlow { datasource.awaitCRAs(roiDir, cras) }
}