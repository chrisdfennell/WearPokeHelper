package com.fennell.wearpokehelper

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
        label = { Text("🎤 Voice Search") },
        modifier = modifier
    )
}

@Composable
fun VersionPickerChip(current: String?, onPick: () -> Unit, modifier: Modifier = Modifier) {
    Chip(
        onClick = onPick,
        label = { Text(current?.replace("-", " ")?.replaceFirstChar { it.titlecase() } ?: "All Versions") },
        secondaryLabel = { Text("Game Version") },
        modifier = modifier
    )
}