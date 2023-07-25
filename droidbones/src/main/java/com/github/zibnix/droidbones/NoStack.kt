package com.github.zibnix.droidbones

import android.content.Context
import androidx.annotation.StringRes

class NoStack(@StringRes val resId: Int): Throwable("") {
    override fun fillInStackTrace() = this
}

fun Throwable.localized(ctx: Context): String {
    val ns = this as? NoStack ?: return ctx.getString(R.string.unknown_error)
    return ctx.getString(ns.resId)
}