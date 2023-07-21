package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.R
import org.blueventures.gemdroid.ui.theme.DarkSlate

data class AppBarState(
    val settings: Click = {},
    val signOut: Click = {},
    val update: AppBarUpdate = AppBarUpdate(""
    ) {
        BasicActions(settings, signOut)
    }
)

data class AppBarUpdate(
    val title: String = "",
    val actions: (@Composable RowScope.() -> Unit)? = null
)

@Composable
fun BasicActions(settings: Click, signOut: Click) {
    val (menu, setMenu) = remember { mutableStateOf(false) }
    IconButton(onClick = { setMenu(!menu) }) {
        Icon(Icons.Filled.MoreVert, "")
    }
    DropdownMenu(expanded = menu, onDismissRequest = { setMenu(false) }, modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
        ActionItem(label = stringResource(R.string.settings_label)) {
            setMenu(false)
            settings()
        }
        Divider(color = DarkSlate, thickness = 1.dp)
        ActionItem(label = stringResource(R.string.logout_label)) {
            setMenu(false)
            signOut()
        }
    }
}

@Composable
fun ActionItem(label: String, onClick: Click) {
    Text(text = label, fontSize = 24.sp, modifier = Modifier
        .padding(bottom = 8.dp)
        .clickable(onClick = onClick))
}
