package com.example.stardict.domain.usecase

import com.example.stardict.domain.model.SearchResult
import com.example.stardict.domain.repository.DictionaryRepository
import javax.inject.Inject

class SearchWordUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) {
    suspend operator fun invoke(
        query: String,
        dictionaryIds: List<Long>? = null
    ): List<SearchResult> {
        if (query.isBlank()) return emptyList()
        return dictionaryRepository.searchWord(query.trim(), dictionaryIds)
    }
}
