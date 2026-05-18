package me.tju244.kop.ui.lake

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MiniTag(text: String, color: Color = LakeColors.primary) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 1.dp),
    ) {
        Text(text, color = color, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
internal fun SolvedTag(solved: Int) {
    val label = when (solved) {
        0 -> "未处理"
        3 -> "已分发"
        1 -> "已回复"
        2 -> "已解决"
        else -> "校务"
    }
    val color = when (solved) {
        0 -> Color(0xFF9AA1AD)
        3 -> Color(0xFF4D82FF)
        1 -> Color(0xFF21A67A)
        2 -> Color(0xFFFF9D2E)
        else -> LakeColors.primary
    }
    MiniTag(label, color = color)
}

@Composable
internal fun ETagBadge(entry: String, full: Boolean) {
    val (label, colors) = when (entry) {
        "recommend" -> (if (full) "精华帖" else "精华") to listOf(Color(0xFFFF7A45), Color(0xFFFF4D5E))
        "top" -> (if (full) "置顶帖" else "置顶") to listOf(Color(0xFF4D82FF), Color(0xFF6B5CFF))
        "theme" -> (if (full) "活动帖" else "活动") to listOf(Color(0xFF19B980), Color(0xFF0E9FBD))
        else -> entry to listOf(LakeColors.primary, LakeColors.primary)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(Brush.linearGradient(colors))
            .padding(horizontal = 5.dp, vertical = 1.dp),
    ) {
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
internal fun TagPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(LakeColors.primary.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, color = LakeColors.primary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) LakeColors.primary else LakeColors.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    ) {
        Text(
            label,
            color = if (selected) Color.White else LakeColors.text,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp,
        )
    }
}

@Composable
internal fun PageTitle(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 4.dp),
    ) {
        Text(title, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
    }
}

@Composable
internal fun MetricText(text: String) { Text(text, color = LakeColors.muted) }

@Composable
internal fun LevelBadge(level: Int) {
    val colors = levelGradient(level)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(Brush.linearGradient(colors))
            .padding(horizontal = 5.dp, vertical = 1.dp),
    ) {
        Text("Lv.$level", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

internal fun levelGradient(level: Int): List<Color> {
    val baseColors = listOf(
        Color(0xFF5EC05B),
        Color(0xFF5B96DE),
        Color(0xFF9F69ED),
        Color(0xFFFF87B2),
        Color(0xFFF8BE19),
        Color(0xFF205B4E),
        Color(0xFF4C4D71),
        Color(0xFF361B6B),
        Color(0xFF821439),
        Color(0xFFF77511),
    )
    if (level == 0) return listOf(Color(0xFFB7BDC6), Color(0xFFB7BDC6))
    if (level < 0) return listOf(Color(0xFF550009), Color(0xFF550009))
    val alpha = ((175 + (level % 10) * 8).coerceAtMost(255)) / 255f
    val color = baseColors[(level / 10) % baseColors.size].copy(alpha = if (level >= 50) 190 / 255f else alpha)
    return listOf(color, color)
}

