package com.example.stardict.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.stardict.data.local.db.entity.FavoriteEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAll(): Flow<List<FavoriteEntryEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE word = :word AND dictionaryId = :dictionaryId)")
    suspend fun exists(word: String, dictionaryId: Long): Boolean

    @Insert
    suspend fun insert(entity: FavoriteEntryEntity)

    @Query("DELETE FROM favorites WHERE word = :word AND dictionaryId = :dictionaryId")
    suspend fun delete(word: String, dictionaryId: Long)
}
