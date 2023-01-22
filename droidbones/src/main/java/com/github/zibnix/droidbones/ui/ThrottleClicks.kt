package com.github.zibnix.droidbones.ui

import android.view.View
import kotlin.math.abs

class ThrottleClicks(private val clickFunc: (view: View) -> Unit, private val interval: Long = DEFAULT_INTERVAL) : View.OnClickListener {
    private var lastClick: Long = 0

    override fun onClick(view: View) {
        val stamp = System.currentTimeMillis()
        val last = lastClick
        lastClick = stamp

        if (abs(stamp - last) >= interval) {
            clickFunc(view)
        }
    }

    companion object {
        const val DEFAULT_INTERVAL = 550L // millis
    }
}