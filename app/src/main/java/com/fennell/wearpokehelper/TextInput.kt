package com.fennell.wearpokehelper

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Text
// Import TextField
import androidx.wear.compose.material.TextField
import androidx.wear.compose.material.TextFieldDefaults

@Composable
fun TextInput(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        colors = TextFieldDefaults.textFieldColors() // Use default colors
    )
}