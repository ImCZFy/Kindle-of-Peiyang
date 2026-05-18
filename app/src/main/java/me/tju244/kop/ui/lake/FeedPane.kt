package me.tju244.kop.ui.lake

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.tju244.kop.lake.ui.LakeUiState
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import me.tju244.kop.ui.miuixScroll
import androidx.compose.foundation.lazy.LazyListState

@Composable
internal fun FeedPane(ui: LakeUiState, onSelectType: (Int) -> Unit, onSelectTag: (Int?) -> Unit, onOpenPost: (Long) -> Unit, onOpenImages: (List<String>, Int) -> Unit, onLikePost: (Long) -> Unit, onRefresh: () -> Unit, onLoadMore: () -> Unit) {
    val selectedTabIndex = ui.tabs.indexOfFirst { it.id == ui.selectedType }.coerceAtLeast(0)
    val tabListState = rememberLazyListState()
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { ui.tabs.size.coerceAtLeast(1) },
    )
    LaunchedEffect(selectedTabIndex, ui.tabs.size) {
        if (ui.tabs.isNotEmpty() && !pagerState.isScrollInProgress && pagerState.currentPage != selectedTabIndex) {
            pagerState.scrollToPage(selectedTabIndex)
        }
    }
    val tabHighlightIndex = remember(ui.tabs.size, selectedTabIndex, pagerState.currentPage, pagerState.isScrollInProgress) {
        if (ui.tabs.isEmpty()) 0 else if (pagerState.isScrollInProgress) {
            pagerState.currentPage.coerceIn(0, ui.tabs.lastIndex)
        } else {
            selectedTabIndex.coerceIn(0, ui.tabs.lastIndex)
        }
    }
    LaunchedEffect(pagerState, ui.tabs, ui.selectedType) {
        snapshotFlow { pagerState.currentPage to pagerState.isScrollInProgress }
            .collect { (page, scrolling) ->
                val tabId = ui.tabs.getOrNull(page)?.id ?: return@collect
                if (!scrolling && tabId != ui.selectedType) {
                    onSelectType(tabId)
                }
            }
    }
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (ui.tabs.isNotEmpty()) {
            TabRow(
                tabs = ui.tabs.map { it.name },
                selectedTabIndex = tabHighlightIndex,
                onTabSelected = { index ->
                    ui.tabs.getOrNull(index)?.let { onSelectType(it.id) }
                },
                listState = tabListState,
                modifier = Modifier.fillMaxWidth(),
                minWidth = 88.dp,
                maxWidth = 132.dp,
                itemSpacing = 6.dp,
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ui.tags, key = { it.id }) { tag -> FilterChip("#${tag.name}", ui.selectedTagId == tag.id) { onSelectTag(tag.id) } }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->
            val pageType = ui.tabs.getOrNull(page)?.id ?: ui.selectedType
            val pageListState = rememberSaveable(pageType, saver = LazyListState.Saver) { LazyListState() }
            val pagePullToRefreshState = rememberPullToRefreshState()
            val pagePosts = if (pageType == ui.selectedType) {
                ui.posts
            } else {
                ui.feedCaches[lakeFeedCacheKey(pageType, ui.selectedTagId, ui.keyword, ui.sortMode)]?.posts.orEmpty()
            }
            val pageIsCurrent = pageType == ui.selectedType
            val pageLoading = ui.loading && pageIsCurrent
            val pageHasMore = if (pageIsCurrent) ui.hasMore else ui.feedCaches[lakeFeedCacheKey(pageType, ui.selectedTagId, ui.keyword, ui.sortMode)]?.hasMore == true
            val deferImages by remember(pageListState, pageIsCurrent) {
                derivedStateOf { pageIsCurrent && pageListState.isScrollInProgress }
            }
            val latestPostsSize by rememberUpdatedState(pagePosts.size)
            val latestHasMore by rememberUpdatedState(pageHasMore)
            val latestLoading by rememberUpdatedState(ui.loading)
            val latestLoadingMore by rememberUpdatedState(ui.loadingMore)
            LaunchedEffect(pageType, pageIsCurrent) {
                if (!pageIsCurrent) return@LaunchedEffect
                snapshotFlow {
                    pageListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                }.collect { lastVisibleIndex ->
                    val loadMoreIndex = (latestPostsSize - 3).coerceAtLeast(0)
                    if (lastVisibleIndex >= loadMoreIndex && latestHasMore && !latestLoading && !latestLoadingMore) {
                        onLoadMore()
                    }
                }
            }
            PullToRefresh(
                isRefreshing = pageLoading && pagePosts.isNotEmpty(),
                onRefresh = { if (pageIsCurrent) onRefresh() },
                modifier = Modifier.fillMaxSize(),
                pullToRefreshState = pagePullToRefreshState,
                contentPadding = PaddingValues(),
            ) {
                CompositionLocalProvider(LocalDeferImageLoading provides deferImages) {
                    if (pageLoading && pagePosts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("加载中...", color = MiuixTheme.colorScheme.onSurfaceVariantSummary, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            state = pageListState,
                            modifier = Modifier.fillMaxSize().miuixScroll(),
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            overscrollEffect = null,
                        ) {
                            items(pagePosts, key = { it.id }, contentType = { "post-card" }) { post ->
                                PostSummaryCard(post = post, onClick = { onOpenPost(post.id) }, onOpenImages = onOpenImages, onLike = { onLikePost(post.id) })
                            }
                            item(contentType = "feed-footer") {
                                Text(
                                    if (pageIsCurrent && ui.loadingMore) "加载中..." else if (pageHasMore) "继续下滑加载更多" else "已经到底了",
                                    color = LakeColors.muted,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

