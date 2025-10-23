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
import java.util.concurrent.TimeUnit

// -------------------------------
// Repository: remote data + caches
// -------------------------------
class Repository(private val api: PokeApi) {
    private var allNames: List<String> = emptyList()
    private var nameToId: Map<String, Int> = emptyMap()
    private var versionNames: List<String> = emptyList()

    // Cache for species allowed per version
    private val versionCache: MutableMap<String, Set<String>> = mutableMapOf()

    // Fetches all Pokémon names (cached)
    suspend fun loadAllNames(): List<String> = withContext(Dispatchers.IO) {
        if (allNames.isEmpty()) {
            val res = api.listPokemon(limit = 2000)
            allNames = res.results.map { it.name }
// Build name -> id map from resource URLs (which end with /pokemon/{id}/)
nameToId = res.results.mapNotNull { r ->
    val id = r.url.trimEnd('/').substringAfterLast('/').toIntOrNull()
    if (id != null) r.name to id else null
}.toMap()
        }
        allNames
    }

    // Fetches types for a specific Pokémon by name
    suspend fun getPokemonTypes(name: String): List<PokeType> = withContext(Dispatchers.IO) {
        val detail = api.getPokemon(name.lowercase())
        detail.types.sortedBy { it.slot }.mapNotNull {
            try {
                // PokeType is an enum; valueOf expects exact lowercase names (matches Models.kt)
                PokeType.valueOf(it.type.name)
            } catch (_: IllegalArgumentException) {
                // API sometimes returns "unknown"/"shadow" on special endpoints
                null
            }
        }
    }

fun getIdForName(name: String): Int? = nameToId[name.lowercase()]

// Suggests example Pokémon for a given attacking type
    suspend fun suggestExamples(attackType: PokeType, limit: Int = 10): List<String> =
        withContext(Dispatchers.IO) {
            val list = api.getType(attackType.name).pokemon.map { it.pokemon.name }
            list.take(limit)
        }

    // Fetches all game version names (cached)
    suspend fun loadVersions(): List<String> = withContext(Dispatchers.IO) {
        if (versionNames.isEmpty()) {
            versionNames = api.listVersions(limit = 2000).results.map { it.name }
        }
        versionNames
    }

    // Fetches species allowed for a given version (cached)
    suspend fun namesForVersion(versionName: String): Set<String> = withContext(Dispatchers.IO) {
        versionCache[versionName]?.let { return@withContext it }

        // Resolve: version -> version_group -> pokedexes -> species
        val version = api.getVersion(versionName)
        val group = api.getVersionGroup(version.version_group.name)
        val species = mutableSetOf<String>()
        for (pdex in group.pokedexes) {
            try {
                val d = api.getPokedex(pdex.name)
                d.pokemon_entries.forEach { species.add(it.pokemon_species.name) }
            } catch (_: Exception) {
                // Ignore individual pokedex failures; continue accumulating
            }
        }
        versionCache[versionName] = species
        species
    }
}

// -------------------------------
// UI State
// -------------------------------
data class UIState(
    val isLoading: Boolean = false,
    val allNames: List<String> = emptyList(),
    val filteredNames: List<String> = emptyList(),  // names shown in search results
    val analysis: AnalysisResult? = null,           // result after selecting a Pokémon
    val versions: List<String> = emptyList(),       // list of game versions
    val selectedVersion: String? = null,            // current version filter (null = all)
    val selectedPokemonName: String? = null,        // currently selected Pokémon (null = none)
    val errorMessage: String? = null                // non-fatal network/logic errors
)

// -------------------------------
// ViewModel
// -------------------------------
class PokeViewModel(private val repo: Repository) : ViewModel() {

    fun spriteIdFor(name: String): Int? = repo.getIdForName(name)

    private val _state = MutableStateFlow(UIState(isLoading = true))
    val state = _state.asStateFlow()

    // Names allowed by current version filter (null = all)
    private var currentAllowedNames: Set<String>? = null

