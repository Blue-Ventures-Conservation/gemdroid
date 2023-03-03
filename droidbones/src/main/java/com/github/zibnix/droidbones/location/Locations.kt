package com.github.zibnix.droidbones.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import com.github.zibnix.droidbones.permissions.Perms
import kotlin.math.abs
import kotlin.math.floor

object Locations {
    private const val FINE = Manifest.permission.ACCESS_FINE_LOCATION
    private const val COARSE = Manifest.permission.ACCESS_COARSE_LOCATION
    private const val GPS_PROVIDER = LocationManager.GPS_PROVIDER
    private const val NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER
    private const val PASSIVE_PROVIDER = LocationManager.PASSIVE_PROVIDER

    private val providers = arrayOf(GPS_PROVIDER, NETWORK_PROVIDER, PASSIVE_PROVIDER)

    fun isGPSEnabled(context: Context): Boolean {
        return (context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)?.isProviderEnabled(
            GPS_PROVIDER) ?: false
    }

    fun isNetworkEnabled(context: Context): Boolean {
        return (context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)?.isProviderEnabled(
            NETWORK_PROVIDER) ?: false
    }

    /**
     * We typically don't care about the passive provider.
     *
     * If this returns false, prompt the user to enable location using the LocationDisabledDialog class:
     *      LocationDisabledDialog.show(supportFragmentManager)
     */
    fun isLocationEnabled(context: Context): Boolean {
        return isGPSEnabled(context) && isNetworkEnabled(context)
    }

    fun isPassiveEnabled(context: Context): Boolean {
        return (context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)?.isProviderEnabled(
            PASSIVE_PROVIDER) ?: false
    }

    fun hasFinePermission(context: Context): Boolean {
        return Perms.has(context, FINE)
    }

    fun hasCoarsePermission(context: Context): Boolean {
        return Perms.has(context, COARSE)
    }

    fun hasBothPermission(context: Context): Boolean {
        return hasFinePermission(context) && hasCoarsePermission(context)
    }

    fun requestFine(host: Fragment, rationale: String, callback: (granted: Boolean) -> Unit) {
        request(host, arrayOf(FINE), rationale, callback)
    }

    fun requestCoarse(host: Fragment, rationale: String, callback: (granted: Boolean) -> Unit) {
        request(host, arrayOf(COARSE), rationale, callback)
    }

    fun requestBoth(host: Fragment, rationale: String, callback: (granted: Boolean) -> Unit) {
        request(host, arrayOf(FINE, COARSE), rationale, callback)
    }

    private fun request(host: Fragment, permissions: Array<String>, rationale: String, callback: (granted: Boolean) -> Unit) {
        val launcher = Perms.registerMultipleReduce(host) { granted ->
            callback(granted)
        }

        Perms.rationale(host.requireActivity(), permissions, rationale) { accepted ->
            if (accepted) {
                Perms.request(launcher, *permissions)
            } else {
                callback(false)
            }
        }
    }

    /**
     * It is assumed that a permissions check for GPS, Network or both, as necessary, has already been performed
     * and was successful.
     *
     * maxAge in millis
     */
    fun getLocation(params: RequestParams, manager: LocationManager, callback: (Location?) -> Unit) {
        bestKnown(params.maxAge, manager)?.let { location ->
            callback(location)
            return@getLocation
        }

        when {
            !params.isGPS && !params.isNetwork -> callback(null)
            params.isGPS && !params.isNetwork -> gpsRequest(params, manager, callback)
            !params.isGPS && params.isNetwork -> networkRequest(params, manager, callback)
            params.isGPS && params.isNetwork -> {
                var loc: Location? = null
                var locCount = 0

                val cb: (Location?) -> Unit = { next ->
                    locCount++

                    if (locCount <= 1) {
                        loc = next
                    } else {
                        loc?.let { locit ->
                            next?.let { nextit ->
                                if (locit.accuracy <= nextit.accuracy) {
                                    callback(locit)
                                } else {
                                    callback(nextit)
                                }
                            } ?: run {
                                callback(locit)
                            }
                        } ?: run {
                            callback(next)
                        }
                    }
                }

                gpsRequest(params, manager, cb)
                networkRequest(params, manager, cb)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun bestKnown(maxAge: Long, manager: LocationManager): Location? {
        val now = System.currentTimeMillis()
        var location: Location? = null

        for (provider in providers) {
            manager.getLastKnownLocation(provider)?.let { loc ->
                if (now - loc.time < maxAge && loc.accuracy < (location?.accuracy ?: Float.MAX_VALUE)) {
                    location = loc
                }
            }
        }

        return location
    }

    @SuppressLint("MissingPermission")
    private fun gpsRequest(params: RequestParams, manager: LocationManager, callback: (Location?) -> Unit) {
        lateinit var listener: Listener
        listener = Listener(params.gpsWait, params.gpsAccuracy) { location ->
            manager.removeUpdates(listener)
            callback(location)
        }

        manager.requestLocationUpdates(GPS_PROVIDER, 0L, 0f, listener)
    }

    @SuppressLint("MissingPermission")
    private fun networkRequest(params: RequestParams, manager: LocationManager, callback: (Location?) -> Unit) {
        lateinit var listener: Listener
        listener = Listener(params.networkWait, params.networkAccuracy) { location ->
            manager.removeUpdates(listener)
            callback(location)
        }

        manager.requestLocationUpdates(NETWORK_PROVIDER, 0L, 0f, listener)
    }

    fun decimalToDMS(decimal: Double): String {
        val dec = abs(decimal)
        val degrees = floor(dec).toInt()
        val minutes = floor((dec - degrees.toDouble()) * 60).toInt()
        val seconds = ((dec - degrees.toDouble() - (minutes.toDouble()/60)) * 3600).toInt()
        return "$degrees/1,$minutes/1,$seconds/1"
    }

    private class Listener(wait: Long, private val accuracy: Float, private val callback: (Location?) -> Unit) : LocationListener {
        override fun onLocationChanged(location: Location) {
            location.let { loc ->
                if (loc.accuracy < accuracy) {
                    doCallback(loc)
                }
            }
        }

        init {
            Handler(Looper.getMainLooper()).postDelayed({
                doCallback(null)
            }, wait)
        }

        private var calledBack = false

        private fun doCallback(location: Location?) {
            if (calledBack) {
                return
            }

            calledBack = true
            callback(location)
        }
    }
}