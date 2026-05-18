package me.tju244.kop.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as rememberKyantLayerBackdrop
import com.kyant.backdrop.drawBackdrop as kyantDrawBackdrop
import com.kyant.backdrop.effects.blur as kyantBlur
import com.kyant.backdrop.effects.lens as kyantLens
import com.kyant.backdrop.effects.vibrancy as kyantVibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow as KyantInnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.abs
import kotlin.math.sign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import me.tju244.kop.R
import me.tju244.kop.RebuildApplication
import me.tju244.kop.auth.ui.SessionViewModel
import me.tju244.kop.lake.network.LakeFloorUi
import me.tju244.kop.lake.network.LakeMessageCategory
import me.tju244.kop.lake.network.LakeMessageCountUi
import me.tju244.kop.lake.network.LakeMessageUi
import me.tju244.kop.lake.network.LakePostUi
import me.tju244.kop.lake.network.LakeVoteDetailUi
import me.tju244.kop.lake.network.LakeVoteOptionUi
import me.tju244.kop.lake.ui.LakeViewModel
import me.tju244.kop.lake.ui.LakeViewModelFactory
import me.tju244.kop.lake.ui.LakeUiState
import me.tju244.kop.ui.liquid.InnerShadow
import me.tju244.kop.ui.liquid.innerShadow
import me.tju244.kop.ui.liquid.lens
import me.tju244.kop.ui.liquid.vibrancy
import me.tju244.kop.ui.lake.*
import me.tju244.kop.ui.component.ExpandableText
import me.tju244.kop.ui.component.PostRichText
import me.tju244.kop.ui.theme.LocalAppDarkMode
import me.tju244.kop.ui.theme.RebuildTheme
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarDisplayMode
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Community
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Create
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.ExpandLess
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.icon.extended.Image as MiuixImageIcon
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.Messages
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.Pin
import top.yukonga.miuix.kmp.icon.extended.Promotions
import top.yukonga.miuix.kmp.icon.extended.Send
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Show
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowListPopup

private enum class MainTab {
    Home,
    Forum,
    Me,
}

private val MainTab.miuixIcon: ImageVector
    get() = when (this) {
        MainTab.Home -> MiuixIcons.Months
        MainTab.Forum -> MiuixIcons.Community
        MainTab.Me -> MiuixIcons.Contacts
    }

private val MainTab.label: String
    get() = when (this) {
        MainTab.Home -> "首页"
        MainTab.Forum -> "论坛"
        MainTab.Me -> "我的"
    }

private enum class LakePane {
    Feed,
    Search,
    SearchResult,
}

private enum class HomePane {
    Home,
    Courses,
    Gpa,
    Exams,
    EntryQr,
    StudyRoom,
}

private enum class MePane {
    Profile,
    ProfileSettings,
    Settings,
    NotificationSettings,
    TjuSettings,
    TjuDebug,
    About,
}

private sealed interface ReportTarget {
    data class Post(val postId: Long) : ReportTarget
    data class Floor(val postId: Long, val floorId: Long) : ReportTarget
}


