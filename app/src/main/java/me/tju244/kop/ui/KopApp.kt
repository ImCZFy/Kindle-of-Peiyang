package me.tju244.kop.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar
import me.tju244.kop.RebuildApplication
import me.tju244.kop.auth.ui.SessionUiState
import me.tju244.kop.auth.ui.SessionViewModel
import me.tju244.kop.tju.network.TjuExamDto
import me.tju244.kop.tju.ui.TjuViewModel
import me.tju244.kop.tju.ui.TjuViewModelFactory
import me.tju244.kop.ui.liquid.lens
import me.tju244.kop.ui.liquid.vibrancy
import me.tju244.kop.ui.theme.RebuildTheme
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarDisplayMode
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.noiseDither
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class KopTab(val label: String, val icon: ImageVector) {
    Courses("课程表", MiuixIcons.Months),
    Rooms("教室", Icons.Outlined.Apartment),
    Grades("成绩与考试", Icons.Outlined.Assignment),
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
        when (pane) {
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
                            label = tab.label,
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
        val backdrop = rememberLayerBackdrop()
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(backdrop),
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
    when (selectedTab) {
        KopTab.Courses -> KopCoursesHome(
            bottomPadding = bottomPadding,
            onOpenCourseTable = onOpenCourseTable,
        )
        KopTab.Rooms -> StudyRoomPage(onBack = {})
        KopTab.Grades -> KopGradesHome(
            bottomPadding = bottomPadding,
            onOpenExams = onOpenExams,
            onOpenGpa = onOpenGpa,
        )
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

@Composable
private fun FloatingKopNavigationBar(
    selectedTab: KopTab,
    onSelectedTabChange: (KopTab) -> Unit,
    glassEnabled: Boolean,
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(36.dp)
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        FloatingNavigationBar(
            modifier = Modifier
                .then(
                    if (glassEnabled) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { shape },
                            effects = {
                                blur(28f)
                                vibrancy()
                                lens(
                                    refractionHeight = 18f,
                                    refractionAmount = 52f,
                                    depthEffect = true,
                                    chromaticAberration = 0.16f,
                                )
                                noiseDither(0.025f)
                            },
                            highlight = { Highlight.GlassStrokeMiddleLight },
                            onDrawSurface = {
                                drawRoundRect(Color.White.copy(alpha = 0.34f))
                            },
                        )
                    } else {
                        Modifier
                    },
                ),
            color = if (glassEnabled) Color.Transparent else MiuixTheme.colorScheme.surfaceContainer,
            cornerRadius = 36.dp,
            shadowElevation = if (glassEnabled) 0.dp else 1.dp,
            showDivider = glassEnabled,
            defaultWindowInsetsPadding = true,
        ) {
            KopTab.entries.forEach { tab ->
                FloatingNavigationBarItem(
                    selected = selectedTab == tab,
                    onClick = { onSelectedTabChange(tab) },
                    icon = tab.icon,
                    label = tab.label,
                )
            }
        }
    }
}

@Composable
private fun KopCoursesHome(bottomPadding: Dp, onOpenCourseTable: () -> Unit) {
    val app = LocalContext.current.applicationContext as RebuildApplication
    val vm: TjuViewModel = viewModel(factory = TjuViewModelFactory(app))
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val customCoursesJson by app.sessionStore.customCoursesFlow.collectAsStateWithLifecycle(initialValue = "[]")
    val courses = remember(ui.courses, customCoursesJson) { ui.courses + customCoursesJson.decodeCustomCourses() }
    val currentWeek = rememberCurrentTeachingWeek(courses.maxTeachingWeek())
    val todayCourses = remember(courses, currentWeek) {
        courses.activeSlots(currentWeek).filter { it.arrange.weekday == kopTodayWeekday() }
    }
    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = "课程表",
                largeTitle = "课程表",
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
        }
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
