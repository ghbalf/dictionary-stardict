package com.example.stardict.domain.usecase

import com.example.stardict.domain.model.Definition
import com.example.stardict.domain.repository.DictionaryRepository
import com.example.stardict.domain.repository.HistoryRepository
import javax.inject.Inject

class GetDefinitionUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(
        word: String,
        dictionaryId: Long,
        offset: Long,
        size: Int
    ): Definition? {
        val definition = dictionaryRepository.getDefinition(word, dictionaryId, offset, size)
        if (definition != null) {
            historyRepository.addToHistory(word, dictionaryId)
        }
        return definition
    }
}
