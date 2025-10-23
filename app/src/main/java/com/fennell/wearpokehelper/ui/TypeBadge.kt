// Copied from wearpokecounter and package name updated
package com.fennell.wearpokehelper.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*

// Composable function for displaying a Pokémon type and its effectiveness multiplier
@Composable
fun TypeBadge(type: String, multiplier: Double) {
    CompactChip(
        onClick = {}, // Not clickable
        label = { Text("${type.replaceFirstChar { it.titlecase() }} x${trimMultiplier(multiplier)}") }, // Format text e.g., "Ground x4"
        modifier = Modifier.padding(end = 2.dp), // Add some spacing
        enabled = false // Visually indicate it's not interactive
    )
}

// Helper function to format the multiplier cleanly (e.g., 4.0 -> "4", 0.25 -> "0.25")
private fun trimMultiplier(m: Double): String {
    val s = if (m % 1.0 == 0.0) { // Check if it's a whole number
        m.toInt().toString()
    } else {
        String.format("%.2f", m).trimEnd('0').trimEnd('.') // Format to 2 decimal places, remove trailing zeros/dot
    }
    return s
}