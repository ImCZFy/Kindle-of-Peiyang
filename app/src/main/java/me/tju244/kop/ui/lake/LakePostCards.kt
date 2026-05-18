package me.tju244.kop.ui.lake

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tju244.kop.lake.network.LakePostUi
import me.tju244.kop.ui.component.ExpandableText
import me.tju244.kop.ui.component.PostRichText
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.icon.extended.Messages
import top.yukonga.miuix.kmp.icon.extended.Show
import top.yukonga.miuix.kmp.window.WindowListPopup

@Composable
internal fun PostSummaryCard(post: LakePostUi, onClick: () -> Unit, onOpenImages: (List<String>, Int) -> Unit, onLike: () -> Unit, onReport: (() -> Unit)? = null, onDelete: (() -> Unit)? = null, onShare: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 7.dp),
        cornerRadius = 18.dp,
        insideMargin = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
        colors = CardDefaults.defaultColors(
            color = LakeColors.card,
            contentColor = LakeColors.text,
        ),
        onClick = onClick,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = if (onReport != null || onDelete != null || onShare != null) 34.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AvatarImage(avatar = post.avatar, author = post.author, size = 36.dp)
                Text(
                    post.author.ifBlank { "匿名用户" },
                    color = LakeColors.text,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                LevelBadge(level = post.level)
            }
            if (onReport != null || onDelete != null || onShare != null) {
                var showMenu by remember { mutableStateOf(false) }
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    Text("···", color = LakeColors.muted, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { showMenu = true }.padding(horizontal = 5.dp, vertical = 1.dp))
                    top.yukonga.miuix.kmp.window.WindowListPopup(
                        show = showMenu,
                        alignment = top.yukonga.miuix.kmp.basic.PopupPositionProvider.Align.End,
                        onDismissRequest = { showMenu = false },
                        minWidth = 100.dp,
                    ) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(14.dp))) {
                            top.yukonga.miuix.kmp.basic.ListPopupColumn {
                                var idx = 0
                                onShare?.let { share -> top.yukonga.miuix.kmp.basic.DropdownImpl("分享", optionSize = 1, isSelected = false, index = idx++) { showMenu = false; share() } }
                                onReport?.let { report -> top.yukonga.miuix.kmp.basic.DropdownImpl("举报", optionSize = 1, isSelected = false, index = idx++) { showMenu = false; report() } }
                                onDelete?.let { delete -> top.yukonga.miuix.kmp.basic.DropdownImpl("删除", optionSize = 1, isSelected = false, index = idx++) { showMenu = false; delete() } }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            if (post.eTag.isNotBlank()) ETagBadge(entry = post.eTag, full = false)
            if (post.variant == 1) MiniTag("投票")
            if (post.type == 1) SolvedTag(post.solved)
            if (post.departmentName.isNotBlank()) MiniTag(post.departmentName)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            post.title.ifBlank { post.content.ifBlank { "(无标题)" } },
            color = LakeColors.text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 21.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (post.content.isNotBlank() && post.title.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                post.content,
                color = LakeColors.muted,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        post.voteDetail?.let { vote ->
            Spacer(Modifier.height(7.dp))
            VoteSummaryPreview(vote = vote)
        }
        if (post.imageUrls.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            CommunityImageStrip(imageUrls = post.imageUrls, onOpenImages = onOpenImages)
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(post.createdAt.toCommunityDate(), color = LakeColors.muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(post.visitCount.toString(), color = LakeColors.muted, fontSize = 11.sp)
                Text(post.mpCode(), color = LakeColors.muted, fontSize = 11.sp)
            }
            CommunityMetric(icon = MiuixIcons.Messages, value = post.comments)
            Spacer(Modifier.width(12.dp))
            CommunityMetric(
                icon = if (post.isLike) MiuixIcons.FavoritesFill else MiuixIcons.Favorites,
                value = post.likes,
                selected = post.isLike,
                onClick = onLike,
            )
        }
    }
}

@Composable
internal fun DetailPostCard(post: LakePostUi, onOpenImages: (List<String>, Int) -> Unit, onVote: (List<Long>) -> Unit, onOpenMpPost: (Long) -> Unit, onOpenUrl: ((String) -> Unit)? = null, onOpenUser: (LakeUserPreview) -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        cornerRadius = 18.dp,
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        colors = CardDefaults.defaultColors(
            color = LakeColors.card,
            contentColor = LakeColors.text,
        ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(21.dp))
                    .clickable { onOpenUser(LakeUserPreview(uid = post.uid, nickname = post.author, avatar = post.avatar)) },
            ) {
                AvatarImage(avatar = post.avatar, author = post.author, size = 42.dp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        post.author.ifBlank { "匿名用户" },
                        color = LakeColors.text,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenUser(LakeUserPreview(uid = post.uid, nickname = post.author, avatar = post.avatar)) }
                            .padding(horizontal = 2.dp, vertical = 1.dp),
                    )
                    LevelBadge(level = post.level)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    if (post.type == 1) SolvedTag(post.solved)
                    if (post.departmentName.isNotBlank()) MiniTag(post.departmentName, color = Color(0xFF22A06B))
                    if (post.eTag.isNotBlank()) ETagBadge(entry = post.eTag, full = false)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            post.title.ifBlank { "(无标题)" },
            color = LakeColors.text,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 28.sp,
        )
        if (post.content.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            PostRichText(text = post.content, fontSize = 16, lineHeight = 25, collapsedLines = 6, onOpenMpPost = onOpenMpPost, onOpenUrl = onOpenUrl)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 10.dp)) {
            if (post.variant == 1) MiniTag("投票")
            if (post.tagName.isNotBlank()) MiniTag("#${post.tagName}")
        }
        post.voteDetail?.let { vote ->
            Spacer(Modifier.height(10.dp))
            VoteDetailCard(vote = vote, onVote = onVote)
        }
        if (post.imageUrls.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            PostImagePreview(imageUrls = post.imageUrls, useThumb = false, onOpenImages = onOpenImages)
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(post.createdAt.ifBlank { "刚刚" }, color = LakeColors.muted, fontSize = 12.sp, maxLines = 1)
                Icon(imageVector = MiuixIcons.Show, contentDescription = "浏览量", tint = LakeColors.muted, modifier = Modifier.size(14.dp))
                Text(post.visitCount.toString(), color = LakeColors.muted, fontSize = 12.sp, maxLines = 1)
            }
            Text(post.mpCode(), color = LakeColors.muted, fontSize = 12.sp, maxLines = 1)
        }
    }
}

