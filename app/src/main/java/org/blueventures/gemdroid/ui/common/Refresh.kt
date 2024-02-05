package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.blueventures.gemdroid.R

@Composable
fun RefreshableError(message: String? = null, doRefresh: (stop: () -> Unit) -> Unit) {
    Refresh(doRefresh) {
        BasicMessage(message ?: stringResource(R.string.unexpected_error))
    }
}

/**
 * Content must be scrollable.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Refresh(doRefresh: (stop: () -> Unit) -> Unit, content: @Composable () -> Unit) {
    var refreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(refreshing, {
        refreshing = true
        doRefresh {
            refreshing = false
        }
    })

    Box(modifier = Modifier
        .pullRefresh(pullRefreshState)
        .fillMaxSize()) {
        content()
        PullRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
    }
}