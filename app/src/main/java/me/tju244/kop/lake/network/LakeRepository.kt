package me.tju244.kop.lake.network

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class LakeApiException(
    val code: Int,
    override val message: String,
) : RuntimeException(message)

class LakeRepository(
    private val lakeApi: LakeApi,
    private val lakePicApi: LakePicApi,
) {
    suspend fun feed(page: Int = 1, type: Int = 0, tagId: Int? = null, keyword: String = "", sortMode: Int = 0): Result<List<LakePostUi>> = runCatching {
        val res = lakeApi.posts(
            page = page,
            type = type,
            searchMode = if (keyword.isBlank()) {
                if (sortMode == 1) 0 else 1
            } else {
                1
            },
            etag = if (keyword.isBlank() && type == 0) "recommend" else "",
            keyword = keyword,
            tagId = tagId?.toString() ?: "",
        )
        ensureSuccess(res.code, res.msg ?: "获取帖子失败")
        res.data?.list.orEmpty().map { it.toUi() }
    }

    suspend fun postTypes(): Result<List<LakeTabUi>> = runCatching {
        val res = lakeApi.postTypes()
        ensureSuccess(res.code, res.msg ?: "获取分区失败")
        listOf(LakeTabUi(id = 0, name = "精华")) + res.data?.list.orEmpty().map { it.toUi() }
    }

    suspend fun hotTags(): Result<List<LakeTagUi>> = runCatching {
        val res = lakeApi.hotTags()
        ensureSuccess(res.code, res.msg ?: "获取标签失败")
        res.data?.list.orEmpty().map { it.toUi() }
    }

    suspend fun favoritePosts(page: Int = 1, pageSize: Int = 10): Result<List<LakePostUi>> = runCatching {
        val res = lakeApi.favoritePosts(pageSize = pageSize, page = page)
        ensureSuccess(res.code, res.msg ?: "获取收藏列表失败")
        res.data?.list.orEmpty().map { it.toUi() }
    }

    suspend fun myPosts(page: Int = 1, pageSize: Int = 10): Result<List<LakePostUi>> = runCatching {
        val res = lakeApi.myPosts(pageSize = pageSize, page = page)
        ensureSuccess(res.code, res.msg ?: "获取我的帖子失败")
        res.data?.list.orEmpty().map { it.toUi() }
    }

    suspend fun historyPosts(page: Int = 1, pageSize: Int = 10): Result<List<LakePostUi>> = runCatching {
        val res = lakeApi.historyPosts(pageSize = pageSize, page = page)
        ensureSuccess(res.code, res.msg ?: "获取浏览历史失败")
        res.data?.list.orEmpty().map { it.toUi() }
    }

    suspend fun departments(): Result<List<LakeDepartmentUi>> = runCatching {
        val res = lakeApi.departments()
        ensureSuccess(res.code, res.msg ?: "获取部门失败")
        res.data?.list.orEmpty().map { it.toUi() }
    }

    suspend fun currentUser(): Result<LakeUserProfileUi> = runCatching {
        val res = lakeApi.currentUser()
        ensureSuccess(res.code, res.msg ?: "获取个人信息失败")
        res.data?.user?.toUi() ?: throw LakeApiException(500, "个人信息为空")
    }

    suspend fun updateUserName(name: String): Result<Unit> = runCatching {
        val res = lakeApi.updateUserName(name = name)
        ensureSuccess(res.code, res.msg ?: "修改昵称失败")
    }

    suspend fun messageCount(): Result<LakeMessageCountUi> = runCatching {
        val res = lakeApi.messageCount()
        ensureSuccess(res.code, res.msg ?: "获取通知数量失败")
        res.data?.count?.toUi() ?: LakeMessageCountUi(like = 0, floor = 0, reply = 0, notice = 0)
    }

    suspend fun lakeNotices(page: Int = 1): Result<List<LakeNoticeUi>> = runCatching {
        val res = lakeApi.lakeNotices(page = page)
        ensureSuccess(res.code, res.msg ?: "获取湖底通知失败")
        res.data?.list.orEmpty().map { it.toUi() }
    }

    suspend fun lakeMessages(category: LakeMessageCategory, page: Int = 1): Result<List<LakeMessageUi>> = runCatching {
        when (category) {
            LakeMessageCategory.Like -> {
                val res = lakeApi.likeMessages(page = page)
                ensureSuccess(res.code, res.msg ?: "获取点赞通知失败")
                res.data?.list.orEmpty().map { it.toUi() }
            }
            LakeMessageCategory.Floor -> {
                val res = lakeApi.floorMessages(page = page)
                ensureSuccess(res.code, res.msg ?: "获取回复通知失败")
                res.data?.list.orEmpty().map { it.toUi() }
            }
            LakeMessageCategory.Reply -> {
                val res = lakeApi.replyMessages(page = page)
                ensureSuccess(res.code, res.msg ?: "获取校务通知失败")
                res.data?.list.orEmpty().map { it.toUi() }
            }
            LakeMessageCategory.Notice -> {
                val res = lakeApi.lakeNotices(page = page)
                ensureSuccess(res.code, res.msg ?: "获取湖底通知失败")
                res.data?.list.orEmpty().map { it.toMessageUi() }
            }
        }
    }

    suspend fun markMessagesRead(category: LakeMessageCategory, messages: List<LakeMessageUi>): Result<Unit> = runCatching {
        val targets = messages
            .filter { it.readId > 0 }
            .distinctBy { "${it.readId}:${it.readType}" }
        var successCount = 0
        var lastError: Throwable? = null
        targets.forEach { message ->
            runCatching {
                when (category) {
                    LakeMessageCategory.Like -> {
                        val primary = lakeApi.readLikeMessage(message.readId, message.readType)
                        if (primary.code == 200) {
                            successCount += 1
                        } else {
                            val fallbackType = if (message.readType == 0) 1 else 0
                            val fallback = lakeApi.readLikeMessage(message.readId, fallbackType)
                            ensureSuccess(fallback.code, fallback.msg ?: "mark read failed")
                            successCount += 1
                        }
                    }
                    LakeMessageCategory.Floor -> {
                        val res = lakeApi.readFloorMessage(message.readId)
                        ensureSuccess(res.code, res.msg ?: "mark read failed")
                        successCount += 1
                    }
                    LakeMessageCategory.Reply -> {
                        val res = lakeApi.readReplyMessage(message.readId)
                        ensureSuccess(res.code, res.msg ?: "mark read failed")
                        successCount += 1
                    }
                    LakeMessageCategory.Notice -> {
                        val res = lakeApi.readNoticeMessage(message.readId)
                        ensureSuccess(res.code, res.msg ?: "mark read failed")
                        successCount += 1
                    }
                }
            }.onFailure { lastError = it }
        }
        if (targets.isNotEmpty() && successCount == 0) {
            throw (lastError ?: LakeApiException(500, "mark read failed"))
        }
    }

    suspend fun createPost(
        type: Int,
        title: String,
        content: String,
        tagId: Int?,
        departmentId: Int?,
        campus: Int = 0,
        masked: String = "",
        images: List<String> = emptyList(),
    ): Result<Unit> = runCatching {
        val res = lakeApi.createPost(
            type = type,
            title = title,
            content = content,
            departmentId = departmentId,
            tagId = tagId,
            campus = campus,
            masked = masked,
            images = images,
        )
        ensureSuccess(res.code, res.msg ?: "发帖失败")
    }

    suspend fun createVotePost(
        type: Int,
        title: String,
        tagId: Int?,
        campus: Int,
        maxSelection: Int,
        options: List<String>,
    ): Result<Unit> = runCatching {
        val res = lakeApi.createVotePost(
            type = type,
            title = title,
            campus = campus,
            tagId = tagId?.toString() ?: "",
            maxSelection = maxSelection,
            options = options,
        )
        ensureSuccess(res.code, res.msg ?: "发布投票失败")
    }

    suspend fun postDetail(postId: Long): Result<LakePostUi> = runCatching {
        val res = lakeApi.postById(postId)
        ensureSuccess(res.code, res.msg ?: "获取帖子详情失败")
        if (res.data?.post == null) throw LakeApiException(404, "post not found")
        res.data.post.toUi()
    }

    suspend fun floors(postId: Long, page: Int = 1, order: Int = 2, onlyOwner: Boolean = false): Result<List<LakeFloorUi>> = runCatching {
        val res = lakeApi.floors(postId = postId, page = page, order = order, onlyOwner = if (onlyOwner) 1 else 0)
        ensureSuccess(res.code, res.msg ?: "获取评论失败")
        res.data?.list.orEmpty().map { it.toUi() }
    }

    suspend fun officialReplies(postId: Long): Result<List<LakeFloorUi>> = runCatching {
        val res = lakeApi.officialReplies(postId = postId)
        ensureSuccess(res.code, res.msg ?: "获取校务回复失败")
        res.data?.list.orEmpty().map { it.toUi() }
    }

    suspend fun visit(postId: Long): Result<Unit> = runCatching {
        val res = lakeApi.visitPost(postId)
        ensureSuccess(res.code, res.msg ?: "记录浏览失败")
    }

    suspend fun toggleLike(postId: Long, isLikeNow: Boolean): Result<Unit> = runCatching {
        val op = if (isLikeNow) 0 else 1
        val res = lakeApi.likePost(postId = postId, op = op)
        ensureSuccess(res.code, res.msg ?: "点赞失败")
    }

    suspend fun toggleFav(postId: Long, isFavNow: Boolean): Result<Unit> = runCatching {
        val op = if (isFavNow) 0 else 1
        val res = lakeApi.favPost(postId = postId, op = op)
        ensureSuccess(res.code, res.msg ?: "收藏失败")
    }

    suspend fun toggleFloorLike(floorId: Long, isLikeNow: Boolean): Result<Unit> = runCatching {
        val op = if (isLikeNow) 0 else 1
        val res = lakeApi.likeFloor(floorId = floorId, op = op)
        ensureSuccess(res.code, res.msg ?: "点赞失败")
    }

    suspend fun vote(voteId: Long, selectedOptionIds: List<Long>): Result<Unit> = runCatching {
        val res = lakeApi.votePost(
            voteId = voteId,
            selected = selectedOptionIds.joinToString(","),
        )
        ensureSuccess(res.code, res.msg ?: "投票失败")
    }

    suspend fun sendComment(postId: Long, content: String, images: List<String> = emptyList()): Result<Unit> = runCatching {
        val res = lakeApi.sendFloor(
            postId = postId.toString().textPart(),
            content = content.textPart(),
            images = images.toImageTextParts(),
        )
        ensureSuccess(res.code, res.msg ?: "发表评论失败")
    }

    suspend fun replyComment(floorId: Long, content: String, images: List<String> = emptyList()): Result<Unit> = runCatching {
        val res = lakeApi.replyFloor(
            floorId = floorId.toString().textPart(),
            content = content.textPart(),
            images = images.toImageTextParts(),
        )
        ensureSuccess(res.code, res.msg ?: "回复失败")
    }

    suspend fun uploadImages(contentResolver: ContentResolver, imageUris: List<Uri>): Result<List<String>> = runCatching {
        if (imageUris.isEmpty()) return@runCatching emptyList()
        val parts = imageUris.mapIndexed { index, uri ->
            val mediaType = contentResolver.getType(uri)?.toMediaTypeOrNull() ?: "image/jpeg".toMediaTypeOrNull()
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw LakeApiException(500, "图片读取失败")
            MultipartBody.Part.createFormData(
                name = "images",
                filename = "lake_${System.currentTimeMillis()}_$index.jpg",
                body = bytes.toRequestBody(mediaType),
            )
        }
        val res = lakePicApi.uploadImages(parts)
        ensureSuccess(res.code, res.msg ?: "上传图片失败")
        res.data?.urls.orEmpty()
    }

    suspend fun report(postId: Long, floorId: Long = 0, reason: String): Result<Unit> = runCatching {
        val type = if (floorId > 0) 2 else 1
        val res = lakeApi.report(type = type, postId = postId, floorId = floorId, reason = reason)
        ensureSuccess(res.code, res.msg ?: "举报失败")
    }

    suspend fun deletePost(postId: Long): Result<Unit> = runCatching {
        val res = lakeApi.deletePost(postId = postId)
        ensureSuccess(res.code, res.msg ?: "删除失败")
    }

    suspend fun deleteFloor(floorId: Long): Result<Unit> = runCatching {
        val res = lakeApi.deleteFloor(floorId = floorId)
        ensureSuccess(res.code, res.msg ?: "删除失败")
    }

    private fun ensureSuccess(code: Int, fallbackMessage: String) {
        if (code == 200) return
        val message = when (code) {
            401, 403 -> "登录状态已过期，请重新登录"
            in 500..599 -> "服务暂时不可用，请稍后重试"
            else -> fallbackMessage
        }
        throw LakeApiException(code, message)
    }

    private fun String.textPart() = toRequestBody("text/plain".toMediaTypeOrNull())

    private fun List<String>.toImageTextParts(): List<MultipartBody.Part> {
        val values = if (isEmpty()) listOf("") else this
        return values.map { value -> MultipartBody.Part.createFormData("images", value) }
    }
}

