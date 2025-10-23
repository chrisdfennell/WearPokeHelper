package com.fennell.wearpokehelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background // Add background import
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
// import androidx.compose.foundation.verticalScroll // Replaced by ScalingLazyColumn
import androidx.compose.material.Divider // Keep M2 Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import coil.compose.rememberAsyncImagePainter
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
// Explicit Wear Compose Material imports
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme // Keep this for theme access
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.material.scrollAway
// Explicit dialog import
import androidx.wear.compose.material.dialog.Alert
import com.fennell.wearpokehelper.data.PokeViewModel
import com.fennell.wearpokehelper.ui.TypeBadge
import com.fennell.wearpokehelper.ui.WearTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure setTheme is NOT called here
        setContent {
            WearTheme { // Apply the custom theme
                val vm: PokeViewModel = viewModel(factory = PokeViewModel.Factory)
                val scope = rememberCoroutineScope()
                val uiState by vm.state.collectAsState()
                val scalingLazyListState = rememberScalingLazyListState()
LaunchedEffect(Unit) {
                    vm.loadAllNames()
                    vm.loadVersions()
                }

                var query by remember { mutableStateOf(TextFieldValue("")) }
                var showVersionPicker by remember { mutableStateOf(false) }

                val keyboardController = LocalSoftwareKeyboardController.current
                val focusManager = LocalFocusManager.current
                var textFieldFocused by remember { mutableStateOf(false) }

                // Apply explicit background color to Scaffold
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background), // FORCE BACKGROUND COLOR
                    timeText = { TimeText(modifier = Modifier.scrollAway(scalingLazyListState)) },
                    vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
                    positionIndicator = { PositionIndicator(scalingLazyListState = scalingLazyListState) }
                ) {
                    // Use ScalingLazyColumn for the main layout
                    ScalingLazyColumn(
                        
                        
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp), // Adjust padding as needed
                        state = scalingLazyListState,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(top = 28.dp, bottom = 28.dp) // Add padding for time/vignette
                    ) {
                        item {
                            Text("PokeHelper", style = MaterialTheme.typography.title2)
                        }

                        item {
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
                        }

                        item {
                            Text(
                                "Which Pokémon are you fighting?",
                                style = MaterialTheme.typography.caption1
                            )
                        }

                        item {
                            TextInput( // Now uses M2 TextField with explicit colors
                                value = query.text,
                                onValueChange = { new ->
                                    val trimmed = new.trimEnd()
                                    query = TextFieldValue(trimmed)
                                    vm.filterNames(trimmed)
                                },
                                label = "Search",
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        val committed = query.text.trim()
                                        if (committed != query.text) {
                                            query = TextFieldValue(committed)
                                        }
                                        vm.filterNames(committed)
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                onFocusChanged = { focused -> textFieldFocused = focused }
                            )
                        }

                        // Search Results List (only shown when needed)
                        if (query.text.isNotEmpty() && uiState.selectedPokemonName == null) {
                            val filtered = uiState.filteredNames.take(20)
                            items(filtered.size) { idx ->
                                val name = filtered[idx]
                                
Chip(
                        onClick = {
                                        query = TextFieldValue("")
                                        vm.filterNames("")
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        scope.launch { vm.selectPokemon(name) }
                                    },
                                    
                                    icon = {
                                        val id = vm.spriteIdFor(name)
                                        if (id != null) {
                                            Icon(painter = rememberAsyncImagePainter("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"), contentDescription = "pokemon sprite", modifier = Modifier.size(28.dp), tint = androidx.compose.ui.graphics.Color.Unspecified)
                                        }
                                    },
                        label = {
                                        Text(
                                            name.replaceFirstChar { it.titlecase() },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Analysis Result Card
                        item {
                            uiState.analysis?.let { analysis ->
                                Card(
                                    onClick = {},
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp) // Increased padding
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
                                            }",
                                            style = MaterialTheme.typography.body2
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "Best attacking types:",
                                            style = MaterialTheme.typography.caption1
                                        )
                                        Spacer(Modifier.height(4.dp))

                                        // --- Standard Row for best types ---
                                        Row(
                                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(
                                                4.dp,
                                                Alignment.Start // Start align if scrollable
                                            )
                                        ) {
                                            analysis.bestTypes.take(10).forEach { // Show more if scrollable
                                                TypeBadge(it.type.name, it.multiplier)
                                            }
                                        }
                                        // --- End Row ---

                                        Spacer(Modifier.height(8.dp))

                                        Text(
                                            "Example counters:",
                                            style = MaterialTheme.typography.caption1
                                        )
                                        Spacer(Modifier.height(4.dp))

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            analysis.examples.take(20).forEach { ex ->
                                                Chip(
                                                    onClick = {},
                                                    
                                                    icon = {
                                                        val id = vm.spriteIdFor(ex)
                                                        if (id != null) {
                                                            Icon(painter = rememberAsyncImagePainter("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"), contentDescription = "pokemon sprite", modifier = Modifier.size(24.dp), tint = androidx.compose.ui.graphics.Color.Unspecified)
                                                        }
                                                    },
                        label = {
                                                        Text(
                                                            ex.replaceFirstChar { it.titlecase() },
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    },
                                                    colors = ChipDefaults.secondaryChipColors(),
                                                    contentPadding = PaddingValues(
                                                        start = 10.dp,
                                                        end = 10.dp,
                                                        top = 4.dp,
                                                        bottom = 4.dp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Error message chip
                        item {
                            uiState.errorMessage?.let { msg ->
                                
Chip(
                        onClick = {},
                                    
                        label = {
                                        Text(
                                            msg,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = ChipDefaults.secondaryChipColors()
                                )
                            }
                        }
                    } // End Main ScalingLazyColumn

                    // Handle Back press when text field is focused
                    BackHandler(enabled = textFieldFocused) {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }

                    // Version Picker Dialog - Using Alert with fixes
                    if (showVersionPicker) {
                        // Create a separate state for the Alert's list
                        val alertListState = rememberScalingLazyListState()

                        Alert(
                            title = {
                                Text(
                                    "Select Version",
                                    style = MaterialTheme.typography.title3,
                                    textAlign = TextAlign.Center
                                )
                            },
                            negativeButton = {
                                Button(
                                    onClick = { showVersionPicker = false },
                                    colors = ButtonDefaults.secondaryButtonColors()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Close"
                                    )
                                }
                            },
                            positiveButton = {}, // Empty positive button
                            modifier = Modifier.fillMaxHeight(0.85f)
                            // DO NOT pass scrollState to Alert itself
                        ) {
                            // Content slot provides ColumnScope implicitly
                            val versionItems = remember(uiState.versions) { uiState.versions.toList() }

                            // Use M2 Divider
                            Divider(
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                            )

                            // Apply height constraint Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp, max = 260.dp)
                                    .padding(bottom = 8.dp) // Padding below the list, before divider
                            ) {
                                ScalingLazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    state = alertListState, // Use the dedicated state
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    item {
                                        Chip(
                                            onClick = {
                                                scope.launch {
                                                    showVersionPicker = false
                                                    yield()
                                                    vm.clearVersionFilter()
                                                }
                                            },
                                            
                        label = { Text("All Versions") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    // Use items(count) signature
                                    items(count = versionItems.size) { index ->
                                        val v = versionItems[index] // Get item by index
                                        Chip(
                                            onClick = {
                                                scope.launch {
                                                    showVersionPicker = false
                                                    yield()
                                                    vm.selectVersion(v)
                                                }
                                            },
                                            
                        label = {
                                                Text(
                                                    // Remove unnecessary .toString()
                                                    v.replace("-", " ")
                                                        .replaceFirstChar { it.titlecase() },
                                                    maxLines = 1
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                } // End ScalingLazyColumn for items
                            } // End Box height constraint

                            // Use M2 Divider
                            Divider(
                                modifier = Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                            )

                        } // End Alert Content Scope
                    } // End if(showVersionPicker)

                } // End Scaffold
            } // End WearTheme
        } // End setContent
    } // End onCreate
} // End Activity