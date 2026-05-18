package me.tju244.kop.ui.lake

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tju244.kop.lake.network.LakeFloorUi
import me.tju244.kop.ui.component.ExpandableText
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowListPopup
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ExpandLess
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import me.tju244.kop.ui.miuixScroll

@Composable
internal fun FloorSortControl(floorOrder: Int, onFloorOrderChange: (Int) -> Unit) {
    val timeSelected = floorOrder == 0 || floorOrder == 1
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(LakeColors.cardAlt)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(15.dp))
                .background(if (!timeSelected) LakeColors.card else Color.Transparent)
                .clickable { onFloorOrderChange(2) }
                .padding(horizontal = 9.dp, vertical = 5.dp),
        ) {
            Text("默认", color = if (!timeSelected) LakeColors.text else LakeColors.muted, fontSize = 12.sp)
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(15.dp))
                .background(if (timeSelected) LakeColors.card else Color.Transparent)
                .clickable { onFloorOrderChange(if (floorOrder == 0) 1 else 0) }
                .padding(start = 9.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text("时间", color = if (timeSelected) LakeColors.text else LakeColors.muted, fontSize = 12.sp)
            Icon(
                imageVector = if (floorOrder == 0) MiuixIcons.ExpandMore else MiuixIcons.ExpandLess,
                contentDescription = "切换时间排序",
                tint = if (timeSelected) LakeColors.text else LakeColors.muted,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
internal fun CommunityMetric(icon: ImageVector, value: Int? = null, text: String = value?.toString().orEmpty(), selected: Boolean = false, onClick: (() -> Unit)? = null) {
    Card(
        cornerRadius = 12.dp,
        insideMargin = PaddingValues(horizontal = 3.dp, vertical = 1.dp),
        colors = CardDefaults.defaultColors(color = Color.Transparent, contentColor = LakeColors.muted),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(
                imageVector = icon, contentDescription = null,
                tint = if (selected) LakeColors.like else LakeColors.muted,
                modifier = Modifier.size(15.dp).alpha(if (selected) 1f else 0.72f),
            )
            Text(text, color = if (selected) LakeColors.like else LakeColors.muted, fontSize = 12.sp)
        }
    }
}

@Composable
internal fun FloorCard(floor: LakeFloorUi, replyFloorId: Long?, replyInput: String, sending: Boolean, onReplyToggle: (Long) -> Unit, onReplyInputChange: (String) -> Unit, onSendReply: (Long) -> Unit, onFloorLike: (Long) -> Unit, onExpandReplies: (Long) -> Unit, onOpenImages: (List<String>, Int) -> Unit, replyImages: List<Uri>, onPickReplyImages: () -> Unit, onRemoveReplyImage: (Uri) -> Unit, postAuthorUid: Long = 0, currentUserUid: Long = 0, canDeleteAllFloors: Boolean = false, onReportFloor: ((Long) -> Unit)? = null, onDeleteFloor: ((Long) -> Unit)? = null, onOpenUser: (LakeUserPreview) -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        cornerRadius = 18.dp,
        insideMargin = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        colors = CardDefaults.defaultColors(color = LakeColors.card, contentColor = LakeColors.text),
    ) {
        FloorBody(floor = floor, onOpenImages = onOpenImages, compact = false, postAuthorUid = postAuthorUid, onOpenUser = onOpenUser)
        Spacer(Modifier.height(6.dp))
        FloorActions(
            floor = floor,
            replying = replyFloorId == floor.id,
            onLike = { onFloorLike(floor.id) },
            onReply = { onReplyToggle(floor.id) },
            onReport = onReportFloor?.let { report -> { report(floor.id) } },
            onDelete = onDeleteFloor.takeIf { canDeleteAllFloors || (currentUserUid > 0 && floor.uid == currentUserUid) }?.let { delete -> { delete(floor.id) } },
        )
        if (replyFloorId == floor.id) {
            ReplyEditor(label = "回复 @${floor.author.ifBlank { "匿名用户" }}", replyInput = replyInput, sending = sending, onReplyInputChange = onReplyInputChange, onSendReply = { onSendReply(floor.id) }, replyImages = replyImages, onPickReplyImages = onPickReplyImages, onRemoveReplyImage = onRemoveReplyImage)
        }
        if (floor.subFloors.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(LakeColors.cardAlt).padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                floor.subFloors.take(3).forEach { subFloor ->
                    SubFloorItem(floor = subFloor, replyFloorId = replyFloorId, replyInput = replyInput, sending = sending, onReplyToggle = onReplyToggle, onReplyInputChange = onReplyInputChange, onSendReply = onSendReply, onFloorLike = onFloorLike, onOpenImages = onOpenImages, replyImages = replyImages, onPickReplyImages = onPickReplyImages, onRemoveReplyImage = onRemoveReplyImage, postAuthorUid = postAuthorUid, layerOwnerUid = floor.uid, currentUserUid = currentUserUid, canDeleteAllFloors = canDeleteAllFloors, onReportFloor = onReportFloor, onDeleteFloor = onDeleteFloor, onOpenUser = onOpenUser)
                }
                val replyCount = maxOf(floor.subFloorCount, floor.subFloors.size)
                if (replyCount > 3) {
                    Text("查看所有 $replyCount 个回复", color = LakeColors.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onExpandReplies(floor.id) })
                }
            }
        }
    }
}

