package com.example.stardict.domain.repository

import com.example.stardict.domain.model.Definition
import com.example.stardict.domain.model.Dictionary
import com.example.stardict.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface DictionaryRepository {
    fun getAllDictionaries(): Flow<List<Dictionary>>
    fun getEnabledDictionaries(): Flow<List<Dictionary>>
    suspend fun getDictionary(id: Long): Dictionary?
    suspend fun insertDictionary(dictionary: Dictionary): Long
    suspend fun updateDictionary(dictionary: Dictionary)
    suspend fun deleteDictionary(id: Long)
    suspend fun searchWord(query: String, dictionaryIds: List<Long>? = null): List<SearchResult>
    suspend fun getDefinition(word: String, dictionaryId: Long, offset: Long, size: Int): Definition?
    suspend fun fuzzySearch(query: String, dictionaryIds: List<Long>? = null): List<SearchResult>
}
