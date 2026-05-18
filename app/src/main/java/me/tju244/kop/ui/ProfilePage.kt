package me.tju244.kop.ui

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.tju244.kop.R
import me.tju244.kop.RebuildApplication
import me.tju244.kop.lake.network.LakeUserProfileUi
import me.tju244.kop.lake.network.LakePostUi
import me.tju244.kop.lake.ui.LakeViewModel
import me.tju244.kop.lake.ui.LakeViewModelFactory
import me.tju244.kop.ui.effect.BgEffectBackground
import me.tju244.kop.ui.theme.LocalAppDarkMode
import kotlinx.coroutines.CancellationException
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Community
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Create
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.Promotions
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Hide
import top.yukonga.miuix.kmp.icon.extended.Show
import me.tju244.kop.ui.lake.*
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
@Composable
fun ProfilePage(
    onOpenSettings: () -> Unit,
    onOpenProfileSettings: () -> Unit,
    onOpenPost: (Long) -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as RebuildApplication
    val vm: LakeViewModel = viewModel(factory = LakeViewModelFactory(app))
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val profile = ui.currentUserProfile
    LaunchedEffect(Unit) {
        vm.loadCurrentUserProfile()
        vm.loadMyPosts(forceRefresh = true)
        vm.loadFavoritePosts(forceRefresh = true)
    }
    val listState = rememberLazyListState()
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = "我的",
                largeTitle = "我的",
                color = if (LocalAppDarkMode.current) Color.Transparent else MiuixTheme.colorScheme.surface,
                titleColor = MiuixTheme.colorScheme.onBackground,
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = MiuixIcons.Settings,
                            contentDescription = "设置",
                            tint = MiuixTheme.colorScheme.onBackground,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.background)
                .miuixScroll()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            overscrollEffect = null,
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = onOpenProfileSettings,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MiuixTheme.colorScheme.secondaryVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            AvatarImage(
                                avatar = profile?.avatar.orEmpty(),
                                author = profile?.nickname.orEmpty(),
                                size = 64.dp,
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    profile?.nickname?.ifBlank { "北洋之炬用户" } ?: "北洋之炬用户",
                                    color = MiuixTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                                LevelBadge(level = profile?.level ?: 0)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                UidChip(uid = profile?.uid)
                                if (profile?.username?.isNotBlank() == true) {
                                    StudentIdChip(username = profile.username)
                                }
                            }
                            ProfileExperienceProgress(profile = profile)
                        }
                    }
                }
            }
            item {
                ProfileForumTabs(
                    ui = ui,
                    onRefreshMyPosts = { vm.loadMyPosts(forceRefresh = true) },
                    onLoadMoreMyPosts = vm::loadMoreMyPosts,
                    onRefreshFavorites = { vm.loadFavoritePosts(forceRefresh = true) },
                    onLoadMoreFavorites = vm::loadMoreFavoritePosts,
                    onOpenPost = onOpenPost,
                )
            }
        }
    }
}

