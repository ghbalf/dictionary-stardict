package com.example.stardict.domain.repository

import com.example.stardict.domain.model.DictionarySource
import com.example.stardict.domain.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

interface SourceRepository {
    fun getAllSources(): Flow<List<DictionarySource>>
    suspend fun getSource(id: Long): DictionarySource?
    suspend fun insertSource(source: DictionarySource): Long
    suspend fun updateSourceStatus(id: Long, status: DownloadStatus, progress: Int = 0)
    suspend fun deleteSource(id: Long)
}
