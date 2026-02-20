package com.example.stardict.data.local.stardict

import com.example.stardict.data.local.stardict.model.IndexEntry
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class IdxParser {

    fun parse(file: File, offsetBits: Int = 32): List<IndexEntry> {
        val entries = mutableListOf<IndexEntry>()

        RandomAccessFile(file, "r").use { raf ->
            val channel = raf.channel
            val buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
            buffer.order(ByteOrder.BIG_ENDIAN)

            while (buffer.hasRemaining()) {
                val word = readNullTerminatedString(buffer)
                val offset: Long
                val size: Int

                if (offsetBits == 64) {
                    offset = buffer.long
                    size = buffer.int
                } else {
                    offset = buffer.int.toLong() and 0xFFFFFFFFL
                    size = buffer.int
                }

                entries.add(IndexEntry(word, offset, size))
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
        // Shouldn't happen in a well-formed file
        val length = buffer.position() - start
        val bytes = ByteArray(length)
        buffer.position(start)
        buffer.get(bytes)
        return String(bytes, Charsets.UTF_8)
    }
}
