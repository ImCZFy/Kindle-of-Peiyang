package me.tju244.kop.ui.lake

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import me.tju244.kop.lake.network.LakeMessageCategory
import me.tju244.kop.lake.network.LakeMessageCountUi
import me.tju244.kop.lake.network.LakePostUi
import me.tju244.kop.ui.theme.LocalAppDarkMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object LakeColors {
    val background: Color
        @Composable get() = if (LocalAppDarkMode.current) Color.Black else Color(0xFFF4F5F7)
    val forumBackground: Color
        @Composable get() = background
    val card: Color
        @Composable get() = if (LocalAppDarkMode.current) Color.Black else Color.White
    val cardAlt: Color
        @Composable get() = if (LocalAppDarkMode.current) Color(0xFF1D1D1D) else Color(0xFFF7F8FA)
    val text: Color
        @Composable get() = if (LocalAppDarkMode.current) Color(0xFFF4F4F4) else Color(0xFF151515)
    val muted: Color
        @Composable get() = if (LocalAppDarkMode.current) Color(0xFF9A9A9A) else Color(0xFF737780)
    val primary: Color
        @Composable get() = Color(0xFF4D82FF)
    val like: Color
        @Composable get() = Color(0xFFFF4D5E)
    val divider: Color
        @Composable get() = if (LocalAppDarkMode.current) Color(0xFF252525) else Color(0xFFE0E3E8)
}

internal data class LakeUserPreview(
    val uid: Long,
    val nickname: String,
    val avatar: String,
)

internal fun LakeMessageCountUi?.countOf(category: LakeMessageCategory): Int {
    val count = this ?: return 0
    return when (category) {
        LakeMessageCategory.Like -> count.like
        LakeMessageCategory.Floor -> count.floor
        LakeMessageCategory.Reply -> count.reply
        LakeMessageCategory.Notice -> count.notice
    }
}

internal fun lakeFeedCacheKey(type: Int, tagId: Int?, keyword: String, sortMode: Int): String {
    return "$type:${tagId ?: "all"}:$sortMode:${keyword.trim()}"
}

internal fun String.toCommunityDate(): String {
    val value = trim()
    if (value.isBlank()) return "刚刚"
    val patterns = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm",
    )
    val output = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    patterns.forEach { pattern ->
        runCatching {
            val date = SimpleDateFormat(pattern, Locale.getDefault()).parse(value)
            if (date != null) return output.format(date)
        }
    }
    val seconds = value.toLongOrNull()
    if (seconds != null) {
        val epochMillis = if (seconds > 1_000_000_000_000L) seconds else seconds * 1000
        return output.format(Date(epochMillis))
    }
    return value.take(16)
}

internal fun LakePostUi.mpCode(): String = "#MP${id.toString().padStart(6, '0')}"

internal fun String.toMpPostIdOrNull(): Long? {
    val value = trim()
    val match = Regex("""^#MP(\d{6})$""", RegexOption.IGNORE_CASE).matchEntire(value) ?: return null
    return match.groupValues.getOrNull(1)?.toLongOrNull()
}

internal fun String.trimLinkTail(): String {
    if (isEmpty()) return this
    return trimEnd('。', '，', ',', '.', '！', '!', '？', '?', '；', ';', '：', ':', ')', '）', ']', '】', '>', '》', '"', '\'')
}

internal fun String.isPostMissingMessage(): Boolean =
    contains("帖子不存在") || contains("详情为空") || contains("404") || contains("not found", ignoreCase = true)

internal fun Context.openExternalUrl(rawUrl: String) {
    val url = if (rawUrl.startsWith("http://", ignoreCase = true) || rawUrl.startsWith("https://", ignoreCase = true)) {
        rawUrl
    } else {
        "https://$rawUrl"
    }
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