@Composable
fun WePeiyangRebuildApp(sessionVm: SessionViewModel = viewModel()) {
    val state by sessionVm.uiState.collectAsStateWithLifecycle()
    var showLaunchSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(760)
        showLaunchSplash = false
    }

    RebuildTheme(themeMode = state.themeMode, fontMode = state.fontMode) {
        val appGate = when {
            showLaunchSplash || !state.initialized -> 0
            !state.oobeCompleted -> 1
            state.token.isBlank() -> 2
            else -> 3
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = appGate,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    if (initialState == 1 && targetState > 1) {
                        (slideInHorizontally(tween(360, easing = FastOutSlowInEasing)) { it / 5 } + fadeIn(tween(260))) togetherWith
                            (slideOutHorizontally(tween(360, easing = FastOutSlowInEasing)) { -it / 3 } + fadeOut(tween(220)) + scaleOut(targetScale = 0.985f, animationSpec = tween(320, easing = FastOutSlowInEasing)))
                    } else {
                        (fadeIn(tween(220)) + scaleIn(initialScale = 0.985f, animationSpec = tween(260, easing = FastOutSlowInEasing))) togetherWith
                            (fadeOut(tween(160)) + scaleOut(targetScale = 0.985f, animationSpec = tween(180, easing = FastOutSlowInEasing)))
                    }
                },
                label = "app-gate-transition",
            ) { gate ->
                when (gate) {
                    0 -> SplashGate()
                    1 -> OobePage(
                        sessionState = state,
                        onSidChange = sessionVm::onSidChanged,
                        onPasswordChange = sessionVm::onPasswordChanged,
                        onLogin = sessionVm::login,
                        themeMode = state.themeMode,
                        onThemeModeChange = sessionVm::setThemeMode,
                        fontMode = state.fontMode,
                        onFontModeChange = sessionVm::setFontMode,
                        bottomBarLabelMode = state.bottomBarLabelMode,
                        onBottomBarLabelModeChange = sessionVm::setBottomBarLabelMode,
                        navigationBarMode = state.navigationBarMode,
                        onNavigationBarModeChange = sessionVm::setNavigationBarMode,
                        bottomBarGlassEnabled = state.bottomBarGlassEnabled,
                        onBottomBarGlassEnabledChange = sessionVm::setBottomBarGlassEnabled,
                        forumNotificationEnabled = state.forumNotificationEnabled,
                        onForumNotificationEnabledChange = sessionVm::setForumNotificationEnabled,
                        courseNotificationEnabled = state.courseNotificationEnabled,
                        onCourseNotificationEnabledChange = sessionVm::setCourseNotificationEnabled,
                        courseLiveUpdateEnabled = state.courseLiveUpdateEnabled,
                        onCourseLiveUpdateEnabledChange = sessionVm::setCourseLiveUpdateEnabled,
                        miIslandNotificationEnabled = state.miIslandNotificationEnabled,
                        onMiIslandNotificationEnabledChange = sessionVm::setMiIslandNotificationEnabled,
                        miIslandBypassEnabled = state.miIslandBypassEnabled,
                        onMiIslandBypassEnabledChange = sessionVm::setMiIslandBypassEnabled,
                        miIslandAuthMode = state.miIslandAuthMode,
                        onMiIslandAuthModeChange = sessionVm::setMiIslandAuthMode,
                        miIslandDisplayMode = state.miIslandDisplayMode,
                        onMiIslandDisplayModeChange = sessionVm::setMiIslandDisplayMode,
                        onFinish = sessionVm::completeOobe,
                    )
                    2 -> LoginGate(
                        sid = state.sidOrPhone,
                        password = state.password,
                        loading = state.loading,
                        error = state.error,
                        onSidChange = sessionVm::onSidChanged,
                        onPasswordChange = sessionVm::onPasswordChanged,
                        onLogin = sessionVm::login,
                        onRequestLoginCode = sessionVm::requestLoginCode,
                        onLoginByCode = sessionVm::loginByCode,
                        onRequestResetCode = sessionVm::requestResetCode,
                        onResetPassword = sessionVm::resetPasswordByPhone,
                    )
                    else -> MainShell(
                        onLogout = sessionVm::logout,
                        themeMode = state.themeMode,
                        onThemeModeChange = sessionVm::setThemeMode,
                        fontMode = state.fontMode,
                        onFontModeChange = sessionVm::setFontMode,
                        bottomBarLabelMode = state.bottomBarLabelMode,
                        onBottomBarLabelModeChange = sessionVm::setBottomBarLabelMode,
                        navigationBarMode = state.navigationBarMode,
                        onNavigationBarModeChange = sessionVm::setNavigationBarMode,
                        tabletRailPosition = state.tabletRailPosition,
                        onTabletRailPositionChange = sessionVm::setTabletRailPosition,
                        bottomBarGlassEnabled = state.bottomBarGlassEnabled,
                        onBottomBarGlassEnabledChange = sessionVm::setBottomBarGlassEnabled,
                        forumNotificationEnabled = state.forumNotificationEnabled,
                        onForumNotificationEnabledChange = sessionVm::setForumNotificationEnabled,
                        courseNotificationEnabled = state.courseNotificationEnabled,
                        onCourseNotificationEnabledChange = sessionVm::setCourseNotificationEnabled,
                        courseLiveUpdateEnabled = state.courseLiveUpdateEnabled,
                        onCourseLiveUpdateEnabledChange = sessionVm::setCourseLiveUpdateEnabled,
                        miIslandNotificationEnabled = state.miIslandNotificationEnabled,
                        onMiIslandNotificationEnabledChange = sessionVm::setMiIslandNotificationEnabled,
                        miIslandBypassEnabled = state.miIslandBypassEnabled,
                        onMiIslandBypassEnabledChange = sessionVm::setMiIslandBypassEnabled,
                        miIslandAuthMode = state.miIslandAuthMode,
                        onMiIslandAuthModeChange = sessionVm::setMiIslandAuthMode,
                        miIslandDisplayMode = state.miIslandDisplayMode,
                        onMiIslandDisplayModeChange = sessionVm::setMiIslandDisplayMode,
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashGate() {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val alpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 320),
        label = "splash-alpha",
    )
    val scale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.94f,
        animationSpec = tween(durationMillis = 320),
        label = "splash-scale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            },
        ) {
            Image(
                painter = painterResource(R.drawable.app_icon),
                contentDescription = "北洋之炬",
                modifier = Modifier.size(72.dp),
            )
            Text("北洋之炬", fontWeight = FontWeight.Bold)
            Text("学在北洋 | 一手掌握", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
    }
}

@Composable
private fun MainShell(
    onLogout: () -> Unit,
    themeMode: Int,
    onThemeModeChange: (Int) -> Unit,
    fontMode: Int,
    onFontModeChange: (Int) -> Unit,
    bottomBarLabelMode: Int,
    onBottomBarLabelModeChange: (Int) -> Unit,
    navigationBarMode: Int,
    onNavigationBarModeChange: (Int) -> Unit,
    tabletRailPosition: Int,
    onTabletRailPositionChange: (Int) -> Unit,
    bottomBarGlassEnabled: Boolean,
    onBottomBarGlassEnabledChange: (Boolean) -> Unit,
    forumNotificationEnabled: Boolean,
    onForumNotificationEnabledChange: (Boolean) -> Unit,
    courseNotificationEnabled: Boolean,
    onCourseNotificationEnabledChange: (Boolean) -> Unit,
    courseLiveUpdateEnabled: Boolean,
    onCourseLiveUpdateEnabledChange: (Boolean) -> Unit,
    miIslandNotificationEnabled: Boolean,
    onMiIslandNotificationEnabledChange: (Boolean) -> Unit,
    miIslandBypassEnabled: Boolean,
    onMiIslandBypassEnabledChange: (Boolean) -> Unit,
    miIslandAuthMode: Int,
    onMiIslandAuthModeChange: (Int) -> Unit,
    miIslandDisplayMode: Int,
    onMiIslandDisplayModeChange: (Int) -> Unit,
) {
    val effectiveNavigationBarMode = navigationBarMode.coerceIn(0, 1)
    var currentTab by remember { mutableIntStateOf(MainTab.Home.ordinal) }
    var hideBottomBar by remember { mutableStateOf(false) }
    var homePane by remember { mutableStateOf(HomePane.Home) }
    var mePane by remember { mutableStateOf(MePane.Profile) }
    var forumComposeRequest by remember { mutableIntStateOf(0) }
    var routedPostId by remember { mutableStateOf<Long?>(null) }
    var shellBackGestureProgress by remember { mutableStateOf(0f) }
    val hasMainBackStack =
        (currentTab == MainTab.Home.ordinal && homePane != HomePane.Home) ||
            (currentTab == MainTab.Me.ordinal && mePane != MePane.Profile)
    val performMainBack: () -> Unit = {
        if (currentTab == MainTab.Home.ordinal && homePane != HomePane.Home) {
            homePane = HomePane.Home
        } else {
            mePane = when (mePane) {
                MePane.About -> MePane.Settings
                MePane.NotificationSettings -> MePane.Settings
                MePane.TjuDebug -> MePane.TjuSettings
                MePane.TjuSettings -> MePane.Settings
                MePane.Settings, MePane.ProfileSettings -> MePane.Profile
                else -> MePane.Profile
            }
        }
    }

    PredictiveBackHandler(enabled = hasMainBackStack) { progress ->
        try {
            progress.collect { backEvent ->
                shellBackGestureProgress = backEvent.progress.coerceIn(0f, 1f)
            }
            performMainBack()
        } catch (e: CancellationException) {
            throw e
        } finally {
            shellBackGestureProgress = 0f
        }
    }
    BackHandler(enabled = hasMainBackStack) { performMainBack() }
    val selected = MainTab.entries[currentTab]
    val backdrop = rememberLayerBackdrop()
    val floatingBackdrop = rememberKyantLayerBackdrop()
    val shellBackShiftPx = with(LocalDensity.current) { 38.dp.toPx() }

    LaunchedEffect(selected) {
        if (selected != MainTab.Forum) hideBottomBar = false
        if (selected != MainTab.Home) homePane = HomePane.Home
        if (selected != MainTab.Me) mePane = MePane.Profile
    }

    LaunchedEffect(Unit) {
        fun openCourses() {
            currentTab = MainTab.Home.ordinal
            homePane = HomePane.Courses
            hideBottomBar = false
            routedPostId = null
        }
        if (AppDeepLinks.consumePending() == AppDeepLinks.ACTION_OPEN_COURSES) {
            openCourses()
        }
        AppDeepLinks.events.collect { action ->
            if (action == AppDeepLinks.ACTION_OPEN_COURSES) {
                AppDeepLinks.consumePending()
                openCourses()
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val tabletLandscape = maxWidth >= 840.dp && maxWidth > maxHeight
        val showTabletRail = tabletLandscape && !hideBottomBar
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MiuixTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0.dp),
            bottomBar = {
                if (!showTabletRail && !hideBottomBar && routedPostId == null && effectiveNavigationBarMode == 0) {
                    FixedBottomNavigationBar(
                        selected = selected,
                        onSelect = { currentTab = it.ordinal },
                        onLongSelect = {},
                        backdrop = backdrop,
                        showLabels = bottomBarLabelMode == 1,
                        liquidGlassEnabled = bottomBarGlassEnabled,
                    )
                }
            },
            floatingToolbar = {
                if (!showTabletRail && !hideBottomBar && routedPostId == null && effectiveNavigationBarMode == 1) {
                    FloatingBottomNavigationBar(
                        selected = selected,
                        onSelect = { currentTab = it.ordinal },
                        onLongSelect = {},
                        backdrop = floatingBackdrop,
                        showLabels = bottomBarLabelMode == 1,
                        liquidGlassEnabled = bottomBarGlassEnabled,
                    )
                }
            },
        ) { innerPadding ->
            Row(modifier = Modifier.fillMaxSize()) {
                if (showTabletRail && tabletRailPosition == 0) {
                    TabletNavigationRail(
                        selected = selected,
                        onSelect = { currentTab = it.ordinal },
                        showLabels = bottomBarLabelMode == 1,
                        liquidGlassEnabled = bottomBarGlassEnabled,
                        backdrop = floatingBackdrop,
                        alignEnd = false,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(bottom = if (!showTabletRail && effectiveNavigationBarMode == 0) innerPadding.calculateBottomPadding() else 0.dp)
                        .graphicsLayer {
                            if (hasMainBackStack) {
                                val eased = shellBackGestureProgress * shellBackGestureProgress
                                translationX = shellBackShiftPx * eased
                                val scale = 1f - 0.015f * eased
                                scaleX = scale
                                scaleY = scale
                                alpha = 1f - 0.08f * eased
                            }
                        }
                        .layerBackdrop(backdrop)
                        .then(
                            if (!showTabletRail && effectiveNavigationBarMode == 1 && bottomBarGlassEnabled) {
                                Modifier.kyantLayerBackdrop(floatingBackdrop)
                            } else {
                                Modifier
                            },
                        )
                ) {
            AnimatedContent(
                targetState = selected,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    (slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it * dir } + fadeIn(tween(220))) togetherWith
                        (slideOutHorizontally(tween(280, easing = FastOutSlowInEasing)) { -it * dir } + fadeOut(tween(170)))
                },
                label = "main-tab-transition",
            ) { tab ->
                when (tab) {
                    MainTab.Home -> {
                        AnimatedContent(
                            targetState = homePane,
                            transitionSpec = {
                                val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                                (slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { it * dir } + fadeIn(tween(200))) togetherWith
                                    (slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { -it * dir } + fadeOut(tween(150)))
                            },
                            label = "home-pane-transition",
                        ) { pane ->
                            when (pane) {
                                HomePane.Home -> HomePage(
                                    onOpenCourses = { homePane = HomePane.Courses },
                                    onOpenGpa = { homePane = HomePane.Gpa },
                                    onOpenExams = { homePane = HomePane.Exams },
                                    onOpenEntryQr = { homePane = HomePane.EntryQr },
                                    onOpenStudyRoom = { homePane = HomePane.StudyRoom },
                                )
                                else -> HomeSecondaryPane(pane = pane, onBack = { homePane = HomePane.Home })
                            }
                        }
                    }
                    MainTab.Forum -> {
                        LakePage(
                            onLogout = onLogout,
                            onFullscreenChange = { hideBottomBar = it },
                            openComposeRequest = forumComposeRequest,
                            onOpenPostExternally = { postId -> routedPostId = postId },
                        )
                    }
                    MainTab.Me -> {
                        AnimatedContent(
                            targetState = mePane,
                            transitionSpec = {
                                val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                                (slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { it * dir } + fadeIn(tween(200))) togetherWith
                                    (slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { -it * dir } + fadeOut(tween(150)))
                            },
                            label = "me-pane-transition",
                        ) { pane ->
                            when (pane) {
                                MePane.Profile -> ProfilePage(
                                    onOpenSettings = { mePane = MePane.Settings },
                                    onOpenProfileSettings = { mePane = MePane.ProfileSettings },
                                    onOpenPost = { postId -> routedPostId = postId },
                                )
                                else -> MeSecondaryPane(
                                    pane = pane,
                                    onPaneChange = { mePane = it },
                                    onLogout = onLogout,
                                    themeMode = themeMode,
                                    onThemeModeChange = onThemeModeChange,
                                    fontMode = fontMode,
                                    onFontModeChange = onFontModeChange,
                                    bottomBarLabelMode = bottomBarLabelMode,
                                    onBottomBarLabelModeChange = onBottomBarLabelModeChange,
                                    navigationBarMode = navigationBarMode,
                                    onNavigationBarModeChange = onNavigationBarModeChange,
                                    tabletRailPosition = tabletRailPosition,
                                    onTabletRailPositionChange = onTabletRailPositionChange,
                                    bottomBarGlassEnabled = bottomBarGlassEnabled,
                                    onBottomBarGlassEnabledChange = onBottomBarGlassEnabledChange,
                                    forumNotificationEnabled = forumNotificationEnabled,
                                    onForumNotificationEnabledChange = onForumNotificationEnabledChange,
                                    courseNotificationEnabled = courseNotificationEnabled,
                                    onCourseNotificationEnabledChange = onCourseNotificationEnabledChange,
                                    courseLiveUpdateEnabled = courseLiveUpdateEnabled,
                                    onCourseLiveUpdateEnabledChange = onCourseLiveUpdateEnabledChange,
                                    miIslandNotificationEnabled = miIslandNotificationEnabled,
                                    onMiIslandNotificationEnabledChange = onMiIslandNotificationEnabledChange,
                                    miIslandBypassEnabled = miIslandBypassEnabled,
                                    onMiIslandBypassEnabledChange = onMiIslandBypassEnabledChange,
                                    miIslandAuthMode = miIslandAuthMode,
                                    onMiIslandAuthModeChange = onMiIslandAuthModeChange,
                                    miIslandDisplayMode = miIslandDisplayMode,
                                    onMiIslandDisplayModeChange = onMiIslandDisplayModeChange,
                                )
                            }
                        }
                    }
                }
            }
            AnimatedContent(
                targetState = routedPostId,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    if (targetState != null) {
                        (slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(220))) togetherWith
                            (slideOutHorizontally(tween(220, easing = FastOutSlowInEasing)) { -it / 4 } + fadeOut(tween(140)))
                    } else {
                        (fadeIn(tween(90))) togetherWith
                            (slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(150)))
                    }
                },
                label = "routed-post-overlay-transition",
            ) { postId ->
                if (postId != null) {
                    LakePage(
                        onLogout = onLogout,
                        onFullscreenChange = { },
                        openComposeRequest = 0,
                        routedMode = true,
                        routedPostId = postId,
                        onRoutedPostClose = { routedPostId = null },
                    )
                }
            }
        }
                if (showTabletRail && tabletRailPosition == 1) {
                    TabletNavigationRail(
                        selected = selected,
                        onSelect = { currentTab = it.ordinal },
                        showLabels = bottomBarLabelMode == 1,
                        liquidGlassEnabled = bottomBarGlassEnabled,
                        backdrop = floatingBackdrop,
                        alignEnd = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSecondaryPane(pane: HomePane, onBack: () -> Unit) {
    when (pane) {
        HomePane.Home -> HomePage(
            onOpenCourses = {},
            onOpenGpa = {},
            onOpenExams = {},
            onOpenEntryQr = {},
            onOpenStudyRoom = {},
        )
        HomePane.Courses -> CourseSchedulePage(onBack = onBack)
        HomePane.Gpa -> GpaPage(onBack = onBack)
        HomePane.Exams -> ExamSchedulePage(onBack = onBack)
        HomePane.EntryQr -> EntryQrPage(onBack = onBack)
        HomePane.StudyRoom -> StudyRoomPage(onBack = onBack)
    }
}

@Composable
private fun MeSecondaryPane(
    pane: MePane,
    onPaneChange: (MePane) -> Unit,
    onLogout: () -> Unit,
    themeMode: Int,
    onThemeModeChange: (Int) -> Unit,
    fontMode: Int,
    onFontModeChange: (Int) -> Unit,
    bottomBarLabelMode: Int,
    onBottomBarLabelModeChange: (Int) -> Unit,
    navigationBarMode: Int,
    onNavigationBarModeChange: (Int) -> Unit,
    tabletRailPosition: Int,
    onTabletRailPositionChange: (Int) -> Unit,
    bottomBarGlassEnabled: Boolean,
    onBottomBarGlassEnabledChange: (Boolean) -> Unit,
    forumNotificationEnabled: Boolean,
    onForumNotificationEnabledChange: (Boolean) -> Unit,
    courseNotificationEnabled: Boolean,
    onCourseNotificationEnabledChange: (Boolean) -> Unit,
    courseLiveUpdateEnabled: Boolean,
    onCourseLiveUpdateEnabledChange: (Boolean) -> Unit,
    miIslandNotificationEnabled: Boolean,
    onMiIslandNotificationEnabledChange: (Boolean) -> Unit,
    miIslandBypassEnabled: Boolean,
    onMiIslandBypassEnabledChange: (Boolean) -> Unit,
    miIslandAuthMode: Int,
    onMiIslandAuthModeChange: (Int) -> Unit,
    miIslandDisplayMode: Int,
    onMiIslandDisplayModeChange: (Int) -> Unit,
) {
    when (pane) {
        MePane.Profile -> ProfilePage(
            onOpenSettings = { onPaneChange(MePane.Settings) },
            onOpenProfileSettings = { onPaneChange(MePane.ProfileSettings) },
            onOpenPost = {},
        )
        MePane.ProfileSettings -> ProfileSettingsPage(
            onBack = { onPaneChange(MePane.Profile) },
            onLogout = onLogout,
        )
        MePane.Settings -> SettingsPage(
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            fontMode = fontMode,
            onFontModeChange = onFontModeChange,
            bottomBarLabelMode = bottomBarLabelMode,
            onBottomBarLabelModeChange = onBottomBarLabelModeChange,
            navigationBarMode = navigationBarMode,
            onNavigationBarModeChange = onNavigationBarModeChange,
            tabletRailPosition = tabletRailPosition,
            onTabletRailPositionChange = onTabletRailPositionChange,
            bottomBarGlassEnabled = bottomBarGlassEnabled,
            onBottomBarGlassEnabledChange = onBottomBarGlassEnabledChange,
            onBack = { onPaneChange(MePane.Profile) },
            onOpenTjuSettings = { onPaneChange(MePane.TjuSettings) },
            onOpenNotificationSettings = { onPaneChange(MePane.NotificationSettings) },
            onOpenAbout = { onPaneChange(MePane.About) },
        )
        MePane.NotificationSettings -> NotificationSettingsPage(
            forumNotificationEnabled = forumNotificationEnabled,
            onForumNotificationEnabledChange = onForumNotificationEnabledChange,
            courseNotificationEnabled = courseNotificationEnabled,
            onCourseNotificationEnabledChange = onCourseNotificationEnabledChange,
            courseLiveUpdateEnabled = courseLiveUpdateEnabled,
            onCourseLiveUpdateEnabledChange = onCourseLiveUpdateEnabledChange,
            miIslandNotificationEnabled = miIslandNotificationEnabled,
            onMiIslandNotificationEnabledChange = onMiIslandNotificationEnabledChange,
            miIslandBypassEnabled = miIslandBypassEnabled,
            onMiIslandBypassEnabledChange = onMiIslandBypassEnabledChange,
            miIslandAuthMode = miIslandAuthMode,
            onMiIslandAuthModeChange = onMiIslandAuthModeChange,
            miIslandDisplayMode = miIslandDisplayMode,
            onMiIslandDisplayModeChange = onMiIslandDisplayModeChange,
            onBack = { onPaneChange(MePane.Settings) },
        )
        MePane.TjuSettings -> TjuSettingsPage(
            onBack = { onPaneChange(MePane.Settings) },
            onOpenDebug = { onPaneChange(MePane.TjuDebug) },
        )
        MePane.TjuDebug -> TjuDebugPage(onBack = { onPaneChange(MePane.TjuSettings) })
        MePane.About -> AboutPage(onBack = { onPaneChange(MePane.Settings) })
    }
}

@Composable
private fun TabletNavigationRail(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    showLabels: Boolean,
    liquidGlassEnabled: Boolean,
    backdrop: Backdrop,
    alignEnd: Boolean,
    modifier: Modifier = Modifier,
) {
    val glassActive = liquidGlassEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val isLight = !LocalAppDarkMode.current
    val containerColor = if (glassActive) {
        MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.46f)
    } else {
        MiuixTheme.colorScheme.surfaceContainer
    }
    val railWidth = if (showLabels) 92.dp else 76.dp
    var railDragOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(railWidth)
            .padding(
                start = if (alignEnd) 0.dp else 12.dp,
                end = if (alignEnd) 12.dp else 0.dp,
                top = 14.dp,
                bottom = 14.dp,
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(selected) {
                detectVerticalDragGestures(
                    onDragStart = { railDragOffset = 0f },
                    onDragCancel = { railDragOffset = 0f },
                    onDragEnd = {
                        val threshold = 34.dp.toPx()
                        val current = selected.ordinal
                        val target = when {
                            railDragOffset <= -threshold -> (current + 1).coerceAtMost(MainTab.entries.lastIndex)
                            railDragOffset >= threshold -> (current - 1).coerceAtLeast(0)
                            else -> current
                        }
                        railDragOffset = 0f
                        MainTab.entries.getOrNull(target)?.let { tab ->
                            if (tab != selected) onSelect(tab)
                        }
                    },
                ) { change, dragAmount ->
                    railDragOffset += dragAmount
                    change.consume()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(railWidth - 12.dp)
                .then(
                    if (glassActive) {
                        Modifier.kyantDrawBackdrop(
                            backdrop = backdrop,
                            shape = { RoundedCornerShape(32.dp) },
                            effects = {
                                kyantVibrancy()
                                kyantBlur(8.dp.toPx())
                                kyantLens(20.dp.toPx(), 20.dp.toPx())
                            },
                            highlight = { Highlight.Default.copy(alpha = 0.9f) },
                            shadow = {
                                Shadow.Default.copy(
                                    color = Color.Black.copy(if (isLight) 0.10f else 0.22f),
                                )
                            },
                            onDrawSurface = { drawRect(containerColor) },
                        )
                    } else {
                        Modifier.background(containerColor, RoundedCornerShape(32.dp))
                    },
                ),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                userScrollEnabled = true,
            ) {
                items(MainTab.entries, key = { it.name }) { tab ->
                    val selectedTab = tab == selected
                    val tint = if (selectedTab) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (showLabels) 66.dp else 52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                if (selectedTab) MiuixTheme.colorScheme.primary.copy(alpha = if (LocalAppDarkMode.current) 0.20f else 0.12f) else Color.Transparent,
                            )
                            .clickable { onSelect(tab) }
                            .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
                    ) {
                        Icon(
                            imageVector = tab.miuixIcon,
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier.size(if (selectedTab) 27.dp else 24.dp),
                        )
                        AnimatedVisibility(visible = showLabels) {
                            Text(
                                tab.label,
                                color = tint,
                                fontSize = 11.sp,
                                maxLines = 1,
                                fontWeight = if (selectedTab) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FixedBottomNavigationBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    onLongSelect: (MainTab) -> Unit,
    backdrop: LayerBackdrop,
    showLabels: Boolean,
    liquidGlassEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val glassActive = liquidGlassEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val surfaceColor = MiuixTheme.colorScheme.surface
    val mode = if (showLabels) NavigationBarDisplayMode.IconAndText else NavigationBarDisplayMode.IconOnly

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                Modifier.background(surfaceColor),
            ),
    ) {
        NavigationBar(
            color = Color.Transparent,
            showDivider = !glassActive,
            mode = mode,
        ) {
            MainTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = selected == tab,
                    onClick = { onSelect(tab) },
                    icon = tab.miuixIcon,
                    label = tab.label,
                )
            }
        }
    }
}

@Composable
private fun FloatingBottomNavigationBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    onLongSelect: (MainTab) -> Unit,
    backdrop: Backdrop,
    showLabels: Boolean,
    liquidGlassEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val isDark = LocalAppDarkMode.current
    val isLight = !isDark
    val glassActive = liquidGlassEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val tabsBackdrop = rememberKyantLayerBackdrop()
    val containerColor = if (glassActive) {
        MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.40f)
    } else {
        MiuixTheme.colorScheme.surfaceContainer
    }
    var contentWidthPx by remember { mutableStateOf(0f) }
    var totalWidthPx by remember { mutableStateOf(0f) }
    var dragOffsetPx by remember { mutableStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val stretchAnim = remember { Animatable(0f) }
    val offsetAnimation = remember { Animatable(0f) }
    val indicatorPosition = remember { Animatable(selected.ordinal.toFloat()) }
    val animationSpec = tween<androidx.compose.ui.unit.Dp>(durationMillis = 360, easing = FastOutSlowInEasing)
    val startPadding by animateDpAsState(28.dp, animationSpec = animationSpec, label = "bottomBarStart")
    val endPadding by animateDpAsState(28.dp, animationSpec = animationSpec, label = "bottomBarEnd")
    val itemHeight by animateDpAsState(56.dp, animationSpec = animationSpec, label = "bottomBarItemHeight")
    val tabCount = MainTab.entries.size
    val tabWidthPx = if (contentWidthPx > 0f) contentWidthPx / tabCount else 0f
    val pressProgress by animateFloatAsState(
        targetValue = if (dragging) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
        label = "floatingBarPressProgress",
    )
    val panelOffset by remember(density) {
        derivedStateOf {
            if (totalWidthPx == 0f) {
                0f
            } else {
                val fraction = (offsetAnimation.value / totalWidthPx).fastCoerceIn(-1f, 1f)
                with(density) { 4.dp.toPx() * fraction.sign * FastOutSlowInEasing.transform(abs(fraction)) }
            }
        }
    }
    LaunchedEffect(selected.ordinal, dragging) {
        if (!dragging) {
            indicatorPosition.animateTo(
                selected.ordinal.toFloat(),
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            )
        }
    }
    val indicatorIndex by remember(selected, dragging, dragOffsetPx, tabWidthPx) {
        derivedStateOf {
            if (dragging && tabWidthPx > 0f) {
                (selected.ordinal.toFloat() + dragOffsetPx / tabWidthPx).coerceIn(0f, (tabCount - 1).toFloat())
            } else {
                indicatorPosition.value.coerceIn(0f, (tabCount - 1).toFloat())
            }
        }
    }
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(start = startPadding, end = endPadding, top = 12.dp, bottom = 12.dp)
            .width(IntrinsicSize.Min)
            .height(64.dp)
            .pointerInput(selected, tabWidthPx, tabCount) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        dragging = true
                        dragOffsetPx = 0f
                        scope.launch { stretchAnim.snapTo(0f) }
                    },
                    onDragEnd = {
                        val endIndex = indicatorIndex.coerceIn(0f, (tabCount - 1).toFloat())
                        val targetIndex = endIndex.roundToInt().coerceIn(0, tabCount - 1)
                        dragging = false
                        dragOffsetPx = 0f
                        scope.launch {
                            stretchAnim.animateTo(
                                0f,
                            animationSpec = spring(dampingRatio = 0.58f, stiffness = 300f),
                            )
                            offsetAnimation.animateTo(0f, spring(dampingRatio = 0.58f, stiffness = 300f))
                        }
                        MainTab.entries.getOrNull(targetIndex)?.let { target ->
                            scope.launch {
                                indicatorPosition.stop()
                                indicatorPosition.snapTo(endIndex)
                                indicatorPosition.animateTo(
                                    target.ordinal.toFloat(),
                                    animationSpec = spring(dampingRatio = 0.72f, stiffness = 560f),
                                )
                            }
                            if (target != selected) onSelect(target)
                        }
                    },
                    onDragCancel = {
                        dragging = false
                        dragOffsetPx = 0f
                        scope.launch {
                            stretchAnim.animateTo(
                                0f,
                                animationSpec = spring(dampingRatio = 0.58f, stiffness = 360f),
                            )
                            offsetAnimation.animateTo(0f, spring(dampingRatio = 0.58f, stiffness = 300f))
                        }
                        scope.launch {
                            indicatorPosition.stop()
                            indicatorPosition.animateTo(
                                selected.ordinal.toFloat(),
                                animationSpec = spring(dampingRatio = 0.72f, stiffness = 560f),
                            )
                        }
                    },
                ) { change, dragAmount ->
                    if (tabWidthPx > 0f) {
                        val minOffset = -selected.ordinal * tabWidthPx
                        val maxOffset = (tabCount - 1 - selected.ordinal) * tabWidthPx
                        dragOffsetPx = (dragOffsetPx + dragAmount).coerceIn(minOffset, maxOffset)
                        val stretchTarget = (stretchAnim.value * 0.68f + kotlin.math.abs(dragAmount) * 0.62f)
                            .coerceAtMost(with(density) { 34.dp.toPx() })
                        scope.launch { stretchAnim.snapTo(stretchTarget) }
                        scope.launch { offsetAnimation.snapTo(offsetAnimation.value + dragAmount) }
                    }
                    change.consume()
                }
            }
            .width(IntrinsicSize.Min),
        contentAlignment = Alignment.CenterStart,
    ) {
        @Composable
        fun RowScope.TabItems(tint: Color) {
            MainTab.entries.forEach { tab ->
                val selectedTab = tab == selected
                Column(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 76.dp)
                        .clip(CircleShape)
                        .clickable {
                                scope.launch {
                                    indicatorPosition.stop()
                                    indicatorPosition.animateTo(
                                        tab.ordinal.toFloat(),
                                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                                    )
                                }
                                onSelect(tab)
                        }
                        .fillMaxHeight()
                        .weight(1f)
                        .graphicsLayer {
                            val scale = if (glassActive) lerp(1f, 1.18f, pressProgress) else 1f
                            scaleX = scale
                            scaleY = scale
                        },
                    verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = tab.miuixIcon,
                        contentDescription = tab.label,
                        tint = tint,
                        modifier = Modifier.size(24.dp),
                    )
                    AnimatedVisibility(visible = showLabels) {
                        Text(
                            tab.label,
                            color = tint,
                            fontSize = 11.sp,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        Row(
            Modifier
                .onGloballyPositioned { coords ->
                    totalWidthPx = coords.size.width.toFloat()
                    val contentWidth = totalWidthPx - with(density) { 8.dp.toPx() }
                    contentWidthPx = contentWidth
                }
                .graphicsLayer { translationX = panelOffset }
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .then(
                    if (glassActive) {
                        Modifier.kyantDrawBackdrop(
                            backdrop = backdrop,
                            shape = { CircleShape },
                            effects = {
                                kyantVibrancy()
                                kyantBlur(8.dp.toPx())
                                kyantLens(24.dp.toPx(), 24.dp.toPx())
                            },
                            highlight = { Highlight.Default.copy(alpha = 1f) },
                            shadow = {
                                Shadow.Default.copy(
                                    color = Color.Black.copy(if (isLight) 0.1f else 0.2f),
                                )
                            },
                            layerBlock = {
                                val scale = lerp(1f, 1f + 16.dp.toPx() / size.width, pressProgress)
                                scaleX = scale
                                scaleY = scale
                            },
                            onDrawSurface = { drawRect(containerColor) },
                        )
                    } else {
                        Modifier.background(containerColor, CircleShape)
                    },
                )
                .height(64.dp)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabItems(MiuixTheme.colorScheme.onSurface)
        }

        Row(
            Modifier
                .clearAndSetSemantics {}
                .alpha(0f)
                .kyantLayerBackdrop(tabsBackdrop)
                .graphicsLayer { translationX = panelOffset }
                .then(
                    if (glassActive) {
                        Modifier.kyantDrawBackdrop(
                            backdrop = backdrop,
                            shape = { CircleShape },
                            effects = {
                                kyantVibrancy()
                                kyantBlur(8.dp.toPx())
                                kyantLens(24.dp.toPx() * pressProgress, 24.dp.toPx() * pressProgress)
                            },
                            highlight = { Highlight.Default.copy(alpha = pressProgress) },
                            onDrawSurface = { drawRect(containerColor) },
                        )
                    } else {
                        Modifier
                    },
                )
                .height(itemHeight)
                .padding(horizontal = 4.dp)
                .graphicsLayer(colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.primary)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabItems(MiuixTheme.colorScheme.onSurface)
        }

        if (tabWidthPx > 0f) {
            Box(
                Modifier
                    .padding(horizontal = 4.dp)
                    .graphicsLayer {
                        val progressOffset = indicatorIndex * tabWidthPx
                        translationX = if (isLtr) progressOffset + panelOffset else -progressOffset + panelOffset
                    }
                    .then(
                        if (glassActive) {
                            Modifier.kyantDrawBackdrop(
                                backdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop),
                                shape = { CircleShape },
                                effects = {
                                    kyantLens(10.dp.toPx() * pressProgress, 14.dp.toPx() * pressProgress, true)
                                },
                                highlight = { Highlight.Default.copy(alpha = pressProgress) },
                                shadow = { Shadow(alpha = pressProgress) },
                                innerShadow = {
                                    KyantInnerShadow(
                                        radius = 8.dp * pressProgress,
                                        alpha = pressProgress,
                                    )
                                },
                                layerBlock = {
                                    scaleX = 1f + stretchAnim.value / size.width
                                    scaleY = 1f - (stretchAnim.value / size.width * 0.24f).coerceIn(0f, 0.16f)
                                },
                                onDrawSurface = {
                                    val progress = pressProgress
                                    drawRect(
                                        color = if (isLight) Color.Black.copy(0.1f) else Color.White.copy(0.1f),
                                        alpha = 1f - progress,
                                    )
                                    drawRect(Color.Black.copy(alpha = 0.03f * progress))
                                },
                            )
                        } else {
                            Modifier.background(MiuixTheme.colorScheme.primary.copy(alpha = 0.14f), CircleShape)
                        },
                    )
                    .height(itemHeight)
                    .width(with(density) { tabWidthPx.toDp() }),
            )
        }
    }
}

@Composable
private fun LakePage(
    onLogout: () -> Unit,
    onFullscreenChange: (Boolean) -> Unit,
    openComposeRequest: Int,
    onOpenPostExternally: ((Long) -> Unit)? = null,
    routedMode: Boolean = false,
    routedPostId: Long? = null,
    onRoutedPostClose: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val app = context.applicationContext as RebuildApplication
    val vm: LakeViewModel = viewModel(factory = LakeViewModelFactory(app))
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val searchHistory = remember { mutableStateListOf<String>() }
    val feedListStateStore = remember { mutableMapOf<String, LazyListState>() }
    var pane by remember { mutableStateOf(LakePane.Feed) }
    var pendingMpJumpId by remember { mutableStateOf<Long?>(null) }
    var commentInput by remember { mutableStateOf("") }
    var postTitleInput by remember { mutableStateOf("") }
    var postContentInput by remember { mutableStateOf("") }
    var searchInput by remember { mutableStateOf(ui.keyword) }
    var composeCampus by remember { mutableIntStateOf(0) }
    var composeVoteEnabled by remember { mutableStateOf(false) }
    var composeVoteMaxSelection by remember { mutableIntStateOf(1) }
    val composeVoteOptions = remember { mutableStateListOf("", "") }
    val composeCustomTags = remember { mutableStateListOf<String>() }
    var showComposeTagSheet by remember { mutableStateOf(false) }
    var composeTagInput by remember { mutableStateOf("") }
    var composeTagsExpanded by remember { mutableStateOf(false) }
    var composeDepartmentsExpanded by remember { mutableStateOf(false) }
    var replyFloorId by remember { mutableStateOf<Long?>(null) }
    var replyInput by remember { mutableStateOf("") }
    var imageViewer by remember { mutableStateOf<Pair<List<String>, Int>?>(null) }
    var pendingExternalUrl by remember { mutableStateOf<String?>(null) }
    var reportTarget by remember { mutableStateOf<ReportTarget?>(null) }
    var reportReason by remember { mutableStateOf("") }
    var selectedUserPreview by remember { mutableStateOf<LakeUserPreview?>(null) }
    var expandedRepliesFloorId by remember { mutableStateOf<Long?>(null) }
    var composeImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showComposeSheet by remember { mutableStateOf(false) }
    var showComposeImageSourceSheet by remember { mutableStateOf(false) }
    var pendingCameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var commentImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var replyImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showNotifications by remember { mutableStateOf(false) }
    val openPostAction: (Long) -> Unit = remember(routedMode, onOpenPostExternally) {
        { postId ->
            if (routedMode) vm.openPost(postId) else (onOpenPostExternally?.invoke(postId) ?: vm.openPost(postId))
        }
    }
    var backGestureProgress by remember { mutableStateOf(0f) }
    val composeImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(9)) { uris ->
        composeImages = uris.take(9)
    }
    val composeCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val cameraUri = pendingCameraImageUri
        if (success && cameraUri != null) {
            composeImages = (composeImages + cameraUri).take(9)
        }
        pendingCameraImageUri = null
    }
    val commentImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        commentImages = listOfNotNull(uri)
    }
    val replyImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        replyImages = listOfNotNull(uri)
    }
    val openReportSheet: (ReportTarget) -> Unit = { target ->
        reportTarget = target
        reportReason = ""
    }

    LaunchedEffect(Unit) { vm.refresh() }
    LaunchedEffect(openComposeRequest) {
        if (openComposeRequest > 0) {
            vm.closeDetail()
            pane = LakePane.Feed
            showComposeSheet = true
            expandedRepliesFloorId = null
            imageViewer = null
        }
    }
    LaunchedEffect(routedMode, routedPostId) {
        if (routedMode && routedPostId != null) {
            showComposeSheet = false
            pane = LakePane.Feed
            vm.openPost(routedPostId)
        }
    }
    LaunchedEffect(ui.error) {
        val message = ui.error ?: return@LaunchedEffect
        val isMpJumpError = pendingMpJumpId != null
        val toastMessage = if (isMpJumpError && message.isPostMissingMessage()) {
            "帖子不存在"
        } else {
            message
        }
        if (isMpJumpError) {
            pendingMpJumpId = null
            pane = LakePane.Feed
        }
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        vm.consumeError()
    }
    LaunchedEffect(ui.requireRelogin) {
        if (ui.requireRelogin) {
            vm.consumeRelogin()
            onLogout()
        }
    }
    var clipboardPostId by rememberSaveable { mutableStateOf<Long?>(null) }
    var ignoredClipboardText by rememberSaveable { mutableStateOf<String?>(null) }
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager }
    val dismissClipboardPrompt = {
        ignoredClipboardText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
        clipboardPostId = null
        vm.clearClipboardPreview()
    }
    LaunchedEffect(pane, ignoredClipboardText) {
        if (pane != LakePane.Feed) return@LaunchedEffect
        kotlinx.coroutines.delay(800)
        if (ui.selectedPost != null) return@LaunchedEffect
        val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: return@LaunchedEffect
        if (text == ignoredClipboardText) return@LaunchedEffect
        val mpMatch = Regex("""#MP(\d{6})""", RegexOption.IGNORE_CASE).find(text)
        val wpyMatch = Regex("""wpy://school_project/(\d+)""").find(text)
        val id = mpMatch?.groupValues?.getOrNull(1)?.toLongOrNull()
            ?: wpyMatch?.groupValues?.getOrNull(1)?.toLongOrNull()
            ?: return@LaunchedEffect
        clipboardPostId = id
        vm.fetchClipboardPreview(id)
    }
    if (clipboardPostId != null) {
        top.yukonga.miuix.kmp.window.WindowBottomSheet(
            show = true,
            title = "检测到帖子链接",
            onDismissRequest = dismissClipboardPrompt,
            sheetMaxWidth = 560.dp,
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("是否打开帖子 #MP${clipboardPostId?.toString()?.padStart(6, '0')}？", color = LakeColors.text, fontWeight = FontWeight.SemiBold)
                val preview = ui.clipboardPreviewPost
                if (preview != null) {
                    Card(modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp, insideMargin = PaddingValues(horizontal = 12.dp, vertical = 10.dp), colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(color = LakeColors.cardAlt, contentColor = LakeColors.text)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AvatarImage(avatar = preview.avatar, author = preview.author, size = 24.dp)
                            Text(preview.author.ifBlank { "匿名用户" }, color = LakeColors.text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            LevelBadge(level = preview.level)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(preview.title.ifBlank { "(无标题)" }, color = LakeColors.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (preview.content.isNotBlank()) { Spacer(Modifier.height(4.dp)); Text(preview.content, color = LakeColors.muted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                    }
                } else if (ui.loadingPostDetail) { Text("加载中...", color = LakeColors.muted) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    top.yukonga.miuix.kmp.basic.Button(onClick = dismissClipboardPrompt, modifier = Modifier.weight(1f)) { Text("忽略") }
                    top.yukonga.miuix.kmp.basic.Button(onClick = { val pid = clipboardPostId; ignoredClipboardText = clipboard.primaryClip?.getItemAt(0)?.text?.toString(); clipboardPostId = null; vm.clearClipboardPreview(); pid?.let { clipboard.setPrimaryClip(android.content.ClipData.newPlainText("", "")); vm.openPost(it) } }, modifier = Modifier.weight(1f)) { Text("打开") }
                }
            }
        }
    }
    LaunchedEffect(ui.selectedPost, pane) {
        if (ui.selectedPost != null) pendingMpJumpId = null
        onFullscreenChange(ui.selectedPost != null || ui.loadingPostDetail || pane != LakePane.Feed || showComposeSheet)
    }
    val resetComposeDraft = {
        postTitleInput = ""
        postContentInput = ""
        composeCampus = 0
        composeImages = emptyList()
        composeVoteEnabled = false
        composeVoteMaxSelection = 1
        composeVoteOptions.clear()
        composeVoteOptions.addAll(listOf("", ""))
        composeCustomTags.clear()
        composeTagInput = ""
        composeTagsExpanded = false
        composeDepartmentsExpanded = false
    }
    val hasLakeBackStack = expandedRepliesFloorId != null || imageViewer != null || ui.selectedPost != null || ui.loadingPostDetail || routedMode || pane != LakePane.Feed || showComposeSheet
    val performLakeBack: () -> Unit = {
        when {
            expandedRepliesFloorId != null -> expandedRepliesFloorId = null
            imageViewer != null -> imageViewer = null
            showComposeSheet -> {
                showComposeSheet = false
                resetComposeDraft()
            }
            ui.selectedPost != null -> {
                replyFloorId = null
                expandedRepliesFloorId = null
                replyInput = ""
                replyImages = emptyList()
                commentImages = emptyList()
                vm.closeDetail()
                if (routedMode) onRoutedPostClose?.invoke()
            }
            routedMode && ui.loadingPostDetail -> {
                vm.closeDetail()
                onRoutedPostClose?.invoke()
            }
            routedMode -> {
                vm.closeDetail()
                onRoutedPostClose?.invoke()
            }
            pane != LakePane.Feed -> {
                when (pane) {
                    LakePane.SearchResult -> pane = LakePane.Search
                    else -> {
                        pane = LakePane.Feed
                        searchInput = ui.keyword
                    }
                }
            }
        }
    }
    PredictiveBackHandler(enabled = hasLakeBackStack) { progress ->
        try {
            progress.collect { backEvent ->
                backGestureProgress = backEvent.progress.coerceIn(0f, 1f)
            }
            performLakeBack()
        } catch (e: CancellationException) {
            throw e
        } finally {
            backGestureProgress = 0f
        }
    }
    BackHandler(enabled = hasLakeBackStack) { performLakeBack() }
    val backShiftPx = with(LocalDensity.current) { 54.dp.toPx() }

    Box(modifier = Modifier.fillMaxSize().background(LakeColors.forumBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .graphicsLayer {
                    val eased = backGestureProgress * backGestureProgress
                    translationX = backShiftPx * eased
                    val scale = 1f - 0.02f * eased
                    scaleX = scale
                    scaleY = scale
                    alpha = 1f - 0.08f * eased
                },
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val selectedPost = ui.selectedPost
            val detailRouteActive = selectedPost != null || ui.loadingPostDetail || (routedMode && routedPostId != null)
            AnimatedContent(
                targetState = detailRouteActive,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(220))) togetherWith
                            (slideOutHorizontally(tween(240, easing = FastOutSlowInEasing)) { -it / 4 } + fadeOut(tween(160)))
                    } else {
                        (slideInHorizontally(tween(240, easing = FastOutSlowInEasing)) { -it / 4 } + fadeIn(tween(200))) togetherWith
                            (slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(150)))
                    }
                },
                label = "lake-detail-transition",
            ) { showingDetail ->
                if (!showingDetail) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (pane == LakePane.Feed) {
                            LakeTopBar(
                                keyword = "",
                                sortMode = ui.sortMode,
                                displayMode = ui.displayMode,
                                unreadCount = ui.messageCount?.total ?: 0,
                                onSearchClick = { searchInput = ""; pane = LakePane.Search },
                                onNotificationsClick = {
                                    showNotifications = true
                                    vm.loadLakeNotifications()
                                },
                                onComposeClick = { showComposeSheet = true },
                                onSortChange = vm::setSortMode,
                                onDisplayModeChange = vm::setDisplayMode,
                            )
                        }
                        AnimatedContent(
                            targetState = pane,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            transitionSpec = {
                                if (initialState == LakePane.Feed || targetState == LakePane.Feed) {
                                    (fadeIn(tween(180)) + scaleIn(initialScale = 0.985f, animationSpec = tween(220, easing = FastOutSlowInEasing))) togetherWith
                                        (fadeOut(tween(120)) + scaleOut(targetScale = 0.985f, animationSpec = tween(140, easing = FastOutSlowInEasing)))
                                } else {
                                    val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                                    (slideInHorizontally(tween(220, easing = FastOutSlowInEasing)) { it * dir / 3 } + fadeIn(tween(180))) togetherWith
                                        (slideOutHorizontally(tween(180, easing = FastOutSlowInEasing)) { -it * dir / 3 } + fadeOut(tween(120)))
                                }
                            },
                            label = "lake-pane-transition",
                        ) { targetPane ->
                            when (targetPane) {
                                LakePane.Search -> SearchPane(
                                    query = searchInput,
                                    onQueryChange = { searchInput = it },
                                    history = searchHistory,
                                    onBack = { pane = LakePane.Feed },
                                    onSearch = { raw ->
                                        val keyword = raw.trim()
                                        if (keyword.isNotBlank()) { searchHistory.remove(keyword); searchHistory.add(0, keyword) }
                                        val mpId = keyword.toMpPostIdOrNull()
                                        if (mpId != null) {
                                            pendingMpJumpId = mpId
                                            openPostAction(mpId)
                                            pane = LakePane.Feed
                                        } else {
                                            pendingMpJumpId = null
                                            vm.search(keyword)
                                            pane = LakePane.SearchResult
                                        }
                                    },
                                )
                                LakePane.SearchResult -> SearchResultPane(
                                    query = ui.searchKeyword,
                                    ui = ui,
                                    onBack = { pane = LakePane.Search },
                                    onSortChange = vm::setSortMode,
                                    onOpenPost = openPostAction,
                                    onOpenImages = { urls, index -> imageViewer = urls to index },
                                    onLikePost = vm::toggleListPostLike,
                                    onRefresh = vm::refreshSearch,
                                    onLoadMore = vm::loadMoreSearch,
                                )
                                LakePane.Feed -> FeedPane(
                                    ui = ui,
                                    listStateStore = feedListStateStore,
                                    onSelectType = vm::selectType,
                                    onSelectTag = vm::selectTag,
                                onOpenPost = openPostAction,
                                onOpenImages = { urls, index -> imageViewer = urls to index },
                                onLikePost = vm::toggleListPostLike,
                                onRefresh = vm::refresh,
                                onLoadMore = vm::loadMore,
                                onReportPost = { postId -> openReportSheet(ReportTarget.Post(postId)) },
                                onDeletePost = { postId -> vm.deletePost(postId = postId); Toast.makeText(context, "删除中...", Toast.LENGTH_SHORT).show() },
                                onSharePost = { postId ->
                                    val text = "我在北洋之炬发现了个有趣的问题【...】\n#MP${postId.toString().padStart(6, '0')} ，你也来看看吧~\n将本条微口令复制到北洋之炬求实论坛打开问题 wpy://school_project/$postId"
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    cb.setPrimaryClip(android.content.ClipData.newPlainText("分享帖子", text))
                                    Toast.makeText(context, "已复制分享链接到剪贴板", Toast.LENGTH_SHORT).show()
                                },
                            )
                            }
                        }
                    }
                } else {
                    val targetPost = ui.selectedPost
                    if (targetPost == null) {
                        DetailLoadingPane(onBack = performLakeBack)
                        return@AnimatedContent
                    }
                    DetailPane(
                        post = targetPost,
                        floors = ui.floors,
                        officialFloors = ui.officialFloors,
                        commentInput = commentInput,
                        replyFloorId = replyFloorId,
                        replyInput = replyInput,
                        sending = ui.sending,
                        floorHasMore = ui.floorHasMore,
                        loadingMoreFloors = ui.loadingMoreFloors,
                        refreshing = ui.loading,
                        floorOrder = ui.floorOrder,
                        currentUserAvatar = ui.currentUserAvatar,
                        currentUserName = ui.currentUserName,
                        currentUserUid = ui.currentUserUid ?: 0,
                        onBack = {
                            vm.closeDetail()
                            if (routedMode) onRoutedPostClose?.invoke()
                        },
                        onLike = vm::toggleLike,
                        onFav = vm::toggleFav,
                        onCommentInputChange = { commentInput = it },
                        onSendComment = { vm.sendComment(commentInput, commentImages, context.contentResolver); commentInput = ""; commentImages = emptyList() },
                        onReplyToggle = { floorId -> replyFloorId = if (replyFloorId == floorId) null else floorId; replyImages = emptyList() },
                        onReplyInputChange = { replyInput = it },
                        onSendReply = { floorId -> vm.sendReply(floorId, replyInput, replyImages, context.contentResolver); replyInput = ""; replyImages = emptyList(); replyFloorId = null },
                        onFloorLike = vm::toggleFloorLike,
                        onVote = vm::submitVote,
                        onFloorOrderChange = vm::setFloorOrder,
                        onExpandReplies = { floorId -> expandedRepliesFloorId = floorId },
                        onLoadMoreFloors = vm::loadMoreFloors,
                        onRefresh = vm::refreshSelectedPost,
                        onOpenImages = { urls, index -> imageViewer = urls to index },
                        onOpenMpPost = openPostAction,
                        onOpenUrl = { url -> pendingExternalUrl = url },
                        commentImages = commentImages,
                        onPickCommentImages = { commentImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        onRemoveCommentImage = { uri -> commentImages = commentImages - uri },
                        replyImages = replyImages,
                        onPickReplyImages = { replyImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        onRemoveReplyImage = { uri -> replyImages = replyImages - uri },
                        onReportPost = { openReportSheet(ReportTarget.Post(targetPost.id)) },
                        onDeletePost = {
                            vm.deletePost(postId = targetPost.id) {
                                replyFloorId = null
                                expandedRepliesFloorId = null
                                replyInput = ""
                                replyImages = emptyList()
                                commentImages = emptyList()
                                if (routedMode) onRoutedPostClose?.invoke()
                                Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                            }
                            Toast.makeText(context, "删除中...", Toast.LENGTH_SHORT).show()
                        },
                        onSharePost = {
                            val text = "我在北洋之炬发现了个有趣的问题【${targetPost.title}】\n#MP${targetPost.id.toString().padStart(6, '0')} ，你也来看看吧~\n将本条微口令复制到北洋之炬求实论坛打开问题 wpy://school_project/${targetPost.id}"
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            cb.setPrimaryClip(android.content.ClipData.newPlainText("分享帖子", text))
                            Toast.makeText(context, "已复制分享链接到剪贴板", Toast.LENGTH_SHORT).show()
                        },
                        onReportFloor = { postId, floorId -> openReportSheet(ReportTarget.Floor(postId, floorId)) },
                        onDeleteFloor = { floorId, postId -> vm.deleteFloor(floorId = floorId, postId = postId); Toast.makeText(context, "删除中...", Toast.LENGTH_SHORT).show() },
                        onlyOwner = ui.onlyOwner,
                        onToggleOnlyOwner = { vm.setOnlyOwner(!ui.onlyOwner) },
                        onOpenUser = { selectedUserPreview = it },
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = imageViewer != null,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.92f, animationSpec = tween(240, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.95f, animationSpec = tween(120)),
        ) {
        imageViewer?.let { (urls, index) ->
            ImageViewerOverlay(
                    urls = urls,
                    initialIndex = index,
                    onDismiss = { imageViewer = null },
                )
            }
        }
        expandedRepliesFloorId?.let { floorId ->
            val floor = ui.floors.firstOrNull { it.id == floorId }
            if (floor != null) {
                RepliesBottomSheet(
                    floor = floor,
                    postAuthorUid = ui.selectedPost?.uid ?: 0,
                    replyFloorId = replyFloorId,
                    replyInput = replyInput,
                    sending = ui.sending,
                    onDismiss = { expandedRepliesFloorId = null },
                    onReplyToggle = { targetId -> replyFloorId = if (replyFloorId == targetId) null else targetId; replyImages = emptyList() },
                    onReplyInputChange = { replyInput = it },
                    onSendReply = { targetId -> vm.sendReply(targetId, replyInput, replyImages, context.contentResolver); replyInput = ""; replyImages = emptyList(); replyFloorId = null },
                    onFloorLike = vm::toggleFloorLike,
                    onOpenImages = { urls, index -> imageViewer = urls to index },
                    replyImages = replyImages,
                    onPickReplyImages = { replyImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onRemoveReplyImage = { uri -> replyImages = replyImages - uri },
                    currentUserUid = ui.currentUserUid ?: 0,
                    canDeleteAllFloors = false,
                    onReportFloor = { targetFloorId -> openReportSheet(ReportTarget.Floor(ui.selectedPost?.id ?: floor.postId, targetFloorId)) },
                    onDeleteFloor = { targetFloorId ->
                        vm.deleteFloor(floorId = targetFloorId, postId = ui.selectedPost?.id ?: floor.postId)
                        Toast.makeText(context, "删除中...", Toast.LENGTH_SHORT).show()
                    },
                    onOpenUser = { selectedUserPreview = it },
                )
            }
        }
        selectedUserPreview?.let { user ->
            LakeUserPreviewSheet(user = user, onDismiss = { selectedUserPreview = null })
        }
        LakeNotificationSheet(
            show = showNotifications,
            messages = ui.notificationMessages,
            loading = ui.loadingNotices,
            messageCount = ui.messageCount,
            selectedCategory = ui.notificationCategory,
            onCategoryChange = vm::loadLakeNotifications,
            onMarkRead = vm::markCurrentNotificationCategoryRead,
            onOpenMessage = { message ->
                val postId = message.postId
                if (postId == null || postId <= 0) {
                    Toast.makeText(context, "这条通知没有可跳转的帖子", Toast.LENGTH_SHORT).show()
                } else {
                    showNotifications = false
                    pane = LakePane.Feed
                    openPostAction(postId)
                }
            },
            onDismiss = { showNotifications = false },
        )
        WindowBottomSheet(
            show = showComposeSheet,
            title = "发布帖子",
            onDismissRequest = { showComposeSheet = false },
            sheetMaxWidth = 620.dp,
            startAction = {
                IconButton(onClick = { showComposeSheet = false }, minWidth = 40.dp, minHeight = 40.dp) {
                    Icon(MiuixIcons.Close, contentDescription = "关闭发布窗口", tint = LakeColors.text)
                }
            },
            endAction = {
                IconButton(
                    onClick = {
                        val customTagId = composeCustomTags.firstNotNullOfOrNull { custom ->
                            ui.tags.firstOrNull { it.name.equals(custom, ignoreCase = true) }?.id
                        }
                        vm.createPost(
                            title = postTitleInput,
                            content = postContentInput,
                            campus = composeCampus,
                            masked = false,
                            tagIdOverride = customTagId,
                            imageUris = composeImages,
                            contentResolver = context.contentResolver,
                            voteOptions = if (composeVoteEnabled) composeVoteOptions.toList() else null,
                            voteMaxSelection = composeVoteMaxSelection,
                        )
                        postTitleInput = ""
                        postContentInput = ""
                        composeCampus = 0
                        composeImages = emptyList()
                        composeVoteEnabled = false
                        composeVoteMaxSelection = 1
                        composeVoteOptions.clear()
                        composeVoteOptions.addAll(listOf("", ""))
                        composeCustomTags.clear()
                        composeTagInput = ""
                        composeTagsExpanded = false
                        composeDepartmentsExpanded = false
                        showComposeSheet = false
                    },
                    enabled = !ui.creatingPost,
                    minWidth = 40.dp,
                    minHeight = 40.dp,
                ) {
                    Icon(MiuixIcons.Send, contentDescription = "发布", tint = if (ui.creatingPost) LakeColors.muted else LakeColors.primary)
                }
            },
        ) {
            ComposePostPane(
                title = postTitleInput,
                content = postContentInput,
                campus = composeCampus,
                ui = ui,
                onTitleChange = { postTitleInput = it },
                onContentChange = { postContentInput = it },
                onCampusChange = { composeCampus = it },
                voteEnabled = composeVoteEnabled,
                voteMaxSelection = composeVoteMaxSelection,
                voteOptions = composeVoteOptions,
                onVoteEnabledChange = { enabled ->
                    composeVoteEnabled = enabled
                    if (enabled && composeVoteOptions.size < 2) {
                        composeVoteOptions.clear()
                        composeVoteOptions.addAll(listOf("", ""))
                    }
                },
                onVoteMaxSelectionChange = { composeVoteMaxSelection = it },
                onVoteOptionChange = { index, value -> composeVoteOptions[index] = value },
                onAddVoteOption = {
                    if (composeVoteOptions.size < 8) composeVoteOptions.add("")
                },
                onRemoveVoteOption = { index ->
                    if (composeVoteOptions.size > 2 && index in composeVoteOptions.indices) {
                        composeVoteOptions.removeAt(index)
                        if (composeVoteMaxSelection > composeVoteOptions.size) {
                            composeVoteMaxSelection = composeVoteOptions.size
                        }
                    }
                },
                onBack = { showComposeSheet = false },
                onSubmit = {
                    val customTagId = composeCustomTags.firstNotNullOfOrNull { custom ->
                        ui.tags.firstOrNull { it.name.equals(custom, ignoreCase = true) }?.id
                    }
                    vm.createPost(
                        title = postTitleInput,
                        content = postContentInput,
                        campus = composeCampus,
                        masked = false,
                        tagIdOverride = customTagId,
                        imageUris = composeImages,
                        contentResolver = context.contentResolver,
                        voteOptions = if (composeVoteEnabled) composeVoteOptions.toList() else null,
                        voteMaxSelection = composeVoteMaxSelection,
                    )
                    postTitleInput = ""
                    postContentInput = ""
                    composeCampus = 0
                    composeImages = emptyList()
                    composeVoteEnabled = false
                    composeVoteMaxSelection = 1
                    composeVoteOptions.clear()
                    composeVoteOptions.addAll(listOf("", ""))
                    composeCustomTags.clear()
                    composeTagInput = ""
                    composeTagsExpanded = false
                    composeDepartmentsExpanded = false
                    showComposeSheet = false
                },
                onSelectType = vm::selectType,
                onSelectTag = vm::selectTag,
                onSelectDepartment = vm::selectDepartment,
                composeCustomTags = composeCustomTags,
                composeTagInput = composeTagInput,
                showComposeTagSheet = showComposeTagSheet,
                composeTagsExpanded = composeTagsExpanded,
                composeDepartmentsExpanded = composeDepartmentsExpanded,
                onComposeTagInputChange = { composeTagInput = it },
                onComposeTagSheetChange = { showComposeTagSheet = it },
                onComposeTagsExpandedChange = { composeTagsExpanded = it },
                onComposeDepartmentsExpandedChange = { composeDepartmentsExpanded = it },
                onAddComposeTag = { tag ->
                    val normalized = tag.trim()
                    if (normalized.isNotBlank() && composeCustomTags.none { it.equals(normalized, ignoreCase = true) }) {
                        composeCustomTags.add(normalized)
                    }
                },
                onRemoveComposeTag = { tag -> composeCustomTags.remove(tag) },
                selectedImages = composeImages,
                onPickImages = { showComposeImageSourceSheet = true },
                onRemoveImage = { uri -> composeImages = composeImages - uri },
                inSheetMode = true,
            )
        }
        pendingExternalUrl?.let { url ->
            WindowBottomSheet(
                show = true,
                title = "即将打开外部链接",
                onDismissRequest = { pendingExternalUrl = null },
                sheetMaxWidth = 520.dp,
            ) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("请确认链接来源可信。外部网页可能收集你的浏览信息或要求登录。", color = LakeColors.text)
                    Text(url, color = LakeColors.muted, fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { pendingExternalUrl = null }, modifier = Modifier.weight(1f)) {
                            Text("取消")
                        }
                        Button(
                            onClick = {
                                context.openExternalUrl(url)
                                pendingExternalUrl = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.buttonColors(
                                color = LakeColors.primary,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text("继续打开")
                        }
                    }
                }
            }
        }
        reportTarget?.let { target ->
            WindowBottomSheet(
                show = true,
                title = "举报",
                onDismissRequest = { reportTarget = null },
                sheetMaxWidth = 520.dp,
                startAction = {
                    IconButton(onClick = { reportTarget = null }, minWidth = 40.dp, minHeight = 40.dp) {
                        Icon(MiuixIcons.Close, contentDescription = "关闭举报", tint = LakeColors.text)
                    }
                },
                endAction = {
                    IconButton(
                        onClick = {
                            when (target) {
                                is ReportTarget.Post -> vm.reportPost(target.postId, reportReason)
                                is ReportTarget.Floor -> vm.reportFloor(target.postId, target.floorId, reportReason)
                            }
                            Toast.makeText(context, "举报已提交", Toast.LENGTH_SHORT).show()
                            reportTarget = null
                            reportReason = ""
                        },
                        minWidth = 40.dp,
                        minHeight = 40.dp,
                    ) {
                        Icon(MiuixIcons.Ok, contentDescription = "提交举报", tint = LakeColors.primary)
                    }
                },
            ) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        when (target) {
                            is ReportTarget.Post -> "你正在举报帖子 #MP${target.postId.toString().padStart(6, '0')}"
                            is ReportTarget.Floor -> "你正在举报这条评论"
                        },
                        color = LakeColors.text,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = "请填写举报理由，如“色情暴力”“政治敏感”等",
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("不填写理由时将以“用户举报”提交。", color = LakeColors.muted, fontSize = 12.sp)
                }
            }
        }
        WindowBottomSheet(
            show = showComposeImageSourceSheet,
            title = "添加图片",
            onDismissRequest = { showComposeImageSourceSheet = false },
            sheetMaxWidth = 560.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        showComposeImageSourceSheet = false
                        composeImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = composeImages.size < 9,
                ) {
                    Text("从相册选择")
                }
                Button(
                    onClick = {
                        showComposeImageSourceSheet = false
                        val cameraUri = context.createComposeCameraImageUri()
                        if (cameraUri == null) {
                            Toast.makeText(context, "暂时无法打开相机", Toast.LENGTH_SHORT).show()
                        } else {
                            pendingCameraImageUri = cameraUri
                            composeCameraLauncher.launch(cameraUri)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = composeImages.size < 9,
                ) {
                    Text("拍照")
                }
                Text(
                    "最多 9 张，当前 ${composeImages.size}/9",
                    color = LakeColors.muted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        WindowBottomSheet(
            show = showComposeTagSheet,
            title = "添加标签",
            onDismissRequest = { showComposeTagSheet = false },
            sheetMaxWidth = 480.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextField(
                    value = composeTagInput,
                    onValueChange = { composeTagInput = it },
                    label = "标签内容",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = {
                        val normalized = composeTagInput.trim()
                        if (normalized.isNotBlank() && composeCustomTags.none { it.equals(normalized, ignoreCase = true) }) {
                            composeCustomTags.add(normalized)
                        }
                        composeTagInput = ""
                        showComposeTagSheet = false
                    },
                    enabled = composeTagInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                ) {
                    Text("确定")
                }
            }
        }
    }
}

@Composable
internal fun LakeBlock(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        cornerRadius = 20.dp,
        insideMargin = PaddingValues(14.dp),
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = LakeColors.card,
            contentColor = LakeColors.text,
        ),
        onClick = onClick,
        content = content,
    )
}

