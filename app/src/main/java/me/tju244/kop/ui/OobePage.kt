package me.tju244.kop.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.tju244.kop.R
import me.tju244.kop.RebuildApplication
import me.tju244.kop.auth.ui.SessionUiState
import me.tju244.kop.tju.ui.TjuViewModel
import me.tju244.kop.tju.ui.TjuViewModelFactory
import me.tju244.kop.ui.effect.BgEffectBackground
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Community
import top.yukonga.miuix.kmp.icon.extended.Create
import top.yukonga.miuix.kmp.icon.extended.Hide
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Promotions
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Show
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun OobePage(
    sessionState: SessionUiState,
    onSidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    themeMode: Int,
    onThemeModeChange: (Int) -> Unit,
    fontMode: Int,
    onFontModeChange: (Int) -> Unit,
    bottomBarLabelMode: Int,
    onBottomBarLabelModeChange: (Int) -> Unit,
    navigationBarMode: Int,
    onNavigationBarModeChange: (Int) -> Unit,
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
    onFinish: () -> Unit,
) {
    var page by remember { mutableIntStateOf(0) }
    var previousPage by remember { mutableIntStateOf(0) }
    val direction = if (page >= previousPage) 1 else -1
    val backdrop = rememberLayerBackdrop()

    BgEffectBackground(
        dynamicBackground = true,
        modifier = Modifier.fillMaxSize(),
        bgModifier = Modifier.layerBackdrop(backdrop),
        effectBackground = true,
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 22.dp)
                    .navigationBarsPadding(),
            ) {
                OobeHeader(page = page)
                AnimatedContent(
                    targetState = page,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    transitionSpec = {
                        (slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { it * direction } + fadeIn(tween(240))) togetherWith
                            (slideOutHorizontally(tween(280, easing = FastOutSlowInEasing)) { -it * direction } + fadeOut(tween(180)))
                    },
                    label = "oobe-page-transition",
                ) { target ->
                    when (target) {
                        0 -> OobeWpyLoginPage(sessionState, onSidChange, onPasswordChange, onLogin)
                        1 -> OobeTjuLoginPage()
                        2 -> OobeAppearancePage(
                            themeMode = themeMode,
                            onThemeModeChange = onThemeModeChange,
                            fontMode = fontMode,
                            onFontModeChange = onFontModeChange,
                            bottomBarLabelMode = bottomBarLabelMode,
                            onBottomBarLabelModeChange = onBottomBarLabelModeChange,
                            navigationBarMode = navigationBarMode,
                            onNavigationBarModeChange = onNavigationBarModeChange,
                            bottomBarGlassEnabled = bottomBarGlassEnabled,
                            onBottomBarGlassEnabledChange = onBottomBarGlassEnabledChange,
                        )
                        3 -> OobeNotificationPage(
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
                        else -> OobeFinishPage()
                    }
                }
                OobeFooter(
                    page = page,
                    canBack = page > 0,
                    onBack = {
                        previousPage = page
                        page = (page - 1).coerceAtLeast(0)
                    },
                    onNext = {
                        if (page >= 4) {
                            onFinish()
                        } else {
                            previousPage = page
                            page += 1
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun OobeHeader(page: Int) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 58.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(
                painter = painterResource(R.drawable.app_icon),
                contentDescription = "北洋之炬",
                modifier = Modifier.size(48.dp),
            )
            Column {
                Text("北洋之炬", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("初始设置", color = MiuixTheme.colorScheme.onSurfaceVariantSummary, fontSize = 13.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .size(width = if (index == page) 22.dp else 7.dp, height = 7.dp)
                        .clip(CircleShape)
                        .background(if (index == page) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.outline.copy(alpha = 0.35f)),
                )
            }
        }
    }
}

@Composable
private fun OobeWpyLoginPage(
    state: SessionUiState,
    onSidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
) {
    OobeScrollablePage(title = "登录北洋之炬账号", subtitle = "用于同步个人信息、论坛与校园服务。") {
        Card(modifier = Modifier.fillMaxWidth()) {
            BasicComponent(
                title = if (state.token.isNotBlank()) "北洋之炬账号已登录" else "账号登录",
                summary = if (state.token.isNotBlank()) "可以继续完成后续设置" else "请输入学号/手机号与密码",
                startAction = {
                    Icon(MiuixIcons.Community, contentDescription = null, tint = MiuixTheme.colorScheme.primary, modifier = Modifier.padding(end = 16.dp))
                },
            )
            if (state.token.isBlank()) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextField(
                        value = state.sidOrPhone,
                        onValueChange = onSidChange,
                        label = "学号/手机号",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().semantics { contentType = androidx.compose.ui.autofill.ContentType.Username },
                    )
                    var passwordVisible by remember { mutableStateOf(false) }
                    TextField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        label = "密码",
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().semantics { contentType = androidx.compose.ui.autofill.ContentType.Password },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) MiuixIcons.Hide else MiuixIcons.Show,
                                    contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        },
                    )
                    if (state.error != null) {
                        Text(state.error, color = MiuixTheme.colorScheme.error, fontSize = 13.sp)
                    }
                    Button(onClick = onLogin, enabled = !state.loading, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        Text(if (state.loading) "登录中..." else "登录北洋之炬")
                    }
                }
            }
        }
    }
}

