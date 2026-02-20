package com.example.stardict.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dictionaries")
data class DictionaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val wordCount: Int = 0,
    val basePath: String,
    val sourceId: Long,
    val enabled: Boolean = true
)
