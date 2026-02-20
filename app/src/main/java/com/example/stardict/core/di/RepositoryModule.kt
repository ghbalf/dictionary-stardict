package com.example.stardict.core.di

import com.example.stardict.data.repository.DictionaryRepositoryImpl
import com.example.stardict.data.repository.FavoriteRepositoryImpl
import com.example.stardict.data.repository.HistoryRepositoryImpl
import com.example.stardict.data.repository.PreferencesRepositoryImpl
import com.example.stardict.data.repository.SourceRepositoryImpl
import com.example.stardict.domain.repository.DictionaryRepository
import com.example.stardict.domain.repository.FavoriteRepository
import com.example.stardict.domain.repository.HistoryRepository
import com.example.stardict.domain.repository.PreferencesRepository
import com.example.stardict.domain.repository.SourceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDictionaryRepository(impl: DictionaryRepositoryImpl): DictionaryRepository

    @Binds
    @Singleton
    abstract fun bindSourceRepository(impl: SourceRepositoryImpl): SourceRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}
