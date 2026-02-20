package com.example.stardict.domain.model

data class Definition(
    val word: String,
    val dictionaryName: String,
    val fields: List<DefinitionField>,
    val isFavorite: Boolean = false
)

data class DefinitionField(
    val type: Char,
    val content: String
)
