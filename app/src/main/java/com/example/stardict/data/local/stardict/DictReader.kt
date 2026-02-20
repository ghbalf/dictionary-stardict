package com.example.stardict.data.local.stardict

import com.example.stardict.data.local.stardict.model.ArticleField
import com.example.stardict.data.local.stardict.model.DictArticle
import org.dict.zip.DictZipInputStream
import org.dict.zip.RandomAccessInputStream
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class DictReader(
    private val dictFile: File,
    private val sametypesequence: String? = null
) {
    private val isCompressed = dictFile.name.endsWith(".dz")

    fun readArticle(offset: Long, size: Int): DictArticle {
        val data = readRawData(offset, size)
        val fields = if (sametypesequence != null) {
            parseSameTypeSequence(data, sametypesequence)
        } else {
            parseGenericFormat(data)
        }
        return DictArticle(fields)
    }

    private fun readRawData(offset: Long, size: Int): ByteArray {
        return if (isCompressed) {
            readCompressed(offset, size)
        } else {
            readUncompressed(offset, size)
        }
    }

    private fun readUncompressed(offset: Long, size: Int): ByteArray {
        RandomAccessFile(dictFile, "r").use { raf ->
            val channel = raf.channel
            val buffer = channel.map(FileChannel.MapMode.READ_ONLY, offset, size.toLong())
            val data = ByteArray(size)
            buffer.get(data)
            return data
        }
    }

    private fun readCompressed(offset: Long, size: Int): ByteArray {
        val raf = RandomAccessFile(dictFile, "r")
        val rais = RandomAccessInputStream(raf)
        DictZipInputStream(rais).use { dzis ->
            dzis.seek(offset)
            val data = ByteArray(size)
            var totalRead = 0
            while (totalRead < size) {
                val read = dzis.read(data, totalRead, size - totalRead)
                if (read == -1) break
                totalRead += read
            }
            return data.copyOf(totalRead)
        }
    }

    /**
     * With sametypesequence: type chars are implied, not stored in data.
     * Each field except the last is null-terminated.
     * The last field consumes remaining data with no terminator.
     */
    private fun parseSameTypeSequence(data: ByteArray, types: String): List<ArticleField> {
        val fields = mutableListOf<ArticleField>()
        var pos = 0

        for (i in types.indices) {
            val type = types[i]
            if (i == types.length - 1) {
                // Last field: consumes all remaining data
                val content = String(data, pos, data.size - pos, Charsets.UTF_8)
                fields.add(ArticleField(type, content))
            } else {
                // Find null terminator
                val nullPos = findNull(data, pos)
                val content = String(data, pos, nullPos - pos, Charsets.UTF_8)
                fields.add(ArticleField(type, content))
                pos = nullPos + 1
            }
        }

        return fields
    }

    /**
     * Without sametypesequence: each field starts with a type byte.
     * Lowercase type = null-terminated string field.
     * Uppercase type = size-prefixed binary field (4-byte big-endian size).
     */
    private fun parseGenericFormat(data: ByteArray): List<ArticleField> {
        val fields = mutableListOf<ArticleField>()
        val buffer = ByteBuffer.wrap(data)

        while (buffer.hasRemaining()) {
            val type = buffer.get().toInt().toChar()

            if (type.isLowerCase()) {
                // Null-terminated string
                val start = buffer.position()
                val nullPos = findNull(data, start)
                val content = String(data, start, nullPos - start, Charsets.UTF_8)
                fields.add(ArticleField(type, content))
                buffer.position(nullPos + 1)
            } else {
                // Size-prefixed binary data
                val size = buffer.int
                val content = ByteArray(size)
                buffer.get(content)
                fields.add(ArticleField(type.lowercaseChar(), String(content, Charsets.UTF_8)))
            }
        }

        return fields
    }

    private fun findNull(data: ByteArray, from: Int): Int {
        for (i in from until data.size) {
            if (data[i] == 0.toByte()) return i
        }
        return data.size
    }

    fun close() {
        // No persistent resources to close for memory-mapped reads
    }
}
