package org.blueventures.gemdroid.model.analysis.cra

import org.blueventures.gemdroid.data.GeojsonPolygonFeature
import org.blueventures.gemdroid.data.analysis.cra.ContemporaryAndHistoricalCRAs
import org.blueventures.gemdroid.data.analysis.cra.LocalOrRemoteCRAFile
import org.blueventures.gemdroid.model.api.ApiRepository
import java.io.File
import java.io.InputStream

class CRARepository(
    private val datasource: CRADatasource = CRADatasource(),
): ApiRepository(datasource) {
    fun getRemoteCRAs() = goFlow { datasource.getRemoteCRAs() }
    fun validateLocalCRA(roiDir: File, files: List<InputStream?>, names: List<String?>, remoteCRAs: List<String>, previous: String?, overwrite: Boolean) = goFlow { datasource.validateLocalCRA(roiDir, files, names, remoteCRAs, previous, overwrite) }
    fun getCRAFields(hist: LocalOrRemoteCRAFile?, cont: LocalOrRemoteCRAFile) = goFlow { datasource.getCRAFields(hist, cont) }
    fun convertToCSVAndZip(roiDir: File, roiName: String, features: List<GeojsonPolygonFeature>) = goFlow { datasource.convertToCSVAndZip(roiDir, roiName, features) }
    fun deleteCreationDir(roiDir: File) = goFlow { datasource.deleteCreationDir(roiDir) }
    fun uploadCRA(cra: LocalOrRemoteCRAFile) = goFlow { datasource.uploadCRA(cra) }
    fun uploadCRAs(c1: LocalOrRemoteCRAFile, c2: LocalOrRemoteCRAFile) = goFlow { datasource.uploadCRAs(c1, c2) }
    fun ingestCRA(cra: LocalOrRemoteCRAFile) = goFlow { datasource.ingestCRA(cra) }
    fun ingestCRAs(c1: LocalOrRemoteCRAFile, c2: LocalOrRemoteCRAFile) = goFlow { datasource.ingestCRAs(c1, c2) }
    fun uploadOneIngestOther(toUpload: LocalOrRemoteCRAFile, toIngest: LocalOrRemoteCRAFile) = goFlow { datasource.uploadOneIngestOther(toUpload, toIngest) }
    fun saveCRAs(roiDir: File, hist: LocalOrRemoteCRAFile?, cont: LocalOrRemoteCRAFile) = goFlow { datasource.saveCRAs(roiDir, hist, cont) }
    fun loadCRAs(roiDir: File) = goFlow { datasource.loadCRAs(roiDir) }
    fun shouldAwaitCRAs(roiDir: File) = goFlow { datasource.shouldAwaitCRAs(roiDir) }
    fun awaitCRAs(roiDir: File, cras: ContemporaryAndHistoricalCRAs) = goFlow { datasource.awaitCRAs(roiDir, cras) }
}