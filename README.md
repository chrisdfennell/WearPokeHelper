# WearPokeHelper

A lightning-fast Wear OS Pokémon helper app. Search Pokémon, see type analysis, and view example counters — all optimized for round/square watches, rotary crown scrolling, and low-power constraints.

## Highlights

- 🔎 **Search** with tiny **sprite icons** next to each result
- 💥 **Haptic feedback** on taps
- 🧭 **Rotary crown** scrolling that just works
- 🖼️ **Aggressive caching** of sprites (memory + disk + optional background prefetch)
- 📶 Offline-friendly: recently viewed + prefetched sprites remain snappy

## Tech

- Kotlin • Jetpack Compose for Wear OS
- Coil for images & caching
- WorkManager prefetch (optional)
- Retrofit + Moshi
- Coroutines / Flows

## Setup

1) Open the project in Android Studio Ladybug or newer.
2) Ensure a Wear OS 3+ emulator/device.
3) Build & Run.

### Dependencies (module `app/build.gradle.kts`)

```kotlin
// Already added by this update:
implementation("io.coil-kt:coil-compose:2.6.0")
implementation("androidx.work:work-runtime-ktx:2.9.1")
```

## Caching

We install a custom Coil `ImageLoader` via an `Application` (`App.kt`) with:
- **Memory cache** ~25% of heap (LRU)
- **Disk cache** ~100 MB (tune for your device)
- **OkHttp cache** ~50 MB for HTTP response reuse
- `respectCacheHeaders(false)` to keep sprites around longer

Optional: `SpritePrefetchWorker` warms sprites for IDs 1–200 on charge + unmetered network.

## Rotary Crown

The main `ScalingLazyColumn` requests focus on launch and is `focusable()` so rotary events scroll the list immediately.

## Haptics

`hapticClick(context)` uses `VibratorManager` on API 31+ and fallback on older. We invoke it on row taps for a subtle click.

## Sprite Icons

- Search results: 28dp sprite next to each name.
- Selected Pokémon header: 56dp sprite to the left of the name.
- Example counters: 24dp sprite for each example chip.

Sprite URL pattern:
`https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/{id}.png`

IDs are resolved from the `/pokemon` list endpoint by parsing the resource `url` → `nameToId` map.

## Future Ideas

- Tile: “Random Pokémon” or “Last viewed”
- Complication: Favorite count with Pokéball icon
- Type filter chips in search
- Stats mini-radials on detail
- Send to phone

## License

MIT (or your preference)