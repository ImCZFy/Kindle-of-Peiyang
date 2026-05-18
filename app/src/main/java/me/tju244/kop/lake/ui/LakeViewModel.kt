package me.tju244.kop.lake.ui

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.tju244.kop.auth.data.LakeTokenRepository
import me.tju244.kop.lake.network.LakeFloorUi
import me.tju244.kop.lake.network.LakeApiException
import me.tju244.kop.lake.network.LakePostUi
import me.tju244.kop.lake.network.LakeRepository
import me.tju244.kop.lake.network.LakeTabUi
import me.tju244.kop.lake.network.LakeDepartmentUi
import me.tju244.kop.lake.network.LakeMessageCountUi
import me.tju244.kop.lake.network.LakeMessageCategory
import me.tju244.kop.lake.network.LakeMessageUi
import me.tju244.kop.lake.network.LakeNoticeUi
import me.tju244.kop.lake.network.LakeUserProfileUi
import me.tju244.kop.lake.network.LakeTagUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LakeUiState(
    val loading: Boolean = false,
    val loadingPostDetail: Boolean = false,
    val posts: List<LakePostUi> = emptyList(),
    val searchPosts: List<LakePostUi> = emptyList(),
    val searchKeyword: String = "",
    val searchPage: Int = 1,
    val searchHasMore: Boolean = true,
    val loadingSearch: Boolean = false,
    val loadingMoreSearch: Boolean = false,
    val feedCaches: Map<String, LakeFeedSnapshot> = emptyMap(),
    val favoritePosts: List<LakePostUi> = emptyList(),
    val myPosts: List<LakePostUi> = emptyList(),
    val historyPosts: List<LakePostUi> = emptyList(),
    val myPostPage: Int = 1,
    val myPostHasMore: Boolean = true,
    val loadingMyPosts: Boolean = false,
    val loadingMoreMyPosts: Boolean = false,
    val historyPage: Int = 1,
    val historyHasMore: Boolean = true,
    val loadingHistory: Boolean = false,
    val loadingMoreHistory: Boolean = false,
    val favoritePage: Int = 1,
    val favoriteHasMore: Boolean = true,
    val loadingFavorites: Boolean = false,
    val loadingMoreFavorites: Boolean = false,
    val tabs: List<LakeTabUi> = emptyList(),
    val tags: List<LakeTagUi> = emptyList(),
    val departments: List<LakeDepartmentUi> = emptyList(),
    val selectedType: Int = 0,
    val selectedTagId: Int? = null,
    val selectedDepartmentId: Int? = null,
    val sortMode: Int = 0,
    val displayMode: Int = 0,
    val selectedPost: LakePostUi? = null,
    val floors: List<LakeFloorUi> = emptyList(),
    val officialFloors: List<LakeFloorUi> = emptyList(),
    val floorPage: Int = 1,
    val floorOrder: Int = 2,
    val onlyOwner: Boolean = false,
    val floorHasMore: Boolean = true,
    val loadingMoreFloors: Boolean = false,
    val creatingPost: Boolean = false,
    val loadingMore: Boolean = false,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val keyword: String = "",
    val currentUserUid: Long? = null,
    val currentUserAvatar: String = "",
    val currentUserName: String = "",
    val currentUserProfile: LakeUserProfileUi? = null,
    val messageCount: LakeMessageCountUi? = null,
    val notices: List<LakeNoticeUi> = emptyList(),
    val notificationCategory: LakeMessageCategory = LakeMessageCategory.Like,
    val notificationMessages: List<LakeMessageUi> = emptyList(),
    val loadingNotices: Boolean = false,
    val profileSaving: Boolean = false,
    val sending: Boolean = false,
    val error: String? = null,
    val requireRelogin: Boolean = false,
    val snackbar: String? = null,
    val shareText: String? = null,
    val clipboardPreviewPost: LakePostUi? = null,
)

data class LakeFeedSnapshot(
    val posts: List<LakePostUi> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = true,
)

