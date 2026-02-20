package com.example.stardict.ui.screen.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stardict.ui.component.WordListItem

@Composable
fun HistoryScreen(
    onWordClick: (word: String, dictionaryId: Long, offset: Long, size: Int) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val history by viewModel.history.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (history.isEmpty()) {
            Text(
                text = "No history yet",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        IconButton(
                            onClick = viewModel::clearHistory,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Clear history",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                items(history) { entry ->
                    WordListItem(
                        word = entry.word,
                        onClick = {
                            // Look up the word again to get offset/size
                            viewModel.lookupWord(entry.word, entry.dictionaryId)
                            // For simplicity, pass 0/0 and let the definition screen
                            // do an exact lookup
                            onWordClick(entry.word, entry.dictionaryId, 0, 0)
                        }
                    )
                }
            }
        }
    }
}
