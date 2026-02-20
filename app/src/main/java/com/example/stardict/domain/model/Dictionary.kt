package com.example.stardict.domain.model

data class Dictionary(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val wordCount: Int = 0,
    val basePath: String,
    val sourceId: Long,
    val enabled: Boolean = true
)