@Composable
private fun UidChip(uid: Long?) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MiuixTheme.colorScheme.secondaryVariant)
            .padding(horizontal = 7.dp, vertical = 2.dp),
    ) {
        Text(
            uid?.let { "UID: $it" } ?: "UID: --",
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            fontSize = 11.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun StudentIdChip(username: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 7.dp, vertical = 2.dp),
    ) {
        Text(username, color = MiuixTheme.colorScheme.primary, fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProfileExperienceProgress(profile: LakeUserProfileUi?) {
    val next = profile?.nextLevelPoint ?: 0
    val current = profile?.levelPoint ?: 0
    val levelStart = profile?.curLevelPoint ?: 0
    val fraction = if (next > levelStart) {
        ((current - levelStart).toFloat() / (next - levelStart).toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("经验值", color = MiuixTheme.colorScheme.onSurfaceVariantSummary, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text(
                if (next > 0) "$current/$next" else profile?.levelPoint?.let { "$it" } ?: "同步中",
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                fontSize = 12.sp,
            )
        }
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(MiuixTheme.colorScheme.secondaryVariant),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(6.dp)
                    .background(androidx.compose.ui.graphics.Brush.linearGradient(levelGradient(profile?.level ?: 0))),
            )
        }
    }
}

@Composable
private fun ProfileForumTabs(
    ui: me.tju244.kop.lake.ui.LakeUiState,
    onRefreshMyPosts: () -> Unit,
    onLoadMoreMyPosts: () -> Unit,
    onRefreshFavorites: () -> Unit,
    onLoadMoreFavorites: () -> Unit,
    onOpenPost: (Long) -> Unit,
) {
    val tabs = remember { listOf("我的帖子", "我的收藏") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var myPostSort by remember { mutableIntStateOf(0) } // 0 默认 1 时间
    var favoriteSort by remember { mutableIntStateOf(0) } // 0 默认 1 时间
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val myPostsListState = rememberLazyListState()
    val favoritesListState = rememberLazyListState()
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) pagerState.animateScrollToPage(selectedTab)
        if (selectedTab == 0) onRefreshMyPosts()
        if (selectedTab == 1) onRefreshFavorites()
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { selectedTab = it }
    }
    LaunchedEffect(myPostsListState, selectedTab, ui.myPosts.size, ui.loadingMoreMyPosts, ui.myPostHasMore) {
        if (selectedTab != 0) return@LaunchedEffect
        snapshotFlow { myPostsListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .collect { lastVisibleIndex ->
                val totalItems = ui.myPosts.size + if (ui.loadingMoreMyPosts) 1 else 0
                if (totalItems <= 0) return@collect
                if (lastVisibleIndex >= totalItems - 2 && ui.myPostHasMore && !ui.loadingMoreMyPosts && !ui.loadingMyPosts) {
                    onLoadMoreMyPosts()
                }
            }
    }
    LaunchedEffect(favoritesListState, selectedTab, ui.favoritePosts.size, ui.loadingMoreFavorites, ui.favoriteHasMore) {
        if (selectedTab != 1) return@LaunchedEffect
        snapshotFlow { favoritesListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .collect { lastVisibleIndex ->
                val totalItems = ui.favoritePosts.size + if (ui.loadingMoreFavorites) 1 else 0
                if (totalItems <= 0) return@collect
                if (lastVisibleIndex >= totalItems - 2 && ui.favoriteHasMore && !ui.loadingMoreFavorites && !ui.loadingFavorites) {
                    onLoadMoreFavorites()
                }
            }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        TabRow(
            tabs = tabs,
            selectedTabIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.fillMaxWidth(),
            minWidth = 0.dp,
            maxWidth = 180.dp,
            itemSpacing = 6.dp,
        )
        Spacer(Modifier.height(16.dp))
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MiuixTheme.colorScheme.surfaceContainer)
                .height(420.dp),
        ) { page ->
            if (page == 0) {
                ProfilePostListPane(
                    title = "我的帖子",
                    posts = ui.myPosts,
                    loading = ui.loadingMyPosts,
                    loadingMore = ui.loadingMoreMyPosts,
                    sortMode = myPostSort,
                    onSortModeChange = { myPostSort = it },
                    listState = myPostsListState,
                    onOpenPost = onOpenPost,
                    emptyText = "还没有发过帖子",
                )
            } else {
                ProfilePostListPane(
                    title = "我的收藏",
                    posts = ui.favoritePosts,
                    loading = ui.loadingFavorites,
                    loadingMore = ui.loadingMoreFavorites,
                    sortMode = favoriteSort,
                    onSortModeChange = { favoriteSort = it },
                    listState = favoritesListState,
                    onOpenPost = onOpenPost,
                    emptyText = "还没有收藏帖子",
                )
            }
        }
    }
}

@Composable
private fun ProfilePostListPane(
    title: String,
    posts: List<LakePostUi>,
    loading: Boolean,
    loadingMore: Boolean,
    sortMode: Int,
    onSortModeChange: (Int) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onOpenPost: (Long) -> Unit,
    emptyText: String,
) {
    if (loading && posts.isEmpty()) {
        ProfileForumPlaceholder(title = title, subtitle = "正在加载列表...")
        return
    }
    if (posts.isEmpty()) {
        ProfileForumPlaceholder(title = title, subtitle = emptyText)
        return
    }
    val displayPosts = remember(posts, sortMode) {
        if (sortMode == 0) {
            posts
        } else {
            posts.sortedByDescending { it.createdAt.toComparableTime() }
        }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .miuixScroll()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        overscrollEffect = null,
    ) {
        item(key = "${title}_sort") {
            Card(modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp) {
                OverlayDropdownPreference(
                    title = "排序方式",
                    items = listOf("默认", "时间"),
                    selectedIndex = sortMode.coerceIn(0, 1),
                    onSelectedIndexChange = onSortModeChange,
                )
            }
        }
        items(displayPosts, key = { it.id }) { post ->
            Card(
                onClick = { onOpenPost(post.id) },
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 12.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AvatarImage(avatar = post.avatar, author = post.author, size = 34.dp)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            post.title.ifBlank { "无标题帖子" },
                            color = MiuixTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp,
                        )
                        Text(
                            "${post.createdAt.toCommunityDateShort()}  ·  评${post.comments}  赞${post.likes}",
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            fontSize = 12.sp,
                            maxLines = 1,
                        )
                    }
                    LevelBadge(level = post.level)
                }
            }
        }
        if (loadingMore) {
            item {
                Text(
                    "正在加载更多...",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ProfileForumPlaceholder(title: String, subtitle: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = MiuixTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}

private fun String.toCommunityDateShort(): String {
    val value = trim()
    if (value.isBlank()) return ""
    val sourcePatterns = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd'T'HH:mm:ss")
    val output = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
    sourcePatterns.forEach { pattern ->
        runCatching {
            val date = java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault()).parse(value)
            if (date != null) return output.format(date)
        }
    }
    return value.take(16)
}

private fun String.toComparableTime(): Long {
    val value = trim()
    if (value.isBlank()) return 0L
    val patterns = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd'T'HH:mm:ss")
    patterns.forEach { pattern ->
        runCatching {
            val date = java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault()).parse(value)
            if (date != null) return date.time
        }
    }
    return value.toLongOrNull() ?: 0L
}

