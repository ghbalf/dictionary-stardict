package com.example.stardict.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.stardict.data.local.db.dao.DictionaryDao
import com.example.stardict.data.local.db.dao.SourceDao
import com.example.stardict.data.local.db.entity.DictionaryEntity
import com.example.stardict.data.local.stardict.IfoParser
import com.example.stardict.domain.model.DownloadStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream

@HiltWorker
class DictionaryIndexWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sourceDao: SourceDao,
    private val dictionaryDao: DictionaryDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val sourceId = inputData.getLong("source_id", -1)
        if (sourceId == -1L) return@withContext Result.failure()

        val source = sourceDao.getById(sourceId) ?: return@withContext Result.failure()
        val zipPath = source.filePath ?: return@withContext Result.failure()

        try {
            sourceDao.updateStatus(sourceId, DownloadStatus.EXTRACTING.name, 0)

            val extractDir = File(applicationContext.filesDir, "dicts/$sourceId")
            extractDir.mkdirs()

            extractArchive(File(zipPath), extractDir)

            // Find all .ifo files and register dictionaries
            val ifoFiles = findIfoFiles(extractDir)
            val ifoParser = IfoParser()

            for (ifoFile in ifoFiles) {
                val ifo = ifoParser.parse(ifoFile)
                val basePath = ifoFile.absolutePath.removeSuffix(".ifo")

                dictionaryDao.insert(
                    DictionaryEntity(
                        name = ifo.bookname,
                        description = ifo.description,
                        wordCount = ifo.wordcount,
                        basePath = basePath,
                        sourceId = sourceId,
                        enabled = true
                    )
                )
            }

            sourceDao.updateStatus(sourceId, DownloadStatus.COMPLETED.name, 100)

            // Clean up ZIP file
            File(zipPath).delete()

            Result.success()
        } catch (e: Exception) {
            sourceDao.updateStatus(sourceId, DownloadStatus.FAILED.name, 0)
            Result.failure()
        }
    }

    private fun extractArchive(archiveFile: File, destDir: File) {
        val name = archiveFile.name.lowercase()
        when {
            name.endsWith(".tar.gz") || name.endsWith(".tgz") ->
                extractTar(TarArchiveInputStream(GZIPInputStream(archiveFile.inputStream().buffered())), destDir)
            name.endsWith(".tar.xz") ->
                extractTar(TarArchiveInputStream(XZCompressorInputStream(archiveFile.inputStream().buffered())), destDir)
            else ->
                extractZip(archiveFile, destDir)
        }
    }

    private fun extractTar(tais: TarArchiveInputStream, destDir: File) {
        tais.use { tar ->
            var entry = tar.nextEntry
            while (entry != null) {
                val file = File(destDir, entry.name)

                if (!file.canonicalPath.startsWith(destDir.canonicalPath)) {
                    throw SecurityException("Path traversal detected: ${entry.name}")
                }

                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    file.outputStream().buffered().use { output ->
                        tar.copyTo(output)
                    }
                }
                entry = tar.nextEntry
            }
        }
    }

    private fun extractZip(zipFile: File, destDir: File) {
        ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val file = File(destDir, entry.name)

                if (!file.canonicalPath.startsWith(destDir.canonicalPath)) {
                    throw SecurityException("Zip slip detected: ${entry.name}")
                }

                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    file.outputStream().buffered().use { output ->
                        zis.copyTo(output)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private fun findIfoFiles(dir: File): List<File> {
        val result = mutableListOf<File>()
        dir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension == "ifo") {
                result.add(file)
            }
        }
        return result
    }
}
