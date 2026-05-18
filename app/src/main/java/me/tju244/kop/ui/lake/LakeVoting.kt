package me.tju244.kop.ui.lake

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
import me.tju244.kop.lake.network.LakeVoteDetailUi
import me.tju244.kop.lake.network.LakeVoteOptionUi
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.Text

@Composable
internal fun VoteSummaryPreview(vote: LakeVoteDetailUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(LakeColors.primary.copy(alpha = 0.08f))
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        vote.options.take(2).forEach { option ->
            Text(option.content, color = LakeColors.text, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("共 ${vote.voteCount} 票", color = LakeColors.muted, fontSize = 11.sp)
    }
}

@Composable
internal fun VoteDetailCard(vote: LakeVoteDetailUi, onVote: (List<Long>) -> Unit) {
    var selectedIds by remember(vote.id, vote.options) {
        mutableStateOf(vote.options.filter { it.selected }.map { it.id }.toSet())
    }
    val showResult = vote.hasSelected
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LakeColors.primary.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MiniTag("POLL")
            Text("最多选 ${vote.maxSelection.coerceAtLeast(1)} 项", color = LakeColors.muted, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text("共 ${vote.voteCount} 票", color = LakeColors.muted, fontSize = 12.sp)
        }
        vote.options.forEach { option ->
            if (showResult) {
                VoteOptionResult(option = option, total = vote.voteCount)
            } else {
                val selected = option.id in selectedIds
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) LakeColors.primary.copy(alpha = 0.16f) else LakeColors.card)
                        .clickable {
                            selectedIds = if (selected) {
                                selectedIds - option.id
                            } else {
                                (selectedIds + option.id).toList().takeLast(vote.maxSelection.coerceAtLeast(1)).toSet()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RadioButton(selected = selected, onClick = {
                        selectedIds = if (selected) {
                            selectedIds - option.id
                        } else {
                            (selectedIds + option.id).toList().takeLast(vote.maxSelection.coerceAtLeast(1)).toSet()
                        }
                    })
                    Text(option.content, color = LakeColors.text, fontSize = 14.sp, modifier = Modifier.weight(1f))
                }
            }
        }
        if (!showResult) {
            Button(
                onClick = { onVote(selectedIds.toList()) },
                enabled = selectedIds.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("投票")
            }
        }
    }
}

@Composable
private fun VoteOptionResult(option: LakeVoteOptionUi, total: Int) {
    val percent = if (total <= 0) 0f else option.count.toFloat() / total
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(option.content, color = LakeColors.text, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("${(percent * 100).toInt()}%", color = if (option.selected) LakeColors.primary else LakeColors.muted, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(LakeColors.cardAlt),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent.coerceIn(0f, 1f))
                    .height(6.dp)
                    .background(if (option.selected) LakeColors.primary else LakeColors.muted.copy(alpha = 0.35f)),
            )
        }
    }
}

