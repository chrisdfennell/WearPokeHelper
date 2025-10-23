package com.fennell.wearpokehelper

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size // Import size modifier
import androidx.compose.material.icons.Icons // Standard material icons
import androidx.compose.material.icons.filled.List // Import List icon
import androidx.compose.material.icons.filled.Mic // Import Mic icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.material.*

@Composable
fun VoiceSearchChip(onResult: (String) -> Unit, modifier: Modifier = Modifier) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == RESULT_OK) {
            val data = res.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = matches?.firstOrNull()
            if (!text.isNullOrBlank()) {
                onResult(text)
            }
        }
    }
    Chip(
        onClick = {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a Pokémon name")
            }
            launcher.launch(intent)
        },
        label = {
            Text(
                "Voice Search",
                maxLines = 1, // Ensure single line
                overflow = TextOverflow.Ellipsis // Handle overflow
            )
        },
        icon = { // Add Mic icon
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "Voice Search",
                modifier = Modifier.size(ChipDefaults.IconSize) // Use default Chip icon size
            )
        },
        colors = ChipDefaults.primaryChipColors(), // Use primary colors
        modifier = modifier
    )
}

@Composable
fun VersionPickerChip(current: String?, onPick: () -> Unit, modifier: Modifier = Modifier) {
    Chip(
        onClick = onPick,
        label = {
            Text(
                current?.replace("-", " ")?.replaceFirstChar { it.titlecase() } ?: "All Versions",
                maxLines = 1, // Ensure single line
                overflow = TextOverflow.Ellipsis // Handle overflow
            )
        },
        secondaryLabel = {
            Text(
                "Game Version",
                maxLines = 1, // Ensure single line
                overflow = TextOverflow.Ellipsis // Handle overflow
            )
        },
        icon = { // Add List icon
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "Select game version",
                modifier = Modifier.size(ChipDefaults.IconSize) // Use default Chip icon size
            )
        },
        colors = ChipDefaults.secondaryChipColors(), // Use secondary colors
        modifier = modifier
    )
}