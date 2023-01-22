package com.github.zibnix.droidbones.ui

interface Progress {
    fun showProgress()
    fun hideProgress()
    fun showSnackBar(msg: String)
}