package com.github.zibnix.droidbones.location

/**
 * Waits are in millis, accuracies are in meters, if either value for gps, or either value for network is negative,
 * then those values are ignored and that provider is not asked for locations.
 */
data class RequestParams(
    val maxAge: Long,
    val gpsWait: Long = -1,
    val gpsAccuracy: Float = -1f,
    val networkWait: Long = -1,
    val networkAccuracy: Float = -1f
) {
    val isGPS: Boolean
        get() = gpsWait >= 0 && gpsAccuracy >= 0

    val isNetwork: Boolean
        get() = networkWait >= 0 && networkAccuracy >= 0
}