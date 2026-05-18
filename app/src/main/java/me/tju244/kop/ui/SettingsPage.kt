package me.tju244.kop.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BackupTable
import androidx.compose.material.icons.rounded.Brightness4
import androidx.compose.material.icons.rounded.CallToAction
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import me.tju244.kop.RebuildApplication
import me.tju244.kop.R
import me.tju244.kop.notification.CourseReminderDebugLog
import me.tju244.kop.notification.CourseReminderPayload
import me.tju244.kop.notification.ExactAlarmPermission
import me.tju244.kop.notification.RebornNotifications
import me.tju244.kop.notification.miui.MiSuperIslandBridge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Community
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Create
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Promotions
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsPage(
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
    onBack: () -> Unit,
    onOpenTjuSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    var backProgress by remember { mutableStateOf(0f) }
    val backShiftPx = with(LocalDensity.current) { 38.dp.toPx() }
    PredictiveBackHandler(enabled = true) { progress ->
        try {
            progress.collect { evt -> backProgress = evt.progress.coerceIn(0f, 1f) }
            onBack()
        } catch (e: CancellationException) {
            throw e
        } finally {
            backProgress = 0f
        }
    }
    BackHandler(enabled = true) { onBack() }

    val scrollBehavior = MiuixScrollBehavior()
    val listState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val showTabletRailPosition = configuration.screenWidthDp >= 840 &&
        configuration.screenWidthDp > configuration.screenHeightDp
    Scaffold(
        topBar = {
            TopAppBar(
                title = "设置",
                largeTitle = "设置",
                color = Color.Transparent,
                titleColor = MiuixTheme.colorScheme.onBackground,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                },
            )
        },
        containerColor = MiuixTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.background)
                .graphicsLayer {
                    val eased = backProgress * backProgress
                    translationX = backShiftPx * eased
                    val scale = 1f - 0.015f * eased
                    scaleX = scale
                    scaleY = scale
                    alpha = 1f - 0.08f * eased
                }
                .miuixScroll()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(18.dp),
            overscrollEffect = null,
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    OverlayDropdownPreference(
                        title = "主题模式",
                        summary = "跟随系统或手动切换白天、夜间",
                        items = listOf("跟随系统", "白天", "夜间"),
                        selectedIndex = themeMode.coerceIn(0, 2),
                        onSelectedIndexChange = onThemeModeChange,
                        startAction = {
                            Icon(
                                imageVector = Icons.Rounded.Brightness4,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 6.dp),
                            )
                        },
                    )
                    OverlayDropdownPreference(
                        title = "字体",
                        summary = if (fontMode == 1) "使用 OPPO Sans 4.0" else "使用系统默认字体",
                        items = listOf("系统字体", "内置字体"),
                        selectedIndex = fontMode.coerceIn(0, 1),
                        onSelectedIndexChange = onFontModeChange,
                        startAction = {
                            Icon(
                                imageVector = Icons.Rounded.FontDownload,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 6.dp),
                            )
                        },
                    )
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    OverlayDropdownPreference(
                        title = "底部导航栏",
                        summary = if (navigationBarMode == 0) "固定在底部，不随页面横向滑动" else "悬浮在内容上方，支持横向滑动切换",
                        items = listOf("固定底栏", "悬浮底栏"),
                        selectedIndex = navigationBarMode.coerceIn(0, 1),
                        onSelectedIndexChange = onNavigationBarModeChange,
                        startAction = {
                            Icon(
                                imageVector = Icons.Outlined.BackupTable,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 6.dp),
                            )
                        },
                    )
                    AnimatedVisibility(visible = navigationBarMode == 1) {
                        SwitchPreference(
                            title = "液态玻璃效果",
                            summary = "悬浮底栏专用的多层玻璃、折射和高光效果",
                            checked = bottomBarGlassEnabled,
                            onCheckedChange = onBottomBarGlassEnabledChange,
                            startAction = {
                                Icon(
                                    imageVector = Icons.Rounded.WaterDrop,
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(end = 6.dp),
                                )
                            },
                        )
                    }
                    OverlayDropdownPreference(
                        title = "悬浮栏样式",
                        summary = "设置图标展示方式",
                        items = listOf("仅图标", "图标和文字"),
                        selectedIndex = bottomBarLabelMode.coerceIn(0, 1),
                        onSelectedIndexChange = onBottomBarLabelModeChange,
                        startAction = {
                            Icon(
                                imageVector = Icons.Rounded.CallToAction,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 6.dp),
                            )
                        },
                    )
                    AnimatedVisibility(visible = showTabletRailPosition) {
                        OverlayDropdownPreference(
                            title = "平板横屏导航位置",
                            summary = if (tabletRailPosition == 0) "横屏平板使用左侧导航栏" else "横屏平板使用右侧导航栏",
                            items = listOf("左侧", "右侧"),
                            selectedIndex = tabletRailPosition.coerceIn(0, 1),
                            onSelectedIndexChange = onTabletRailPositionChange,
                            startAction = {
                                Icon(
                                    imageVector = Icons.Outlined.BackupTable,
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(end = 6.dp),
                                )
                            },
                        )
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    ArrowPreference(
                        title = "通知设置",
                        summary = "课程提醒、Live Update 与超级岛",
                        onClick = onOpenNotificationSettings,
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Promotions,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 6.dp),
                            )
                        },
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    ArrowPreference(
                        title = "教务网账号设置",
                        summary = "绑定天津大学教务网账号以使用课程表、成绩等功能",
                        onClick = onOpenTjuSettings,
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Contacts,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 6.dp),
                            )
                        },
                    )
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    ArrowPreference(
                        title = "关于北洋之炬",
                        summary = "版本信息与项目说明",
                        onClick = onOpenAbout,
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Settings,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 6.dp),
                            )
                        },
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun NotificationSettingsPage(
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
    onBack: () -> Unit,
) {
    BackHandler(enabled = true) { onBack() }
    val context = LocalContext.current
    val app = context.applicationContext as RebuildApplication
    val scope = rememberCoroutineScope()
    val courseReminderLogs by CourseReminderDebugLog.lines.collectAsStateWithLifecycle()
    var shizukuStatus by remember { mutableStateOf(MiSuperIslandBridge.shizukuStatusText()) }
    var exactAlarmStatus by remember { mutableStateOf(ExactAlarmPermission.statusText(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(context, lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                exactAlarmStatus = ExactAlarmPermission.statusText(context)
            }
        }
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = "通知设置",
                largeTitle = "通知设置",
                color = Color.Transparent,
                titleColor = MiuixTheme.colorScheme.onBackground,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(MiuixIcons.Back, contentDescription = "返回", tint = MiuixTheme.colorScheme.onBackground)
                    }
                },
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
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 12.dp, bottom = 24.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(18.dp),
            overscrollEffect = null,
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title = "课程提醒",
                        summary = "下一节课开始前 20 分钟提醒",
                        checked = courseNotificationEnabled,
                        onCheckedChange = onCourseNotificationEnabledChange,
                        startAction = {
                            Icon(MiuixIcons.Months, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                        },
                    )
                    AnimatedVisibility(visible = courseNotificationEnabled) {
                        SwitchPreference(
                            title = "课程 Live Update",
                            summary = "使用更高优先级的持续课程提醒样式",
                            checked = courseLiveUpdateEnabled,
                            onCheckedChange = onCourseLiveUpdateEnabledChange,
                            startAction = {
                                Icon(MiuixIcons.Promotions, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                            },
                        )
                    }
                    AnimatedVisibility(visible = courseNotificationEnabled) {
                        BasicComponent(
                            title = "精确闹钟权限",
                            summary = exactAlarmStatus,
                            startAction = {
                                Icon(MiuixIcons.Promotions, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                            },
                            endActions = {
                                Button(
                                    onClick = {
                                        val intent = ExactAlarmPermission.requestIntent(context)
                                        if (intent != null) {
                                            runCatching { context.startActivity(intent) }
                                        }
                                        exactAlarmStatus = ExactAlarmPermission.statusText(context)
                                    },
                                ) {
                                    top.yukonga.miuix.kmp.basic.Text("授权")
                                }
                            },
                        )
                    }
                }
            }
            if (courseNotificationEnabled) item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title = "小米超级岛",
                        summary = "在支持的系统上用超级岛展示课程提醒",
                        checked = miIslandNotificationEnabled,
                        onCheckedChange = onMiIslandNotificationEnabledChange,
                        startAction = {
                            Icon(MiuixIcons.Promotions, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                        },
                    )
                    AnimatedVisibility(visible = miIslandNotificationEnabled) {
                        OverlayDropdownPreference(
                            title = "显示方案",
                            summary = miIslandDisplayModeLabel(miIslandDisplayMode),
                            items = listOf(
                                "课程名 / 地点",
                                "课程名 / 时间",
                                "课程名 / 地点时间",
                                "图标 / 课程名",
                            ),
                            selectedIndex = miIslandDisplayMode.coerceIn(0, 3),
                            onSelectedIndexChange = onMiIslandDisplayModeChange,
                            startAction = {
                                Icon(MiuixIcons.Promotions, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                            },
                        )
                    }
                    AnimatedVisibility(visible = miIslandNotificationEnabled) {
                        MiIslandPreview(displayMode = miIslandDisplayMode)
                    }
                    AnimatedVisibility(visible = miIslandNotificationEnabled) {
                        OverlayDropdownPreference(
                            title = "授权方案",
                            summary = if (miIslandAuthMode == 1) "使用 Shizuku 临时绕过白名单" else "直接发送，适合已由模块绕过白名单的设备",
                            items = listOf("直接发送", "Shizuku"),
                            selectedIndex = miIslandAuthMode.coerceIn(0, 1),
                            onSelectedIndexChange = onMiIslandAuthModeChange,
                            startAction = {
                                Icon(MiuixIcons.Settings, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                            },
                        )
                    }
                    AnimatedVisibility(visible = miIslandNotificationEnabled && miIslandAuthMode == 1) {
                        BasicComponent(
                            title = "Shizuku 授权",
                            summary = shizukuStatus,
                            startAction = {
                                Icon(MiuixIcons.Settings, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                            },
                            endActions = {
                                Button(
                                    onClick = {
                                        MiSuperIslandBridge.requestShizukuPermission()
                                        shizukuStatus = MiSuperIslandBridge.shizukuStatusText()
                                    },
                                ) {
                                    top.yukonga.miuix.kmp.basic.Text("授权")
                                }
                            },
                        )
                    }
                    AnimatedVisibility(visible = miIslandNotificationEnabled && miIslandAuthMode == 1) {
                        SwitchPreference(
                            title = "临时绕过限制",
                            summary = "需要设备具备可执行授权，失败时自动降级为普通通知",
                            checked = miIslandBypassEnabled,
                            onCheckedChange = onMiIslandBypassEnabledChange,
                            startAction = {
                                Icon(MiuixIcons.Settings, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                            },
                        )
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = "发送测试课程提醒",
                        summary = "用于检查普通通知、Live Update 与小米超级岛展示效果",
                        startAction = {
                            Icon(MiuixIcons.Promotions, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                        },
                        endActions = {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val now = System.currentTimeMillis()
                                        val startAt = now + 20 * 60 * 1000L
                                        val startTime = SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(startAt))
                                        RebornNotifications.notifyCourseReminder(
                                            context = context,
                                            payload = CourseReminderPayload(
                                                courseName = "测试课程提醒",
                                                location = "46楼 A303",
                                                timeRange = startTime,
                                                teacher = "任课教师",
                                                startAtMillis = startAt,
                                            ),
                                            liveUpdateEnabled = app.sessionStore.courseLiveUpdateEnabledFlow.first(),
                                            miIslandEnabled = app.sessionStore.miIslandNotificationEnabledFlow.first(),
                                            miIslandBypassEnabled = app.sessionStore.miIslandBypassEnabledFlow.first(),
                                            miIslandAuthMode = app.sessionStore.miIslandAuthModeFlow.first(),
                                            miIslandDisplayMode = app.sessionStore.miIslandDisplayModeFlow.first(),
                                        )
                                        shizukuStatus = MiSuperIslandBridge.shizukuStatusText()
                                    }
                                },
                            ) {
                                top.yukonga.miuix.kmp.basic.Text("测试")
                            }
                        },
                    )
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = "课程提醒调度日志",
                        summary = "查看下一节课提醒的读取、选择、调度与触发流程",
                        startAction = {
                            Icon(MiuixIcons.Months, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                        },
                        endActions = {
                            Button(onClick = CourseReminderDebugLog::clear) {
                                top.yukonga.miuix.kmp.basic.Text("清空")
                            }
                        },
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (courseReminderLogs.isEmpty()) {
                            top.yukonga.miuix.kmp.basic.Text(
                                "暂无调度日志",
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                fontSize = 13.sp,
                            )
                        } else {
                            courseReminderLogs.takeLast(80).forEach { line ->
                                top.yukonga.miuix.kmp.basic.Text(
                                    line,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    fontSize = 12.sp,
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
private fun MiIslandPreview(displayMode: Int) {
    val leftText = if (displayMode == 3) "" else "高等数学"
    val rightText = when (displayMode.coerceIn(0, 3)) {
        0 -> "46楼 A303"
        1 -> "08:30"
        2 -> "46楼 A303 · 08:30"
        else -> "高等数学"
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MiuixTheme.colorScheme.surface, RoundedCornerShape(28.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (displayMode == 3) {
                Image(
                    painter = painterResource(R.drawable.tju_badge_tp),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            } else {
                top.yukonga.miuix.kmp.basic.Text(
                    leftText,
                    color = MiuixTheme.colorScheme.onBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
            }
            top.yukonga.miuix.kmp.basic.Text(
                rightText,
                color = MiuixTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = if (displayMode == 3) Modifier.weight(1f) else Modifier,
            )
            Box(
                modifier = Modifier
                    .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(MiuixIcons.Ok, contentDescription = null, tint = MiuixTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    top.yukonga.miuix.kmp.basic.Text("已读", color = MiuixTheme.colorScheme.primary, fontSize = 12.sp)
                }
            }
        }
    }
}

private fun miIslandDisplayModeLabel(mode: Int): String = when (mode.coerceIn(0, 3)) {
    0 -> "课程名在左边，地点在右边"
    1 -> "课程名在左边，时间在右边"
    2 -> "课程名在左边，地点和时间在右边"
    else -> "课程名在右边，左边仅展示图标"
}

