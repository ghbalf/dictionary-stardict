package com.example.stardict.domain.model

data class FavoriteEntry(
    val id: Long = 0,
    val word: String,
    val dictionaryId: Long,
    val timestamp: Long
)
