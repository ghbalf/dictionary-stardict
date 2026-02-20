package com.example.stardict.ui.screen.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
fun SearchScreen(
    onWordClick: (word: String, dictionaryId: Long, offset: Long, size: Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val dictionaries by viewModel.dictionaries.collectAsState()
    val selectedDictId by viewModel.selectedDictId.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search words...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true
        )

        if (dictionaries.size > 1) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedDictId == null,
                        onClick = { viewModel.onDictFilterChange(null) },
                        label = { Text("All") },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                items(dictionaries) { dict ->
                    FilterChip(
                        selected = selectedDictId == dict.id,
                        onClick = { viewModel.onDictFilterChange(dict.id) },
                        label = { Text(dict.name) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isSearching -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                query.isNotEmpty() && results.isEmpty() -> {
                    Text(
                        text = "No results found",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                query.isEmpty() -> {
                    Text(
                        text = "Type to search",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    LazyColumn {
                        for (result in results) {
                            items(result.entries) { entry ->
                                WordListItem(
                                    word = entry.word,
                                    dictionaryName = if (dictionaries.size > 1) entry.dictionaryName else null,
                                    onClick = {
                                        onWordClick(
                                            entry.word,
                                            entry.dictionaryId,
                                            entry.offset,
                                            entry.size
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
