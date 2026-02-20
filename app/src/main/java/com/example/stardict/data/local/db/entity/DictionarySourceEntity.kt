package com.example.stardict.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dictionary_sources")
data class DictionarySourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val status: String = "PENDING",
    val progress: Int = 0,
    val filePath: String? = null
)