class LakeViewModel(
    private val lakeTokenRepository: LakeTokenRepository,
    private val lakeRepository: LakeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LakeUiState())
    val uiState: StateFlow<LakeUiState> = _uiState.asStateFlow()

    private val pageSize = 10
    private val floorPageSize = 20
    private var detailLoadGeneration: Long = 0L

    fun loadFavoritePosts(forceRefresh: Boolean = false) {
        val state = _uiState.value
        if (state.loadingFavorites && !forceRefresh) return
        if (!forceRefresh && state.favoritePosts.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadingFavorites = true, error = null)
            val tokenResult = lakeTokenRepository.ensureLakeToken()
            if (tokenResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    loadingFavorites = false,
                    error = userFacingError(tokenResult.exceptionOrNull(), "湖底 token 获取失败"),
                    requireRelogin = true,
                )
                return@launch
            }
            val result = lakeRepository.favoritePosts(page = 1, pageSize = pageSize)
            val favorites = result.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                favoritePosts = favorites,
                favoritePage = 1,
                favoriteHasMore = favorites.size >= pageSize,
                loadingFavorites = false,
                error = userFacingError(result.exceptionOrNull(), "").ifBlank { null },
                requireRelogin = shouldRelogin(result.exceptionOrNull()),
            )
        }
    }

    fun loadHistoryPosts(forceRefresh: Boolean = false) {
        val state = _uiState.value
        if (state.loadingHistory && !forceRefresh) return
        if (!forceRefresh && state.historyPosts.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadingHistory = true, error = null)
            val tokenResult = lakeTokenRepository.ensureLakeToken()
            if (tokenResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    loadingHistory = false,
                    error = userFacingError(tokenResult.exceptionOrNull(), "湖底 token 获取失败"),
                    requireRelogin = true,
                )
                return@launch
            }
            val result = lakeRepository.historyPosts(page = 1, pageSize = pageSize)
            val list = result.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                historyPosts = list,
                historyPage = 1,
                historyHasMore = list.size >= pageSize,
                loadingHistory = false,
                error = userFacingError(result.exceptionOrNull(), "").ifBlank { null },
                requireRelogin = shouldRelogin(result.exceptionOrNull()),
            )
        }
    }

    fun loadMyPosts(forceRefresh: Boolean = false) {
        val state = _uiState.value
        if (state.loadingMyPosts && !forceRefresh) return
        if (!forceRefresh && state.myPosts.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadingMyPosts = true, error = null)
            val tokenResult = lakeTokenRepository.ensureLakeToken()
            if (tokenResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    loadingMyPosts = false,
                    error = userFacingError(tokenResult.exceptionOrNull(), "湖底 token 获取失败"),
                    requireRelogin = true,
                )
                return@launch
            }
            val result = lakeRepository.myPosts(page = 1, pageSize = pageSize)
            val list = result.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                myPosts = list,
                myPostPage = 1,
                myPostHasMore = list.size >= pageSize,
                loadingMyPosts = false,
                error = userFacingError(result.exceptionOrNull(), "").ifBlank { null },
                requireRelogin = shouldRelogin(result.exceptionOrNull()),
            )
        }
    }

    fun loadMoreMyPosts() {
        val state = _uiState.value
        if (state.loadingMyPosts || state.loadingMoreMyPosts || !state.myPostHasMore) return
        viewModelScope.launch {
            _uiState.value = state.copy(loadingMoreMyPosts = true, error = null)
            val nextPage = state.myPostPage + 1
            val result = lakeRepository.myPosts(page = nextPage, pageSize = pageSize)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    loadingMoreMyPosts = false,
                    error = userFacingError(result.exceptionOrNull(), "加载更多帖子失败"),
                    requireRelogin = shouldRelogin(result.exceptionOrNull()),
                )
                return@launch
            }
            val append = result.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                myPosts = (state.myPosts + append).distinctBy { it.id },
                myPostPage = nextPage,
                myPostHasMore = append.size >= pageSize,
                loadingMoreMyPosts = false,
            )
        }
    }

    fun loadMoreFavoritePosts() {
        val state = _uiState.value
        if (state.loadingFavorites || state.loadingMoreFavorites || !state.favoriteHasMore) return
        viewModelScope.launch {
            _uiState.value = state.copy(loadingMoreFavorites = true, error = null)
            val nextPage = state.favoritePage + 1
            val result = lakeRepository.favoritePosts(page = nextPage, pageSize = pageSize)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    loadingMoreFavorites = false,
                    error = userFacingError(result.exceptionOrNull(), "加载更多收藏失败"),
                    requireRelogin = shouldRelogin(result.exceptionOrNull()),
                )
                return@launch
            }
            val append = result.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                favoritePosts = (state.favoritePosts + append).distinctBy { it.id },
                favoritePage = nextPage,
                favoriteHasMore = append.size >= pageSize,
                loadingMoreFavorites = false,
            )
        }
    }

    fun loadMoreHistoryPosts() {
        val state = _uiState.value
        if (state.loadingHistory || state.loadingMoreHistory || !state.historyHasMore) return
        viewModelScope.launch {
            _uiState.value = state.copy(loadingMoreHistory = true, error = null)
            val nextPage = state.historyPage + 1
            val result = lakeRepository.historyPosts(page = nextPage, pageSize = pageSize)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    loadingMoreHistory = false,
                    error = userFacingError(result.exceptionOrNull(), "加载更多历史失败"),
                    requireRelogin = shouldRelogin(result.exceptionOrNull()),
                )
                return@launch
            }
            val append = result.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                historyPosts = (state.historyPosts + append).distinctBy { it.id },
                historyPage = nextPage,
                historyHasMore = append.size >= pageSize,
                loadingMoreHistory = false,
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val tokenResult = lakeTokenRepository.ensureLakeToken()
            if (tokenResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = userFacingError(tokenResult.exceptionOrNull(), "湖底 token 获取失败"),
                    requireRelogin = true,
                )
                return@launch
            }

            val tokenUid = lakeTokenRepository.currentLakeUid()
            val tokenAvatar = lakeTokenRepository.currentLakeAvatar()
            val tokenName = lakeTokenRepository.currentLakeNickname()
            val tabResult = lakeRepository.postTypes()
            val tagResult = lakeRepository.hotTags()
            val departmentResult = lakeRepository.departments()
            val messageCountResult = lakeRepository.messageCount()
            val current = _uiState.value
            val type = current.selectedType
            val tagId = current.selectedTagId
            val postsResult = lakeRepository.feed(page = 1, type = type, tagId = tagId, keyword = current.keyword, sortMode = current.sortMode)
            val posts = postsResult.getOrDefault(emptyList())
            val snapshot = LakeFeedSnapshot(posts = posts, page = 1, hasMore = posts.size >= pageSize)
            val cacheKey = current.feedCacheKey()
            val latest = _uiState.value
            if (latest.feedCacheKey() != cacheKey) {
                _uiState.value = latest.copy(
                    loading = false,
                    tabs = if (tabResult.isSuccess) tabResult.getOrDefault(emptyList()) else latest.tabs,
                    tags = if (tagResult.isSuccess) tagResult.getOrDefault(emptyList()) else latest.tags,
                    departments = if (departmentResult.isSuccess) departmentResult.getOrDefault(emptyList()) else latest.departments,
                    messageCount = messageCountResult.getOrNull() ?: latest.messageCount,
                    feedCaches = latest.feedCaches + (cacheKey to snapshot),
                )
                return@launch
            }
            _uiState.value = latest.copy(
                loading = false,
                tabs = if (tabResult.isSuccess) tabResult.getOrDefault(emptyList()) else latest.tabs,
                tags = if (tagResult.isSuccess) tagResult.getOrDefault(emptyList()) else latest.tags,
                departments = if (departmentResult.isSuccess) departmentResult.getOrDefault(emptyList()) else latest.departments,
                messageCount = messageCountResult.getOrNull() ?: latest.messageCount,
                posts = posts,
                feedCaches = latest.feedCaches + (cacheKey to snapshot),
                currentUserUid = tokenUid,
                currentUserAvatar = posts.findPostUserAvatar(tokenUid).ifBlank { tokenAvatar },
                currentUserName = posts.findPostUserName(tokenUid).ifBlank { tokenName },
                page = 1,
                hasMore = posts.size >= pageSize,
                error = userFacingError(
                    postsResult.exceptionOrNull()
                        ?: tabResult.exceptionOrNull()
                        ?: tagResult.exceptionOrNull()
                        ?: departmentResult.exceptionOrNull()
                        ?: messageCountResult.exceptionOrNull(),
                    ""
                ).ifBlank { null },
                requireRelogin = shouldRelogin(
                    postsResult.exceptionOrNull(),
                    tabResult.exceptionOrNull(),
                    tagResult.exceptionOrNull(),
                    departmentResult.exceptionOrNull(),
                    messageCountResult.exceptionOrNull(),
                ),
            )
            preloadVisibleTabCaches(_uiState.value.tabs)
        }
    }

    fun loadCurrentUserProfile() {
        viewModelScope.launch {
            val tokenResult = lakeTokenRepository.ensureLakeToken()
            if (tokenResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = userFacingError(tokenResult.exceptionOrNull(), "湖底 token 获取失败"),
                    requireRelogin = true,
                )
                return@launch
            }
            val result = lakeRepository.currentUser()
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = userFacingError(result.exceptionOrNull(), "获取个人信息失败"),
                    requireRelogin = shouldRelogin(result.exceptionOrNull()),
                )
                return@launch
            }
            val profile = result.getOrThrow()
            val storedUsername = lakeTokenRepository.currentLakeUsername()
            val authUserNumber = lakeTokenRepository.authUserNumber()
            val finalUsername = when {
                profile.username.isStudentNumber() -> profile.username
                storedUsername.isStudentNumber() -> storedUsername
                authUserNumber.isStudentNumber() -> authUserNumber
                else -> ""
            }
            _uiState.value = _uiState.value.copy(
                currentUserProfile = profile.copy(username = finalUsername),
                currentUserUid = profile.uid,
                currentUserAvatar = profile.avatar,
                currentUserName = profile.nickname,
            )
        }
    }

    fun updateProfileNickname(nickname: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val normalized = nickname.trim()
        if (normalized.isBlank()) {
            onResult(false, "昵称不能为空")
            return
        }
        if (normalized.length > 20) {
            onResult(false, "昵称最多 20 字")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(profileSaving = true, error = null)
            val result = lakeRepository.updateUserName(normalized)
            if (result.isFailure) {
                val message = userFacingError(result.exceptionOrNull(), "保存失败")
                _uiState.value = _uiState.value.copy(
                    profileSaving = false,
                    error = message,
                    requireRelogin = shouldRelogin(result.exceptionOrNull()),
                )
                onResult(false, message)
                return@launch
            }
            val state = _uiState.value
            val profile = state.currentUserProfile
            _uiState.value = state.copy(
                profileSaving = false,
                currentUserName = normalized,
                currentUserProfile = profile?.copy(nickname = normalized),
                posts = state.posts.map { post ->
                    if (state.currentUserUid != null && post.uid == state.currentUserUid) post.copy(author = normalized) else post
                },
                favoritePosts = state.favoritePosts.map { post ->
                    if (state.currentUserUid != null && post.uid == state.currentUserUid) post.copy(author = normalized) else post
                },
            )
            onResult(true, "保存成功")
        }
    }

    fun loadLakeNotifications(category: LakeMessageCategory = _uiState.value.notificationCategory) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                loadingNotices = true,
                notificationCategory = category,
                error = null,
            )
            val countResult = lakeRepository.messageCount()
            val messageResult = lakeRepository.lakeMessages(category = category, page = 1)
            _uiState.value = _uiState.value.copy(
                messageCount = countResult.getOrNull() ?: _uiState.value.messageCount,
                notificationMessages = messageResult.getOrDefault(_uiState.value.notificationMessages),
                loadingNotices = false,
                error = userFacingError(
                    countResult.exceptionOrNull() ?: messageResult.exceptionOrNull(),
                    "获取通知失败",
                ).takeIf { countResult.isFailure || messageResult.isFailure },
                requireRelogin = shouldRelogin(countResult.exceptionOrNull(), messageResult.exceptionOrNull()),
            )
        }
    }

    fun markCurrentNotificationCategoryRead() {
        val state = _uiState.value
        val category = state.notificationCategory
        val messages = state.notificationMessages
        if (messages.isEmpty() || state.loadingNotices) return
        viewModelScope.launch {
            val optimisticMessages = messages.map { it.copy(isRead = true) }
            _uiState.value = state.copy(
                notificationMessages = optimisticMessages,
                loadingNotices = true,
                error = null,
            )
            val readResult = lakeRepository.markMessagesRead(category, messages)
            val countResult = if (readResult.isSuccess) lakeRepository.messageCount() else Result.failure(readResult.exceptionOrNull() ?: IllegalStateException("mark read failed"))
            val messageResult = if (readResult.isSuccess) lakeRepository.lakeMessages(category = category, page = 1) else Result.failure(readResult.exceptionOrNull() ?: IllegalStateException("mark read failed"))
            _uiState.value = _uiState.value.copy(
                messageCount = countResult.getOrNull() ?: _uiState.value.messageCount,
                notificationMessages = messageResult.getOrDefault(optimisticMessages),
                loadingNotices = false,
                error = userFacingError(
                    readResult.exceptionOrNull()
                        ?: countResult.exceptionOrNull()
                        ?: messageResult.exceptionOrNull(),
                    "标记已读失败",
                ).takeIf { readResult.isFailure || countResult.isFailure || messageResult.isFailure },
                requireRelogin = shouldRelogin(
                    readResult.exceptionOrNull(),
                    countResult.exceptionOrNull(),
                    messageResult.exceptionOrNull(),
                ),
            )
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.loading || state.loadingMore || !state.hasMore || state.selectedPost != null) return
        val requestKey = state.feedCacheKey()
        viewModelScope.launch {
            _uiState.value = state.copy(loadingMore = true, error = null)
            val nextPage = state.page + 1
            val result = lakeRepository.feed(page = nextPage, type = state.selectedType, tagId = state.selectedTagId, keyword = state.keyword, sortMode = state.sortMode)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    loadingMore = false,
                    error = userFacingError(result.exceptionOrNull(), "加载更多失败"),
                    requireRelogin = shouldRelogin(result.exceptionOrNull())
                )
                return@launch
            }
            val append = result.getOrDefault(emptyList())
            val basePosts = _uiState.value.feedCaches[requestKey]?.posts ?: state.posts
            val mergedPosts = (basePosts + append).distinctBy { it.id }
            val snapshot = LakeFeedSnapshot(
                posts = mergedPosts,
                page = nextPage,
                hasMore = append.size >= pageSize,
            )
            val current = _uiState.value
            _uiState.value = if (current.feedCacheKey() == requestKey) {
                current.copy(
                    loadingMore = false,
                    posts = mergedPosts,
                    feedCaches = current.feedCaches + (requestKey to snapshot),
                    page = nextPage,
                    hasMore = append.size >= pageSize,
                )
            } else {
                current.copy(
                    loadingMore = false,
                    feedCaches = current.feedCaches + (requestKey to snapshot),
                )
            }
        }
    }

    fun setKeyword(keyword: String) {
        _uiState.value = _uiState.value.copy(keyword = keyword)
        refresh()
    }

    fun search(keyword: String) {
        val normalized = keyword.trim()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                searchKeyword = normalized,
                searchPosts = emptyList(),
                searchPage = 1,
                searchHasMore = true,
                loadingSearch = true,
                error = null,
            )
            val result = lakeRepository.feed(page = 1, keyword = normalized, sortMode = _uiState.value.sortMode)
            val posts = result.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                searchPosts = posts,
                searchPage = 1,
                searchHasMore = posts.size >= pageSize,
                loadingSearch = false,
                error = userFacingError(result.exceptionOrNull(), "").ifBlank { null },
                requireRelogin = shouldRelogin(result.exceptionOrNull()),
            )
        }
    }

    fun refreshSearch() {
        val keyword = _uiState.value.searchKeyword
        if (keyword.isBlank()) return
        viewModelScope.launch {
            val state = _uiState.value
            if (state.loadingSearch) return@launch
            _uiState.value = state.copy(
                loadingSearch = true,
                loadingMoreSearch = false,
                error = null,
            )
            val result = lakeRepository.feed(page = 1, keyword = keyword, sortMode = state.sortMode)
            val posts = result.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                searchPosts = posts,
                searchPage = 1,
                searchHasMore = posts.size >= pageSize,
                loadingSearch = false,
                error = userFacingError(result.exceptionOrNull(), "").ifBlank { null },
                requireRelogin = shouldRelogin(result.exceptionOrNull()),
            )
        }
    }

    fun loadMoreSearch() {
        val state = _uiState.value
        if (state.loadingSearch || state.loadingMoreSearch || !state.searchHasMore || state.searchKeyword.isBlank()) return
        viewModelScope.launch {
            _uiState.value = state.copy(loadingMoreSearch = true, error = null)
            val nextPage = state.searchPage + 1
            val result = lakeRepository.feed(page = nextPage, keyword = state.searchKeyword, sortMode = state.sortMode)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    loadingMoreSearch = false,
                    error = userFacingError(result.exceptionOrNull(), "加载更多失败"),
                    requireRelogin = shouldRelogin(result.exceptionOrNull()),
                )
                return@launch
            }
            val append = result.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                loadingMoreSearch = false,
                searchPosts = (state.searchPosts + append).distinctBy { it.id },
                searchPage = nextPage,
                searchHasMore = append.size >= pageSize,
            )
        }
    }

    fun selectType(type: Int) {
        val nextState = _uiState.value.copy(selectedType = type, selectedDepartmentId = if (type == 1) _uiState.value.selectedDepartmentId else null)
        val cached = nextState.feedCaches[nextState.feedCacheKey()]
        _uiState.value = if (cached != null) {
            nextState.copy(posts = cached.posts, page = cached.page, hasMore = cached.hasMore, loading = false)
        } else {
            nextState.copy(posts = emptyList(), page = 1, hasMore = true)
        }
        if (cached == null) refresh()
    }

    fun selectDepartment(departmentId: Int?) {
        _uiState.value = _uiState.value.copy(selectedDepartmentId = departmentId)
    }

    fun selectTag(tagId: Int?) {
        val nextState = _uiState.value.copy(selectedTagId = tagId)
        val cached = nextState.feedCaches[nextState.feedCacheKey()]
        _uiState.value = if (cached != null) {
            nextState.copy(posts = cached.posts, page = cached.page, hasMore = cached.hasMore, loading = false)
        } else {
            nextState.copy(posts = emptyList(), page = 1, hasMore = true)
        }
        if (cached == null) refresh()
    }

    fun setSortMode(sortMode: Int) {
        if (_uiState.value.sortMode == sortMode) return
        _uiState.value = _uiState.value.copy(sortMode = sortMode)
        refresh()
    }

    fun setFloorOrder(order: Int) {
        if (_uiState.value.floorOrder == order) return
        _uiState.value = _uiState.value.copy(floorOrder = order)
        refreshSelectedPost()
    }

    fun setOnlyOwner(onlyOwner: Boolean) {
        if (_uiState.value.onlyOwner == onlyOwner) return
        _uiState.value = _uiState.value.copy(onlyOwner = onlyOwner)
        refreshSelectedPost()
    }

    fun setDisplayMode(displayMode: Int) {
        _uiState.value = _uiState.value.copy(displayMode = displayMode)
    }

    fun openPost(postId: Long) {
        viewModelScope.launch {
            val generation = ++detailLoadGeneration
            _uiState.value = _uiState.value.copy(loading = true, loadingPostDetail = true, error = null)
            lakeRepository.visit(postId)
            loadPostAndFirstFloors(postId, generation)
        }
    }

    fun refreshSelectedPost() {
        val postId = _uiState.value.selectedPost?.id ?: return
        viewModelScope.launch {
            val generation = ++detailLoadGeneration
            _uiState.value = _uiState.value.copy(loading = true, loadingPostDetail = true, error = null)
            loadPostAndFirstFloors(postId, generation)
        }
    }

    private suspend fun loadPostAndFirstFloors(postId: Long, generation: Long) {
        val postRes = lakeRepository.postDetail(postId)
        val floorRes = lakeRepository.floors(postId, page = 1, order = _uiState.value.floorOrder, onlyOwner = _uiState.value.onlyOwner)
        val officialRes = lakeRepository.officialReplies(postId)
        if (generation != detailLoadGeneration) return
        if (postRes.isFailure) {
            _uiState.value = _uiState.value.copy(
                loading = false,
                loadingPostDetail = false,
                error = userFacingError(postRes.exceptionOrNull(), "加载详情失败"),
                requireRelogin = shouldRelogin(postRes.exceptionOrNull())
            )
            return
        }
        val floors = floorRes.getOrDefault(emptyList())
        val tokenUid = lakeTokenRepository.currentLakeUid()
        val tokenAvatar = lakeTokenRepository.currentLakeAvatar()
        val tokenName = lakeTokenRepository.currentLakeNickname()
        _uiState.value = _uiState.value.copy(
            loading = false,
            loadingPostDetail = false,
            selectedPost = postRes.getOrNull(),
            floors = floors,
            officialFloors = officialRes.getOrDefault(emptyList()),
            currentUserUid = tokenUid,
            currentUserAvatar = listOfNotNull(postRes.getOrNull()).findPostUserAvatar(tokenUid)
                .ifBlank { floors.findFloorUserAvatar(tokenUid) }
                .ifBlank { _uiState.value.currentUserAvatar }
                .ifBlank { tokenAvatar },
            currentUserName = listOfNotNull(postRes.getOrNull()).findPostUserName(tokenUid)
                .ifBlank { floors.findFloorUserName(tokenUid) }
                .ifBlank { _uiState.value.currentUserName }
                .ifBlank { tokenName },
            floorPage = 1,
            floorHasMore = floors.size >= floorPageSize,
            error = userFacingError(floorRes.exceptionOrNull() ?: officialRes.exceptionOrNull(), "").ifBlank { null },
            requireRelogin = shouldRelogin(floorRes.exceptionOrNull(), officialRes.exceptionOrNull()),
        )
    }

    fun loadMoreFloors() {
        val state = _uiState.value
        val post = state.selectedPost ?: return
        if (state.loadingMoreFloors || !state.floorHasMore) return
        viewModelScope.launch {
            _uiState.value = state.copy(loadingMoreFloors = true, error = null)
            val nextPage = state.floorPage + 1
            val result = lakeRepository.floors(post.id, page = nextPage, order = state.floorOrder, onlyOwner = state.onlyOwner)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    loadingMoreFloors = false,
                    error = userFacingError(result.exceptionOrNull(), "加载更多评论失败"),
                    requireRelogin = shouldRelogin(result.exceptionOrNull()),
                )
                return@launch
            }
            val append = result.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                loadingMoreFloors = false,
                floors = _uiState.value.floors + append,
                floorPage = nextPage,
                floorHasMore = append.size >= floorPageSize,
            )
        }
    }

    fun closeDetail() {
        detailLoadGeneration += 1
        _uiState.value = _uiState.value.copy(
            loading = false,
            selectedPost = null,
            floors = emptyList(),
            officialFloors = emptyList(),
            loadingPostDetail = false,
            error = null,
        )
    }

    fun toggleLike() {
        val post = _uiState.value.selectedPost ?: return
        togglePostLike(post)
    }

    fun toggleListPostLike(postId: Long) {
        val state = _uiState.value
        val post = state.posts.firstOrNull { it.id == postId }
            ?: state.searchPosts.firstOrNull { it.id == postId }
            ?: return
        togglePostLike(post)
    }

    private fun togglePostLike(post: LakePostUi) {
        val nextLike = !post.isLike
        val nextLikes = (post.likes + if (nextLike) 1 else -1).coerceAtLeast(0)
        updatePost(post.id) { it.copy(isLike = nextLike, likes = nextLikes) }
        viewModelScope.launch {
            val result = lakeRepository.toggleLike(post.id, post.isLike)
            if (result.isFailure) {
                updatePost(post.id) { it.copy(isLike = post.isLike, likes = post.likes) }
                _uiState.value = _uiState.value.copy(
                    error = userFacingError(result.exceptionOrNull(), "点赞失败"),
                    requireRelogin = shouldRelogin(result.exceptionOrNull())
                )
            }
        }
    }

    fun toggleFav() {
        val post = _uiState.value.selectedPost ?: return
        val nextFav = !post.isFav
        updatePost(post.id) { it.copy(isFav = nextFav) }
        viewModelScope.launch {
            val result = lakeRepository.toggleFav(post.id, post.isFav)
            if (result.isFailure) {
                updatePost(post.id) { it.copy(isFav = post.isFav) }
                _uiState.value = _uiState.value.copy(
                    error = userFacingError(result.exceptionOrNull(), "收藏失败"),
                    requireRelogin = shouldRelogin(result.exceptionOrNull())
                )
            }
        }
    }

    fun toggleFloorLike(floorId: Long) {
        val floor = findFloor(_uiState.value.floors, floorId) ?: return
        val nextLike = !floor.isLike
        val nextLikes = (floor.likes + if (nextLike) 1 else -1).coerceAtLeast(0)
        updateFloor(floorId) { it.copy(isLike = nextLike, likes = nextLikes) }
        viewModelScope.launch {
            val result = lakeRepository.toggleFloorLike(floorId, floor.isLike)
            if (result.isFailure) {
                updateFloor(floorId) { it.copy(isLike = floor.isLike, likes = floor.likes) }
                _uiState.value = _uiState.value.copy(
                    error = userFacingError(result.exceptionOrNull(), "点赞失败"),
                    requireRelogin = shouldRelogin(result.exceptionOrNull())
                )
            }
        }
    }

    fun submitVote(optionIds: List<Long>) {
        val post = _uiState.value.selectedPost ?: return
        val vote = post.voteDetail ?: return
        val normalized = optionIds.distinct().take(vote.maxSelection.coerceAtLeast(1))
        if (normalized.isEmpty()) return
        val previousPost = post
        val previousVote = vote
        val previousSelection = previousVote.options.filter { it.selected }.map { it.id }.toSet()
        val nextSelection = normalized.toSet()
        val hadSelection = previousSelection.isNotEmpty()
        val nextVote = previousVote.copy(
            voteCount = previousVote.voteCount + if (hadSelection) 0 else 1,
            options = previousVote.options.map { option ->
                val wasSelected = option.id in previousSelection
                val isSelected = option.id in nextSelection
                option.copy(
                    selected = isSelected,
                    count = (option.count + when {
                        !wasSelected && isSelected -> 1
                        wasSelected && !isSelected -> -1
                        else -> 0
                    }).coerceAtLeast(0),
                )
            },
        )
        updatePost(post.id) { it.copy(voteDetail = nextVote) }
        viewModelScope.launch {
            val result = lakeRepository.vote(vote.id, normalized)
            if (result.isFailure) {
                updatePost(post.id) { previousPost }
                _uiState.value = _uiState.value.copy(
                    error = userFacingError(result.exceptionOrNull(), "投票失败"),
                    requireRelogin = shouldRelogin(result.exceptionOrNull())
                )
                return@launch
            }
            refreshSelectedPost()
        }
    }

    fun sendComment(content: String, imageUris: List<Uri> = emptyList(), contentResolver: ContentResolver? = null) {
        val post = _uiState.value.selectedPost ?: return
        if (content.isBlank() && imageUris.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(sending = true, error = null)
            val imageResult = uploadIfNeeded(contentResolver, imageUris)
            if (imageResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    sending = false,
                    error = userFacingError(imageResult.exceptionOrNull(), "上传图片失败"),
                    requireRelogin = shouldRelogin(imageResult.exceptionOrNull())
                )
                return@launch
            }
            val result = lakeRepository.sendComment(post.id, content, imageResult.getOrDefault(emptyList()))
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    sending = false,
                    error = userFacingError(result.exceptionOrNull(), "发表评论失败"),
                    requireRelogin = shouldRelogin(result.exceptionOrNull())
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(sending = false)
            openPost(post.id)
        }
    }

    fun createPost(
        title: String,
        content: String,
        campus: Int = 0,
        masked: Boolean = false,
        tagIdOverride: Int? = null,
        imageUris: List<Uri> = emptyList(),
        contentResolver: ContentResolver? = null,
        voteOptions: List<String>? = null,
        voteMaxSelection: Int = 1,
    ) {
        val normalizedTitle = title.trim()
        val normalizedContent = content.trim()
        val normalizedVoteOptions = voteOptions?.map { it.trim() }?.filter { it.isNotEmpty() }
        val isVotePost = !normalizedVoteOptions.isNullOrEmpty()
        if (normalizedTitle.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "标题不能为空")
            return
        }
        if (!isVotePost && normalizedContent.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "正文不能为空")
            return
        }
        if (normalizedTitle.length > 50) {
            _uiState.value = _uiState.value.copy(error = "标题最多 50 字")
            return
        }
        if (_uiState.value.selectedType == 1 && _uiState.value.selectedDepartmentId == null) {
            _uiState.value = _uiState.value.copy(error = "校务板块请先选择部门")
            return
        }
        if (normalizedContent.length > 2000) {
            _uiState.value = _uiState.value.copy(error = "正文最多 2000 字")
            return
        }
        if (isVotePost) {
            val options = normalizedVoteOptions.orEmpty()
            if (options.size < 2) {
                _uiState.value = _uiState.value.copy(error = "投票至少需要 2 个选项")
                return
            }
            if (options.size > 8) {
                _uiState.value = _uiState.value.copy(error = "投票最多支持 8 个选项")
                return
            }
            if (options.toSet().size != options.size) {
                _uiState.value = _uiState.value.copy(error = "投票选项不能重复")
                return
            }
            if (voteMaxSelection < 1 || voteMaxSelection > options.size) {
                _uiState.value = _uiState.value.copy(error = "最大可选数量不合法")
                return
            }
        }
        viewModelScope.launch {
            val state = _uiState.value
            val effectiveTagId = tagIdOverride ?: state.selectedTagId
            _uiState.value = state.copy(creatingPost = true, error = null)
            if (isVotePost) {
                val result = lakeRepository.createVotePost(
                    type = state.selectedType,
                    title = normalizedTitle,
                    tagId = effectiveTagId,
                    campus = campus,
                    maxSelection = voteMaxSelection.coerceAtLeast(1),
                    options = normalizedVoteOptions.orEmpty(),
                )
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        creatingPost = false,
                        error = userFacingError(result.exceptionOrNull(), "发帖失败"),
                        requireRelogin = shouldRelogin(result.exceptionOrNull()),
                    )
                    return@launch
                }
                _uiState.value = _uiState.value.copy(creatingPost = false)
                refresh()
                return@launch
            }
            val imageResult = uploadIfNeeded(contentResolver, imageUris)
            if (imageResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    creatingPost = false,
                    error = userFacingError(imageResult.exceptionOrNull(), "上传图片失败"),
                    requireRelogin = shouldRelogin(imageResult.exceptionOrNull()),
                )
                return@launch
            }
            val result = lakeRepository.createPost(
                type = state.selectedType,
                title = normalizedTitle,
                content = normalizedContent,
                tagId = effectiveTagId,
                departmentId = state.selectedDepartmentId,
                campus = campus,
                masked = if (masked) "1" else "",
                images = imageResult.getOrDefault(emptyList()),
            )
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    creatingPost = false,
                    error = userFacingError(result.exceptionOrNull(), "发帖失败"),
                    requireRelogin = shouldRelogin(result.exceptionOrNull()),
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(creatingPost = false)
            refresh()
        }
    }

    fun sendReply(floorId: Long, content: String, imageUris: List<Uri> = emptyList(), contentResolver: ContentResolver? = null) {
        if (content.isBlank() && imageUris.isEmpty()) return
        val post = _uiState.value.selectedPost ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(sending = true, error = null)
            val imageResult = uploadIfNeeded(contentResolver, imageUris)
            if (imageResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    sending = false,
                    error = userFacingError(imageResult.exceptionOrNull(), "上传图片失败"),
                    requireRelogin = shouldRelogin(imageResult.exceptionOrNull())
                )
                return@launch
            }
            val result = lakeRepository.replyComment(floorId, content, imageResult.getOrDefault(emptyList()))
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    sending = false,
                    error = userFacingError(result.exceptionOrNull(), "回复失败"),
                    requireRelogin = shouldRelogin(result.exceptionOrNull())
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(sending = false)
            openPost(post.id)
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun consumeRelogin() {
        _uiState.value = _uiState.value.copy(requireRelogin = false)
    }

    private suspend fun uploadIfNeeded(contentResolver: ContentResolver?, imageUris: List<Uri>): Result<List<String>> {
        if (imageUris.isEmpty()) return Result.success(emptyList())
        if (contentResolver == null) return Result.failure(LakeApiException(500, "图片选择器不可用"))
        return lakeRepository.uploadImages(contentResolver, imageUris)
    }

    private suspend fun preloadVisibleTabCaches(tabs: List<LakeTabUi>) {
        val state = _uiState.value
        if (state.keyword.isNotBlank() || state.selectedTagId != null) return
        val pendingSnapshots = mutableMapOf<String, LakeFeedSnapshot>()
        tabs.take(6).forEach { tab ->
            val key = feedCacheKey(type = tab.id, tagId = null, keyword = "", sortMode = state.sortMode)
            if (_uiState.value.feedCaches.containsKey(key) || pendingSnapshots.containsKey(key)) return@forEach
            val result = lakeRepository.feed(page = 1, type = tab.id, tagId = null, keyword = "", sortMode = state.sortMode)
            val posts = result.getOrDefault(emptyList())
            if (result.isSuccess) {
                pendingSnapshots[key] = LakeFeedSnapshot(
                    posts = posts,
                    page = 1,
                    hasMore = posts.size >= pageSize,
                )
            }
        }
        if (pendingSnapshots.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(feedCaches = _uiState.value.feedCaches + pendingSnapshots)
        }
    }

    private fun LakeUiState.feedCacheKey(): String = feedCacheKey(
        type = selectedType,
        tagId = selectedTagId,
        keyword = keyword,
        sortMode = sortMode,
    )

    private fun feedCacheKey(type: Int, tagId: Int?, keyword: String, sortMode: Int): String {
        return "$type:${tagId ?: "all"}:$sortMode:${keyword.trim()}"
    }

    private fun updatePost(postId: Long, transform: (LakePostUi) -> LakePostUi) {
        val state = _uiState.value
        _uiState.value = state.copy(
            posts = state.posts.map { if (it.id == postId) transform(it) else it },
            searchPosts = state.searchPosts.map { if (it.id == postId) transform(it) else it },
            favoritePosts = state.favoritePosts.map { if (it.id == postId) transform(it) else it },
            myPosts = state.myPosts.map { if (it.id == postId) transform(it) else it },
            historyPosts = state.historyPosts.map { if (it.id == postId) transform(it) else it },
            feedCaches = state.feedCaches.mapValues { (_, snapshot) ->
                snapshot.copy(posts = snapshot.posts.map { if (it.id == postId) transform(it) else it })
            },
            selectedPost = state.selectedPost?.let { if (it.id == postId) transform(it) else it },
        )
    }

    private fun updateFloor(floorId: Long, transform: (LakeFloorUi) -> LakeFloorUi) {
        _uiState.value = _uiState.value.copy(
            floors = _uiState.value.floors.map { it.updateNestedFloor(floorId, transform) },
        )
    }

    private fun LakeFloorUi.updateNestedFloor(floorId: Long, transform: (LakeFloorUi) -> LakeFloorUi): LakeFloorUi {
        val updated = if (id == floorId) transform(this) else this
        return updated.copy(subFloors = updated.subFloors.map { it.updateNestedFloor(floorId, transform) })
    }

    private fun findFloor(floors: List<LakeFloorUi>, floorId: Long): LakeFloorUi? {
        floors.forEach { floor ->
            if (floor.id == floorId) return floor
            findFloor(floor.subFloors, floorId)?.let { return it }
        }
        return null
    }

    private fun List<LakePostUi>.findPostUserAvatar(uid: Long?): String {
        if (uid == null) return ""
        return firstOrNull { it.uid == uid && it.avatar.isNotBlank() }?.avatar.orEmpty()
    }

    private fun List<LakePostUi>.findPostUserName(uid: Long?): String {
        if (uid == null) return ""
        return firstOrNull { it.uid == uid && it.author.isNotBlank() }?.author.orEmpty()
    }

    private fun List<LakeFloorUi>.findFloorUserAvatar(uid: Long?): String {
        if (uid == null) return ""
        forEach { floor ->
            if (floor.uid == uid && floor.avatar.isNotBlank()) return floor.avatar
            floor.subFloors.findFloorUserAvatar(uid).ifBlank { null }?.let { return it }
        }
        return ""
    }

    private fun List<LakeFloorUi>.findFloorUserName(uid: Long?): String {
        if (uid == null) return ""
        forEach { floor ->
            if (floor.uid == uid && floor.author.isNotBlank()) return floor.author
            floor.subFloors.findFloorUserName(uid).ifBlank { null }?.let { return it }
        }
        return ""
    }

    fun reportPost(postId: Long, reason: String) {
        report(postId = postId, reason = reason.ifBlank { "用户举报" })
    }

    fun reportFloor(postId: Long, floorId: Long, reason: String) {
        report(postId = postId, floorId = floorId, reason = reason.ifBlank { "用户举报" })
    }

    private fun report(postId: Long, floorId: Long = 0, reason: String) {
        viewModelScope.launch {
            val result = lakeRepository.report(postId = postId, floorId = floorId, reason = reason)
            _uiState.value = _uiState.value.copy(
                snackbar = if (result.isSuccess) "举报已提交" else userFacingError(result.exceptionOrNull(), "举报失败"),
            )
        }
    }

    fun deletePost(postId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = lakeRepository.deletePost(postId)
            if (result.isSuccess) {
                removePostFromLocalState(postId)
                onSuccess()
                refresh()
                loadMyPosts(forceRefresh = true)
                loadFavoritePosts(forceRefresh = true)
                loadHistoryPosts(forceRefresh = true)
                return@launch
            }
            _uiState.value = _uiState.value.copy(
                snackbar = userFacingError(result.exceptionOrNull(), "删除失败"),
            )
        }
    }

    fun deleteFloor(floorId: Long, postId: Long) {
        viewModelScope.launch {
            val result = lakeRepository.deleteFloor(floorId)
            if (result.isSuccess) {
                removeFloorFromLocalState(floorId)
                _uiState.value = _uiState.value.copy(snackbar = "已删除")
                refreshSelectedPost()
                return@launch
            }
            _uiState.value = _uiState.value.copy(
                snackbar = userFacingError(result.exceptionOrNull(), "删除失败"),
            )
        }
    }

    private fun removePostFromLocalState(postId: Long) {
        val state = _uiState.value
        _uiState.value = state.copy(
            posts = state.posts.filterNot { it.id == postId },
            searchPosts = state.searchPosts.filterNot { it.id == postId },
            favoritePosts = state.favoritePosts.filterNot { it.id == postId },
            myPosts = state.myPosts.filterNot { it.id == postId },
            historyPosts = state.historyPosts.filterNot { it.id == postId },
            feedCaches = state.feedCaches.mapValues { (_, snapshot) ->
                snapshot.copy(posts = snapshot.posts.filterNot { it.id == postId })
            },
            selectedPost = state.selectedPost?.takeUnless { it.id == postId },
            floors = if (state.selectedPost?.id == postId) emptyList() else state.floors,
            officialFloors = if (state.selectedPost?.id == postId) emptyList() else state.officialFloors,
            snackbar = "已删除",
        )
    }

    private fun removeFloorFromLocalState(floorId: Long) {
        val state = _uiState.value
        _uiState.value = state.copy(
            floors = state.floors.removeFloorById(floorId),
            officialFloors = state.officialFloors.removeFloorById(floorId),
        )
    }

    private fun List<LakeFloorUi>.removeFloorById(floorId: Long): List<LakeFloorUi> {
        return mapNotNull { floor ->
            if (floor.id == floorId) {
                null
            } else {
                floor.copy(subFloors = floor.subFloors.removeFloorById(floorId))
            }
        }
    }

    fun sharePost(postId: Long) {
        val mpCode = "#MP${postId.toString().padStart(6, '0')}"
        _uiState.value = _uiState.value.copy(shareText = "分享帖子: $mpCode\nhttps://qnhd.twt.edu.cn/post/$postId")
    }

    fun fetchClipboardPreview(postId: Long) {
        viewModelScope.launch {
            val result = lakeRepository.postDetail(postId)
            _uiState.value = _uiState.value.copy(clipboardPreviewPost = result.getOrNull())
        }
    }

    fun clearClipboardPreview() {
        _uiState.value = _uiState.value.copy(clipboardPreviewPost = null)
    }

    fun clearShareText() {
        _uiState.value = _uiState.value.copy(shareText = null)
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbar = null)
    }

    private fun shouldRelogin(vararg errors: Throwable?): Boolean {
        val e = errors.firstOrNull { it != null } ?: return false
        return (e is LakeApiException && (e.code == 401 || e.code == 403)) ||
            (e.message?.lowercase()?.contains("token") == true)
    }

    private fun userFacingError(error: Throwable?, fallback: String): String {
        if (error == null) return fallback
        return when (error) {
            is LakeApiException -> error.message
            else -> "网络异常，请稍后重试"
        }
    }
}

private fun String.isStudentNumber(): Boolean {
    val value = trim()
    return value.matches(Regex("""\d{8,12}""")) &&
        !value.matches(Regex("""1[3-9]\d{9}"""))
}

