package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

/**
 * accompanist.SwipeRefresh is deprecated, but material3 doesn't have a working, equivalent library.
 *
 * Until an official solution is available, one possible interim solution is here:
 * https://github.com/Omico/androidx-compose-material3-pullrefresh
 * https://stackoverflow.com/questions/75683184/jetpack-compose-material3-pull-to-refresh-functionality
 * https://issuetracker.google.com/issues/261760718
 */
@Composable
fun RefreshableError(message: String? = null, fetchFunc: (() -> Unit) -> Unit) {
    var refreshing by remember { mutableStateOf(false) }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = refreshing),
        onRefresh = {
            refreshing = true
            fetchFunc {
                refreshing = false
            }
        },
        indicator = { st, trigger ->
            SwipeRefreshIndicator(state = st, refreshTriggerDistance = trigger)
        },
        modifier = Modifier.fillMaxSize()
    ) {
        BasicMessage(message ?: "Something unexpected went wrong there! You can swipe down to try again.")
    }
}