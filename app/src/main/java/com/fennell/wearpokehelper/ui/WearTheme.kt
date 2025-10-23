package com.fennell.wearpokehelper.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Shapes
import androidx.wear.compose.material.Typography

/**
 * Simple Wear Compose theme wrapper.
 * NOTE: Make sure you import androidx.wear.compose.material.Typography,
 * NOT kotlin.text.Typography.
 */
@Composable
fun WearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = MaterialTheme.colors,   // default Wear colors
        typography = Typography(),       // Wear Typography
        shapes = Shapes(),               // Wear Shapes
        content = content
    )
}