package com.fennell.wearpokehelper.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
// Removed darkColors import

@Composable
fun WearTheme( // Renamed to WearTheme to avoid conflict if you add Material 3 later
    content: @Composable () -> Unit
) {
    MaterialTheme(
        // Remove explicit colors = darkColors(...), MaterialTheme defaults to dark on Wear OS
        typography = Typography, // Assuming you have a Typography object defined
        // You can still override specific colors if needed:
        // colors = MaterialTheme.colors.copy(primary = /* your color */),
        content = content
    )
}

// NOTE: You'll likely need to define your Typography object somewhere, e.g.:
// import androidx.compose.ui.text.TextStyle
// import androidx.wear.compose.material.Typography
//
// val Typography = Typography(
//    // Define your text styles here, e.g.
//    // title1 = TextStyle(...)
// )
// If you don't have one, MaterialTheme will use defaults.