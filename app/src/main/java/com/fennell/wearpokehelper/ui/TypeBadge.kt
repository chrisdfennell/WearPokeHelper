package com.fennell.wearpokehelper.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

/**
 * Small badge to display a Pokémon type with an optional multiplier.
 * Example: TypeBadge("Electric", 2.0) -> "Electric x2"
 */
@Composable
fun TypeBadge(typeName: String, multiplier: Double? = null) {
    val label = if (multiplier != null && multiplier != 1.0) {
        "$typeName x${trimTrailingZero(multiplier)}"
    } else {
        typeName
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colors.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label.replaceFirstChar { it.titlecase() },
            style = MaterialTheme.typography.caption2,
            color = MaterialTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun trimTrailingZero(value: Double): String {
    val s = value.toString()
    return if (s.endsWith(".0")) s.dropLast(2) else s
}