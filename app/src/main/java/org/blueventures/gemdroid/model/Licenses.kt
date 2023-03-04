package org.blueventures.gemdroid.model

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

object Licenses {
    var anychart: String? = null

    fun set(context: Context) {
        val bundle = context.packageManager.getApplicationInfoCompat(context.packageName, PackageManager.GET_META_DATA).metaData

        anychart = bundle.getString(anychartMetaKey)
    }

    private const val anychartMetaKey = "org.blueventures.anychart.license"
}

fun PackageManager.getApplicationInfoCompat(packageName: String, flags: Int = 0): ApplicationInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION") getApplicationInfo(packageName, flags)
    }
}