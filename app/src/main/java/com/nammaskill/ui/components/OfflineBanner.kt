package com.nammaskill.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OfflineBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Red)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "You are currently offline. Showing cached data.",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
