package me.tju244.kop.lake.network

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val lakeDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

enum class LakeMessageCategory(val label: String) {
    Like("点赞"),
    Floor("回复"),
    Reply("校务"),
    Notice("湖底管理"),
}

data class LakeBaseResp<T>(
    val code: Int = 0,
    val msg: String? = null,
    val data: T? = null,
)

data class LakeTokenData(
    val token: String? = null,
    val uid: Long? = null,
    val user: LakeCurrentUserDto? = null,
)

data class LakeCurrentUserDto(
    val nickname: String = "",
    val username: String = "",
    val avatar: String = "",
    val avatar_frame: String = "",
)

data class LakePostListData(
    val list: List<LakePostDto> = emptyList(),
    val total: Int = 0,
)

data class LakePostDetailData(
    val post: LakePostDto? = null,
)

data class LakeFloorListData(
    val list: List<LakeFloorDto> = emptyList(),
    val total: Int = 0,
)

data class LakeTabDto(
    val id: Int = 0,
    val shortname: String = "",
    val name: String = "",
)

data class LakeTagDto(
    val id: Int = 0,
    val name: String = "",
)

data class LakeDepartmentDto(
    val id: Int = 0,
    val name: String = "",
    val introduction: String = "",
)

data class LakeVoteDetailDto(
    val id: Long = 0,
    val options: List<LakeVoteOptionDto> = emptyList(),
    val max_selection: Int = 1,
    val vote_count: Int = 0,
)

data class LakeVoteOptionDto(
    val id: Long = 0,
    val vote_id: Long = 0,
    val content: String = "",
    val count: Int = 0,
    val selected: Boolean = false,
)

data class LakeUserInfoDto(
    val level: Int = 0,
    val avatar: String = "",
    val avatar_frame: String = "",
)

data class LakeLevelInfoDto(
    val level: Int = 0,
    val level_name: String = "",
    val cur_level_point: Int = 0,
    val next_level_point: Int = 0,
)

data class LakeUserProfileDto(
    val id: Long = 0,
    val nickname: String = "",
    val username: String = "",
    val avatar: String = "",
    val level_point: Int = 0,
    val level_info: LakeLevelInfoDto? = null,
)

data class LakeMessageCountDto(
    val like: Int = 0,
    val floor: Int = 0,
    val reply: Int = 0,
    val notice: Int = 0,
)

data class LakeNoticeDto(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val sender: String = "",
    val created_at: String = "",
    val is_read: Boolean = false,
)

data class LakeLikeMessageDto(
    val type: Int = 0,
    val post: LakePostDto? = null,
    val floor: LakeFloorDto? = null,
)

data class LakeFloorMessageDto(
    val type: Int = 0,
    val is_read: Boolean = false,
    val to_floor: LakeFloorDto? = null,
    val post: LakePostDto? = null,
    val floor: LakeFloorDto? = null,
)

data class LakeReplyMessageDto(
    val is_read: Boolean = false,
    val post: LakePostDto? = null,
    val reply: LakeReplyDto? = null,
)

data class LakeReplyDto(
    val id: Long = 0,
    val created_at: String = "",
    val post_id: Long = 0,
    val sender: Int = 0,
    val content: String = "",
    val image_urls: List<String> = emptyList(),
)

data class LakePostDto(
    val id: Long = 0,
    val uid: Long = 0,
    val type: Int = 0,
    val solved: Int = 0,
    val title: String = "",
    val content: String = "",
    val nickname: String = "",
    val like_count: Int = 0,
    val fav_count: Int = 0,
    val comment_count: Int = 0,
    val created_at: String = "",
    val is_like: Boolean = false,
    val is_fav: Boolean = false,
    val is_owner: Boolean = false,
    val rating: Int = 0,
    val visit_count: Int = 0,
    val e_tag: String = "",
    val variant: Int = 0,
    val vote_detail: LakeVoteDetailDto? = null,
    val department: LakeDepartmentDto? = null,
    val user_info: LakeUserInfoDto? = null,
    val tag: LakeTagDto? = null,
    val image_urls: List<String> = emptyList(),
)

data class LakeFloorDto(
    val id: Long = 0,
    val uid: Long = 0,
    val post_id: Long = 0,
    val content: String = "",
    val nickname: String = "",
    val created_at: String = "",
    val like_count: Int = 0,
    val user_info: LakeUserInfoDto? = null,
    val image_url: String = "",
    val reply_to: Long = 0,
    val reply_to_name: String = "",
    val sender: Int = 0,
    val sub_to: Long = 0,
    val sub_floors: List<LakeFloorDto> = emptyList(),
    val sub_floor_cnt: Int = 0,
    val is_like: Boolean = false,
)

