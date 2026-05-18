package me.tju244.kop.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.tju244.kop.RebuildApplication
import me.tju244.kop.auth.ui.SessionUiState
import me.tju244.kop.tju.ui.TjuViewModel
import me.tju244.kop.tju.ui.TjuViewModelFactory
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
import top.yukonga.miuix.kmp.icon.extended.Send
import top.yukonga.miuix.kmp.icon.extended.Show
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AccountSettingsPage(
    session: SessionUiState,
    onSidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onOpenDebug: () -> Unit,
) {
    BackHandler(enabled = true) { onBack() }
    val context = LocalContext.current
    val app = context.applicationContext as RebuildApplication
    val tjuVm: TjuViewModel = viewModel(factory = TjuViewModelFactory(app))
    val tju by tjuVm.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = MiuixScrollBehavior()
    val listState = rememberLazyListState()
    var tjuPasswordVisible by remember { mutableStateOf(false) }
    var twtPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "账号设置",
                largeTitle = "账号设置",
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
                        title = if (session.token.isBlank()) "天外天个人中心账号" else "天外天个人中心账号已登录",
                        summary = if (session.token.isBlank()) "用于同步天外天个人中心相关服务" else "账号信息已保存在本地",
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Contacts,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            visualTransformation = if (twtPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { twtPasswordVisible = !twtPasswordVisible }) {
                                    Icon(
                                        imageVector = if (twtPasswordVisible) MiuixIcons.Hide else MiuixIcons.Show,
                                        contentDescription = if (twtPasswordVisible) "隐藏" else "显示",
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(
                                onClick = onLogin,
                                enabled = !session.loading,
                                modifier = Modifier.weight(1f).height(48.dp),
                            ) {
                                Text(if (session.loading) "登录中..." else "登录")
                            }
                            Button(
                                onClick = onLogout,
                                enabled = session.token.isNotBlank() && !session.loading,
                                modifier = Modifier.weight(1f).height(48.dp),
                            ) {
                                Text("退出")
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = if (tju.loggedIn) "教务网账号已绑定 ${tju.username}" else "教务网账号",
                        summary = if (tju.hasCache) {
                            "本地已有 ${tju.courses.size} 门课程、${tju.gpaCourses.size} 条成绩、${tju.exams.size} 场考试"
                        } else {
                            "绑定后会同步课程、成绩、考试、入校码和教室相关数据"
                        },
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Months,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = tjuVm::refresh,
                            enabled = tju.loggedIn && !tju.loading,
                            modifier = Modifier.weight(1f).height(52.dp),
                        ) {
                            Text(if (tju.loading) "同步中..." else "立即同步", maxLines = 1)
                        }
                        Button(
                            onClick = tjuVm::logout,
                            enabled = tju.loggedIn && !tju.loading,
                            modifier = Modifier.weight(1f).height(52.dp),
                        ) {
                            Text("解绑并清除", maxLines = 1)
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = "绑定教务网账号",
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
                            visualTransformation = if (tjuPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { tjuPasswordVisible = !tjuPasswordVisible }) {
                                    Icon(
                                        imageVector = if (tjuPasswordVisible) MiuixIcons.Hide else MiuixIcons.Show,
                                        contentDescription = if (tjuPasswordVisible) "隐藏" else "显示",
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

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
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
                        onClick = onOpenDebug,
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
