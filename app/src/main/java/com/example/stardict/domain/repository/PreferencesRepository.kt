package com.example.stardict.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}
