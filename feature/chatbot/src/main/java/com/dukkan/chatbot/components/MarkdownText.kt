package com.dukkan.chatbot.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Renders a lightweight subset of Markdown commonly produced by chat models:
 * headings, unordered/ordered lists, **bold**, *italic*, `inline code`,
 * ~~strikethrough~~ and [links](url). Kept dependency-free on purpose so the
 * chatbot bubble stays self-contained.
 */
@Composable
fun MarkdownText(
    markdown: String,
    color: Color,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val codeBackground = color.copy(alpha = 0.14f)
    val lines = markdown.trim().split("\n")

    Column(modifier) {
        lines.forEach { raw ->
            val line = raw.trimEnd()
            val bullet = Regex("^\\s*[-*+]\\s+").find(line)
            val numbered = Regex("^\\s*(\\d+)\\.\\s+").find(line)
            when {
                line.isBlank() -> Spacer(Modifier.height(4.dp))

                line.startsWith("### ") -> Text(
                    text = inline(line.removePrefix("### "), linkColor, codeBackground),
                    color = color,
                    style = style.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )

                line.startsWith("## ") -> Text(
                    text = inline(line.removePrefix("## "), linkColor, codeBackground),
                    color = color,
                    style = style.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )

                line.startsWith("# ") -> Text(
                    text = inline(line.removePrefix("# "), linkColor, codeBackground),
                    color = color,
                    style = style.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                )

                bullet != null -> MarkdownListRow(
                    marker = "•",
                    content = inline(line.substring(bullet.range.last + 1), linkColor, codeBackground),
                    color = color,
                    style = style
                )

                numbered != null -> MarkdownListRow(
                    marker = "${numbered.groupValues[1]}.",
                    content = inline(line.substring(numbered.range.last + 1), linkColor, codeBackground),
                    color = color,
                    style = style
                )

                else -> Text(
                    text = inline(line, linkColor, codeBackground),
                    color = color,
                    style = style
                )
            }
        }
    }
}

@Composable
private fun MarkdownListRow(
    marker: String,
    content: AnnotatedString,
    color: Color,
    style: TextStyle
) {
    Row(modifier = Modifier.padding(vertical = 1.dp)) {
        Text(text = marker, color = color, style = style)
        Spacer(Modifier.width(6.dp))
        Text(text = content, color = color, style = style)
    }
}

private fun inline(text: String, linkColor: Color, codeBackground: Color): AnnotatedString =
    buildAnnotatedString { appendInline(text, linkColor, codeBackground) }

private fun AnnotatedString.Builder.appendInline(
    text: String,
    linkColor: Color,
    codeBackground: Color
) {
    var i = 0
    while (i < text.length) {
        val c = text[i]

        // [label](url) — rendered as a styled link (label may contain markup)
        if (c == '[') {
            val close = text.indexOf(']', i)
            if (close != -1 && close + 1 < text.length && text[close + 1] == '(') {
                val urlEnd = text.indexOf(')', close + 2)
                if (urlEnd != -1) {
                    withStyle(
                        SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
                    ) {
                        appendInline(text.substring(i + 1, close), linkColor, codeBackground)
                    }
                    i = urlEnd + 1
                    continue
                }
            }
        }

        // **bold** / __bold__
        val boldMarker = when {
            text.startsWith("**", i) -> "**"
            text.startsWith("__", i) -> "__"
            else -> null
        }
        if (boldMarker != null) {
            val end = text.indexOf(boldMarker, i + 2)
            if (end != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    appendInline(text.substring(i + 2, end), linkColor, codeBackground)
                }
                i = end + 2
                continue
            }
        }

        // ~~strikethrough~~
        if (text.startsWith("~~", i)) {
            val end = text.indexOf("~~", i + 2)
            if (end != -1) {
                withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                    appendInline(text.substring(i + 2, end), linkColor, codeBackground)
                }
                i = end + 2
                continue
            }
        }

        // `inline code`
        if (c == '`') {
            val end = text.indexOf('`', i + 1)
            if (end != -1) {
                withStyle(
                    SpanStyle(fontFamily = FontFamily.Monospace, background = codeBackground)
                ) {
                    append(text.substring(i + 1, end))
                }
                i = end + 1
                continue
            }
        }

        // *italic* / _italic_
        if (c == '*' || c == '_') {
            val end = text.indexOf(c, i + 1)
            if (end != -1 && end > i + 1) {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    appendInline(text.substring(i + 1, end), linkColor, codeBackground)
                }
                i = end + 1
                continue
            }
        }

        append(c)
        i++
    }
}
