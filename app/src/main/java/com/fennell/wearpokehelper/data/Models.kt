package com.fennell.wearpokehelper.data

import com.squareup.moshi.JsonClass

// ---------- Core PokeAPI DTOs ----------

@JsonClass(generateAdapter = true)
data class NamedApiResource(
    val name: String,
    val url: String
)

@JsonClass(generateAdapter = true)
data class PokemonListResponse(
    val count: Int,
    val results: List<NamedApiResource>
)

@JsonClass(generateAdapter = true)
data class PokemonTypeEntry(
    val slot: Int,
    val type: NamedApiResource
)

@JsonClass(generateAdapter = true)
data class PokemonDetail(
    val name: String,
    val types: List<PokemonTypeEntry>
)

@JsonClass(generateAdapter = true)
data class TypePokemonList(
    val pokemon: List<TypePokemonEntry>
)

@JsonClass(generateAdapter = true)
data class TypePokemonEntry(
    val pokemon: NamedApiResource
)

// ---------- Types / Analysis models ----------

// Enum for Pokémon types (lowercase to match PokeAPI names exactly)
enum class PokeType {
    normal, fire, water, electric, grass, ice,
    fighting, poison, ground, flying,
    psychic, bug, rock, ghost, dragon,
    dark, steel, fairy
}

// Effectiveness result for a single attacking type vs target types
data class TypeMultiplier(
    val type: PokeType,
    val multiplier: Double
)

// Final analysis payload for the UI
data class AnalysisResult(
    val targetName: String,
    val targetTypes: List<PokeType>,
    val bestTypes: List<TypeMultiplier>,
    val examples: List<String>
)

// ---------- Version / Pokedex DTOs ----------

@JsonClass(generateAdapter = true)
data class VersionListResponse(
    val count: Int,
    val results: List<NamedApiResource>
)

@JsonClass(generateAdapter = true)
data class VersionDetail(
    val name: String,
    val version_group: NamedApiResource
)

@JsonClass(generateAdapter = true)
data class VersionGroupDetail(
    val name: String,
    val pokedexes: List<NamedApiResource>
)

@JsonClass(generateAdapter = true)
data class PokedexDetail(
    val name: String,
    val pokemon_entries: List<PokedexEntry>
)

@JsonClass(generateAdapter = true)
data class PokedexEntry(
    val entry_number: Int,
    val pokemon_species: NamedApiResource
)