@Composable
internal fun FloorBody(floor: LakeFloorUi, onOpenImages: (List<String>, Int) -> Unit, compact: Boolean, postAuthorUid: Long = 0, layerOwnerUid: Long = 0, onOpenUser: (LakeUserPreview) -> Unit = {}) {
    val avatarSize = if (compact) 24.dp else 30.dp
    val nameSize = if (compact) 12.sp else 13.sp
    val bodySize = if (compact) 14 else 14
    val bodyLine = if (compact) 20 else 21
    val collapsedLines = if (compact) 3 else 5
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onOpenUser(LakeUserPreview(uid = floor.uid, nickname = floor.author, avatar = floor.avatar)) },
        ) {
            AvatarImage(avatar = floor.avatar, author = floor.author, size = avatarSize)
        }
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                floor.author.ifBlank { "匿名用户" },
                color = LakeColors.text,
                fontWeight = FontWeight.SemiBold,
                fontSize = nameSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenUser(LakeUserPreview(uid = floor.uid, nickname = floor.author, avatar = floor.avatar)) }
                    .padding(horizontal = 2.dp, vertical = 1.dp),
            )
            if (postAuthorUid > 0 && floor.uid == postAuthorUid) MiniTag("楼主")
            if (layerOwnerUid > 0 && floor.uid == layerOwnerUid && floor.uid != postAuthorUid) MiniTag("层主")
            if (floor.sender == 1) MiniTag("校务")
            if (!compact || floor.level > 0) LevelBadge(level = floor.level)
        }
    }
    Spacer(Modifier.height(if (compact) 5.dp else 7.dp))
    val prefix = if (floor.replyToName.isNotBlank()) "回复 @${floor.replyToName}：" else ""
    ExpandableText(text = prefix + floor.content, fontSize = bodySize, lineHeight = bodyLine, collapsedLines = collapsedLines)
    if (floor.imageUrl.isNotBlank()) {
        Spacer(Modifier.height(if (compact) 6.dp else 8.dp))
        PostImagePreview(imageUrls = listOf(floor.imageUrl), useThumb = compact, onOpenImages = onOpenImages)
    }
}

@Composable
internal fun SubFloorItem(floor: LakeFloorUi, replyFloorId: Long?, replyInput: String, sending: Boolean, onReplyToggle: (Long) -> Unit, onReplyInputChange: (String) -> Unit, onSendReply: (Long) -> Unit, onFloorLike: (Long) -> Unit, onOpenImages: (List<String>, Int) -> Unit, replyImages: List<Uri>, onPickReplyImages: () -> Unit, onRemoveReplyImage: (Uri) -> Unit, postAuthorUid: Long = 0, layerOwnerUid: Long = 0, currentUserUid: Long = 0, canDeleteAllFloors: Boolean = false, onReportFloor: ((Long) -> Unit)? = null, onDeleteFloor: ((Long) -> Unit)? = null, onOpenUser: (LakeUserPreview) -> Unit = {}) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FloorBody(floor = floor, onOpenImages = onOpenImages, compact = true, postAuthorUid = postAuthorUid, layerOwnerUid = layerOwnerUid, onOpenUser = onOpenUser)
        FloorActions(
            floor = floor,
            replying = replyFloorId == floor.id,
            onLike = { onFloorLike(floor.id) },
            onReply = { onReplyToggle(floor.id) },
            onReport = onReportFloor?.let { report -> { report(floor.id) } },
            onDelete = onDeleteFloor.takeIf { canDeleteAllFloors || (currentUserUid > 0 && floor.uid == currentUserUid) }?.let { delete -> { delete(floor.id) } },
        )
        if (replyFloorId == floor.id) {
            ReplyEditor(label = "回复 @${floor.author.ifBlank { "匿名用户" }}", replyInput = replyInput, sending = sending, onReplyInputChange = onReplyInputChange, onSendReply = { onSendReply(floor.id) }, replyImages = replyImages, onPickReplyImages = onPickReplyImages, onRemoveReplyImage = onRemoveReplyImage)
        }
    }
}

