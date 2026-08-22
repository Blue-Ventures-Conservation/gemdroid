package com.github.zibnix.droidbones

import android.content.Context

class NoStack(val resId: Int): Throwable("") {
    override fun fillInStackTrace() = this
}

fun Throwable.localized(ctx: Context): String {
    val ns = this as? NoStack ?: return message ?: ctx.getString(R.string.unknown_error)
    return ctx.getString(ns.resId)
}