package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AppBarState(
    val signOut: () -> Unit = {},
    val update: AppBarUpdate = AppBarUpdate(""
    ) {
        SignOut {
            signOut()
        }
    }
)

data class AppBarUpdate(
    val title: String = "",
    val actions: (@Composable RowScope.() -> Unit)? = null
)

@Composable
fun SignOut(signOut: () -> Unit) {
    val (menu, setMenu) = remember { mutableStateOf(false) }
    IconButton(onClick = { setMenu(!menu) }) {
        Icon(Icons.Filled.MoreVert, "")
    }
    DropdownMenu(expanded = menu, onDismissRequest = { setMenu(false) }) {
        Text(text = "Logout", fontSize = 24.sp, modifier = Modifier.padding(8.dp).clickable {
            signOut()
        })
    }
}
