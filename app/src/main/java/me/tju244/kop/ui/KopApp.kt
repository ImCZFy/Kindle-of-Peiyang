package me.tju244.kop.ui

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as rememberKyantLayerBackdrop
import com.kyant.backdrop.drawBackdrop as kyantDrawBackdrop
import com.kyant.backdrop.effects.blur as kyantBlur
import com.kyant.backdrop.effects.lens as kyantLens
import com.kyant.backdrop.effects.vibrancy as kyantVibrancy
import com.kyant.backdrop.highlight.Highlight as KyantHighlight
import com.kyant.backdrop.shadow.InnerShadow as KyantInnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import me.tju244.kop.RebuildApplication
import me.tju244.kop.auth.ui.SessionUiState
import me.tju244.kop.auth.ui.SessionViewModel
import me.tju244.kop.tju.network.TjuExamDto
import me.tju244.kop.tju.ui.TjuViewModel
import me.tju244.kop.tju.ui.TjuViewModelFactory
import me.tju244.kop.ui.theme.LocalAppDarkMode
import me.tju244.kop.ui.theme.RebuildTheme
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarDisplayMode
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class KopTab(val label: String, val icon: ImageVector) {
    Courses("课程", MiuixIcons.Months),
    Rooms("教室", Icons.Outlined.Apartment),
    EntryQr("入校码", Icons.Outlined.QrCode2),
    Settings("设置", MiuixIcons.Settings),
}

private enum class KopPane {
    Root,
    CourseTable,
    Exams,
    Gpa,
    AccountSettings,
    NotificationSettings,
    About,
    TjuDebug,
}

private fun KopTab.displayLabel(): String = when (this) {
    KopTab.Courses -> "课程"
    KopTab.Rooms -> "教室"
    KopTab.EntryQr -> "入校码"
    KopTab.Settings -> "设置"
}

@Composable
fun KopApp(sessionVm: SessionViewModel = viewModel()) {
    val session by sessionVm.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(KopTab.Courses) }
    var pane by remember { mutableStateOf(KopPane.Root) }

    LaunchedEffect(Unit) {
        AppDeepLinks.consumePending()?.let {
            selectedTab = KopTab.Courses
            pane = KopPane.CourseTable
        }
        AppDeepLinks.events.collect { action ->
            if (action == AppDeepLinks.ACTION_OPEN_COURSES) {
                selectedTab = KopTab.Courses
                pane = KopPane.CourseTable
            }
        }
    }

    BackHandler(enabled = pane != KopPane.Root) { pane = KopPane.Root }

    RebuildTheme(themeMode = session.themeMode, fontMode = session.fontMode) {
        if (!session.initialized) {
            KopBootSplash()
        } else if (!session.oobeCompleted) {
            KopOobePage(
                session = session,
                onSidChange = sessionVm::onSidChanged,
                onPasswordChange = sessionVm::onPasswordChanged,
                onLogin = sessionVm::login,
                onThemeModeChange = sessionVm::setThemeMode,
                onFontModeChange = sessionVm::setFontMode,
                onNavigationBarModeChange = sessionVm::setNavigationBarMode,
                onBottomBarGlassEnabledChange = sessionVm::setBottomBarGlassEnabled,
                onCourseNotificationEnabledChange = sessionVm::setCourseNotificationEnabled,
                onCourseLiveUpdateEnabledChange = sessionVm::setCourseLiveUpdateEnabled,
                onMiIslandNotificationEnabledChange = sessionVm::setMiIslandNotificationEnabled,
                onFinish = sessionVm::completeOobe,
            )
        } else {
            AnimatedContent(
                targetState = pane,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MiuixTheme.colorScheme.background),
                transitionSpec = {
                    val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    (slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { it * dir } + fadeIn(tween(200))) togetherWith
                        (slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { -it * dir } + fadeOut(tween(150)))
                },
                label = "kop-pane-transition",
            ) { currentPane ->
                when (currentPane) {
                    KopPane.CourseTable -> CourseSchedulePage(onBack = { pane = KopPane.Root })
                    KopPane.Exams -> ExamSchedulePage(onBack = { pane = KopPane.Root })
                    KopPane.Gpa -> GpaPage(onBack = { pane = KopPane.Root })
                    KopPane.AccountSettings -> AccountSettingsPage(
                        session = session,
                        onSidChange = sessionVm::onSidChanged,
                        onPasswordChange = sessionVm::onPasswordChanged,
                        onLogin = sessionVm::login,
                        onLogout = sessionVm::logout,
                        onBack = { pane = KopPane.Root },
                        onOpenDebug = { pane = KopPane.TjuDebug },
                    )
                    KopPane.NotificationSettings -> NotificationSettingsPage(
                        courseNotificationEnabled = session.courseNotificationEnabled,
                        onCourseNotificationEnabledChange = sessionVm::setCourseNotificationEnabled,
                        courseLiveUpdateEnabled = session.courseLiveUpdateEnabled,
                        onCourseLiveUpdateEnabledChange = sessionVm::setCourseLiveUpdateEnabled,
                        miIslandNotificationEnabled = session.miIslandNotificationEnabled,
                        onMiIslandNotificationEnabledChange = sessionVm::setMiIslandNotificationEnabled,
                        miIslandBypassEnabled = session.miIslandBypassEnabled,
                        onMiIslandBypassEnabledChange = sessionVm::setMiIslandBypassEnabled,
                        miIslandAuthMode = session.miIslandAuthMode,
                        onMiIslandAuthModeChange = sessionVm::setMiIslandAuthMode,
                        miIslandDisplayMode = session.miIslandDisplayMode,
                        onMiIslandDisplayModeChange = sessionVm::setMiIslandDisplayMode,
                        onBack = { pane = KopPane.Root },
                    )
                    KopPane.About -> AboutPage(onBack = { pane = KopPane.Root })
                    KopPane.TjuDebug -> TjuDebugPage(onBack = { pane = KopPane.AccountSettings })
                    KopPane.Root -> KopRootShell(
                        selectedTab = selectedTab,
                        onSelectedTabChange = {
                            selectedTab = it
                            pane = KopPane.Root
                        },
                        session = session,
                        sessionVm = sessionVm,
                        onOpenCourseTable = { pane = KopPane.CourseTable },
                        onOpenExams = { pane = KopPane.Exams },
                        onOpenGpa = { pane = KopPane.Gpa },
                        onOpenTjuSettings = { pane = KopPane.AccountSettings },
                        onOpenNotificationSettings = { pane = KopPane.NotificationSettings },
                        onOpenAbout = { pane = KopPane.About },
                    )
                }
            }
        }
    }
}

