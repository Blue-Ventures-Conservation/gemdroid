package org.blueventures.gemdroid.model.roi

import com.github.zibnix.droidbones.mvvm.FileService
import org.blueventures.gemdroid.data.ROI
import java.io.File

class RoiDatasource() {
    /**
     * Each ROI has its own subdir in the ROI dir.
     *
     * Within an ROI specific subdir, there should be a json file of the coarse ROI polygon
     * as a Feature, with properties that include the historical and contemporary date ranges
     * (year range & month range).
     *
     * In addition, there may be cached tiles for the 4 possible sets used by the classifier:
     * High Tide Contemporary
     * Low Tide Contemporary
     * High Tide Historical
     * Low Tide Historical
     *
     * As well as tiles for the combine classification (both historical and contemporary) and
     * tiles for the persistence, loss and gain.
     *
     * In addition there may also be serialized representations of the backend computed objects from GEE
     * for all nine of the image sets that we might store tiles for.
     * The purpose of keeping these is to fetch new tile URLs without asking the user to go through the
     * whole flow again.
     *
     * We should also hold onto calculations for area of mangrove gain, loss, persistence and the
     * classification (mangrove) area calculations for the historical and contemporary data sets.
     *
     * At max resolution with a 5km buffer size, the 2022 training materials produce contemporary images
     * around 35MB in size and historical images around 22MB in size, when exporting just the 3 bands used
     * for visualization (B4, B3, B2). The combine classification images are about 1MB and the
     * gain/loss/persistence images are around 500KB. So, if we cache at full resolution:
     *
     * 2x35 (low and high tide) + 2x22 (high and low tide) + 2 (classified) + 1.5 (g/l/p)
     *                      = ~120MB of imagery
     * for a coarse ROI the size of the 2022 training materials (5,656,185,247m² or 5,656 km²).
     *
     * This is a minimal set of full resolution data that likely isn't actually ideal because the buffer
     * size is a little too small to capture all the surrounding mangroves, and it is still too much data
     * to ask our users to download to their phones.
     *
     * All this taken together indicates we should stick to caching tiles and not full resolution imagery,
     * in the hope that the user will only have to download a small set of tiles to get the coverage they
     * are interested in.
     *
     * When users create a polygon on the phone, we will reject it if the area is too big. Right now, a good
     * size seems to be less than ~6,000 km², based on the 2022 training materials coarse ROI.
     *
     * With testing we might try to bump that to 10,000 km² for a nice round number.
     */
    fun getRois(filesDir: File): List<File> {
        return try {
            val roiDir = File(filesDir, dirname)

            if (roiDir.exists() || roiDir.mkdirs()) {
                FileService.getSubdirs(roiDir)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveRoi(filesDir: File, roi: RoiState): Boolean {
        return try {
            val roiDir = File(filesDir, "$dirname${FileService.sep}${roi.name}")

            if (roiDir.exists() || roiDir.mkdirs()) {
                ROI.toFile(File(roiDir, filename), ROI.fromState(roi))
            } else {
                false
            }
        } catch(e: Exception) {
            false
        }
    }

    fun deleteRoi(dir: File) = FileService.deleteDir(dir)

    companion object {
        const val filename = "roi.json"
        const val dirname = "rois"
    }
}