    // Initial data loading: names + versions
    suspend fun loadAllNames() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        try {
            val names = repo.loadAllNames()
            val versions = repo.loadVersions()
            _state.value = _state.value.copy(
                isLoading = false,
                allNames = names,
                filteredNames = names.take(20),
                versions = versions.sorted()
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "Failed to load data: ${e.javaClass.simpleName}"
            )
        }
    }

    // Expose versions loading to match MainActivity's vm.loadVersions()
    suspend fun loadVersions() {
        try {
            val versions = repo.loadVersions()
            _state.value = _state.value.copy(versions = versions.sorted(), errorMessage = null)
        } catch (e: Exception) {
            _state.value = _state.value.copy(errorMessage = "Failed to load versions: ${e.javaClass.simpleName}")
        }
    }

    // Query filter
    fun filterNames(query: String) {
        val base = (currentAllowedNames ?: _state.value.allNames.toSet()).toList().sorted()
        val filtered = if (query.isBlank()) {
            base.take(20)
        } else {
            base.filter { it.contains(query.trim().lowercase()) }.take(20)
        }
        _state.value = _state.value.copy(filteredNames = filtered)
    }

    // Version selection
    suspend fun selectVersion(version: String?) {
        _state.value = _state.value.copy(isLoading = true, selectedVersion = version, errorMessage = null)
        try {
            currentAllowedNames = if (version.isNullOrBlank()) null else repo.namesForVersion(version)
            val list = (currentAllowedNames ?: _state.value.allNames.toSet()).toList().sorted()
            _state.value = _state.value.copy(
                isLoading = false,
                filteredNames = list.take(20),
                analysis = null,
                selectedPokemonName = null
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, errorMessage = "Failed to set version: ${e.javaClass.simpleName}")
        }
    }

    // Clear the version filter without any network calls (non-suspend)
    fun clearVersionFilter() {
        val base = _state.value.allNames
        val newFiltered = if (base.isNotEmpty()) base.toSet().toList().sorted().take(20) else emptyList()

        currentAllowedNames = null
        _state.value = _state.value.copy(
            selectedVersion = null,
            filteredNames = newFiltered,
            analysis = null,
            selectedPokemonName = null,
            errorMessage = null
        )
    }

    // Pokémon selection + analysis
    suspend fun selectPokemon(name: String) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        try {
            val types = repo.getPokemonTypes(name)

            // Compute effectiveness for all attack types vs target's types
            val multipliers = PokeType.values()
                .mapNotNull { atk ->
                    runCatching { TypeMultiplier(atk, TypeChart.effectiveness(atk, types)) }.getOrNull()
                }
                .sortedByDescending { it.multiplier }

            // Super-effective only (> 1.0x), take top 5
            val top = multipliers.filter { it.multiplier > 1.0 }.take(5)

            // Suggestions from top 1-2 attacking types
            val suggestions = mutableSetOf<String>()
            for (t in top.take(2)) {
                runCatching { repo.suggestExamples(t.type, limit = 8) }
                    .onSuccess { suggestions.addAll(it) }
            }

            // Apply version filter if present
            val allowed = currentAllowedNames?.let { allow -> suggestions.filter { it in allow } }
                ?: suggestions.toList()

            val displayName = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            _state.value = _state.value.copy(
                isLoading = false,
                selectedPokemonName = name,
                analysis = AnalysisResult(
                    targetName = displayName,
                    targetTypes = types,
                    bestTypes = top,
                    examples = allowed.take(12)
                )
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, errorMessage = "Analyze failed: ${e.javaClass.simpleName}")
        }
    }

    // DI Factory for ViewModel
    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val logger = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
                val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build()
                val moshi = Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .build()
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://pokeapi.co/api/v2/")
                    .client(client)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()
                val api = retrofit.create(PokeApi::class.java)
                return PokeViewModel(Repository(api)) as T
            }
        }
    }
}