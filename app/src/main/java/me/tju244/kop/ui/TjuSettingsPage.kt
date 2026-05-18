package me.tju244.kop.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import me.tju244.kop.RebuildApplication
import me.tju244.kop.tju.data.TjuDebugLog
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Hide
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.Promotions
import top.yukonga.miuix.kmp.icon.extended.Send
import top.yukonga.miuix.kmp.icon.extended.Show
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun TjuSettingsPage(onBack: () -> Unit, onOpenDebug: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as RebuildApplication
    val vm: me.tju244.kop.tju.ui.TjuViewModel = viewModel(factory = me.tju244.kop.tju.ui.TjuViewModelFactory(app))
    val ui by vm.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = "教务网账号设置",
                largeTitle = "教务网账号设置",
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            overscrollEffect = null,
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = if (ui.loggedIn) "已绑定 ${ui.username}" else "未绑定教务网账号",
                        summary = if (ui.hasCache) {
                            "本地已有 ${ui.courses.size} 门课程、${ui.gpaCourses.size} 条成绩、${ui.exams.size} 场考试"
                        } else {
                            "绑定后会同步课程、成绩和考试安排"
                        },
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Contacts,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        },
                    )
                    if (ui.error != null) {
                        Text(
                            text = ui.error!!,
                            color = MiuixTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = "本地数据",
                        summary = "数据保存在应用本地存储中，首页会优先展示缓存内容",
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Months,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        },
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = vm::refresh,
                            enabled = ui.loggedIn && !ui.loading,
                            modifier = Modifier.weight(1f).height(52.dp),
                        ) {
                            Text(if (ui.loading) "同步中..." else "立即同步", maxLines = 1)
                        }
                        Button(
                            onClick = vm::logout,
                            enabled = ui.loggedIn && !ui.loading,
                            modifier = Modifier.weight(1f).height(52.dp),
                        ) {
                            Text("解绑并清除", maxLines = 1)
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenDebug),
                ) {
                    BasicComponent(
                        title = "同步调试日志",
                        summary = "实时输出登录、考试、成绩请求和解析步骤",
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Tune,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        },
                    )
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = "绑定账号",
                        summary = "使用天津大学办公网账号密码同步教务数据",
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Send,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        },
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        TextField(
                            value = ui.username,
                            onValueChange = vm::onUsernameChange,
                            label = "学号",
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        TextField(
                            value = ui.password,
                            onValueChange = vm::onPasswordChange,
                            label = "密码",
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) MiuixIcons.Hide else MiuixIcons.Show,
                                        contentDescription = if (passwordVisible) "隐藏" else "显示",
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                        )
                        Button(
                            onClick = { vm.login() },
                            enabled = !ui.loading,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) {
                            Text(if (ui.loading) "绑定中..." else "绑定并同步")
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun TjuDebugPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as RebuildApplication
    val vm: me.tju244.kop.tju.ui.TjuViewModel = viewModel(factory = me.tju244.kop.tju.ui.TjuViewModelFactory(app))
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val logs by TjuDebugLog.lines.collectAsStateWithLifecycle()
    var backProgress by remember { mutableStateOf(0f) }
    val backShiftPx = with(LocalDensity.current) { 38.dp.toPx() }
    val scrollBehavior = MiuixScrollBehavior()
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem((logs.size + 3).coerceAtLeast(0))
        }
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = "同步调试日志",
                largeTitle = "同步调试日志",
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            overscrollEffect = null,
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = if (ui.loading) "正在同步" else "当前缓存",
                        summary = "课程 ${ui.courses.size} · 成绩 ${ui.gpaCourses.size} · 考试 ${ui.exams.size}",
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Months,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        },
                    )
                    if (ui.error != null) {
                        Text(
                            text = ui.error!!,
                            color = MiuixTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = TjuDebugLog::clear,
                            enabled = !ui.loading,
                            modifier = Modifier.weight(1f).height(48.dp),
                        ) {
                            Text("清空日志", maxLines = 1)
                        }
                        Button(
                            onClick = vm::refresh,
                            enabled = ui.loggedIn && !ui.loading,
                            modifier = Modifier.weight(1f).height(48.dp),
                        ) {
                            Text(if (ui.loading) "同步中..." else "重新同步", maxLines = 1)
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = "日志输出",
                        summary = "不会记录密码、token 或 cookie；只保留最近 300 行",
                    )
                }
            }
            if (logs.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        BasicComponent(title = "暂无日志", summary = "点击重新同步后会实时输出每一步状态")
                    }
                }
            } else {
                items(logs) { line ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = line,
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

