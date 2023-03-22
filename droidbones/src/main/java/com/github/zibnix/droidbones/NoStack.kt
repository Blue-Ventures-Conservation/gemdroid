package com.github.zibnix.droidbones

class NoStack(message: String): Throwable(message) {
    constructor(err: Throwable) : this(err.message!!)
    override fun fillInStackTrace() = this
}