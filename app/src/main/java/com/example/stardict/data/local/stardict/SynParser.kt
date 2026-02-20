package com.example.stardict.data.local.stardict

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * Parses the optional .syn synonym file.
 * Format: [synonym UTF-8 + \0] [4-byte big-endian index into .idx entry list]
 * Returns map of synonym -> index position in the idx entry list.
 */
class SynParser {

    data class SynonymEntry(
        val synonym: String,
        val originalIndex: Int
    )

    fun parse(file: File): List<SynonymEntry> {
        if (!file.exists()) return emptyList()

        val entries = mutableListOf<SynonymEntry>()

        RandomAccessFile(file, "r").use { raf ->
            val channel = raf.channel
            val buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
            buffer.order(ByteOrder.BIG_ENDIAN)

            while (buffer.hasRemaining()) {
                val synonym = readNullTerminatedString(buffer)
                val originalIndex = buffer.int
                entries.add(SynonymEntry(synonym, originalIndex))
            }
        }

        return entries
    }

    private fun readNullTerminatedString(buffer: ByteBuffer): String {
        val start = buffer.position()
        while (buffer.hasRemaining()) {
            if (buffer.get() == 0.toByte()) {
                val length = buffer.position() - 1 - start
                val bytes = ByteArray(length)
                buffer.position(start)
                buffer.get(bytes)
                buffer.get() // consume null terminator
                return String(bytes, Charsets.UTF_8)
            }
        }
        val length = buffer.position() - start
        val bytes = ByteArray(length)
        buffer.position(start)
        buffer.get(bytes)
        return String(bytes, Charsets.UTF_8)
    }
}
