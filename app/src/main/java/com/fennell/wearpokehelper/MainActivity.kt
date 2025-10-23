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
// Import Alert from the dialog package
import androidx.wear.compose.material.dialog.Alert
// Import Alignment
import androidx.compose.ui.Alignment
// Import specific ViewModel and Theme from local packages
import com.fennell.wearpokehelper.data.PokeViewModel
import com.fennell.wearpokehelper.ui.TypeBadge
import com.fennell.wearpokehelper.ui.WearTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearTheme { // Use the updated WearTheme
                val vm: PokeViewModel = viewModel(factory = PokeViewModel.Factory)
                val scope = rememberCoroutineScope()
                val uiState by vm.state.collectAsState()

                LaunchedEffect(Unit) {
                    vm.loadAllNames()
                    // Also load versions when the app starts
                    vm.loadVersions()
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
                            .padding(horizontal = 12.dp, vertical = 6.dp) // Adjusted padding
                            .verticalScroll(rememberScrollState()), // Make the whole column scrollable
                        horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
                    ) {
                        Text("PokeCounter", style = MaterialTheme.typography.title3)
                        Spacer(Modifier.height(6.dp))

                        // Chips in a Row
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp) // Add space between chips
                        ) {
                            VersionPickerChip(
                                current = uiState.selectedVersion,
                                onPick = { showVersionPicker = true },
                                modifier = Modifier.weight(1f) // Chips share space equally
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

                        // Pokémon List or Loading Indicator
                        if (uiState.isLoading && uiState.filteredNames.isEmpty()) { // Show loading only if names aren't ready
                            CircularProgressIndicator() // Use a standard loading indicator
                        } else if (query.text.isNotEmpty() && uiState.selectedPokemonName == null) {
                            // Only show the list if the user has typed something and hasn't selected a Pokemon yet
                            // Limit height to avoid taking too much space before selection
                            Column(modifier = Modifier.heightIn(max = 150.dp)) {
                                ScalingLazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    autoCentering = AutoCenteringParams(itemIndex = 0) // Center first item initially
                                ) {
                                    val filtered = uiState.filteredNames.take(10)
                                    items(filtered.size) { idx ->
                                        val name = filtered[idx]
                                        Chip(
                                            onClick = {
                                                // Clear query and trigger selection
                                                query = TextFieldValue("")
                                                vm.filterNames("") // Clear filter
                                                scope.launch { vm.selectPokemon(name) }
                                            },
                                            label = { Text(name.replaceFirstChar { it.titlecase() }) },
                                            modifier = Modifier.fillMaxWidth() // Make chips fill width
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp)) // Add space after the list
                        }


                        // Analysis Card - Only show if analysis is available
                        uiState.analysis?.let { analysis ->
                            // Removed Spacer(Modifier.height(6.dp)) - space handled by list spacer
                            Card(
                                onClick = {}, // Cards don't need onClick unless interactive
                                enabled = false,
                                modifier = Modifier.fillMaxWidth() // Make card fill width
                            ) {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                    // Removed verticalScroll - whole outer column scrolls now
                                ) {
                                    Text(
                                        "Target: ${analysis.targetName.replaceFirstChar { it.titlecase() }}", // Title case name
                                        style = MaterialTheme.typography.title3
                                    )
                                    Text("Types: ${analysis.targetTypes.joinToString { it.name.replaceFirstChar { t -> t.titlecase() } }}") // Title case types
                                    Spacer(Modifier.height(6.dp))
                                    Text("Best attacking types:", style = MaterialTheme.typography.caption2) // Added colon
                                    Spacer(Modifier.height(4.dp))
                                    // Use FlowRow for type badges to wrap if needed
                                    Row( // Using Row for now as FlowRow isn't in default Wear Compose
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        analysis.bestTypes.take(4).forEach { // Show up to 4
                                            TypeBadge(it.type.name, it.multiplier)
                                            // Spacer(Modifier.width(4.dp)) // Handled by Arrangement
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("Example counters:", style = MaterialTheme.typography.caption2) // Added colon
                                    Spacer(Modifier.height(4.dp))
                                    // Use FlowRow for example chips too
                                    Column( // Using Column of Rows for now
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        analysis.examples.take(6).chunked(3).forEach { row ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                row.forEach { ex ->
                                                    Chip(
                                                        onClick = {},
                                                        label = { Text(ex.replaceFirstChar { it.titlecase() }) },
                                                        colors = ChipDefaults.secondaryChipColors() // Use secondary colors for examples
                                                    )
                                                    // Spacer(Modifier.width(4.dp)) // Handled by Arrangement
                                                }
                                            }
                                            // Spacer(Modifier.height(4.dp)) // Handled by Arrangement
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp)) // Add some padding at the bottom
                        }
                    }

                    // Version Picker Dialog
                    if (showVersionPicker) {
                        // Use Alert Dialog correctly
                        Alert(
                            title = { Text("Select Game Version") },
                            negativeButton = { Button(onClick = { showVersionPicker = false }) { Text("Cancel") } },
                            positiveButton = {}, // No OK needed, selection happens on chip click
                            modifier = Modifier.fillMaxHeight(0.8f) // Limit dialog height
                        ) { // Content lambda for Alert
                            ScalingLazyColumn {
                                item {
                                    Chip(onClick = {
                                        showVersionPicker = false
                                        scope.launch { vm.selectVersion(null) }
                                    }, label = { Text("All Versions") }, modifier = Modifier.fillMaxWidth())
                                }
                                items(uiState.versions.size) { idx -> // Iterate through all loaded versions
                                    val v = uiState.versions[idx]
                                    Chip(onClick = {
                                        showVersionPicker = false
                                        scope.launch { vm.selectVersion(v) }
                                    }, label = { Text(v.replace("-", " ").replaceFirstChar { it.titlecase() }) }, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}