package com.example.stardict.ui.screen.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stardict.ui.component.WordListItem

@Composable
fun FavoritesScreen(
    onWordClick: (word: String, dictionaryId: Long, offset: Long, size: Int) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (favorites.isEmpty()) {
            Text(
                text = "No favorites yet",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn {
                items(favorites) { entry ->
                    WordListItem(
                        word = entry.word,
                        onClick = {
                            onWordClick(entry.word, entry.dictionaryId, 0, 0)
                        }
                    )
                }
            }
        }
    }
}
