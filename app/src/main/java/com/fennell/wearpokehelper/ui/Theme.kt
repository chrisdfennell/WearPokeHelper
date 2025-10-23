package com.fennell.wearpokehelper.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Shapes
import androidx.wear.compose.material.Typography

/**
 * Minimal Wear theme wrapper.
 * Make sure you do NOT import kotlin.text.Typography.
 */
@Composable
fun WearPokeHelperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = MaterialTheme.colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}