data class LakePostUi(
    val id: Long,
    val uid: Long,
    val type: Int,
    val solved: Int,
    val title: String,
    val content: String,
    val author: String,
    val likes: Int,
    val favs: Int,
    val comments: Int,
    val createdAt: String,
    val isLike: Boolean,
    val isFav: Boolean,
    val isOwner: Boolean,
    val rating: Int,
    val visitCount: Int,
    val eTag: String,
    val variant: Int,
    val voteDetail: LakeVoteDetailUi?,
    val departmentId: Int?,
    val departmentName: String,
    val level: Int,
    val avatar: String,
    val tagId: Int?,
    val tagName: String,
    val imageUrls: List<String>,
)

data class LakeVoteDetailUi(
    val id: Long,
    val options: List<LakeVoteOptionUi>,
    val maxSelection: Int,
    val voteCount: Int,
) {
    val hasSelected: Boolean get() = options.any { it.selected }
}

data class LakeVoteOptionUi(
    val id: Long,
    val voteId: Long,
    val content: String,
    val count: Int,
    val selected: Boolean,
)

data class LakeFloorUi(
    val id: Long,
    val uid: Long,
    val postId: Long,
    val content: String,
    val author: String,
    val createdAt: String,
    val likes: Int,
    val level: Int,
    val avatar: String,
    val imageUrl: String,
    val replyTo: Long,
    val replyToName: String,
    val sender: Int,
    val subTo: Long,
    val subFloors: List<LakeFloorUi>,
    val subFloorCount: Int,
    val isLike: Boolean,
)

data class LakeTabUi(val id: Int, val name: String)
data class LakeTagUi(val id: Int, val name: String)
data class LakeDepartmentUi(val id: Int, val name: String)
data class LakeUserProfileUi(
    val uid: Long,
    val nickname: String,
    val username: String,
    val avatar: String,
    val level: Int,
    val levelName: String,
    val levelPoint: Int,
    val curLevelPoint: Int,
    val nextLevelPoint: Int,
)

data class LakeMessageCountUi(
    val like: Int,
    val floor: Int,
    val reply: Int,
    val notice: Int,
) {
    val total: Int get() = like + floor + reply + notice
}

data class LakeNoticeUi(
    val id: Long,
    val title: String,
    val content: String,
    val sender: String,
    val createdAt: String,
    val isRead: Boolean,
)

data class LakeMessageUi(
    val id: Long,
    val title: String,
    val content: String,
    val sender: String,
    val createdAt: String,
    val isRead: Boolean,
    val postId: Long?,
    val readId: Long = id,
    val readType: Int = 0,
)

fun LakePostDto.toUi(): LakePostUi = LakePostUi(
    id = id,
    uid = uid,
    type = type,
    solved = solved,
    title = title,
    content = content.withoutLegacyVoteWarning(),
    author = nickname,
    likes = like_count,
    favs = fav_count,
    comments = comment_count,
    createdAt = created_at.toLakeDateTime(),
    isLike = is_like,
    isFav = is_fav,
    isOwner = is_owner,
    rating = rating,
    visitCount = visit_count,
    eTag = e_tag.normalizedPostETag(rating),
    variant = variant,
    voteDetail = if (variant == 1) vote_detail?.toUi() else null,
    departmentId = department?.id,
    departmentName = department?.name.orEmpty(),
    level = user_info?.level ?: 0,
    avatar = user_info?.avatar.orEmpty(),
    tagId = tag?.id,
    tagName = tag?.name.orEmpty(),
    imageUrls = image_urls,
)

private fun String.normalizedPostETag(rating: Int): String {
    return when (this) {
        "top" -> if (rating > 0) "top" else ""
        "recommend", "theme" -> this
        else -> ""
    }
}

private fun String.withoutLegacyVoteWarning(): String =
    replace(
        "您的客户端版本过低请升级到最新版本后查看投票内容https://mobile.twt.edu.cn/wpy/index.html 下载最新版本(需要校园网环境)",
        "",
    ).trim()

fun LakeFloorDto.toUi(): LakeFloorUi = LakeFloorUi(
    id = id,
    uid = uid,
    postId = post_id,
    content = content.withoutLegacyVoteWarning(),
    author = nickname,
    createdAt = created_at.toLakeDateTime(),
    likes = like_count,
    level = user_info?.level ?: 0,
    avatar = user_info?.avatar.orEmpty(),
    imageUrl = image_url,
    replyTo = reply_to,
    replyToName = reply_to_name,
    sender = sender,
    subTo = sub_to,
    subFloors = sub_floors.map { it.toUi() }.sortedBy { it.createdAt },
    subFloorCount = sub_floor_cnt,
    isLike = is_like,
)