@Composable
internal fun FloorActions(floor: LakeFloorUi, replying: Boolean, onLike: () -> Unit, onReply: () -> Unit, onReport: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
    var showMenu by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(floor.createdAt.ifBlank { "刚刚" }, color = LakeColors.muted, fontSize = 11.sp, maxLines = 1, modifier = Modifier.weight(1f))
        CommunityMetric(icon = if (floor.isLike) MiuixIcons.FavoritesFill else MiuixIcons.Favorites, value = floor.likes, selected = floor.isLike, onClick = onLike)
        Spacer(Modifier.width(8.dp))
        Text(if (replying) "取消回复" else "回复", color = if (replying) LakeColors.primary else LakeColors.muted, fontSize = 12.sp, modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onReply).padding(horizontal = 6.dp, vertical = 3.dp))
        if (onReport != null || onDelete != null) {
            Spacer(Modifier.width(2.dp))
            Box {
                Text("…", color = LakeColors.muted, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { showMenu = true }.padding(horizontal = 5.dp, vertical = 1.dp))
                top.yukonga.miuix.kmp.window.WindowListPopup(
                    show = showMenu,
                    alignment = top.yukonga.miuix.kmp.basic.PopupPositionProvider.Align.End,
                    onDismissRequest = { showMenu = false },
                    minWidth = 100.dp,
                ) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(14.dp))) {
                        top.yukonga.miuix.kmp.basic.ListPopupColumn {
                            onReport?.let { report ->
                                top.yukonga.miuix.kmp.basic.DropdownImpl("举报", optionSize = 1, isSelected = false, index = 0) { showMenu = false; report() }
                            }
                            onDelete?.let { delete ->
                                top.yukonga.miuix.kmp.basic.DropdownImpl("删除", optionSize = 1, isSelected = false, index = 1) { showMenu = false; delete() }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun RepliesBottomSheet(floor: LakeFloorUi, replyFloorId: Long?, replyInput: String, sending: Boolean, onDismiss: () -> Unit, onReplyToggle: (Long) -> Unit, onReplyInputChange: (String) -> Unit, onSendReply: (Long) -> Unit, onFloorLike: (Long) -> Unit, onOpenImages: (List<String>, Int) -> Unit, replyImages: List<Uri>, onPickReplyImages: () -> Unit, onRemoveReplyImage: (Uri) -> Unit, postAuthorUid: Long = 0, currentUserUid: Long = 0, canDeleteAllFloors: Boolean = false, onReportFloor: ((Long) -> Unit)? = null, onDeleteFloor: ((Long) -> Unit)? = null, onOpenUser: (LakeUserPreview) -> Unit = {}) {
    WindowBottomSheet(show = true, title = "全部 ${maxOf(floor.subFloorCount, floor.subFloors.size)} 个回复", onDismissRequest = onDismiss, sheetMaxWidth = 560.dp) {
        LazyColumn(modifier = Modifier.height(420.dp).miuixScroll(), verticalArrangement = Arrangement.spacedBy(8.dp), overscrollEffect = null) {
            items(floor.subFloors, key = { it.id }) { subFloor ->
                SubFloorItem(floor = subFloor, replyFloorId = replyFloorId, replyInput = replyInput, sending = sending, onReplyToggle = onReplyToggle, onReplyInputChange = onReplyInputChange, onSendReply = onSendReply, onFloorLike = onFloorLike, onOpenImages = onOpenImages, replyImages = replyImages, onPickReplyImages = onPickReplyImages, onRemoveReplyImage = onRemoveReplyImage, postAuthorUid = postAuthorUid, layerOwnerUid = floor.uid, currentUserUid = currentUserUid, canDeleteAllFloors = canDeleteAllFloors, onReportFloor = onReportFloor, onDeleteFloor = onDeleteFloor, onOpenUser = onOpenUser)
            }
        }
    }
}

