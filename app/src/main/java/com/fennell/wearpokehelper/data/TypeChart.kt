// Copied from wearpokecounter and package name updated
package com.fennell.wearpokehelper.data

// Object containing the Pokémon type effectiveness chart (Gen 9)
object TypeChart {
    // Defines multipliers for Attack Type vs Defense Type.
    // Only non-1.0x multipliers are listed for brevity.
    private val chart: Map<PokeType, Map<PokeType, Double>> = mapOf(
        PokeType.normal to mapOf(PokeType.rock to 0.5, PokeType.ghost to 0.0, PokeType.steel to 0.5),
        PokeType.fire to mapOf(PokeType.grass to 2.0, PokeType.ice to 2.0, PokeType.bug to 2.0, PokeType.steel to 2.0,
            PokeType.fire to 0.5, PokeType.water to 0.5, PokeType.rock to 0.5, PokeType.dragon to 0.5),
        PokeType.water to mapOf(PokeType.fire to 2.0, PokeType.ground to 2.0, PokeType.rock to 2.0,
            PokeType.water to 0.5, PokeType.grass to 0.5, PokeType.dragon to 0.5),
        PokeType.electric to mapOf(PokeType.water to 2.0, PokeType.flying to 2.0,
            PokeType.electric to 0.5, PokeType.grass to 0.5, PokeType.dragon to 0.5, PokeType.ground to 0.0),
        PokeType.grass to mapOf(PokeType.water to 2.0, PokeType.ground to 2.0, PokeType.rock to 2.0,
            PokeType.fire to 0.5, PokeType.grass to 0.5, PokeType.poison to 0.5, PokeType.flying to 0.5, PokeType.bug to 0.5, PokeType.dragon to 0.5, PokeType.steel to 0.5),
        PokeType.ice to mapOf(PokeType.grass to 2.0, PokeType.ground to 2.0, PokeType.flying to 2.0, PokeType.dragon to 2.0,
            PokeType.fire to 0.5, PokeType.water to 0.5, PokeType.ice to 0.5, PokeType.steel to 0.5),
        PokeType.fighting to mapOf(PokeType.normal to 2.0, PokeType.ice to 2.0, PokeType.rock to 2.0, PokeType.dark to 2.0, PokeType.steel to 2.0,
            PokeType.poison to 0.5, PokeType.flying to 0.5, PokeType.psychic to 0.5, PokeType.bug to 0.5, PokeType.fairy to 0.5, PokeType.ghost to 0.0),
        PokeType.poison to mapOf(PokeType.grass to 2.0, PokeType.fairy to 2.0,
            PokeType.poison to 0.5, PokeType.ground to 0.5, PokeType.rock to 0.5, PokeType.ghost to 0.5, PokeType.steel to 0.0),
        PokeType.ground to mapOf(PokeType.fire to 2.0, PokeType.electric to 2.0, PokeType.poison to 2.0, PokeType.rock to 2.0, PokeType.steel to 2.0,
            PokeType.grass to 0.5, PokeType.bug to 0.5, PokeType.flying to 0.0),
        PokeType.flying to mapOf(PokeType.grass to 2.0, PokeType.fighting to 2.0, PokeType.bug to 2.0,
            PokeType.electric to 0.5, PokeType.rock to 0.5, PokeType.steel to 0.5),
        PokeType.psychic to mapOf(PokeType.fighting to 2.0, PokeType.poison to 2.0,
            PokeType.psychic to 0.5, PokeType.steel to 0.5, PokeType.dark to 0.0),
        PokeType.bug to mapOf(PokeType.grass to 2.0, PokeType.psychic to 2.0, PokeType.dark to 2.0,
            PokeType.fire to 0.5, PokeType.fighting to 0.5, PokeType.poison to 0.5, PokeType.flying to 0.5, PokeType.ghost to 0.5, PokeType.steel to 0.5, PokeType.fairy to 0.5),
        PokeType.rock to mapOf(PokeType.fire to 2.0, PokeType.ice to 2.0, PokeType.flying to 2.0, PokeType.bug to 2.0,
            PokeType.fighting to 0.5, PokeType.ground to 0.5, PokeType.steel to 0.5),
        PokeType.ghost to mapOf(PokeType.psychic to 2.0, PokeType.ghost to 2.0,
            PokeType.dark to 0.5, PokeType.normal to 0.0),
        PokeType.dragon to mapOf(PokeType.dragon to 2.0,
            PokeType.steel to 0.5, PokeType.fairy to 0.0),
        PokeType.dark to mapOf(PokeType.psychic to 2.0, PokeType.ghost to 2.0,
            PokeType.fighting to 0.5, PokeType.dark to 0.5, PokeType.fairy to 0.5),
        PokeType.steel to mapOf(PokeType.ice to 2.0, PokeType.rock to 2.0, PokeType.fairy to 2.0,
            PokeType.fire to 0.5, PokeType.water to 0.5, PokeType.electric to 0.5, PokeType.steel to 0.5),
        PokeType.fairy to mapOf(PokeType.fighting to 2.0, PokeType.dragon to 2.0, PokeType.dark to 2.0,
            PokeType.fire to 0.5, PokeType.poison to 0.5, PokeType.steel to 0.5)
    )

    /**
     * Calculates the combined effectiveness multiplier of an attacking type against a list of defending types.
     * @param attack The attacking Pokémon type.
     * @param defenseTypes A list of the defending Pokémon's types (usually 1 or 2).
     * @return The resulting damage multiplier (e.g., 4.0, 2.0, 1.0, 0.5, 0.25, 0.0).
     */
    fun effectiveness(attack: PokeType, defenseTypes: List<PokeType>): Double {
        // Start with a base multiplier of 1.0x
        // Fold (reduce) the list of defense types, multiplying the accumulator by the effectiveness
        // of the attack type against each defense type.
        return defenseTypes.fold(1.0) { acc, def ->
            acc * (chart[attack]?.get(def) ?: 1.0) // Get multiplier from chart, default to 1.0 if not found
        }
    }
}