@Composable
private fun LakeNotificationSheet(
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
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
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

private fun LakeMessageCountUi?.countOf(category: LakeMessageCategory): Int {
    val count = this ?: return 0
    return when (category) {
        LakeMessageCategory.Like -> count.like
        LakeMessageCategory.Floor -> count.floor
        LakeMessageCategory.Reply -> count.reply
        LakeMessageCategory.Notice -> count.notice
    }
}

@Composable
private fun LakeNoticeItem(notice: LakeMessageUi, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        cornerRadius = 16.dp,
        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
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

@Composable
private fun LakeTopBar(keyword: String, sortMode: Int, displayMode: Int, unreadCount: Int, onSearchClick: () -> Unit, onNotificationsClick: () -> Unit, onComposeClick: () -> Unit, onSortChange: (Int) -> Unit, onDisplayModeChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "求实论坛",
                    color = LakeColors.primary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp,
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .clickable {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onNotificationsClick()
                        },
                )
                Icon(
                    imageVector = MiuixIcons.Promotions,
                    contentDescription = "通知",
                    tint = LakeColors.text,
                    modifier = Modifier.size(24.dp),
                )
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 1.dp, y = 1.dp)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(LakeColors.like),
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LakeColors.primary)
                    .clickable {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onComposeClick()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.Add,
                    contentDescription = "发帖",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                modifier = Modifier.weight(1f).height(48.dp),
                cornerRadius = 18.dp,
                insideMargin = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                    color = LakeColors.card,
                    contentColor = LakeColors.text,
                ),
                onClick = onSearchClick,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = MiuixIcons.Basic.Search,
                        contentDescription = null,
                        tint = LakeColors.muted,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        keyword.ifBlank { "搜索帖子或内容" },
                        color = LakeColors.muted,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Card(
                modifier = Modifier.height(48.dp),
                cornerRadius = 18.dp,
                insideMargin = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                    color = LakeColors.card,
                    contentColor = LakeColors.text,
                ),
                onClick = {
                    expanded = !expanded
                    if (expanded) hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                },
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(imageVector = MiuixIcons.ListView, contentDescription = null, tint = LakeColors.muted, modifier = Modifier.size(18.dp))
                    Text("分类", color = LakeColors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                WindowListPopup(
                    show = expanded,
                    alignment = PopupPositionProvider.Align.End,
                    enableWindowDim = true,
                    minWidth = 168.dp,
                    onDismissRequest = { expanded = false },
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp)),
                    ) {
                        ListPopupColumn {
                            Text(
                                "排序方式",
                                color = if (LocalAppDarkMode.current) Color(0xFFB6B8BD) else Color.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 6.dp),
                            )
                            DropdownImpl("默认排序", optionSize = 2, isSelected = sortMode == 0, index = 0) {
                                onSortChange(0)
                                expanded = false
                            }
                            DropdownImpl("最新发帖", optionSize = 2, isSelected = sortMode == 1, index = 1) {
                                onSortChange(1)
                                expanded = false
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedPane(ui: LakeUiState, listStateStore: MutableMap<String, LazyListState>, onSelectType: (Int) -> Unit, onSelectTag: (Int?) -> Unit, onOpenPost: (Long) -> Unit, onOpenImages: (List<String>, Int) -> Unit, onLikePost: (Long) -> Unit, onRefresh: () -> Unit, onLoadMore: () -> Unit, onReportPost: ((Long) -> Unit)? = null, onDeletePost: ((Long) -> Unit)? = null, onSharePost: ((Long) -> Unit)? = null) {
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
            LazyRow(
                state = tabListState,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                items(ui.tabs, key = { it.id }) { tab ->
                    FilterChip(tab.name, ui.selectedType == tab.id) { onSelectType(tab.id) }
                }
            }
        }
        if (ui.tags.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                item { FilterChip("全部标签", ui.selectedTagId == null) { onSelectTag(null) } }
                items(ui.tags, key = { it.id }) { tag -> FilterChip(tag.name, ui.selectedTagId == tag.id) { onSelectTag(tag.id) } }
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->
            val pageType = ui.tabs.getOrNull(page)?.id ?: ui.selectedType
            val pageStateKey = lakeFeedCacheKey(pageType, ui.selectedTagId, ui.keyword, ui.sortMode)
            val pageListState = remember(pageStateKey) {
                listStateStore.getOrPut(pageStateKey) { LazyListState() }
            }
            val pagePullToRefreshState = rememberPullToRefreshState()
            val pagePosts = if (pageType == ui.selectedType) {
                ui.posts
            } else {
                ui.feedCaches[pageStateKey]?.posts.orEmpty()
            }
            val pageIsCurrent = pageType == ui.selectedType
            val pageLoading = ui.loading && pageIsCurrent
            val pageHasMore = if (pageIsCurrent) ui.hasMore else ui.feedCaches[pageStateKey]?.hasMore == true
            val deferImages by remember(pageListState, pageIsCurrent) {
                derivedStateOf {
                    pageIsCurrent && pageListState.isScrollInProgress
                }
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "加载中...",
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        LazyColumn(
                            state = pageListState,
                            modifier = Modifier.fillMaxSize().miuixScroll(),
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            overscrollEffect = null,
                        ) {
                            items(pagePosts, key = { it.id }, contentType = { "post-card" }) { post ->
                                PostSummaryCard(post = post, onClick = { onOpenPost(post.id) }, onOpenImages = onOpenImages, onLike = { onLikePost(post.id) }, onReport = onReportPost?.let { f -> { f(post.id) } }, onDelete = if (post.isOwner) onDeletePost?.let { f -> { f(post.id) } } else null, onShare = onSharePost?.let { f -> { f(post.id) } })
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

private fun lakeFeedCacheKey(type: Int, tagId: Int?, keyword: String, sortMode: Int): String {
    return "$type:${tagId ?: "all"}:$sortMode:${keyword.trim()}"
}

@Composable
private fun SearchPane(query: String, onQueryChange: (String) -> Unit, history: MutableList<String>, onBack: () -> Unit, onSearch: (String) -> Unit) {
    val listState = rememberLazyListState()
    val scrollBehavior = MiuixScrollBehavior()
    val pinnedMp = query.startsWith("#MP", ignoreCase = true)
    val handleQueryChange: (String) -> Unit = { next ->
        if (pinnedMp && !next.startsWith("#MP", ignoreCase = true)) {
            onQueryChange("")
        } else {
            onQueryChange(next)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = "搜索",
                largeTitle = "搜索",
                color = LakeColors.background,
                titleColor = LakeColors.text,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                            tint = LakeColors.text,
                        )
                    }
                },
            )
        },
        containerColor = LakeColors.background,
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .miuixScroll()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            overscrollEffect = null,
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextField(value = query, onValueChange = handleQueryChange, label = "搜索标题、正文或 MP 号", singleLine = true, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            if (pinnedMp) onQueryChange("") else onQueryChange("#MP")
                        },
                        minWidth = 44.dp,
                        minHeight = 44.dp,
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Pin,
                            contentDescription = "填充 MP 口令",
                            tint = if (pinnedMp) LakeColors.primary else LakeColors.muted,
                        )
                    }
                    IconButton(onClick = { onSearch(query) }, minWidth = 44.dp, minHeight = 44.dp) {
                        Icon(
                            imageVector = MiuixIcons.Basic.Search,
                            contentDescription = "搜索",
                            tint = LakeColors.text,
                        )
                    }
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("搜索历史", color = LakeColors.text, fontWeight = FontWeight.SemiBold)
                    if (history.isNotEmpty()) {
                        Text(
                            "清空",
                            color = LakeColors.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { history.clear() }
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                        )
                    }
                }
            }
            if (history.isEmpty()) {
                item { Text("暂无搜索历史", color = MiuixTheme.colorScheme.onSurfaceVariantSummary) }
            } else {
                items(history) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                            color = LakeColors.cardAlt,
                            contentColor = LakeColors.text,
                        ),
                        onClick = { onSearch(item) },
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = MiuixIcons.Basic.Search,
                                contentDescription = null,
                                tint = LakeColors.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(item, color = LakeColors.text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            IconButton(onClick = { history.remove(item) }, minWidth = 32.dp, minHeight = 32.dp) {
                                Icon(
                                    imageVector = MiuixIcons.Close,
                                    contentDescription = "删除搜索历史",
                                    tint = LakeColors.muted,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultPane(
    query: String,
    ui: LakeUiState,
    onBack: () -> Unit,
    onSortChange: (Int) -> Unit,
    onOpenPost: (Long) -> Unit,
    onOpenImages: (List<String>, Int) -> Unit,
    onLikePost: (Long) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val listState = rememberSaveable("search_result_list", saver = LazyListState.Saver) { LazyListState() }
    val deferImages by remember(listState) {
        derivedStateOf {
            listState.isScrollInProgress
        }
    }
    val latestPostsSize by rememberUpdatedState(ui.searchPosts.size)
    val latestHasMore by rememberUpdatedState(ui.searchHasMore)
    val latestLoading by rememberUpdatedState(ui.loadingSearch)
    val latestLoadingMore by rememberUpdatedState(ui.loadingMoreSearch)
    val scrollBehavior = MiuixScrollBehavior()
    LaunchedEffect(Unit) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        }.collect { lastVisibleIndex ->
            val loadMoreIndex = (latestPostsSize - 3).coerceAtLeast(0)
            if (lastVisibleIndex >= loadMoreIndex && latestHasMore && !latestLoading && !latestLoadingMore) {
                onLoadMore()
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = "搜索结果",
                largeTitle = "搜索结果",
                subtitle = if (query.isBlank()) "请输入关键词" else query,
                color = LakeColors.background,
                titleColor = LakeColors.text,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                            tint = LakeColors.text,
                        )
                    }
                },
            )
        },
        containerColor = LakeColors.background,
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip("默认排序", ui.sortMode == 0) { onSortChange(0) }
                FilterChip("最新发帖", ui.sortMode == 1) { onSortChange(1) }
            }
            PullToRefresh(
                isRefreshing = ui.loadingSearch && ui.searchPosts.isNotEmpty(),
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
                pullToRefreshState = pullToRefreshState,
                contentPadding = PaddingValues(),
            ) {
                CompositionLocalProvider(LocalDeferImageLoading provides deferImages) {
                    if (ui.loadingSearch && ui.searchPosts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("加载中...", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        }
                    } else if (ui.searchPosts.isEmpty() && query.isNotBlank()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("未找到相关帖子", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize().miuixScroll(),
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            overscrollEffect = null,
                        ) {
                            items(ui.searchPosts, key = { it.id }, contentType = { "search-post-card" }) { post ->
                                PostSummaryCard(post = post, onClick = { onOpenPost(post.id) }, onOpenImages = onOpenImages, onLike = { onLikePost(post.id) })
                            }
                            item(contentType = "search-footer") {
                                Text(
                                    if (ui.loadingMoreSearch) "加载中..." else if (ui.searchHasMore) "继续下滑加载更多" else "已经到底了",
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

@Composable
private fun ComposePostPane(
    title: String,
    content: String,
    campus: Int,
    ui: LakeUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onCampusChange: (Int) -> Unit,
    voteEnabled: Boolean,
    voteMaxSelection: Int,
    voteOptions: List<String>,
    onVoteEnabledChange: (Boolean) -> Unit,
    onVoteMaxSelectionChange: (Int) -> Unit,
    onVoteOptionChange: (Int, String) -> Unit,
    onAddVoteOption: () -> Unit,
    onRemoveVoteOption: (Int) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    onSelectType: (Int) -> Unit,
    onSelectTag: (Int?) -> Unit,
    onSelectDepartment: (Int?) -> Unit,
    composeCustomTags: List<String>,
    composeTagInput: String,
    showComposeTagSheet: Boolean,
    composeTagsExpanded: Boolean,
    composeDepartmentsExpanded: Boolean,
    onComposeTagInputChange: (String) -> Unit,
    onComposeTagSheetChange: (Boolean) -> Unit,
    onComposeTagsExpandedChange: (Boolean) -> Unit,
    onComposeDepartmentsExpandedChange: (Boolean) -> Unit,
    onAddComposeTag: (String) -> Unit,
    onRemoveComposeTag: (String) -> Unit,
    selectedImages: List<Uri>,
    onPickImages: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    inSheetMode: Boolean = false,
) {
    val publishTabs = remember(ui.tabs) { ui.tabs.filterNot { it.name.contains("精华") } }
    val selectedTabIndex = publishTabs.indexOfFirst { it.id == ui.selectedType }.coerceAtLeast(0)
    LaunchedEffect(publishTabs, ui.selectedType) {
        if (publishTabs.isNotEmpty() && publishTabs.none { it.id == ui.selectedType }) {
            onSelectType(publishTabs.first().id)
        }
    }
    if (inSheetMode) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .miuixScroll()
                .heightIn(max = 620.dp),
            contentPadding = PaddingValues(start = 12.dp, top = 10.dp, end = 12.dp, bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            overscrollEffect = null,
        ) {
            if (publishTabs.isNotEmpty()) {
                item {
                    ComposeSectionCard(title = "发布板块") {
                        WindowDropdownPreference(
                            title = "选择板块",
                            items = publishTabs.map { it.name },
                            selectedIndex = selectedTabIndex,
                            onSelectedIndexChange = { index ->
                                publishTabs.getOrNull(index)?.let { onSelectType(it.id) }
                            },
                        )
                    }
                }
            }
            item {
                ComposeSectionCard(title = "标签") {
                    Button(
                        onClick = { onComposeTagSheetChange(true) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("添加标签", modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Icon(imageVector = MiuixIcons.Add, contentDescription = "添加标签", modifier = Modifier.size(18.dp))
                        }
                    }
                    if (composeCustomTags.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("暂无标签", color = LakeColors.muted, fontSize = 13.sp)
                    } else {
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            maxLines = if (composeTagsExpanded) Int.MAX_VALUE else 2,
                        ) {
                            composeCustomTags.forEach { tag ->
                                Card(
                                    onClick = { onSelectTag(ui.tags.firstOrNull { it.name.equals(tag, ignoreCase = true) }?.id) },
                                    cornerRadius = 12.dp,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        IconButton(
                                            onClick = { onRemoveComposeTag(tag) },
                                            minWidth = 26.dp,
                                            minHeight = 26.dp,
                                        ) {
                                            Icon(
                                                imageVector = MiuixIcons.Close,
                                                contentDescription = "删除标签",
                                                tint = LakeColors.muted,
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                        Text("#$tag", color = LakeColors.text, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                        if (composeCustomTags.size > 6) {
                            Spacer(Modifier.height(6.dp))
                            IconButton(
                                onClick = { onComposeTagsExpandedChange(!composeTagsExpanded) },
                                modifier = Modifier.align(Alignment.End),
                                minWidth = 34.dp,
                                minHeight = 34.dp,
                            ) {
                                Icon(
                                    imageVector = if (composeTagsExpanded) MiuixIcons.ExpandLess else MiuixIcons.ExpandMore,
                                    contentDescription = if (composeTagsExpanded) "收起标签" else "展开标签",
                                    tint = LakeColors.muted,
                                )
                            }
                        }
                    }
                }
            }
            if (ui.selectedType == 1) {
                item {
                    ComposeSectionCard(title = "校务部门") {
                        if (ui.departments.isEmpty()) {
                            Text("暂无可选部门", color = LakeColors.muted, fontSize = 13.sp)
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                maxLines = if (composeDepartmentsExpanded) Int.MAX_VALUE else 2,
                            ) {
                                Card(
                                    onClick = { onSelectDepartment(null) },
                                    cornerRadius = 12.dp,
                                    insideMargin = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                                        color = if (ui.selectedDepartmentId == null) LakeColors.primary.copy(alpha = 0.18f) else LakeColors.cardAlt,
                                        contentColor = LakeColors.text,
                                    ),
                                ) {
                                    Text("请选择", color = if (ui.selectedDepartmentId == null) LakeColors.primary else LakeColors.text, fontSize = 13.sp)
                                }
                                ui.departments.forEach { department ->
                                    Card(
                                        onClick = { onSelectDepartment(department.id) },
                                        cornerRadius = 12.dp,
                                        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                                            color = if (ui.selectedDepartmentId == department.id) LakeColors.primary.copy(alpha = 0.18f) else LakeColors.cardAlt,
                                            contentColor = LakeColors.text,
                                        ),
                                    ) {
                                        Text(
                                            department.name,
                                            color = if (ui.selectedDepartmentId == department.id) LakeColors.primary else LakeColors.text,
                                            fontSize = 13.sp,
                                        )
                                    }
                                }
                            }
                            if (ui.departments.size > 8) {
                                Spacer(Modifier.height(6.dp))
                                IconButton(
                                    onClick = { onComposeDepartmentsExpandedChange(!composeDepartmentsExpanded) },
                                    modifier = Modifier.align(Alignment.End),
                                    minWidth = 34.dp,
                                    minHeight = 34.dp,
                                ) {
                                    Icon(
                                        imageVector = if (composeDepartmentsExpanded) MiuixIcons.ExpandLess else MiuixIcons.ExpandMore,
                                        contentDescription = if (composeDepartmentsExpanded) "收起部门" else "展开部门",
                                        tint = LakeColors.muted,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                ComposeSectionCard(title = if (voteEnabled) "投票内容" else "帖子内容") {
                    TextField(
                        value = title,
                        onValueChange = onTitleChange,
                        label = if (voteEnabled) "投票标题" else "标题",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(10.dp))
                    TextField(
                        value = content,
                        onValueChange = onContentChange,
                        label = if (voteEnabled) "正文（可选）" else "正文",
                        minLines = 7,
                        maxLines = 14,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            item {
                ComposeSectionCard(title = "发布类型") {
                    WindowDropdownPreference(
                        title = "类型",
                        items = listOf("普通", "投票"),
                        selectedIndex = if (voteEnabled) 1 else 0,
                        onSelectedIndexChange = { onVoteEnabledChange(it == 1) },
                    )
                }
            }
            if (voteEnabled) {
                item {
                    ComposeSectionCard(title = "投票选项") {
                        voteOptions.forEachIndexed { index, option ->
                            BasicComponent(
                                title = "选项 ${index + 1}",
                                endActions = {
                                    if (voteOptions.size > 2) {
                                        IconButton(onClick = { onRemoveVoteOption(index) }, minWidth = 36.dp, minHeight = 36.dp) {
                                            Text("删", color = LakeColors.like, fontSize = 13.sp)
                                        }
                                    }
                                },
                                bottomAction = {
                                    TextField(
                                        value = option,
                                        onValueChange = { onVoteOptionChange(index, it) },
                                        label = "请输入选项内容",
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    )
                                },
                            )
                            if (index != voteOptions.lastIndex) HorizontalDivider(modifier = Modifier.fillMaxWidth())
                        }
                        if (voteOptions.size < 8) {
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = onAddVoteOption, modifier = Modifier.fillMaxWidth()) {
                                Icon(imageVector = MiuixIcons.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("添加选项")
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        BasicComponent(
                            title = "最多可选",
                            summary = "当前 $voteMaxSelection 项",
                            endActions = {
                                IconButton(onClick = { onVoteMaxSelectionChange((voteMaxSelection - 1).coerceAtLeast(1)) }, minWidth = 36.dp, minHeight = 36.dp) {
                                    Text("-", color = LakeColors.text, fontSize = 18.sp)
                                }
                                IconButton(onClick = { onVoteMaxSelectionChange((voteMaxSelection + 1).coerceAtMost(voteOptions.size.coerceAtLeast(1))) }, minWidth = 36.dp, minHeight = 36.dp) {
                                    Text("+", color = LakeColors.text, fontSize = 18.sp)
                                }
                            },
                        )
                    }
                }
            } else {
                item {
                    ComposeSectionCard(title = "图片") {
                        SelectedImageStrip(images = selectedImages, onPickImages = onPickImages, onRemoveImage = onRemoveImage, maxText = "最多 9 张")
                    }
                }
            }
            item {
                ComposeSectionCard(title = "校区") {
                    WindowDropdownPreference(
                        title = "校区",
                        items = listOf("不区分", "卫津路", "北洋园"),
                        selectedIndex = campus.coerceIn(0, 2),
                        onSelectedIndexChange = onCampusChange,
                    )
                }
            }
        }
    } else {
        val listState = rememberLazyListState()
        val scrollBehavior = MiuixScrollBehavior()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = "发布新帖",
                    largeTitle = "发布新帖",
                    color = LakeColors.background,
                    titleColor = LakeColors.text,
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = "返回",
                                tint = LakeColors.text,
                            )
                        }
                    },
                    actions = {
                        Button(onClick = onSubmit, enabled = !ui.creatingPost) {
                            Text(if (ui.creatingPost) "发布中" else "发布")
                        }
                    },
                )
            },
            containerColor = LakeColors.background,
            contentWindowInsets = WindowInsets(0.dp),
        ) { innerPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .miuixScroll()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                overscrollEffect = null,
            ) {
                item {
                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                    ) {
                        Text("返回")
                    }
                }
            }
        }
    }
}

@Composable
private fun ComposeSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        insideMargin = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = LakeColors.card,
            contentColor = LakeColors.text,
        ),
    ) {
        Text(title, color = LakeColors.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun DetailPane(post: LakePostUi, floors: List<LakeFloorUi>, officialFloors: List<LakeFloorUi>, commentInput: String, replyFloorId: Long?, replyInput: String, sending: Boolean, floorHasMore: Boolean, loadingMoreFloors: Boolean, refreshing: Boolean, floorOrder: Int, currentUserAvatar: String, currentUserName: String, currentUserUid: Long, onBack: () -> Unit, onLike: () -> Unit, onFav: () -> Unit, onCommentInputChange: (String) -> Unit, onSendComment: () -> Unit, onReplyToggle: (Long) -> Unit, onReplyInputChange: (String) -> Unit, onSendReply: (Long) -> Unit, onFloorLike: (Long) -> Unit, onVote: (List<Long>) -> Unit, onFloorOrderChange: (Int) -> Unit, onExpandReplies: (Long) -> Unit, onLoadMoreFloors: () -> Unit, onRefresh: () -> Unit, onOpenImages: (List<String>, Int) -> Unit, onOpenMpPost: (Long) -> Unit, onOpenUrl: (String) -> Unit, commentImages: List<Uri>, onPickCommentImages: () -> Unit, onRemoveCommentImage: (Uri) -> Unit, replyImages: List<Uri>, onPickReplyImages: () -> Unit, onRemoveReplyImage: (Uri) -> Unit, onReportPost: (() -> Unit)? = null, onDeletePost: (() -> Unit)? = null, onSharePost: (() -> Unit)? = null, onReportFloor: ((Long, Long) -> Unit)? = null, onDeleteFloor: ((Long, Long) -> Unit)? = null, onlyOwner: Boolean = false, onToggleOnlyOwner: (() -> Unit)? = null, onOpenUser: (LakeUserPreview) -> Unit = {}) {
    val listState = rememberLazyListState()
    val scrollBehavior = MiuixScrollBehavior()
    val pullToRefreshState = rememberPullToRefreshState()
    val latestFloorCount by rememberUpdatedState(floors.size)
    val latestFloorHasMore by rememberUpdatedState(floorHasMore)
    val latestLoadingMoreFloors by rememberUpdatedState(loadingMoreFloors)
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { lastVisibleIndex ->
                val loadMoreIndex = (latestFloorCount - 2).coerceAtLeast(0)
                if (lastVisibleIndex >= loadMoreIndex && latestFloorHasMore && !latestLoadingMoreFloors) {
                    onLoadMoreFloors()
                }
            }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = "帖子详情",
                largeTitle = "帖子详情",
                color = LakeColors.background,
                titleColor = LakeColors.text,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                            tint = LakeColors.text,
                        )
                    }
                },
                actions = {
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(imageVector = MiuixIcons.More, contentDescription = "更多", tint = LakeColors.text)
                        }
                        top.yukonga.miuix.kmp.window.WindowListPopup(
                            show = menuExpanded,
                            alignment = top.yukonga.miuix.kmp.basic.PopupPositionProvider.Align.End,
                            onDismissRequest = { menuExpanded = false },
                            minWidth = 140.dp,
                        ) {
                            Box(modifier = Modifier.clip(RoundedCornerShape(14.dp))) {
                                top.yukonga.miuix.kmp.basic.ListPopupColumn {
                                    top.yukonga.miuix.kmp.basic.DropdownImpl("分享", optionSize = 1, isSelected = false, index = 0) { menuExpanded = false; onSharePost?.invoke() }
                                    top.yukonga.miuix.kmp.basic.DropdownImpl("举报", optionSize = 1, isSelected = false, index = 1) { menuExpanded = false; onReportPost?.invoke() }
                                    if (post.isOwner) {
                                        top.yukonga.miuix.kmp.basic.DropdownImpl("删除", optionSize = 1, isSelected = false, index = 2) { menuExpanded = false; onDeletePost?.invoke() }
                                    }
                                }
                            }
                        }
                    }
                },
            )
        },
        containerColor = LakeColors.background,
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PullToRefresh(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                modifier = Modifier.weight(1f),
                pullToRefreshState = pullToRefreshState,
                contentPadding = PaddingValues(),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .miuixScroll()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    overscrollEffect = null,
                ) {
                item { DetailPostCard(post = post, onOpenImages = onOpenImages, onVote = onVote, onOpenMpPost = onOpenMpPost, onOpenUrl = onOpenUrl, onOpenUser = onOpenUser) }
                if (post.type == 1 && officialFloors.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 2.dp),
                            cornerRadius = 18.dp,
                            insideMargin = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                                color = LakeColors.card,
                                contentColor = LakeColors.text,
                            ),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                MiniTag("校务回复")
                                Text("${officialFloors.size} 条", color = LakeColors.muted, fontSize = 12.sp)
                            }
                        }
                    }
                    items(officialFloors, key = { "official_${it.id}" }) { floor ->
                        FloorCard(
                            floor = floor,
                            replyFloorId = replyFloorId,
                            replyInput = replyInput,
                            sending = sending,
                            onReplyToggle = onReplyToggle,
                            onReplyInputChange = onReplyInputChange,
                            onSendReply = onSendReply,
                            onFloorLike = onFloorLike,
                            onExpandReplies = onExpandReplies,
                            onOpenImages = onOpenImages,
                            replyImages = replyImages,
                            onPickReplyImages = onPickReplyImages,
                            onRemoveReplyImage = onRemoveReplyImage,
                            postAuthorUid = post.uid,
                            currentUserUid = currentUserUid,
                            canDeleteAllFloors = false,
                            onReportFloor = onReportFloor?.let { f -> { floorId -> f(post.id, floorId) } },
                            onDeleteFloor = onDeleteFloor?.let { delete -> { floorId -> delete(floorId, post.id) } },
                            onOpenUser = onOpenUser,
                        )
                    }
                }
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        cornerRadius = 18.dp,
                        insideMargin = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                            color = LakeColors.card,
                            contentColor = LakeColors.text,
                        ),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("全部评论 ${post.comments}", color = LakeColors.text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                FloorSortControl(floorOrder = floorOrder, onFloorOrderChange = onFloorOrderChange)
                            }
                            Text(
                                if (onlyOwner) "只看楼主：开" else "只看楼主",
                                color = if (onlyOwner) LakeColors.primary else LakeColors.muted,
                                fontSize = 12.sp,
                                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onToggleOnlyOwner?.invoke() }.padding(horizontal = 6.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
                items(floors, key = { it.id }) { floor ->
                    FloorCard(
                        floor = floor,
                        replyFloorId = replyFloorId,
                        replyInput = replyInput,
                        sending = sending,
                        onReplyToggle = onReplyToggle,
                        onReplyInputChange = onReplyInputChange,
                        onSendReply = onSendReply,
                        onFloorLike = onFloorLike,
                        onExpandReplies = onExpandReplies,
                        onOpenImages = onOpenImages,
                        replyImages = replyImages,
                        onPickReplyImages = onPickReplyImages,
                        onRemoveReplyImage = onRemoveReplyImage,
                        postAuthorUid = post.uid,
                        currentUserUid = currentUserUid,
                        canDeleteAllFloors = false,
                        onReportFloor = onReportFloor?.let { f -> { floorId -> f(post.id, floorId) } },
                        onDeleteFloor = onDeleteFloor?.let { delete -> { floorId -> delete(floorId, post.id) } },
                        onOpenUser = onOpenUser,
                    )
                }
                item {
                    Text(
                        if (loadingMoreFloors) "评论加载中..." else if (floorHasMore) "继续下滑加载更多评论" else "评论到底了",
                        color = LakeColors.muted,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            }
            DetailCommentBar(
                commentInput = commentInput,
                sending = sending,
                post = post,
                currentUserAvatar = currentUserAvatar,
                currentUserName = currentUserName,
                commentImages = commentImages,
                onCommentInputChange = onCommentInputChange,
                onPickCommentImages = onPickCommentImages,
                onRemoveCommentImage = onRemoveCommentImage,
                onSendComment = onSendComment,
                onFav = onFav,
                onLike = onLike,
            )
        }
    }
}

