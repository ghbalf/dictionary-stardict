package com.example.stardict.data.local.stardict.model

data class IfoData(
    val version: String,
    val bookname: String,
    val wordcount: Int,
    val idxfilesize: Long,
    val sametypesequence: String? = null,
    val description: String? = null,
    val author: String? = null,
    val email: String? = null,
    val website: String? = null,
    val date: String? = null,
    val synwordcount: Int? = null,
    val idxoffsetbits: Int = 32
)
