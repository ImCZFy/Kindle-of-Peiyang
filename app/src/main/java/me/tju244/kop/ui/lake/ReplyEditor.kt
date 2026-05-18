package me.tju244.kop.ui.lake

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Image as MiuixImageIcon

@Composable
internal fun ReplyEditor(label: String, replyInput: String, sending: Boolean, onReplyInputChange: (String) -> Unit, onSendReply: () -> Unit, replyImages: List<Uri>, onPickReplyImages: () -> Unit, onRemoveReplyImage: (Uri) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val activeInput = focused || replyInput.isNotBlank() || replyImages.isNotEmpty()
    val canSend = (replyInput.isNotBlank() || replyImages.isNotEmpty()) && !sending
    Spacer(Modifier.height(6.dp))
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(LakeColors.card).padding(horizontal = 8.dp, vertical = 7.dp),
    ) {
        if (replyImages.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                items(replyImages) { uri ->
                    Box {
                        LocalUriImage(uri = uri, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)))
                        Box(
                            modifier = Modifier.align(Alignment.TopEnd).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f)).clickable { onRemoveReplyImage(uri) }.padding(horizontal = 5.dp, vertical = 1.dp),
                        ) { Text("×", color = Color.White, fontSize = 10.sp) }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(22.dp)).background(LakeColors.cardAlt).padding(start = 6.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = replyInput, onValueChange = onReplyInputChange, label = label,
                    backgroundColor = Color.Transparent, borderColor = Color.Transparent, cornerRadius = 0.dp,
                    useLabelAsPlaceholder = true, minLines = 1, maxLines = 3,
                    modifier = Modifier.weight(1f).focusRequester(focusRequester).onFocusChanged { focused = it.isFocused },
                )
                AnimatedVisibility(visible = activeInput, enter = fadeIn(tween(120)), exit = fadeOut(tween(120))) {
                    Icon(
                        imageVector = MiuixIcons.MiuixImageIcon, contentDescription = "添加图片", tint = LakeColors.muted,
                        modifier = Modifier.size(30.dp).clip(CircleShape).clickable(onClick = onPickReplyImages).padding(5.dp),
                    )
                }
            }
            Text(
                if (sending) "发送中" else "发送",
                color = if (canSend) LakeColors.primary else LakeColors.muted,
                fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable(enabled = canSend, onClick = onSendReply).padding(horizontal = 7.dp, vertical = 6.dp),
            )
        }
    }
}

