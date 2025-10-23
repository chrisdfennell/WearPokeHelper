package com.fennell.wearpokehelper.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color // Ensure Color is imported
import androidx.wear.compose.material.Colors // Correct Colors import
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Shapes
import androidx.wear.compose.material.Typography
// import androidx.wear.compose.material.darkColors // Keep removed as it caused issues

// Define a custom dark color palette again, ensuring imports are correct
private val WearAppDarkColorPalette = Colors(
    primary = Color(0xFFBB86FC), // Example Purple
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6), // Example Teal
    secondaryVariant = Color(0xFF018786),
    background = Color.Black, // Explicitly Black background
    surface = Color(0xFF303030),    // Dark Grey surface for cards/chips
    error = Color(0xFFCF6679),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFCACACA), // Lighter grey for secondary text
    onError = Color.Black
)


/**
 * Simple Wear Compose theme wrapper using a custom dark palette.
 */
@Composable
fun WearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = WearAppDarkColorPalette, // Apply the custom dark palette
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}