@Composable
private fun OobeTjuLoginPage() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as RebuildApplication
    val vm: TjuViewModel = viewModel(factory = TjuViewModelFactory(app))
    val ui by vm.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    OobeScrollablePage(title = "登录教务网账号", subtitle = "用于课程表、成绩、考试安排、入校码与教室查询。") {
        Card(modifier = Modifier.fillMaxWidth()) {
            BasicComponent(
                title = if (ui.loggedIn) "教务网账号已绑定" else "绑定教务网账号",
                summary = if (ui.hasCache) "已同步课程 ${ui.courses.size} 门、成绩 ${ui.gpaCourses.size} 条、考试 ${ui.exams.size} 场" else "也可以稍后在设置中绑定",
                startAction = {
                    Icon(MiuixIcons.Months, contentDescription = null, tint = MiuixTheme.colorScheme.primary, modifier = Modifier.padding(end = 16.dp))
                },
            )
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TextField(value = ui.username, onValueChange = vm::onUsernameChange, label = "学号", singleLine = true, modifier = Modifier.fillMaxWidth())
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
                            Icon(if (passwordVisible) MiuixIcons.Hide else MiuixIcons.Show, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    },
                )
                if (ui.error != null) Text(ui.error!!, color = MiuixTheme.colorScheme.error, fontSize = 13.sp)
                Button(onClick = { vm.login() }, enabled = !ui.loading, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text(if (ui.loading) "同步中..." else "绑定并同步")
                }
            }
        }
    }
}

@Composable
private fun OobeAppearancePage(
    themeMode: Int,
    onThemeModeChange: (Int) -> Unit,
    fontMode: Int,
    onFontModeChange: (Int) -> Unit,
    bottomBarLabelMode: Int,
    onBottomBarLabelModeChange: (Int) -> Unit,
    navigationBarMode: Int,
    onNavigationBarModeChange: (Int) -> Unit,
    bottomBarGlassEnabled: Boolean,
    onBottomBarGlassEnabledChange: (Boolean) -> Unit,
) {
    OobeScrollablePage(title = "系统美化性设置", subtitle = "这些设置之后也可以在设置页面里随时修改。") {
        Card(modifier = Modifier.fillMaxWidth()) {
            OverlayDropdownPreference(
                title = "主题模式",
                summary = "跟随系统或手动切换白天、夜间",
                items = listOf("跟随系统", "白天", "夜间"),
                selectedIndex = themeMode.coerceIn(0, 2),
                onSelectedIndexChange = onThemeModeChange,
                startAction = { Icon(MiuixIcons.Promotions, contentDescription = null, modifier = Modifier.padding(end = 6.dp)) },
            )
            OverlayDropdownPreference(
                title = "字体",
                summary = if (fontMode == 1) "使用 OPPO Sans 4.0" else "使用系统默认字体",
                items = listOf("系统字体", "内置字体"),
                selectedIndex = fontMode.coerceIn(0, 1),
                onSelectedIndexChange = onFontModeChange,
                startAction = { Icon(MiuixIcons.Create, contentDescription = null, modifier = Modifier.padding(end = 6.dp)) },
            )
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            OverlayDropdownPreference(
                title = "底部导航栏",
                summary = if (navigationBarMode == 0) "固定底栏" else "悬浮底栏",
                items = listOf("固定底栏", "悬浮底栏"),
                selectedIndex = navigationBarMode.coerceIn(0, 1),
                onSelectedIndexChange = onNavigationBarModeChange,
                startAction = { Icon(MiuixIcons.Settings, contentDescription = null, modifier = Modifier.padding(end = 6.dp)) },
            )
            if (navigationBarMode == 1) {
                SwitchPreference(
                    title = "液态玻璃效果",
                    summary = "悬浮底栏专用效果",
                    checked = bottomBarGlassEnabled,
                    onCheckedChange = onBottomBarGlassEnabledChange,
                    startAction = { Icon(MiuixIcons.Promotions, contentDescription = null, modifier = Modifier.padding(end = 6.dp)) },
                )
            }
            OverlayDropdownPreference(
                title = "悬浮栏样式",
                summary = "设置图标展示方式",
                items = listOf("仅图标", "图标和文字"),
                selectedIndex = bottomBarLabelMode.coerceIn(0, 1),
                onSelectedIndexChange = onBottomBarLabelModeChange,
                startAction = { Icon(MiuixIcons.Settings, contentDescription = null, modifier = Modifier.padding(end = 6.dp)) },
            )
        }
    }
}

