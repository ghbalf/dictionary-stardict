package com.example.stardict.domain.model

data class WordEntry(
    val word: String,
    val dictionaryId: Long,
    val dictionaryName: String,
    val offset: Long,
    val size: Int
)
