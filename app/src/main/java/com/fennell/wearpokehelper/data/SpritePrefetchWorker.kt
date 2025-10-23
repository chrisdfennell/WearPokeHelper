package com.fennell.wearpokehelper.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpritePrefetchWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(appContext)
            val ids = 1..200 // tune: hot set

            for (id in ids) {
                val url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
                val req = ImageRequest.Builder(appContext)
                    .data(url)
                    .build()
                loader.execute(req)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}