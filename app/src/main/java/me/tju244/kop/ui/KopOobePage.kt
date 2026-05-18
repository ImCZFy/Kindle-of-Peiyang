package me.tju244.kop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.tju244.kop.R
import me.tju244.kop.RebuildApplication
import me.tju244.kop.auth.ui.SessionUiState
import me.tju244.kop.tju.ui.TjuViewModel
import me.tju244.kop.tju.ui.TjuViewModelFactory
import me.tju244.kop.ui.effect.BgEffectBackground
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Hide
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.Promotions
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Show
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun KopOobePage(
    session: SessionUiState,
    onSidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onThemeModeChange: (Int) -> Unit,
    onFontModeChange: (Int) -> Unit,
    onNavigationBarModeChange: (Int) -> Unit,
    onBottomBarGlassEnabledChange: (Boolean) -> Unit,
    onCourseNotificationEnabledChange: (Boolean) -> Unit,
    onCourseLiveUpdateEnabledChange: (Boolean) -> Unit,
    onMiIslandNotificationEnabledChange: (Boolean) -> Unit,
    onFinish: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val backdrop = rememberLayerBackdrop()

    BgEffectBackground(
        dynamicBackground = true,
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background),
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
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(innerPadding)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                OobeHeader(currentPage = pagerState.currentPage)
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = false,
                    modifier = Modifier.weight(1f),
                ) { page ->
                    when (page) {
                        0 -> TwtAccountOobePage(
                            session = session,
                            onSidChange = onSidChange,
                            onPasswordChange = onPasswordChange,
                            onLogin = onLogin,
                        )
                        1 -> TjuAccountOobePage()
                        2 -> OobePreferencePage(
                            session = session,
                            onThemeModeChange = onThemeModeChange,
                            onFontModeChange = onFontModeChange,
                            onNavigationBarModeChange = onNavigationBarModeChange,
                            onBottomBarGlassEnabledChange = onBottomBarGlassEnabledChange,
                            onCourseNotificationEnabledChange = onCourseNotificationEnabledChange,
                            onCourseLiveUpdateEnabledChange = onCourseLiveUpdateEnabledChange,
                            onMiIslandNotificationEnabledChange = onMiIslandNotificationEnabledChange,
                        )
                        else -> OobeFinishPage()
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (pagerState.currentPage > 0) {
                        Button(
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                        ) {
                            Text("上一步")
                        }
                    }
                    Button(
                        onClick = {
                            if (pagerState.currentPage == 3) {
                                onFinish()
                            } else {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            }
                        },
                        modifier = Modifier.weight(if (pagerState.currentPage > 0) 1f else 2f).height(50.dp),
                    ) {
                        Text(if (pagerState.currentPage == 3) "进入北洋之炬" else "继续")
                    }
                }
            }
        }
    }
}

@Composable
private fun OobeHeader(currentPage: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Image(
            painter = painterResource(R.drawable.app_icon),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp)),
        )
        Text(
            text = "北洋之炬",
            color = MiuixTheme.colorScheme.onBackground,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = listOf("个人中心", "教务数据", "偏好设置", "准备完成")[currentPage],
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            fontSize = 15.sp,
        )
    }
}

