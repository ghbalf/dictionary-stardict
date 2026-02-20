package com.example.stardict.domain.model

data class DictionarySource(
    val id: Long = 0,
    val name: String,
    val url: String,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val progress: Int = 0
)

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    EXTRACTING,
    COMPLETED,
    FAILED
}
