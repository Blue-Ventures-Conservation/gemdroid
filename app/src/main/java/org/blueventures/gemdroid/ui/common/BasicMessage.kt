package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BasicMessage(message: String) {
    Col.Between {
        Text(
            text = message,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp)
        )
        Spacer(modifier = Modifier.size(0.dp))
    }
}