package com.example.stardict.data.repository

import android.util.LruCache
import com.example.stardict.data.local.db.dao.DictionaryDao
import com.example.stardict.data.local.db.dao.FavoriteDao
import com.example.stardict.data.local.db.entity.DictionaryEntity
import com.example.stardict.data.local.stardict.StarDictIndex
import com.example.stardict.domain.model.Definition
import com.example.stardict.domain.model.DefinitionField
import com.example.stardict.domain.model.Dictionary
import com.example.stardict.domain.model.SearchResult
import com.example.stardict.domain.model.WordEntry
import com.example.stardict.domain.repository.DictionaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictionaryRepositoryImpl @Inject constructor(
    private val dictionaryDao: DictionaryDao,
    private val favoriteDao: FavoriteDao
) : DictionaryRepository {

    private val indexCache = LruCache<Long, StarDictIndex>(3)
    private val cacheMutex = Mutex()

    override fun getAllDictionaries(): Flow<List<Dictionary>> =
        dictionaryDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getEnabledDictionaries(): Flow<List<Dictionary>> =
        dictionaryDao.getEnabled().map { list -> list.map { it.toDomain() } }

    override suspend fun getDictionary(id: Long): Dictionary? =
        dictionaryDao.getById(id)?.toDomain()

    override suspend fun insertDictionary(dictionary: Dictionary): Long =
        dictionaryDao.insert(dictionary.toEntity())

    override suspend fun updateDictionary(dictionary: Dictionary) {
        dictionaryDao.update(dictionary.toEntity())
        // Evict from cache if disabled
        if (!dictionary.enabled) {
            cacheMutex.withLock {
                indexCache.remove(dictionary.id)?.close()
            }
        }
    }

    override suspend fun deleteDictionary(id: Long) {
        cacheMutex.withLock {
            indexCache.remove(id)?.close()
        }
        dictionaryDao.delete(id)
    }

    override suspend fun searchWord(query: String, dictionaryIds: List<Long>?): List<SearchResult> =
        withContext(Dispatchers.IO) {
            val dicts = getTargetDictionaries(dictionaryIds)
            val resultMap = mutableMapOf<String, MutableList<WordEntry>>()

            for (dict in dicts) {
                val index = getOrLoadIndex(dict)
                val entries = index.prefixSearch(query)
                for (entry in entries) {
                    val wordEntry = WordEntry(
                        word = entry.word,
                        dictionaryId = dict.id,
                        dictionaryName = dict.name,
                        offset = entry.offset,
                        size = entry.size
                    )
                    resultMap.getOrPut(entry.word) { mutableListOf() }.add(wordEntry)
                }
            }

            resultMap.map { (word, entries) -> SearchResult(word, entries) }
                .sortedBy { it.word.lowercase() }
        }

    override suspend fun getDefinition(
        word: String,
        dictionaryId: Long,
        offset: Long,
        size: Int
    ): Definition? = withContext(Dispatchers.IO) {
        val dict = dictionaryDao.getById(dictionaryId) ?: return@withContext null
        val index = getOrLoadIndex(dict)

        // If offset and size are 0, do an exact lookup first
        val indexEntry = if (offset == 0L && size == 0) {
            index.exactLookup(word).firstOrNull() ?: return@withContext null
        } else {
            com.example.stardict.data.local.stardict.model.IndexEntry(word, offset, size)
        }

        val article = index.readArticle(indexEntry)
        val isFavorite = favoriteDao.exists(word, dictionaryId)
        Definition(
            word = word,
            dictionaryName = dict.name,
            fields = article.fields.map { DefinitionField(it.type, it.content) },
            isFavorite = isFavorite
        )
    }

    override suspend fun fuzzySearch(query: String, dictionaryIds: List<Long>?): List<SearchResult> =
        withContext(Dispatchers.IO) {
            val dicts = getTargetDictionaries(dictionaryIds)
            val resultMap = mutableMapOf<String, MutableList<WordEntry>>()

            for (dict in dicts) {
                val index = getOrLoadIndex(dict)
                val entries = index.fuzzySearch(query)
                for (entry in entries) {
                    val wordEntry = WordEntry(
                        word = entry.word,
                        dictionaryId = dict.id,
                        dictionaryName = dict.name,
                        offset = entry.offset,
                        size = entry.size
                    )
                    resultMap.getOrPut(entry.word) { mutableListOf() }.add(wordEntry)
                }
            }

            resultMap.map { (word, entries) -> SearchResult(word, entries) }
                .sortedBy { it.word.lowercase() }
        }

    private suspend fun getTargetDictionaries(dictionaryIds: List<Long>?): List<DictionaryEntity> {
        return if (dictionaryIds != null) {
            dictionaryDao.getAll().first().filter { it.id in dictionaryIds }
        } else {
            dictionaryDao.getEnabled().first()
        }
    }

    private suspend fun getOrLoadIndex(dict: DictionaryEntity): StarDictIndex {
        cacheMutex.withLock {
            val cached = indexCache.get(dict.id)
            if (cached != null) return cached

            val index = StarDictIndex.load(dict.basePath)
            indexCache.put(dict.id, index)
            return index
        }
    }

    private fun DictionaryEntity.toDomain() = Dictionary(
        id = id,
        name = name,
        description = description,
        wordCount = wordCount,
        basePath = basePath,
        sourceId = sourceId,
        enabled = enabled
    )

    private fun Dictionary.toEntity() = DictionaryEntity(
        id = id,
        name = name,
        description = description,
        wordCount = wordCount,
        basePath = basePath,
        sourceId = sourceId,
        enabled = enabled
    )
}
