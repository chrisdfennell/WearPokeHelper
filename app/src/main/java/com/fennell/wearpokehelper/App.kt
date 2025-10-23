package com.fennell.wearpokehelper

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import com.fennell.wearpokehelper.data.scheduleSpritePrefetch

class App : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        // Optional: warm the sprite cache occasionally
        scheduleSpritePrefetch(this)
    }

    override fun newImageLoader(): ImageLoader {
        val coilDiskCache = DiskCache.Builder()
            .directory(File(cacheDir, "coil_disk_cache"))
            .maxSizeBytes(100L * 1024 * 1024) // 100 MB
            .build()

        val okHttpCacheDir = File(cacheDir, "okhttp_cache")
        val okHttpClient = OkHttpClient.Builder()
            .cache(Cache(okHttpCacheDir, 50L * 1024 * 1024)) // 50 MB
            .build()

        val memoryCache = MemoryCache.Builder(this)
            .maxSizePercent(0.25) // up to 25% of heap
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .diskCache(coilDiskCache)
            .memoryCache(memoryCache)
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }
}