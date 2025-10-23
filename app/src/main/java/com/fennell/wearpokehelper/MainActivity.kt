package com.fennell.wearpokehelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import com.fennell.wearpokecounter.ui.TypeBadge
import com.fennell.wearpokecounter.ui.WearTheme
import com.fennell.wearpokecounter.data.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearTheme {
                val vm: PokeViewModel = viewModel(factory = PokeViewModel.Factory)
                val scope = rememberCoroutineScope()
                val uiState by vm.state.collectAsState()

                LaunchedEffect(Unit) {
                    vm.loadAllNames()
                }

                var query by remember { mutableStateOf(TextFieldValue("")) }
                var showVersionPicker by remember { mutableStateOf(false) }

                Scaffold(
                    timeText = { TimeText() },
                    vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Text("PokeCounter", style = MaterialTheme.typography.title3)
                        Spacer(Modifier.height(6.dp))

                        Row(Modifier.fillMaxWidth()) {
                            VersionPickerChip(
                                current = uiState.selectedVersion,
                                onPick = { showVersionPicker = true },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(6.dp))
                            VoiceSearchChip(
                                onResult = { spoken ->
                                    query = TextFieldValue(spoken)
                                    vm.filterNames(spoken.lowercase())
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(6.dp))
                        Text("Which Pokémon are you fighting?", style = MaterialTheme.typography.caption3)
                        Spacer(Modifier.height(4.dp))
                        TextInput(
                            value = query.text,
                            onValueChange = { new ->
                                query = TextFieldValue(new)
                                vm.filterNames(new)
                            },
                            label = "Search",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))
                        if (uiState.isLoading) {
                            Text("Loading…")
                        } else {
                            ScalingLazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                autoCentering = AutoCenteringParams(0)
                            ) {
                                val filtered = uiState.filteredNames.take(10)
                                items(filtered.size) { idx ->
                                    val name = filtered[idx]
                                    Chip(
                                        onClick = {
                                            scope.launch { vm.selectPokemon(name) }
                                        },
                                        label = { Text(name.replaceFirstChar { it.titlecase() }) }
                                    )
                                }
                            }
                        }

                        uiState.analysis?.let { analysis ->
                            Spacer(Modifier.height(6.dp))
                            Card(onClick = {}, enabled = false) {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text("Target: ${analysis.targetName}", style = MaterialTheme.typography.title3)
                                    Text("Target Types: ${analysis.targetTypes.joinToString { it.name }}")
                                    Spacer(Modifier.height(6.dp))
                                    Text("Best attacking types", style = MaterialTheme.typography.caption2)
                                    Spacer(Modifier.height(4.dp))
                                    Row(Modifier.fillMaxWidth()) {
                                        analysis.bestTypes.take(3).forEach {
                                            TypeBadge(it.type.name, it.multiplier)
                                            Spacer(Modifier.width(4.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("Example counters", style = MaterialTheme.typography.caption2)
                                    Spacer(Modifier.height(4.dp))
                                    analysis.examples.take(6).chunked(3).forEach { row ->
                                        Row {
                                            row.forEach { ex ->
                                                Chip(onClick = {}, label = { Text(ex.replaceFirstChar { it.titlecase() }) })
                                                Spacer(Modifier.width(4.dp))
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }

                    if (showVersionPicker) {
                        Alert(
                            onDismissRequest = { showVersionPicker = false },
                            title = { Text("Select Game Version") },
                            message = {
                                ScalingLazyColumn {
                                    item {
                                        Chip(onClick = {
                                            showVersionPicker = false
                                            scope.launch { vm.selectVersion(null) }
                                        }, label = { Text("All Versions") })
                                    }
                                    items(uiState.versions.take(50).size) { idx ->
                                        val v = uiState.versions[idx]
                                        Chip(onClick = {
                                            showVersionPicker = false
                                            scope.launch { vm.selectVersion(v) }
                                        }, label = { Text(v.replace("-", " ")) })
                                    }
                                }
                            },
                            confirmButton = {},
                            negativeButton = {}
                        )
                    }
                }
            }
        }
    }
}