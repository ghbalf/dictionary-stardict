package com.example.stardict.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val dictionaryId: Long,
    val timestamp: Long = System.currentTimeMillis()
)
