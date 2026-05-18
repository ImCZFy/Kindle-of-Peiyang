package me.tju244.kop.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tju244.kop.ui.theme.LocalAppFontFamily
import me.tju244.kop.ui.lake.LakeColors
import me.tju244.kop.ui.lake.openExternalUrl
import me.tju244.kop.ui.lake.trimLinkTail
import top.yukonga.miuix.kmp.basic.Text

@Composable
internal fun ExpandableText(text: String, fontSize: Int, lineHeight: Int, collapsedLines: Int, modifier: Modifier = Modifier) {
    var expanded by remember(text) { mutableStateOf(false) }
    var overflowed by remember(text) { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text,
            color = LakeColors.text,
            fontSize = fontSize.sp,
            lineHeight = lineHeight.sp,
            maxLines = if (expanded) Int.MAX_VALUE else collapsedLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { overflowed = it.hasVisualOverflow },
        )
        if (overflowed || expanded) {
            Spacer(Modifier.height(3.dp))
            Text(
                if (expanded) "收起" else "展开",
                color = LakeColors.primary,
                fontSize = (fontSize - 1).coerceAtLeast(12).sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expanded = !expanded }
                    .padding(end = 8.dp, top = 2.dp, bottom = 2.dp),
            )
        }
    }
}

private val PostLinkRegex = Regex(
    """(https?://[A-Za-z0-9\-._~:/?#\[\]@!$&'()*+,;=%]+|#MP\d{6}(?!\d))""",
    RegexOption.IGNORE_CASE,
)

@Composable
internal fun PostRichText(
    text: String,
    fontSize: Int,
    lineHeight: Int,
    collapsedLines: Int,
    onOpenMpPost: (Long) -> Unit,
    onOpenUrl: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val urlHandler: (String) -> Unit = remember(context, onOpenUrl) {
        { url -> onOpenUrl?.invoke(url) ?: context.openExternalUrl(url) }
    }
    val primaryColor = LakeColors.primary
    val appFontFamily = LocalAppFontFamily.current
    var expanded by remember(text) { mutableStateOf(false) }
    var overflowed by remember(text) { mutableStateOf(false) }
    val annotated = remember(text, primaryColor) {
        buildAnnotatedString {
            var index = 0
            PostLinkRegex.findAll(text).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                if (start > index) append(text.substring(index, start))
                val rawValue = match.value
                val value = rawValue.trimLinkTail()
                val tail = rawValue.removePrefix(value)
                if (value.startsWith("#MP", ignoreCase = true)) {
                    pushStringAnnotation(tag = "mp", annotation = value.removePrefix("#MP").trim())
                    withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.SemiBold)) { append(value) }
                    pop()
                } else {
                    pushStringAnnotation(tag = "url", annotation = value)
                    withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.SemiBold)) { append(value) }
                    pop()
                }
                if (tail.isNotEmpty()) append(tail)
                index = end
            }
            if (index < text.length) append(text.substring(index))
        }
    }
    Column(modifier = modifier) {
        SelectionContainer {
            ClickableText(
                text = annotated,
                style = androidx.compose.ui.text.TextStyle(
                    color = LakeColors.text,
                    fontSize = fontSize.sp,
                    lineHeight = lineHeight.sp,
                    fontFamily = appFontFamily,
                ),
                maxLines = if (expanded) Int.MAX_VALUE else collapsedLines,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { overflowed = it.hasVisualOverflow },
                onClick = { offset ->
                    annotated.getStringAnnotations("mp", offset, offset).firstOrNull()?.let { ann ->
                        ann.item.toLongOrNull()?.let(onOpenMpPost)
                        return@ClickableText
                    }
                    annotated.getStringAnnotations("url", offset, offset).firstOrNull()?.let { ann ->
                        urlHandler(ann.item)
                    }
                },
            )
        }
        if (overflowed || expanded) {
            Spacer(Modifier.height(3.dp))
            Text(
                if (expanded) "收起" else "展开",
                color = LakeColors.primary,
                fontSize = (fontSize - 1).coerceAtLeast(12).sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expanded = !expanded }
                    .padding(end = 8.dp, top = 2.dp, bottom = 2.dp),
            )
        }
    }
}

