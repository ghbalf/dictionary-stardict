package com.example.stardict.domain.usecase

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.stardict.domain.model.DictionarySource
import com.example.stardict.domain.model.DownloadStatus
import com.example.stardict.domain.repository.SourceRepository
import com.example.stardict.worker.DictionaryDownloadWorker
import com.example.stardict.worker.DictionaryIndexWorker
import javax.inject.Inject

class DownloadDictionaryUseCase @Inject constructor(
    private val sourceRepository: SourceRepository,
    private val workManager: WorkManager
) {
    suspend operator fun invoke(name: String, url: String) {
        val source = DictionarySource(
            name = name,
            url = url,
            status = DownloadStatus.PENDING
        )
        val sourceId = sourceRepository.insertSource(source)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadWork = OneTimeWorkRequestBuilder<DictionaryDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(
                Data.Builder()
                    .putLong("source_id", sourceId)
                    .putString("url", url)
                    .build()
            )
            .build()

        val indexWork = OneTimeWorkRequestBuilder<DictionaryIndexWorker>()
            .setInputData(
                Data.Builder()
                    .putLong("source_id", sourceId)
                    .build()
            )
            .build()

        workManager.beginUniqueWork(
            "dict_download_$sourceId",
            ExistingWorkPolicy.KEEP,
            downloadWork
        ).then(indexWork).enqueue()
    }
}
