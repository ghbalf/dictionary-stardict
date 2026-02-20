package com.example.stardict.data.repository

import com.example.stardict.data.local.db.dao.SourceDao
import com.example.stardict.data.local.db.entity.DictionarySourceEntity
import com.example.stardict.domain.model.DictionarySource
import com.example.stardict.domain.model.DownloadStatus
import com.example.stardict.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceRepositoryImpl @Inject constructor(
    private val sourceDao: SourceDao
) : SourceRepository {

    override fun getAllSources(): Flow<List<DictionarySource>> =
        sourceDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getSource(id: Long): DictionarySource? =
        sourceDao.getById(id)?.toDomain()

    override suspend fun insertSource(source: DictionarySource): Long =
        sourceDao.insert(source.toEntity())

    override suspend fun updateSourceStatus(id: Long, status: DownloadStatus, progress: Int) {
        sourceDao.updateStatus(id, status.name, progress)
    }

    override suspend fun deleteSource(id: Long) {
        sourceDao.delete(id)
    }

    private fun DictionarySourceEntity.toDomain() = DictionarySource(
        id = id,
        name = name,
        url = url,
        status = try { DownloadStatus.valueOf(status) } catch (_: Exception) { DownloadStatus.PENDING },
        progress = progress
    )

    private fun DictionarySource.toEntity() = DictionarySourceEntity(
        id = id,
        name = name,
        url = url,
        status = status.name,
        progress = progress
    )
}
