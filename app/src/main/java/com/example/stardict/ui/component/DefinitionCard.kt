package com.example.stardict.ui.component

import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.stardict.domain.model.DefinitionField

@Composable
fun DefinitionCard(
    fields: List<DefinitionField>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            for (field in fields) {
                when (field.type) {
                    'h' -> HtmlContent(html = field.content)
                    't' -> PhoneticContent(text = field.content)
                    'x' -> HtmlContent(html = field.content) // XDXF as HTML fallback
                    else -> PlainTextContent(text = field.content)
                }
            }
        }
    }
}

@Composable
private fun HtmlContent(html: String, modifier: Modifier = Modifier) {
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val linkColor = MaterialTheme.colorScheme.primary.toArgb()

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            TextView(context).apply {
                setTextColor(textColor)
                setLinkTextColor(linkColor)
                textSize = 16f
            }
        },
        update = { textView ->
            textView.text = android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_COMPACT)
        }
    )
}

@Composable
private fun PhoneticContent(text: String, modifier: Modifier = Modifier) {
    Text(
        text = "/$text/",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun PlainTextContent(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 4.dp)
    )
}
