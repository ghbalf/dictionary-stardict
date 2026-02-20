package com.example.stardict.domain.model

data class SearchResult(
    val word: String,
    val entries: List<WordEntry>
)
