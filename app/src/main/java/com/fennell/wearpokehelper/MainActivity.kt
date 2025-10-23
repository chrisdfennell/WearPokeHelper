package com.fennell.wearpokehelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import com.fennell.wearpokehelper.data.PokeViewModel
import com.fennell.wearpokehelper.ui.TypeBadge
import com.fennell.wearpokehelper.ui.WearTheme
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
                    vm.loadVersions() // <-- keep VM-based loader
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
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("PokeCounter", style = MaterialTheme.typography.title3)
                        Spacer(Modifier.height(6.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            VersionPickerChip(
                                current = uiState.selectedVersion,
                                onPick = { showVersionPicker = true },
                                modifier = Modifier.weight(1f)
                            )
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

                        // Show loader only when names aren’t ready at all
                        if (uiState.isLoading && uiState.filteredNames.isEmpty()) {
                            CircularProgressIndicator()
                        } else if (query.text.isNotEmpty() && uiState.selectedPokemonName == null) {
                            Column(modifier = Modifier.heightIn(max = 150.dp)) {
                                ScalingLazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    autoCentering = AutoCenteringParams(itemIndex = 0)
                                ) {
                                    val filtered = uiState.filteredNames.take(10)
                                    items(filtered.size) { idx ->
                                        val name = filtered[idx]
                                        Chip(
                                            onClick = {
                                                query = TextFieldValue("")
                                                vm.filterNames("")
                                                scope.launch { vm.selectPokemon(name) }
                                            },
                                            label = { Text(name.replaceFirstChar { it.titlecase() }) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        uiState.analysis?.let { analysis ->
                            Card(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        "Target: ${analysis.targetName.replaceFirstChar { it.titlecase() }}",
                                        style = MaterialTheme.typography.title3
                                    )
                                    Text(
                                        "Types: ${
                                            analysis.targetTypes.joinToString {
                                                it.name.replaceFirstChar { t -> t.titlecase() }
                                            }
                                        }"
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text("Best attacking types:", style = MaterialTheme.typography.caption2)
                                    Spacer(Modifier.height(4.dp))
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        analysis.bestTypes.take(4).forEach {
                                            TypeBadge(it.type.name, it.multiplier)
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("Example counters:", style = MaterialTheme.typography.caption2)
                                    Spacer(Modifier.height(4.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        analysis.examples.take(6).chunked(3).forEach { row ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                row.forEach { ex ->
                                                    Chip(
                                                        onClick = {},
                                                        label = {
                                                            Text(ex.replaceFirstChar { it.titlecase() })
                                                        },
                                                        colors = ChipDefaults.secondaryChipColors()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                    }

                    if (showVersionPicker) {
                        Alert(
                            title = { Text("Select Game Version") },
                            negativeButton = {
                                Button(onClick = { showVersionPicker = false }) { Text("Cancel") }
                            },
                            positiveButton = {},
                            modifier = Modifier.fillMaxHeight(0.8f)
                        ) {
                            ScalingLazyColumn {
                                item {
                                    Chip(
                                        onClick = {
                                            showVersionPicker = false
                                            scope.launch { vm.selectVersion(null) }
                                        },
                                        label = { Text("All Versions") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                items(uiState.versions.size) { idx ->
                                    val v = uiState.versions[idx]
                                    Chip(
                                        onClick = {
                                            showVersionPicker = false
                                            scope.launch { vm.selectVersion(v) }
                                        },
                                        label = {
                                            Text(v.replace("-", " ")
                                                .replaceFirstChar { it.titlecase() })
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}