package com.example.stardict.data.repository

import com.example.stardict.data.local.db.dao.FavoriteDao
import com.example.stardict.data.local.db.entity.FavoriteEntryEntity
import com.example.stardict.domain.model.FavoriteEntry
import com.example.stardict.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {

    override fun getFavorites(): Flow<List<FavoriteEntry>> =
        favoriteDao.getAll().map { list ->
            list.map {
                FavoriteEntry(
                    id = it.id,
                    word = it.word,
                    dictionaryId = it.dictionaryId,
                    timestamp = it.timestamp
                )
            }
        }

    override suspend fun isFavorite(word: String, dictionaryId: Long): Boolean =
        favoriteDao.exists(word, dictionaryId)

    override suspend fun toggleFavorite(word: String, dictionaryId: Long) {
        if (favoriteDao.exists(word, dictionaryId)) {
            favoriteDao.delete(word, dictionaryId)
        } else {
            favoriteDao.insert(FavoriteEntryEntity(word = word, dictionaryId = dictionaryId))
        }
    }

    override suspend fun removeFavorite(word: String, dictionaryId: Long) {
        favoriteDao.delete(word, dictionaryId)
    }
}
