package org.blueventures.gemdroid.ui.common

import android.content.Context

typealias Click = () -> Unit
typealias SnackFun = (String) -> Unit
typealias RemoteErrHandler = (Context, String?) -> Pair<String?, Boolean>