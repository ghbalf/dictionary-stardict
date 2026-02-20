package com.example.stardict.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stardict.ui.screen.definition.DefinitionScreen
import com.example.stardict.ui.screen.dictmanager.DictManagerScreen
import com.example.stardict.ui.screen.favorites.FavoritesScreen
import com.example.stardict.ui.screen.history.HistoryScreen
import com.example.stardict.ui.screen.search.SearchScreen
import com.example.stardict.ui.screen.settings.SettingsScreen
import java.net.URLDecoder
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarDictNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.screen.route }
    val showTopBar = showBottomBar

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text("StarDict") },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.DictManager.route) }) {
                            @Suppress("DEPRECATION")
                            Icon(Icons.Default.LibraryBooks, contentDescription = "Dictionaries")
                        }
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Search.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Search.route) {
                SearchScreen(
                    onWordClick = { word, dictId, offset, size ->
                        val encoded = URLEncoder.encode(word, "UTF-8")
                        navController.navigate(
                            Screen.Definition.createRoute(encoded, dictId, offset, size)
                        )
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onWordClick = { word, dictId, offset, size ->
                        val encoded = URLEncoder.encode(word, "UTF-8")
                        navController.navigate(
                            Screen.Definition.createRoute(encoded, dictId, offset, size)
                        )
                    }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onWordClick = { word, dictId, offset, size ->
                        val encoded = URLEncoder.encode(word, "UTF-8")
                        navController.navigate(
                            Screen.Definition.createRoute(encoded, dictId, offset, size)
                        )
                    }
                )
            }

            composable(
                route = Screen.Definition.route,
                arguments = listOf(
                    navArgument("word") { type = NavType.StringType },
                    navArgument("dictionaryId") { type = NavType.LongType },
                    navArgument("offset") { type = NavType.LongType },
                    navArgument("size") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val word = URLDecoder.decode(
                    backStackEntry.arguments?.getString("word") ?: "",
                    "UTF-8"
                )
                DefinitionScreen(
                    word = word,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.DictManager.route) {
                DictManagerScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
