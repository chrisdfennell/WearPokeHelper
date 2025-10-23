// Copied from wearpokecounter and package name updated
package com.fennell.wearpokehelper.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Retrofit interface defining PokeAPI endpoints
interface PokeApi {
    @GET("pokemon")
    suspend fun listPokemon(@Query("limit") limit: Int = 2000, @Query("offset") offset: Int = 0): PokemonListResponse

    @GET("pokemon/{name}")
    suspend fun getPokemon(@Path("name") name: String): PokemonDetail

    @GET("type/{type}")
    suspend fun getType(@Path("type") type: String): TypePokemonList

    // Versions & Pokedex endpoints
    @GET("version")
    suspend fun listVersions(@Query("limit") limit: Int = 2000, @Query("offset") offset: Int = 0): VersionListResponse

    @GET("version/{name}")
    suspend fun getVersion(@Path("name") name: String): VersionDetail

    @GET("version-group/{name}")
    suspend fun getVersionGroup(@Path("name") name: String): VersionGroupDetail

    @GET("pokedex/{name}")
    suspend fun getPokedex(@Path("name") name: String): PokedexDetail
}