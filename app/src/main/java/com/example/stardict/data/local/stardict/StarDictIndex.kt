package com.example.stardict.data.local.stardict

import com.example.stardict.data.local.stardict.model.DictArticle
import com.example.stardict.data.local.stardict.model.IfoData
import com.example.stardict.data.local.stardict.model.IndexEntry
import java.io.File

/**
 * In-memory sorted index for a single StarDict dictionary.
 * Supports exact lookup, prefix search, and fuzzy search.
 */
class StarDictIndex(
    val ifo: IfoData,
    private val entries: List<IndexEntry>,
    private val synonyms: Map<String, List<Int>>,
    private val dictReader: DictReader
) {
    val wordCount: Int get() = entries.size

    fun exactLookup(word: String): List<IndexEntry> {
        val lower = word.lowercase()
        val idx = binarySearchExact(lower)
        if (idx >= 0) return listOf(entries[idx])

        // Check synonyms
        val synIndices = synonyms[lower]
        if (synIndices != null) {
            return synIndices.mapNotNull { i -> entries.getOrNull(i) }
        }

        return emptyList()
    }

    fun prefixSearch(prefix: String, limit: Int = 50): List<IndexEntry> {
        if (prefix.isEmpty()) return emptyList()
        val lower = prefix.lowercase()
        val results = mutableListOf<IndexEntry>()

        val startIdx = findFirstPrefix(lower)
        if (startIdx < 0) return emptyList()

        for (i in startIdx until entries.size) {
            if (!entries[i].word.lowercase().startsWith(lower)) break
            results.add(entries[i])
            if (results.size >= limit) break
        }

        // Also check synonyms for prefix matches
        for ((syn, indices) in synonyms) {
            if (syn.startsWith(lower) && results.size < limit) {
                for (idx in indices) {
                    val entry = entries.getOrNull(idx)
                    if (entry != null && entry !in results) {
                        results.add(entry)
                    }
                }
            }
        }

        return results.take(limit)
    }

    fun fuzzySearch(word: String, maxDistance: Int = 2, limit: Int = 20): List<IndexEntry> {
        if (word.isEmpty()) return emptyList()
        val lower = word.lowercase()
        val results = mutableListOf<Pair<IndexEntry, Int>>()

        // Scan neighborhood around where the word would be
        val insertPoint = findInsertionPoint(lower)
        val scanRange = (maxDistance + 1) * 50 // scan wider neighborhood
        val start = (insertPoint - scanRange).coerceAtLeast(0)
        val end = (insertPoint + scanRange).coerceAtMost(entries.size)

        for (i in start until end) {
            val dist = levenshteinDistance(lower, entries[i].word.lowercase())
            if (dist <= maxDistance) {
                results.add(entries[i] to dist)
            }
        }

        return results
            .sortedBy { it.second }
            .take(limit)
            .map { it.first }
    }

    fun readArticle(entry: IndexEntry): DictArticle {
        return dictReader.readArticle(entry.offset, entry.size)
    }

    private fun binarySearchExact(lower: String): Int {
        var lo = 0
        var hi = entries.size - 1
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            val cmp = entries[mid].word.lowercase().compareTo(lower)
            when {
                cmp < 0 -> lo = mid + 1
                cmp > 0 -> hi = mid - 1
                else -> return mid
            }
        }
        return -1
    }

    private fun findFirstPrefix(prefix: String): Int {
        var lo = 0
        var hi = entries.size - 1
        var result = -1
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            val word = entries[mid].word.lowercase()
            when {
                word.startsWith(prefix) -> {
                    result = mid
                    hi = mid - 1
                }
                word < prefix -> lo = mid + 1
                else -> hi = mid - 1
            }
        }
        return result
    }

    private fun findInsertionPoint(word: String): Int {
        var lo = 0
        var hi = entries.size
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (entries[mid].word.lowercase() < word) lo = mid + 1 else hi = mid
        }
        return lo
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)

        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(prev[j] + 1, curr[j - 1] + 1, prev[j - 1] + cost)
            }
            prev.indices.forEach { prev[it] = curr[it] }
        }

        return prev[b.length]
    }

    fun close() {
        dictReader.close()
    }

    companion object {
        fun load(basePath: String): StarDictIndex {
            val ifoFile = File("$basePath.ifo")
            val idxFile = File("$basePath.idx")

            val ifoParser = IfoParser()
            val idxParser = IdxParser()
            val synParser = SynParser()

            val ifo = ifoParser.parse(ifoFile)
            val entries = idxParser.parse(idxFile, ifo.idxoffsetbits)

            // Find dict file: try .dict.dz first, then .dict
            val dictDzFile = File("$basePath.dict.dz")
            val dictFile = if (dictDzFile.exists()) dictDzFile else File("$basePath.dict")
            val dictReader = DictReader(dictFile, ifo.sametypesequence)

            // Load optional synonyms
            val synFile = File("$basePath.syn")
            val synonymMap = mutableMapOf<String, MutableList<Int>>()
            if (synFile.exists()) {
                val synEntries = synParser.parse(synFile)
                for (syn in synEntries) {
                    synonymMap.getOrPut(syn.synonym.lowercase()) { mutableListOf() }
                        .add(syn.originalIndex)
                }
            }

            return StarDictIndex(ifo, entries, synonymMap, dictReader)
        }
    }
}
