package com.example.stardict.data.local.stardict

import com.example.stardict.data.local.stardict.model.IfoData
import java.io.File

class IfoParser {

    fun parse(file: File): IfoData {
        val lines = file.readLines(Charsets.UTF_8)

        require(lines.isNotEmpty() && lines[0].trim() == "StarDict's dict ifo file") {
            "Invalid .ifo file: missing magic header"
        }

        val props = mutableMapOf<String, String>()
        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue
            val eqIdx = line.indexOf('=')
            if (eqIdx > 0) {
                props[line.substring(0, eqIdx)] = line.substring(eqIdx + 1)
            }
        }

        return IfoData(
            version = props["version"] ?: error("Missing version in .ifo"),
            bookname = props["bookname"] ?: error("Missing bookname in .ifo"),
            wordcount = props["wordcount"]?.toIntOrNull() ?: error("Missing wordcount in .ifo"),
            idxfilesize = props["idxfilesize"]?.toLongOrNull() ?: error("Missing idxfilesize in .ifo"),
            sametypesequence = props["sametypesequence"],
            description = props["description"],
            author = props["author"],
            email = props["email"],
            website = props["website"],
            date = props["date"],
            synwordcount = props["synwordcount"]?.toIntOrNull(),
            idxoffsetbits = props["idxoffsetbits"]?.toIntOrNull() ?: 32
        )
    }
}
