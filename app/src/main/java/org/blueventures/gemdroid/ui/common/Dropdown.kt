package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.ui.theme.OffWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(title: String, labels: Iterable<String>, onIndexSelected: (Int) -> Unit) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val (selected, setSelected) = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { setExpanded(!expanded) }
        ) {
            TextField(
                "",
                {},
                label = { Text(selected, fontSize = 14.sp, color = OffWhite) },
                enabled = false,
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { setExpanded(false) }
            ) {
                labels.forEachIndexed { i, label ->
                    DropdownMenuItem(
                        onClick = {
                            setSelected(label)
                            onIndexSelected(i)
                            setExpanded(false)
                        },
                        text = {
                            Text(text = label, fontSize = 16.sp)
                        },
                    )
                }
            }
        }
    }
}