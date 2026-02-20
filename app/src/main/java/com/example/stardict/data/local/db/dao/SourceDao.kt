package com.example.stardict.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.stardict.data.local.db.entity.DictionarySourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    @Query("SELECT * FROM dictionary_sources ORDER BY id DESC")
    fun getAll(): Flow<List<DictionarySourceEntity>>

    @Query("SELECT * FROM dictionary_sources WHERE id = :id")
    suspend fun getById(id: Long): DictionarySourceEntity?

    @Insert
    suspend fun insert(entity: DictionarySourceEntity): Long

    @Query("UPDATE dictionary_sources SET status = :status, progress = :progress WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, progress: Int)

    @Query("UPDATE dictionary_sources SET filePath = :filePath WHERE id = :id")
    suspend fun updateFilePath(id: Long, filePath: String)

    @Query("DELETE FROM dictionary_sources WHERE id = :id")
    suspend fun delete(id: Long)
}
