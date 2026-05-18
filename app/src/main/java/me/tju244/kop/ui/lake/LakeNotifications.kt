package me.tju244.kop.ui.lake

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tju244.kop.lake.network.LakeMessageCategory
import me.tju244.kop.lake.network.LakeMessageCountUi
import me.tju244.kop.lake.network.LakeMessageUi
import me.tju244.kop.ui.miuixScroll
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Ok

@Composable
internal fun LakeNotificationSheet(
    show: Boolean,
    messages: List<LakeMessageUi>,
    loading: Boolean,
    messageCount: LakeMessageCountUi?,
    selectedCategory: LakeMessageCategory,
    onCategoryChange: (LakeMessageCategory) -> Unit,
    onMarkRead: () -> Unit,
    onOpenMessage: (LakeMessageUi) -> Unit,
    onDismiss: () -> Unit,
) {
    WindowBottomSheet(
        show = show,
        title = "湖底通知",
        onDismissRequest = onDismiss,
        sheetMaxWidth = 560.dp,
        startAction = {
            IconButton(onClick = onDismiss, minWidth = 40.dp, minHeight = 40.dp) {
                Icon(
                    imageVector = MiuixIcons.Close,
                    contentDescription = "关闭通知",
                    tint = LakeColors.text,
                    modifier = Modifier.size(22.dp),
                )
            }
        },
        endAction = {
            IconButton(
                onClick = onMarkRead,
                enabled = messages.isNotEmpty() && !loading,
                minWidth = 40.dp,
                minHeight = 40.dp,
            ) {
                Icon(
                    imageVector = MiuixIcons.Ok,
                    contentDescription = "标记当前分类已读",
                    tint = if (messages.isNotEmpty() && !loading) LakeColors.primary else LakeColors.muted,
                    modifier = Modifier.size(22.dp),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(LakeMessageCategory.entries, key = { it.name }) { category ->
                    NotificationCategoryChip(
                        category = category,
                        count = messageCount.countOf(category),
                        selected = category == selectedCategory,
                        onClick = { onCategoryChange(category) },
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                if (loading && messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("加载中...", color = LakeColors.muted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().miuixScroll(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        overscrollEffect = null,
                    ) {
                        if (messages.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(160.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("暂时没有${selectedCategory.label}通知", color = LakeColors.muted)
                                }
                            }
                        }
                        items(messages, key = { "${it.id}_${it.title}_${it.createdAt}_${it.sender}" }) { message ->
                            LakeNoticeItem(
                                notice = message,
                                onClick = { onOpenMessage(message) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCategoryChip(category: LakeMessageCategory, count: Int, selected: Boolean, onClick: () -> Unit) {
    Card(
        cornerRadius = 18.dp,
        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
        colors = CardDefaults.defaultColors(
            color = if (selected) LakeColors.primary.copy(alpha = 0.14f) else LakeColors.cardAlt,
            contentColor = LakeColors.text,
        ),
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(category.label, color = if (selected) LakeColors.primary else LakeColors.text, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, fontSize = 13.sp)
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(LakeColors.like)
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                ) {
                    Text(count.coerceAtMost(99).toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LakeNoticeItem(notice: LakeMessageUi, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        cornerRadius = 16.dp,
        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        colors = CardDefaults.defaultColors(
            color = LakeColors.cardAlt,
            contentColor = LakeColors.text,
        ),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                notice.title.ifBlank { "湖底通知" },
                color = LakeColors.text,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (!notice.isRead) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(LakeColors.like),
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            notice.content,
            color = LakeColors.muted,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(5.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(notice.sender.ifBlank { "求实论坛" }, color = LakeColors.muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            if (notice.createdAt.isNotBlank()) {
                Text(notice.createdAt, color = LakeColors.muted, fontSize = 11.sp)
            }
        }
    }
}

