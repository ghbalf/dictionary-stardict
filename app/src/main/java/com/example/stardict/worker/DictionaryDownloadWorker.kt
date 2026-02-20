package com.example.stardict.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.stardict.data.local.db.dao.SourceDao
import com.example.stardict.data.remote.DownloadService
import com.example.stardict.domain.model.DownloadStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class DictionaryDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val downloadService: DownloadService,
    private val sourceDao: SourceDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val sourceId = inputData.getLong("source_id", -1)
        val url = inputData.getString("url") ?: return Result.failure()

        if (sourceId == -1L) return Result.failure()

        return try {
            sourceDao.updateStatus(sourceId, DownloadStatus.DOWNLOADING.name, 0)

            val downloadDir = File(applicationContext.filesDir, "downloads")
            downloadDir.mkdirs()
            val extension = guessExtension(url)
            val zipFile = File(downloadDir, "dict_${sourceId}$extension")

            downloadService.downloadFile(url, zipFile) { progress ->
                sourceDao.updateStatus(sourceId, DownloadStatus.DOWNLOADING.name, progress)
                setProgress(workDataOf("progress" to progress))
            }

            sourceDao.updateFilePath(sourceId, zipFile.absolutePath)

            Result.success(
                Data.Builder()
                    .putLong("source_id", sourceId)
                    .putString("zip_path", zipFile.absolutePath)
                    .build()
            )
        } catch (e: Exception) {
            sourceDao.updateStatus(sourceId, DownloadStatus.FAILED.name, 0)
            Result.failure()
        }
    }

    private fun guessExtension(url: String): String {
        val lower = url.lowercase()
        return when {
            lower.endsWith(".tar.gz") || lower.endsWith(".tgz") -> ".tar.gz"
            lower.endsWith(".tar.xz") -> ".tar.xz"
            lower.endsWith(".tar.bz2") -> ".tar.bz2"
            else -> ".zip"
        }
    }
}
