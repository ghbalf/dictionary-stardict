package com.example.stardict.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stardict.data.local.db.dao.DictionaryDao
import com.example.stardict.data.local.db.dao.FavoriteDao
import com.example.stardict.data.local.db.dao.HistoryDao
import com.example.stardict.data.local.db.dao.SourceDao
import com.example.stardict.data.local.db.entity.DictionaryEntity
import com.example.stardict.data.local.db.entity.DictionarySourceEntity
import com.example.stardict.data.local.db.entity.FavoriteEntryEntity
import com.example.stardict.data.local.db.entity.HistoryEntryEntity

@Database(
    entities = [
        DictionaryEntity::class,
        DictionarySourceEntity::class,
        HistoryEntryEntity::class,
        FavoriteEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dictionaryDao(): DictionaryDao
    abstract fun sourceDao(): SourceDao
    abstract fun historyDao(): HistoryDao
    abstract fun favoriteDao(): FavoriteDao
}
