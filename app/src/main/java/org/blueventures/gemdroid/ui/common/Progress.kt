package org.blueventures.gemdroid.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blueventures.gemdroid.R

@Composable
fun Progress() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(152.dp))
    }
}

@Composable
fun PleaseWait() {
    TitledProgress(stringResource(R.string.please_wait), stringResource(R.string.this_may_take_minutes))
}

@Composable
fun TitledProgress(header: String, footer: String) {
    Col.MidPad {
        Text(text = header, fontSize = 32.sp, textAlign = TextAlign.Center)
        CircularProgressIndicator(modifier = Modifier.size(152.dp))
        Text(text = footer, fontSize = 24.sp, textAlign = TextAlign.Center)
    }
}