@Composable
private fun KopBootSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "北洋之炬",
            color = MiuixTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun KopRootShell(
    selectedTab: KopTab,
    onSelectedTabChange: (KopTab) -> Unit,
    session: SessionUiState,
    sessionVm: SessionViewModel,
    onOpenCourseTable: () -> Unit,
    onOpenExams: () -> Unit,
    onOpenGpa: () -> Unit,
    onOpenTjuSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    if (session.navigationBarMode == 0) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    mode = if (session.bottomBarLabelMode == 0) {
                        NavigationBarDisplayMode.IconOnly
                    } else {
                        NavigationBarDisplayMode.IconAndText
                    },
                ) {
                    KopTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { onSelectedTabChange(tab) },
                            icon = tab.icon,
                            label = tab.displayLabel(),
                        )
                    }
                }
            },
            contentWindowInsets = WindowInsets(0.dp),
        ) { innerPadding ->
            KopRootContent(
                selectedTab = selectedTab,
                bottomPadding = innerPadding.calculateBottomPadding(),
                session = session,
                sessionVm = sessionVm,
                onOpenCourseTable = onOpenCourseTable,
                onOpenExams = onOpenExams,
                onOpenGpa = onOpenGpa,
                onOpenTjuSettings = onOpenTjuSettings,
                onOpenNotificationSettings = onOpenNotificationSettings,
                onOpenAbout = onOpenAbout,
            )
        }
    } else {
        val backdrop = rememberKyantLayerBackdrop()
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (session.bottomBarGlassEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.kyantLayerBackdrop(backdrop)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                KopRootContent(
                    selectedTab = selectedTab,
                    bottomPadding = 116.dp,
                    session = session,
                    sessionVm = sessionVm,
                    onOpenCourseTable = onOpenCourseTable,
                    onOpenExams = onOpenExams,
                    onOpenGpa = onOpenGpa,
                    onOpenTjuSettings = onOpenTjuSettings,
                    onOpenNotificationSettings = onOpenNotificationSettings,
                    onOpenAbout = onOpenAbout,
                )
            }
            FloatingKopNavigationBar(
                selectedTab = selectedTab,
                onSelectedTabChange = onSelectedTabChange,
                glassEnabled = session.bottomBarGlassEnabled,
                showLabels = session.bottomBarLabelMode == 1,
                backdrop = backdrop,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun KopRootContent(
    selectedTab: KopTab,
    bottomPadding: Dp,
    session: SessionUiState,
    sessionVm: SessionViewModel,
    onOpenCourseTable: () -> Unit,
    onOpenExams: () -> Unit,
    onOpenGpa: () -> Unit,
    onOpenTjuSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    AnimatedContent(
        targetState = selectedTab,
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background),
        transitionSpec = {
            val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
            (slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it * dir } + fadeIn(tween(220))) togetherWith
                (slideOutHorizontally(tween(280, easing = FastOutSlowInEasing)) { -it * dir } + fadeOut(tween(170)))
        },
        label = "kop-tab-transition",
    ) { tab ->
        when (tab) {
            KopTab.Courses -> KopCoursesHome(
                bottomPadding = bottomPadding,
                onOpenCourseTable = onOpenCourseTable,
                onOpenExams = onOpenExams,
                onOpenGpa = onOpenGpa,
            )
            KopTab.Rooms -> StudyRoomPage(onBack = {})
            KopTab.EntryQr -> EntryQrPage(onBack = {})
            KopTab.Settings -> SettingsPage(
                themeMode = session.themeMode,
                onThemeModeChange = sessionVm::setThemeMode,
                fontMode = session.fontMode,
                onFontModeChange = sessionVm::setFontMode,
                bottomBarLabelMode = session.bottomBarLabelMode,
                onBottomBarLabelModeChange = sessionVm::setBottomBarLabelMode,
                navigationBarMode = session.navigationBarMode,
                onNavigationBarModeChange = sessionVm::setNavigationBarMode,
                tabletRailPosition = session.tabletRailPosition,
                onTabletRailPositionChange = sessionVm::setTabletRailPosition,
                bottomBarGlassEnabled = session.bottomBarGlassEnabled,
                onBottomBarGlassEnabledChange = sessionVm::setBottomBarGlassEnabled,
                onBack = {},
                onOpenTjuSettings = onOpenTjuSettings,
                onOpenNotificationSettings = onOpenNotificationSettings,
                onOpenAbout = onOpenAbout,
            )
        }
    }
}

