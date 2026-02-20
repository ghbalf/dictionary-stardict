package com.example.stardict.ui.screen.dictmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stardict.domain.model.DownloadStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictManagerScreen(
    onBack: () -> Unit,
    viewModel: DictManagerViewModel = hiltViewModel()
) {
    val dictionaries by viewModel.dictionaries.collectAsState()
    val sources by viewModel.sources.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    var editingSource by remember { mutableStateOf<com.example.stardict.domain.model.DictionarySource?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dictionaries") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showAddDialog) {
                Icon(Icons.Default.Add, contentDescription = "Add dictionary")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Active downloads / pending sources
            val activeSources = sources.filter { it.status != DownloadStatus.COMPLETED }
            if (activeSources.isNotEmpty()) {
                item {
                    Text(
                        text = "Downloads",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                items(activeSources) { source ->
                    DownloadItem(
                        source = source,
                        onEdit = { editingSource = source },
                        onRetry = { viewModel.retrySource(source) },
                        onDelete = { viewModel.deleteSource(source) }
                    )
                }
                item { HorizontalDivider() }
            }

            // Installed dictionaries
            if (dictionaries.isNotEmpty()) {
                item {
                    Text(
                        text = "Installed",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                items(dictionaries) { dict ->
                    DictionaryItem(
                        name = dict.name,
                        description = dict.description,
                        wordCount = dict.wordCount,
                        enabled = dict.enabled,
                        onToggle = { viewModel.toggleDictionary(dict) },
                        onDelete = { viewModel.deleteDictionary(dict) }
                    )
                }
            }

            if (dictionaries.isEmpty() && activeSources.isEmpty()) {
                item {
                    Text(
                        text = "No dictionaries installed.\nTap + to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }
        }

        if (showAddDialog) {
            EditDictionaryDialog(
                title = "Add Dictionary",
                initialName = "",
                initialUrl = "",
                confirmLabel = "Download",
                onDismiss = viewModel::hideAddDialog,
                onConfirm = viewModel::addDictionary
            )
        }

        editingSource?.let { source ->
            EditDictionaryDialog(
                title = "Edit Dictionary",
                initialName = source.name,
                initialUrl = source.url,
                confirmLabel = "Save & Retry",
                onDismiss = { editingSource = null },
                onConfirm = { name, url ->
                    viewModel.editAndRetrySource(source, name, url)
                    editingSource = null
                }
            )
        }
    }
}

@Composable
private fun DownloadItem(
    source: com.example.stardict.domain.model.DictionarySource,
    onEdit: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = source.name, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (source.status) {
                        DownloadStatus.PENDING -> "Waiting..."
                        DownloadStatus.DOWNLOADING -> "Downloading ${source.progress}%"
                        DownloadStatus.EXTRACTING -> "Extracting..."
                        DownloadStatus.FAILED -> "Failed"
                        DownloadStatus.COMPLETED -> "Completed"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (source.status == DownloadStatus.FAILED)
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (source.status == DownloadStatus.DOWNLOADING) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { source.progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            if (source.status == DownloadStatus.FAILED) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, contentDescription = "Retry")
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DictionaryItem(
    name: String,
    description: String?,
    wordCount: Int,
    enabled: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = MaterialTheme.typography.bodyLarge)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$wordCount words",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = enabled, onCheckedChange = { onToggle() })
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
    HorizontalDivider()
}

@Composable
private fun EditDictionaryDialog(
    title: String,
    initialName: String,
    initialUrl: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, url: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var url by remember { mutableStateOf(initialUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, url) },
                enabled = name.isNotBlank() && url.isNotBlank()
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