fun LakeTabDto.toUi(): LakeTabUi = LakeTabUi(id = id, name = name.ifBlank { shortname })
fun LakeTagDto.toUi(): LakeTagUi = LakeTagUi(id = id, name = name)
fun LakeDepartmentDto.toUi(): LakeDepartmentUi = LakeDepartmentUi(id = id, name = name)
fun LakeUserProfileDto.toUi(): LakeUserProfileUi = LakeUserProfileUi(
    uid = id,
    nickname = nickname,
    username = username,
    avatar = avatar,
    level = level_info?.level ?: 0,
    levelName = level_info?.level_name.orEmpty(),
    levelPoint = level_point,
    curLevelPoint = level_info?.cur_level_point ?: 0,
    nextLevelPoint = level_info?.next_level_point ?: 0,
)

fun LakeMessageCountDto.toUi(): LakeMessageCountUi = LakeMessageCountUi(
    like = like,
    floor = floor,
    reply = reply,
    notice = notice,
)

fun LakeNoticeDto.toUi(): LakeNoticeUi = LakeNoticeUi(
    id = id,
    title = title,
    content = content,
    sender = sender,
    createdAt = created_at.toLakeDateTime(),
    isRead = is_read,
)

fun LakeNoticeDto.toMessageUi(): LakeMessageUi = LakeMessageUi(
    id = id,
    title = title.ifBlank { "湖底通知" },
    content = content,
    sender = sender.ifBlank { "湖底管理" },
    createdAt = created_at.toLakeDateTime(),
    isRead = is_read,
    postId = null,
)

fun LakeLikeMessageDto.toUi(): LakeMessageUi {
    val targetPost = post
    val targetFloor = floor
    return LakeMessageUi(
        id = targetFloor?.id ?: targetPost?.id ?: 0,
        title = targetPost?.title?.ifBlank { "有人点赞了你的内容" } ?: "有人点赞了你的内容",
        content = targetFloor?.content?.ifBlank { targetPost?.content.orEmpty() }.orEmpty(),
        sender = "点赞提醒",
        createdAt = targetFloor?.created_at?.toLakeDateTime() ?: targetPost?.created_at?.toLakeDateTime().orEmpty(),
        isRead = false,
        postId = targetPost?.id ?: targetFloor?.post_id,
        readId = if (type == 0) targetPost?.id ?: 0 else targetFloor?.id ?: 0,
        readType = type,
    )
}

fun LakeFloorMessageDto.toUi(): LakeMessageUi {
    val targetPost = post
    val targetFloor = floor
    return LakeMessageUi(
        id = targetFloor?.id ?: targetPost?.id ?: 0,
        title = targetPost?.title?.ifBlank { "有人评论了你的帖子" } ?: "有人评论了你的帖子",
        content = targetFloor?.content.orEmpty(),
        sender = targetFloor?.nickname?.ifBlank { "评论提醒" } ?: "评论提醒",
        createdAt = targetFloor?.created_at?.toLakeDateTime().orEmpty(),
        isRead = is_read,
        postId = targetPost?.id ?: targetFloor?.post_id,
    )
}

fun LakeReplyMessageDto.toUi(): LakeMessageUi = LakeMessageUi(
    id = reply?.id ?: post?.id ?: 0,
    title = post?.title?.ifBlank { "校务回复" } ?: "校务回复",
    content = reply?.content.orEmpty(),
    sender = "校务回复",
    createdAt = reply?.created_at?.toLakeDateTime().orEmpty(),
    isRead = is_read,
    postId = post?.id ?: reply?.post_id,
)

fun LakeVoteDetailDto.toUi(): LakeVoteDetailUi = LakeVoteDetailUi(
    id = id,
    options = options.map { it.toUi() },
    maxSelection = max_selection,
    voteCount = vote_count,
)

fun LakeVoteOptionDto.toUi(): LakeVoteOptionUi = LakeVoteOptionUi(
    id = id,
    voteId = vote_id,
    content = content,
    count = count,
    selected = selected,
)

private fun String.toLakeDateTime(): String {
    if (isBlank()) return ""
    return runCatching {
        OffsetDateTime.parse(this).toLocalDateTime()
    }.recoverCatching {
        LocalDateTime.parse(replace("Z", ""))
    }.getOrNull()
        ?.format(lakeDateTimeFormatter)
        ?: take(19).replace('T', ' ')
}

