package com.example.stardict.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.stardict.data.local.db.entity.DictionaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DictionaryDao {
    @Query("SELECT * FROM dictionaries ORDER BY name")
    fun getAll(): Flow<List<DictionaryEntity>>

    @Query("SELECT * FROM dictionaries WHERE enabled = 1 ORDER BY name")
    fun getEnabled(): Flow<List<DictionaryEntity>>

    @Query("SELECT * FROM dictionaries WHERE id = :id")
    suspend fun getById(id: Long): DictionaryEntity?

    @Query("SELECT * FROM dictionaries WHERE sourceId = :sourceId")
    suspend fun getBySourceId(sourceId: Long): List<DictionaryEntity>

    @Insert
    suspend fun insert(entity: DictionaryEntity): Long

    @Update
    suspend fun update(entity: DictionaryEntity)

    @Query("DELETE FROM dictionaries WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM dictionaries WHERE sourceId = :sourceId")
    suspend fun deleteBySourceId(sourceId: Long)
}