@Composable
private fun LakeUserPreviewSheet(user: LakeUserPreview, onDismiss: () -> Unit) {
    WindowBottomSheet(
        show = true,
        title = "用户详情",
        onDismissRequest = onDismiss,
        sheetMaxWidth = 520.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AvatarImage(avatar = user.avatar, author = user.nickname, size = 72.dp)
            Text(
                user.nickname.ifBlank { "匿名用户" },
                color = LakeColors.text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                insideMargin = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                    color = LakeColors.cardAlt,
                    contentColor = LakeColors.text,
                ),
            ) {
                BasicComponent(
                    title = "UID",
                    summary = user.uid.takeIf { it > 0 }?.toString() ?: "未知",
                )
                BasicComponent(
                    title = "昵称",
                    summary = user.nickname.ifBlank { "匿名用户" },
                )
            }
        }
    }
}

@Composable
private fun DetailLoadingPane(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = "帖子详情",
                largeTitle = "帖子详情",
                color = LakeColors.background,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                            tint = LakeColors.text,
                        )
                    }
                },
            )
        },
        containerColor = LakeColors.background,
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text("帖子加载中...", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
    }
}

@Composable
private fun DetailCommentBar(commentInput: String, sending: Boolean, post: LakePostUi, currentUserAvatar: String, currentUserName: String, commentImages: List<Uri>, onCommentInputChange: (String) -> Unit, onPickCommentImages: () -> Unit, onRemoveCommentImage: (Uri) -> Unit, onSendComment: () -> Unit, onFav: () -> Unit, onLike: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val activeInput = focused || commentInput.isNotBlank() || commentImages.isNotEmpty()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LakeColors.card)
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        if (commentImages.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                items(commentImages) { uri ->
                    Box {
                        LocalUriImage(uri = uri, modifier = Modifier.size(42.dp).clip(RoundedCornerShape(8.dp)))
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                                .clickable { onRemoveCommentImage(uri) }
                                .padding(horizontal = 5.dp, vertical = 1.dp),
                        ) { Text("×", color = Color.White, fontSize = 10.sp) }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AvatarImage(avatar = currentUserAvatar, author = currentUserName.ifBlank { "我" }, size = 34.dp)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(LakeColors.cardAlt)
                    .padding(start = 6.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = commentInput,
                    onValueChange = onCommentInputChange,
                    label = "说说你的想法...",
                    backgroundColor = Color.Transparent,
                    borderColor = Color.Transparent,
                    cornerRadius = 0.dp,
                    useLabelAsPlaceholder = true,
                    minLines = 1,
                    maxLines = 3,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focused = it.isFocused },
                )
                AnimatedVisibility(visible = activeInput, enter = fadeIn(tween(120)), exit = fadeOut(tween(120))) {
                    Icon(
                        imageVector = MiuixIcons.MiuixImageIcon,
                        contentDescription = "添加图片",
                        tint = LakeColors.muted,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onPickCommentImages)
                            .padding(5.dp),
                    )
                }
            }
            if (commentInput.isNotBlank() || commentImages.isNotEmpty()) {
                Text(
                    if (sending) "发送中" else "发送",
                    color = LakeColors.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = !sending, onClick = onSendComment)
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                )
            } else {
                BottomBarAction(icon = MiuixIcons.Create, label = "收藏", active = post.isFav, onClick = onFav)
                BottomBarAction(icon = MiuixIcons.Messages, label = post.comments.toString(), onClick = {})
                BottomBarAction(icon = if (post.isLike) MiuixIcons.FavoritesFill else MiuixIcons.Favorites, label = post.likes.toString(), active = post.isLike, activeColor = LakeColors.like, onClick = onLike)
            }
        }
    }
}

@Composable
private fun BottomBarAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, active: Boolean = false, activeColor: Color = LakeColors.primary, onClick: () -> Unit) {
    Card(
        cornerRadius = 12.dp,
        insideMargin = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = Color.Transparent,
            contentColor = LakeColors.text,
        ),
        onClick = onClick,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) activeColor else LakeColors.text,
                modifier = Modifier.size(25.dp),
            )
            Text(label, color = if (active) activeColor else LakeColors.muted, fontSize = 11.sp, maxLines = 1)
        }
    }
}

