package com.example.stardict.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val dictionaryId: Long,
    val timestamp: Long = System.currentTimeMillis()
)
