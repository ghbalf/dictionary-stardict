package com.example.stardict.data.repository

import com.example.stardict.data.local.preferences.UserPreferencesDataStore
import com.example.stardict.domain.repository.PreferencesRepository
import com.example.stardict.domain.repository.ThemeMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore
) : PreferencesRepository {

    override fun getThemeMode(): Flow<ThemeMode> = dataStore.getThemeMode()

    override suspend fun setThemeMode(mode: ThemeMode) = dataStore.setThemeMode(mode)
}
