package com.nammaskill.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SimpleText() {
    Text(text = "Hello Namma Skill!")
}

@Preview(showBackground = true)
@Composable
fun SimplePreview() {
    SimpleText()
}
