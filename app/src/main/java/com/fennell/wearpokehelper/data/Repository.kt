// Copied from wearpokecounter and package name updated
package com.fennell.wearpokehelper.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


// Repository class to handle data fetching and caching
class Repository(private val api: PokeApi) {
    private var allNames: List<String> = emptyList()
    private var versionNames: List<String> = emptyList()
    // Cache for storing Pokémon names available in specific game versions
    private val versionCache: MutableMap<String, Set<String>> = mutableMapOf()

    // Fetches all Pokémon names from the API (caches the result)
    suspend fun loadAllNames(): List<String> = withContext(Dispatchers.IO) {
        if (allNames.isEmpty()) {
            val res = api.listPokemon(limit = 2000) // Fetch a large limit
            allNames = res.results.map { it.name }
        }
        allNames
    }

    // Fetches the types for a specific Pokémon by name
    suspend fun getPokemonTypes(name: String): List<PokeType> = withContext(Dispatchers.IO) {
        val detail = api.getPokemon(name.lowercase())
        detail.types.sortedBy { it.slot }.mapNotNull {
            try {
                PokeType.valueOf(it.type.name)
            } catch (e: IllegalArgumentException) {
                // Handle cases where API returns a type not in our enum (e.g., "unknown", "shadow")
                println("Warning: Unknown Pokémon type '${it.type.name}' for $name")
                null
            }
        }
    }

    // Fetches example Pokémon names for a given attacking type
    suspend fun suggestExamples(attackType: PokeType, limit: Int = 10): List<String> = withContext(Dispatchers.IO) {
        val list = api.getType(attackType.name).pokemon.map { it.pokemon.name }
        list.take(limit)
    }

    // Fetches all game version names from the API (caches the result)
    suspend fun loadVersions(): List<String> = withContext(Dispatchers.IO) {
        if (versionNames.isEmpty()) {
            versionNames = api.listVersions(limit = 2000).results.map { it.name }
        }
        versionNames
    }

    // Fetches the set of Pokémon species available in a specific game version (caches the result)
    suspend fun namesForVersion(versionName: String): Set<String> = withContext(Dispatchers.IO) {
        versionCache[versionName]?.let { return@withContext it } // Return cached set if available

        // Resolve version -> version_group -> pokedexes -> species names
        val version = api.getVersion(versionName)
        val group = api.getVersionGroup(version.version_group.name)
        val species = mutableSetOf<String>()
        for (pdex in group.pokedexes) {
            try {
                val d = api.getPokedex(pdex.name)
                d.pokemon_entries.forEach { species.add(it.pokemon_species.name) }
            } catch (e: Exception) {
                // Handle cases where a Pokedex might not be found or API error occurs
                println("Warning: Could not load Pokedex '${pdex.name}' for version group '${group.name}'. Error: ${e.message}")
            }
        }
        versionCache[versionName] = species // Cache the result
        species
    }
}

// Data class representing the UI state
data class UIState(
    val isLoading: Boolean = false,
    val allNames: List<String> = emptyList(),
    val filteredNames: List<String> = emptyList(), // Names shown in the search results
    val analysis: AnalysisResult? = null, // Result after selecting a Pokémon
    val versions: List<String> = emptyList(), // List of game versions
    val selectedVersion: String? = null // Currently selected game version filter
)

// ViewModel to manage UI state and interactions with the Repository
class PokeViewModel(private val repo: Repository): ViewModel() {
    private val _state = MutableStateFlow(UIState(isLoading = true))
    val state = _state.asStateFlow() // Expose state as a read-only StateFlow

    private var currentAllowedNames: Set<String>? = null // Names allowed by the current version filter

    // Initial data loading
    suspend fun loadAllNames() {
        _state.value = _state.value.copy(isLoading = true)
        val names = repo.loadAllNames()
        val versions = repo.loadVersions()
        _state.value = _state.value.copy(
            isLoading = false,
            allNames = names,
            filteredNames = names.take(20), // Show initial subset
            versions = versions.sorted()
        )
    }

    // Filters the displayed Pokémon names based on user query and version filter
    fun filterNames(query: String) {
        // Start with the set of names allowed by the version filter (or all names if no filter)
        val base = (currentAllowedNames ?: _state.value.allNames.toSet()).toList().sorted()
        val filtered = if (query.isBlank()) {
            base.take(20) // Show initial subset if query is empty
        } else {
            // Filter by query and take a limited number of results
            base.filter { it.contains(query.trim().lowercase()) }.take(20)
        }
        _state.value = _state.value.copy(filteredNames = filtered)
    }

    // Handles selection of a game version filter
    suspend fun selectVersion(version: String?) {
        _state.value = _state.value.copy(isLoading = true, selectedVersion = version)
        // Fetch and store the allowed names for the selected version (or null if "All Versions")
        currentAllowedNames = if (version.isNullOrBlank()) null else repo.namesForVersion(version)
        // Reset the filtered list based on the new set of allowed names
        val list = (currentAllowedNames ?: _state.value.allNames.toSet()).toList().sorted()
        _state.value = _state.value.copy(isLoading = false, filteredNames = list.take(20))
        // Clear previous analysis when version changes
        _state.value = _state.value.copy(analysis = null)
    }


    // Handles selection of a Pokémon to analyze
    suspend fun selectPokemon(name: String) {
        _state.value = _state.value.copy(isLoading = true)
        val types = repo.getPokemonTypes(name)

        // Calculate effectiveness of all attack types against the target's types
        val multipliers = PokeType.values().mapNotNull { atk ->
            try {
                TypeMultiplier(atk, TypeChart.effectiveness(atk, types))
            } catch (e: Exception) {
                println("Error calculating effectiveness for ${atk.name} vs $name types $types: ${e.message}")
                null // Skip if there's an error (e.g., type not in chart)
            }
        }.sortedByDescending { it.multiplier } // Sort by effectiveness (highest first)

        // Filter for types that are super effective (> 1.0x)
        val best = multipliers.filter { it.multiplier > 1.0 }
        val top = best.take(5) // Take top 5 super effective types

        // Suggest example Pokémon counters based on the top 1-2 best attacking types
        val suggestions = mutableSetOf<String>()
        for (t in top.take(2)) { // Look at the top 2 types
            try {
                suggestions.addAll(repo.suggestExamples(t.type, limit = 8))
            } catch (e: Exception) {
                println("Error suggesting examples for type ${t.type.name}: ${e.message}")
            }
        }
        // Filter suggestions to only include Pokémon allowed by the current version filter
        val allowedSuggestions = if (currentAllowedNames != null) {
            suggestions.filter { currentAllowedNames!!.contains(it) }
        } else {
            suggestions.toList()
        }


        // Update UI state with the analysis result
        _state.value = _state.value.copy(
            isLoading = false,
            analysis = AnalysisResult(
                targetName = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }, // Capitalize name
                targetTypes = types,
                bestTypes = top,
                examples = allowedSuggestions.take(12).toList() // Show up to 12 examples
            )
        )
    }

    // Factory object to create the ViewModel instance with dependencies
    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // Setup Retrofit, OkHttp with logging, and Moshi
                val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
                val client = OkHttpClient.Builder().addInterceptor(logger).build()
                val moshi = Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory()) // Add Kotlin support
                    .build()
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://pokeapi.co/api/v2/")
                    .client(client)
                    .addConverterFactory(MoshiConverterFactory.create(moshi)) // Use Moshi with Kotlin support
                    .build()
                val api = retrofit.create(PokeApi::class.java)
                // Create Repository and ViewModel instances
                return PokeViewModel(Repository(api)) as T
            }
        }
    }
}