@Composable
private fun OobeNotificationPage(
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
    OobeScrollablePage(title = "通知与提醒", subtitle = "设置课程提醒、论坛通知和支持设备上的实时展示能力。") {
        Card(modifier = Modifier.fillMaxWidth()) {
            SwitchPreference(
                title = "论坛通知",
                summary = "有新回复、点赞或湖底通知时发送系统通知",
                checked = forumNotificationEnabled,
                onCheckedChange = onForumNotificationEnabledChange,
                startAction = { Icon(MiuixIcons.Community, contentDescription = null, modifier = Modifier.padding(end = 6.dp)) },
            )
            SwitchPreference(
                title = "课程提醒",
                summary = "下一节课开始前 20 分钟提醒",
                checked = courseNotificationEnabled,
                onCheckedChange = onCourseNotificationEnabledChange,
                startAction = { Icon(MiuixIcons.Months, contentDescription = null, modifier = Modifier.padding(end = 6.dp)) },
            )
            if (courseNotificationEnabled) {
                SwitchPreference(
                    title = "课程 Live Update",
                    summary = "支持设备上使用实时提醒样式",
                    checked = courseLiveUpdateEnabled,
                    onCheckedChange = onCourseLiveUpdateEnabledChange,
                    startAction = { Icon(MiuixIcons.Promotions, contentDescription = null, modifier = Modifier.padding(end = 6.dp)) },
                )
            }
        }
        if (courseNotificationEnabled) {
            Card(modifier = Modifier.fillMaxWidth()) {
                SwitchPreference(
                    title = "小米超级岛",
                    summary = "在支持的 HyperOS 设备上随课程提醒展示",
                    checked = miIslandNotificationEnabled,
                    onCheckedChange = onMiIslandNotificationEnabledChange,
                    startAction = { Icon(MiuixIcons.Promotions, contentDescription = null, modifier = Modifier.padding(end = 6.dp)) },
                )
                if (miIslandNotificationEnabled) {
                    OverlayDropdownPreference(
                        title = "显示方案",
                        summary = when (miIslandDisplayMode.coerceIn(0, 3)) {
                            0 -> "课程名在左边，地点在右边"
                            1 -> "课程名在左边，时间在右边"
                            2 -> "课程名在左边，地点和时间在右边"
                            else -> "左边展示图标，课程名在右边"
                        },
                        items = listOf("课程名 / 地点", "课程名 / 时间", "课程名 / 地点时间", "图标 / 课程名"),
                        selectedIndex = miIslandDisplayMode.coerceIn(0, 3),
                        onSelectedIndexChange = onMiIslandDisplayModeChange,
                        startAction = { Icon(MiuixIcons.Promotions, contentDescription = null, modifier = Modifier.padding(end = 6.dp)) },
                    )
                    OverlayDropdownPreference(
                        title = "授权方案",
                        summary = if (miIslandAuthMode == 1) "使用 Shizuku 临时绕过白名单" else "直接发送，适合已由模块绕过白名单的设备",
                        items = listOf("直接发送", "Shizuku"),
                        selectedIndex = miIslandAuthMode.coerceIn(0, 1),
                        onSelectedIndexChange = onMiIslandAuthModeChange,
                        startAction = { Icon(MiuixIcons.Settings, contentDescription = null, modifier = Modifier.padding(end = 6.dp)) },
                    )
                    AnimatedVisibility(visible = miIslandAuthMode == 1) {
                        SwitchPreference(
                            title = "临时绕过限制",
                            summary = "失败时会自动降级为普通通知",
                            checked = miIslandBypassEnabled,
                            onCheckedChange = onMiIslandBypassEnabledChange,
                            startAction = { Icon(MiuixIcons.Settings, contentDescription = null, modifier = Modifier.padding(end = 6.dp)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OobeFinishPage() {
    OobeScrollablePage(title = "设置完毕", subtitle = "欢迎来到北洋之炬。") {
        Card(modifier = Modifier.fillMaxWidth()) {
            BasicComponent(
                title = "准备就绪",
                summary = "现在可以开始使用课程表、求实论坛、成绩、考试安排和更多校园服务。",
                startAction = {
                    Icon(MiuixIcons.Ok, contentDescription = null, tint = Color(0xFF2FD66B), modifier = Modifier.padding(end = 16.dp))
                },
            )
        }
    }
}

@Composable
private fun OobeScrollablePage(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(title, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, fontSize = 15.sp, lineHeight = 22.sp)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun OobeFooter(page: Int, canBack: Boolean, onBack: () -> Unit, onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            Text(if (page == 4) "开始使用" else "继续")
        }
        if (canBack) {
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("返回")
            }
        }
    }
}

