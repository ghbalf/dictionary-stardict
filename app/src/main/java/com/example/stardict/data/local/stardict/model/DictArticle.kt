package com.example.stardict.data.local.stardict.model

data class DictArticle(
    val fields: List<ArticleField>
) {
    fun toDisplayText(): String = fields.joinToString("\n") { it.content }

    fun htmlContent(): String? = fields.firstOrNull { it.type == 'h' }?.content

    fun phoneticContent(): String? = fields.firstOrNull { it.type == 't' }?.content
}

data class ArticleField(
    val type: Char,
    val content: String
)
