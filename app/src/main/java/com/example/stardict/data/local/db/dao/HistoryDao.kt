package com.example.stardict.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.stardict.data.local.db.entity.HistoryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 200")
    fun getAll(): Flow<List<HistoryEntryEntity>>

    @Insert
    suspend fun insert(entity: HistoryEntryEntity)

    @Query("DELETE FROM history WHERE word = :word AND dictionaryId = :dictionaryId")
    suspend fun deleteByWordAndDict(word: String, dictionaryId: Long)

    @Query("DELETE FROM history")
    suspend fun clearAll()
}
