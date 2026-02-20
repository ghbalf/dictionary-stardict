package com.example.stardict.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Search : Screen("search")
    data object History : Screen("history")
    data object Favorites : Screen("favorites")
    data object Definition : Screen("definition/{word}/{dictionaryId}/{offset}/{size}") {
        fun createRoute(word: String, dictionaryId: Long, offset: Long, size: Int) =
            "definition/$word/$dictionaryId/$offset/$size"
    }
    data object DictManager : Screen("dict_manager")
    data object Settings : Screen("settings")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Search, "Search", Icons.Default.Search),
    BottomNavItem(Screen.History, "History", Icons.Default.History),
    BottomNavItem(Screen.Favorites, "Favorites", Icons.Default.Bookmark)
)
