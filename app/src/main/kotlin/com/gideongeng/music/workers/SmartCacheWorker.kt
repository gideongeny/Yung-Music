package com.gideongeng.music.workers

import android.content.Context
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gideongeng.music.db.MusicDatabase
import com.gideongeng.music.playback.ExoDownloadService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

import com.gideongeng.music.constants.SongSortType

@HiltWorker
class SmartCacheWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: MusicDatabase,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val likedSongs = database.likedSongs(SongSortType.CREATE_DATE, descending = true).first()
            for (song in likedSongs) {
                if (!song.song.isDownloaded) {
                    val downloadRequest =
                        DownloadRequest
                            .Builder(song.id, song.id.toUri())
                            .setCustomCacheKey(song.id)
                            .setData(song.song.title.toByteArray())
                            .build()
                    DownloadService.sendAddDownload(
                        appContext,
                        ExoDownloadService::class.java,
                        downloadRequest,
                        false,
                    )
                }
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
