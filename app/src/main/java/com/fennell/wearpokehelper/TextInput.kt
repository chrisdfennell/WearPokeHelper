package com.fennell.wearpokehelper

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TextField

@Composable
fun TextInput(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
    )
}