@Composable
private fun FloatingKopNavigationBar(
    selectedTab: KopTab,
    onSelectedTabChange: (KopTab) -> Unit,
    glassEnabled: Boolean,
    showLabels: Boolean,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val isLight = !LocalAppDarkMode.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val glassActive = glassEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
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
    val indicatorPosition = remember { Animatable(selectedTab.ordinal.toFloat()) }
    val tabCount = KopTab.entries.size
    val tabWidthPx = if (contentWidthPx > 0f) contentWidthPx / tabCount else 0f
    val pressProgress by animateFloatAsState(
        targetValue = if (dragging) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
        label = "kop-floating-press",
    )
    val animationSpec = tween<Dp>(durationMillis = 360, easing = FastOutSlowInEasing)
    val startPadding by animateDpAsState(28.dp, animationSpec = animationSpec, label = "kop-floating-start")
    val endPadding by animateDpAsState(28.dp, animationSpec = animationSpec, label = "kop-floating-end")
    val itemHeight by animateDpAsState(56.dp, animationSpec = animationSpec, label = "kop-floating-item-height")
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
    LaunchedEffect(selectedTab.ordinal, dragging) {
        if (!dragging) {
            indicatorPosition.animateTo(
                selectedTab.ordinal.toFloat(),
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            )
        }
    }
    val indicatorIndex by remember(selectedTab, dragging, dragOffsetPx, tabWidthPx) {
        derivedStateOf {
            if (dragging && tabWidthPx > 0f) {
                (selectedTab.ordinal.toFloat() + dragOffsetPx / tabWidthPx).coerceIn(0f, (tabCount - 1).toFloat())
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
            .pointerInput(selectedTab, tabWidthPx, tabCount) {
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
                            stretchAnim.animateTo(0f, animationSpec = spring(dampingRatio = 0.58f, stiffness = 300f))
                            offsetAnimation.animateTo(0f, spring(dampingRatio = 0.58f, stiffness = 300f))
                        }
                        KopTab.entries.getOrNull(targetIndex)?.let { target ->
                            scope.launch {
                                indicatorPosition.stop()
                                indicatorPosition.snapTo(endIndex)
                                indicatorPosition.animateTo(
                                    target.ordinal.toFloat(),
                                    animationSpec = spring(dampingRatio = 0.72f, stiffness = 560f),
                                )
                            }
                            if (target != selectedTab) onSelectedTabChange(target)
                        }
                    },
                    onDragCancel = {
                        dragging = false
                        dragOffsetPx = 0f
                        scope.launch {
                            stretchAnim.animateTo(0f, animationSpec = spring(dampingRatio = 0.58f, stiffness = 360f))
                            offsetAnimation.animateTo(0f, spring(dampingRatio = 0.58f, stiffness = 300f))
                        }
                        scope.launch {
                            indicatorPosition.stop()
                            indicatorPosition.animateTo(
                                selectedTab.ordinal.toFloat(),
                                animationSpec = spring(dampingRatio = 0.72f, stiffness = 560f),
                            )
                        }
                    },
                ) { change, dragAmount ->
                    if (tabWidthPx > 0f) {
                        val minOffset = -selectedTab.ordinal * tabWidthPx
                        val maxOffset = (tabCount - 1 - selectedTab.ordinal) * tabWidthPx
                        dragOffsetPx = (dragOffsetPx + dragAmount).coerceIn(minOffset, maxOffset)
                        val stretchTarget = (stretchAnim.value * 0.68f + abs(dragAmount) * 0.62f)
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
            KopTab.entries.forEach { tab ->
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
                            onSelectedTabChange(tab)
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
                        imageVector = tab.icon,
                        contentDescription = tab.displayLabel(),
                        tint = tint,
                        modifier = Modifier.size(24.dp),
                    )
                    AnimatedVisibility(visible = showLabels) {
                        Text(
                            text = tab.displayLabel(),
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
                    contentWidthPx = totalWidthPx - with(density) { 8.dp.toPx() }
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
                            highlight = { KyantHighlight.Default.copy(alpha = 1f) },
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
                            highlight = { KyantHighlight.Default.copy(alpha = pressProgress) },
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
                                highlight = { KyantHighlight.Default.copy(alpha = pressProgress) },
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
private fun KopCoursesHome(
    bottomPadding: Dp,
    onOpenCourseTable: () -> Unit,
    onOpenExams: () -> Unit,
    onOpenGpa: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as RebuildApplication
    val vm: TjuViewModel = viewModel(factory = TjuViewModelFactory(app))
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val customCoursesJson by app.sessionStore.customCoursesFlow.collectAsStateWithLifecycle(initialValue = "[]")
    val courses = remember(ui.courses, customCoursesJson) { ui.courses + customCoursesJson.decodeCustomCourses() }
    val currentWeek = rememberCurrentTeachingWeek(courses.maxTeachingWeek())
    val exams = remember(ui.exams) { ui.exams.sortedForKop().take(3) }
    val nowMillis by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(30_000L)
        }
    }
    val todayCourses = remember(courses, currentWeek, nowMillis) {
        courses.activeSlots(currentWeek)
            .filter { it.arrange.weekday == kopTodayWeekday() }
            .filter { it.homeStatus(nowMillis) != HomeCourseStatus.Finished }
            .sortedBy { it.arrange.unitList.minOrNull() ?: Int.MAX_VALUE }
    }
    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = "课程",
                largeTitle = "课程",
                color = Color.Transparent,
                titleColor = MiuixTheme.colorScheme.onBackground,
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = MiuixTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.background)
                .miuixScroll()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = bottomPadding + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            overscrollEffect = null,
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    if (todayCourses.isEmpty()) {
                        BasicComponent(title = "今日无课", summary = "可以安心安排自己的时间")
                    } else {
                        todayCourses.forEach { slot ->
                            val status = slot.homeStatus(nowMillis)
                            BasicComponent(
                                title = slot.title,
                                summary = listOf(slot.displayTime, slot.arrange.location)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" · "),
                                startAction = {
                                    Icon(
                                        MiuixIcons.Months,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 16.dp),
                                    )
                                },
                                endActions = {
                                    Text(
                                        text = status.label,
                                        color = if (status == HomeCourseStatus.Ongoing) Color(0xFF2FD66B) else MiuixTheme.colorScheme.primary,
                                        fontSize = 12.sp,
                                    )
                                },
                            )
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = "完整课程表",
                        summary = "第 $currentWeek 周 · ${courses.activeSlots(currentWeek).size} 个上课安排",
                        startAction = {
                            Icon(
                                MiuixIcons.Months,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        },
                        onClick = onOpenCourseTable,
                    )
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    if (exams.isEmpty()) {
                        BasicComponent(title = "近期暂无考试", summary = "未安排时间地点的考试会在考试安排页展示")
                    } else {
                        exams.forEach { exam ->
                            BasicComponent(
                                title = exam.name.ifBlank { "未命名考试" },
                                summary = listOf(
                                    exam.date.ifBlank { "时间未安排" },
                                    exam.arrange.ifBlank { "场次未安排" },
                                    exam.location.ifBlank { "地点未安排" },
                                ).joinToString(" · "),
                                startAction = {
                                    Icon(
                                        Icons.Outlined.Assignment,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 16.dp),
                                    )
                                },
                            )
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = "考试安排",
                        summary = "查看全部考试与未安排考试",
                        startAction = {
                            Icon(
                                Icons.Outlined.Assignment,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        },
                        onClick = onOpenExams,
                    )
                    BasicComponent(
                        title = "成绩",
                        summary = ui.gpaTotal?.let { "加权 ${it.displayScore} · 绩点 ${it.gpa}" } ?: "查看每学期成绩",
                        startAction = {
                            Icon(
                                MiuixIcons.Contacts,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        },
                        onClick = onOpenGpa,
                    )
                }
            }
        }
    }
}

private enum class HomeCourseStatus(val label: String) {
    Ongoing("正在上课"),
    Upcoming("即将上课"),
    Finished("已上完"),
}

private fun CourseSlot.homeStatus(nowMillis: Long): HomeCourseStatus {
    val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
    val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val start = arrange.unitList.minOrNull()?.let { kopSectionTimeMinutes(it)?.first }
    val end = arrange.unitList.maxOrNull()?.let { kopSectionTimeMinutes(it)?.second }
    return when {
        start == null || end == null -> HomeCourseStatus.Upcoming
        nowMinutes in start until end -> HomeCourseStatus.Ongoing
        nowMinutes >= end -> HomeCourseStatus.Finished
        else -> HomeCourseStatus.Upcoming
    }
}

private fun kopSectionTimeMinutes(section: Int): Pair<Int, Int>? {
    fun minutes(hour: Int, minute: Int) = hour * 60 + minute
    return when (section) {
        1 -> minutes(8, 30) to minutes(9, 15)
        2 -> minutes(9, 20) to minutes(10, 5)
        3 -> minutes(10, 25) to minutes(11, 10)
        4 -> minutes(11, 15) to minutes(12, 0)
        5 -> minutes(13, 30) to minutes(14, 15)
        6 -> minutes(14, 20) to minutes(15, 5)
        7 -> minutes(15, 25) to minutes(16, 10)
        8 -> minutes(16, 15) to minutes(17, 0)
        9 -> minutes(18, 30) to minutes(19, 15)
        10 -> minutes(19, 20) to minutes(20, 5)
        11 -> minutes(20, 10) to minutes(20, 55)
        12 -> minutes(21, 0) to minutes(21, 45)
        else -> null
    }
}

@Composable
private fun KopGradesHome(bottomPadding: Dp, onOpenExams: () -> Unit, onOpenGpa: () -> Unit) {
    val app = LocalContext.current.applicationContext as RebuildApplication
    val vm: TjuViewModel = viewModel(factory = TjuViewModelFactory(app))
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = MiuixScrollBehavior()
    val exams = remember(ui.exams) { ui.exams.sortedForKop().take(3) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "成绩与考试",
                largeTitle = "成绩与考试",
                color = Color.Transparent,
                titleColor = MiuixTheme.colorScheme.onBackground,
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = MiuixTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.background)
                .miuixScroll()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = bottomPadding + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            overscrollEffect = null,
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    if (exams.isEmpty()) {
                        BasicComponent(title = "近期暂无考试", summary = "未安排时间地点的考试会在考试安排页展示")
                    } else {
                        exams.forEach { exam ->
                            BasicComponent(
                                title = exam.name.ifBlank { "未命名考试" },
                                summary = listOf(
                                    exam.date.ifBlank { "时间未安排" },
                                    exam.arrange.ifBlank { "场次未安排" },
                                    exam.location.ifBlank { "地点未安排" },
                                ).joinToString(" · "),
                                startAction = {
                                    Icon(
                                        Icons.Outlined.Assignment,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 16.dp),
                                    )
                                },
                            )
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = "考试安排",
                        summary = "查看全部考试与未安排考试",
                        startAction = {
                            Icon(
                                Icons.Outlined.Assignment,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        },
                        onClick = onOpenExams,
                    )
                    BasicComponent(
                        title = "成绩",
                        summary = ui.gpaTotal?.let { "加权 ${it.displayScore} · 绩点 ${it.gpa}" } ?: "查看每学期成绩",
                        startAction = {
                            Icon(
                                MiuixIcons.Contacts,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        },
                        onClick = onOpenGpa,
                    )
                }
            }
        }
    }
}

private fun List<TjuExamDto>.sortedForKop(): List<TjuExamDto> {
    return sortedWith(
        compareBy<TjuExamDto> { exam ->
            if (exam.date.isBlank()) "9999-99-99" else exam.date
        }.thenBy { it.name },
    )
}

private fun kopTodayWeekday(): Int {
    return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        else -> 7
    }
}
