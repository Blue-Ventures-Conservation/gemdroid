package org.blueventures.gemdroid.ui.common

import android.content.Context

typealias Click = () -> Unit
typealias SnackFun = (String) -> Unit
typealias RemoteErrHandler = (Context, Int?, String?) -> Pair<String?, Boolean>