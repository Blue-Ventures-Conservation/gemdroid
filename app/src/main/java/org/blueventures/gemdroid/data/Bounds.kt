package org.blueventures.gemdroid.data

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

object Bounds {
    fun centerFromList(list: List<LatLng>): LatLng? {
        if (list.isEmpty()) {
            return null
        }

        val builder = LatLngBounds.builder()
        for (pt in list) {
            builder.include(pt)
        }

        return builder.build().center
    }
}