@Composable
private fun TwtAccountOobePage(
    session: SessionUiState,
    onSidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    OobePageColumn {
        Card(modifier = Modifier.fillMaxWidth()) {
            BasicComponent(
                title = if (session.token.isBlank()) "天外天个人中心账号" else "天外天个人中心账号已登录",
                summary = "用于同步天外天个人中心相关服务；也可以稍后在设置里登录。",
                startAction = {
                    Icon(
                        painter = painterResource(R.drawable.ic_twt_cloud),
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                },
            )
            if (session.error != null) {
                Text(
                    text = session.error,
                    color = MiuixTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextField(
                    value = session.sidOrPhone,
                    onValueChange = onSidChange,
                    label = "学号 / 手机号",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = session.password,
                    onValueChange = onPasswordChange,
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
                    onClick = onLogin,
                    enabled = !session.loading,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Text(if (session.loading) "登录中..." else "登录")
                }
            }
        }
    }
}

@Composable
private fun TjuAccountOobePage() {
    val app = LocalContext.current.applicationContext as RebuildApplication
    val tjuVm: TjuViewModel = viewModel(factory = TjuViewModelFactory(app))
    val tju by tjuVm.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    OobePageColumn {
        Card(modifier = Modifier.fillMaxWidth()) {
            BasicComponent(
                title = if (tju.loggedIn) "教务网账号已绑定" else "教务网账号",
                summary = if (tju.loggedIn) "课程、成绩、考试、教室和入校码数据会自动同步。" else "用于同步课程、成绩、考试、教室和入校码；也可以稍后绑定。",
                startAction = {
                    Icon(
                        imageVector = MiuixIcons.Months,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                },
            )
            if (tju.error != null) {
                Text(
                    text = tju.error!!,
                    color = MiuixTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextField(
                    value = tju.username,
                    onValueChange = tjuVm::onUsernameChange,
                    label = "学号",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = tju.password,
                    onValueChange = tjuVm::onPasswordChange,
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
                    onClick = { tjuVm.login() },
                    enabled = !tju.loading,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Text(if (tju.loading) "绑定中..." else "绑定并同步")
                }
            }
        }
    }
}

@Composable
private fun OobePreferencePage(
    session: SessionUiState,
    onThemeModeChange: (Int) -> Unit,
    onFontModeChange: (Int) -> Unit,
    onNavigationBarModeChange: (Int) -> Unit,
    onBottomBarGlassEnabledChange: (Boolean) -> Unit,
    onCourseNotificationEnabledChange: (Boolean) -> Unit,
    onCourseLiveUpdateEnabledChange: (Boolean) -> Unit,
    onMiIslandNotificationEnabledChange: (Boolean) -> Unit,
) {
    OobePageColumn {
        Card(modifier = Modifier.fillMaxWidth()) {
            OverlayDropdownPreference(
                title = "主题模式",
                summary = "跟随系统或手动切换白天、夜间",
                items = listOf("跟随系统", "白天", "夜间"),
                selectedIndex = session.themeMode.coerceIn(0, 2),
                onSelectedIndexChange = onThemeModeChange,
                startAction = {
                    Icon(MiuixIcons.Settings, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                },
            )
            OverlayDropdownPreference(
                title = "字体",
                summary = if (session.fontMode == 1) "使用 OPPO Sans 4.0" else "使用系统默认字体",
                items = listOf("系统字体", "内置字体"),
                selectedIndex = session.fontMode.coerceIn(0, 1),
                onSelectedIndexChange = onFontModeChange,
                startAction = {
                    Icon(MiuixIcons.Settings, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                },
            )
            OverlayDropdownPreference(
                title = "底部导航栏",
                summary = if (session.navigationBarMode == 1) "悬浮底栏，可开启液态玻璃" else "固定底栏",
                items = listOf("固定底栏", "悬浮底栏"),
                selectedIndex = session.navigationBarMode.coerceIn(0, 1),
                onSelectedIndexChange = onNavigationBarModeChange,
                startAction = {
                    Icon(MiuixIcons.Settings, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                },
            )
            SwitchPreference(
                title = "液态玻璃效果",
                summary = "悬浮底栏启用多层模糊、折射和高光",
                checked = session.bottomBarGlassEnabled,
                onCheckedChange = onBottomBarGlassEnabledChange,
                enabled = session.navigationBarMode == 1,
                startAction = {
                    Icon(MiuixIcons.Settings, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                },
            )
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            SwitchPreference(
                title = "课程提醒",
                summary = "下一节课开始前提醒",
                checked = session.courseNotificationEnabled,
                onCheckedChange = onCourseNotificationEnabledChange,
                startAction = {
                    Icon(MiuixIcons.Promotions, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                },
            )
            SwitchPreference(
                title = "Live Update",
                summary = "支持时使用持续课程提醒样式",
                checked = session.courseLiveUpdateEnabled,
                onCheckedChange = onCourseLiveUpdateEnabledChange,
                enabled = session.courseNotificationEnabled,
                startAction = {
                    Icon(MiuixIcons.Promotions, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                },
            )
            SwitchPreference(
                title = "小米超级岛",
                summary = "支持的设备上展示课程提醒",
                checked = session.miIslandNotificationEnabled,
                onCheckedChange = onMiIslandNotificationEnabledChange,
                enabled = session.courseNotificationEnabled,
                startAction = {
                    Icon(MiuixIcons.Promotions, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                },
            )
        }
    }
}

@Composable
private fun OobeFinishPage() {
    OobePageColumn {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.app_icon),
                contentDescription = "北洋之炬",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(26.dp)),
            )
            Text(
                text = "准备就绪",
                color = MiuixTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "课程表、教室、成绩与考试、入校码会集中在一个更轻的校园工具里。",
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 18.dp),
            )
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            BasicComponent(
                title = "Kindle of Peiyang",
                summary = "北洋之炬，面向天津大学教务场景的独立客户端。",
            )
        }
    }
}

@Composable
private fun OobePageColumn(content: @Composable ColumnScope.() -> Unit) {
    val scrollBehavior = MiuixScrollBehavior()
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .miuixScroll()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(top = 28.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        overscrollEffect = null,
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}
