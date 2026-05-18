package me.tju244.kop.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import me.tju244.kop.RebuildApplication
import me.tju244.kop.tju.network.TjuExamDto
import me.tju244.kop.tju.ui.TjuViewModel
import me.tju244.kop.tju.ui.TjuViewModelFactory
import me.tju244.kop.ui.theme.LocalAppDarkMode
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
import top.yukonga.miuix.kmp.icon.extended.Community
import top.yukonga.miuix.kmp.icon.extended.Create
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.Hide
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.NotesFill
import top.yukonga.miuix.kmp.icon.extended.Promotions
import top.yukonga.miuix.kmp.icon.extended.Show
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet

@Composable
fun HomePage(
    onOpenCourses: () -> Unit,
    onOpenGpa: () -> Unit,
    onOpenExams: () -> Unit,
    onOpenEntryQr: () -> Unit,
    onOpenStudyRoom: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as RebuildApplication
    val vm: TjuViewModel = viewModel(factory = TjuViewModelFactory(app))
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val customCoursesJson by app.sessionStore.customCoursesFlow.collectAsStateWithLifecycle(initialValue = "[]")
    val courses = remember(ui.courses, customCoursesJson) { ui.courses + customCoursesJson.decodeCustomCourses() }
    var minuteTick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            delay((60_000L - now % 60_000L).coerceAtLeast(1_000L))
            minuteTick++
        }
    }
    val currentWeek = rememberCurrentTeachingWeek(courses.maxTeachingWeek())
    val todaySlots = remember(courses, currentWeek, minuteTick) {
        courses.activeSlots(currentWeek)
            .filter { it.arrange.weekday == todayWeekday() }
            .filter { it.todayStatus() != TodayCourseStatus.Finished }
            .take(3)
    }
    val scrollBehavior = MiuixScrollBehavior()
    var showTjuLoginSheet by remember { mutableStateOf(false) }
    var pendingTjuTarget by remember { mutableStateOf<HomeTjuTarget?>(null) }
    val hasTjuCredentials = ui.username.isNotBlank() && ui.password.isNotBlank()
    fun openTjuTarget(target: HomeTjuTarget) {
        when (target) {
            HomeTjuTarget.Courses -> onOpenCourses()
            HomeTjuTarget.Gpa -> onOpenGpa()
            HomeTjuTarget.Exams -> onOpenExams()
            HomeTjuTarget.EntryQr -> onOpenEntryQr()
        }
    }
    fun syncOrLogin(target: HomeTjuTarget) {
        if (ui.loading) return
        if (hasTjuCredentials) {
            vm.refresh()
        } else {
            pendingTjuTarget = target
            showTjuLoginSheet = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "首页",
                largeTitle = "首页",
                color = if (LocalAppDarkMode.current) Color.Transparent else MiuixTheme.colorScheme.surface,
                titleColor = MiuixTheme.colorScheme.onBackground,
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.background)
                .miuixScroll()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            overscrollEffect = null,
        ) {
            if (todaySlots.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        todaySlots.forEach { slot -> TodayCourseRow(slot = slot) }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    if (courses.isNotEmpty()) {
                        BasicComponent(
                            title = "课表",
                            summary = "第 ${currentWeek} 周 · ${courses.activeSlots(currentWeek).size} 个上课安排",
                            startAction = {
                                Icon(imageVector = MiuixIcons.Months, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 16.dp))
                            },
                            onClick = onOpenCourses,
                        )
                    } else {
                        BasicComponent(
                            title = "课表",
                            summary = missingTjuSummary(ui.loading, "点击绑定或重新同步课程数据"),
                            startAction = {
                                Icon(imageVector = MiuixIcons.Months, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 16.dp))
                            },
                            onClick = { syncOrLogin(HomeTjuTarget.Courses) },
                        )
                    }
                    if (ui.gpaStats.isNotEmpty() || ui.gpaCourses.isNotEmpty()) {
                        BasicComponent(
                            title = "成绩",
                            summary = if (ui.gpaTotal != null) "加权 ${ui.gpaTotal!!.displayScore} · 绩点 ${ui.gpaTotal!!.gpa} · 学分 ${ui.gpaTotal!!.displayCredit}" else "查看每学期成绩",
                            startAction = {
                                Icon(imageVector = MiuixIcons.NotesFill, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 16.dp))
                            },
                            onClick = onOpenGpa,
                        )
                    } else {
                        BasicComponent(
                            title = "成绩",
                            summary = missingTjuSummary(ui.loading, "暂无成绩数据，点击重新同步"),
                            startAction = {
                                Icon(imageVector = MiuixIcons.NotesFill, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 16.dp))
                            },
                            onClick = { syncOrLogin(HomeTjuTarget.Gpa) },
                        )
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    if (ui.exams.isNotEmpty()) {
                        BasicComponent(
                            title = "考试安排",
                            summary = "近期 ${ui.exams.size} 场考试",
                            startAction = { Icon(imageVector = Icons.Outlined.Assignment, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 16.dp)) },
                            onClick = onOpenExams,
                        )
                        ui.exams.take(3).forEach { exam -> ExamRow(exam = exam, onClick = onOpenExams) }
                    } else if (ui.loggedIn) {
                        BasicComponent(
                            title = "考试安排",
                            summary = missingTjuSummary(ui.loading, "近期暂无考试，点击重新同步"),
                            startAction = { Icon(imageVector = Icons.Outlined.Assignment, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 16.dp)) },
                            onClick = { syncOrLogin(HomeTjuTarget.Exams) },
                        )
                    } else {
                        BasicComponent(
                            title = "考试安排",
                            summary = missingTjuSummary(ui.loading, "点击绑定或重新同步考试安排"),
                            startAction = { Icon(imageVector = Icons.Outlined.Assignment, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 16.dp)) },
                            onClick = { syncOrLogin(HomeTjuTarget.Exams) },
                        )
                    }
                    BasicComponent(
                        title = "入校码",
                        summary = "融合门户二维码，自动刷新",
                        startAction = { Icon(imageVector = Icons.Outlined.QrCode2, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 16.dp)) },
                        onClick = {
                            if (hasTjuCredentials || ui.loggedIn) {
                                onOpenEntryQr()
                            } else {
                                pendingTjuTarget = HomeTjuTarget.EntryQr
                                showTjuLoginSheet = true
                            }
                        },
                    )
                    BasicComponent(
                        title = "教室查询",
                        summary = "按校区和教学楼查看空闲教室",
                        startAction = { Icon(imageVector = Icons.Outlined.Apartment, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 16.dp)) },
                        onClick = onOpenStudyRoom,
                    )
                }
            }
        }
    }
    if (showTjuLoginSheet) {
        TjuHomeLoginSheet(
            username = ui.username,
            password = ui.password,
            loading = ui.loading,
            error = ui.error,
            onUsernameChange = vm::onUsernameChange,
            onPasswordChange = vm::onPasswordChange,
            onDismiss = {
                showTjuLoginSheet = false
                pendingTjuTarget = null
            },
            onLogin = {
                vm.login {
                    val target = pendingTjuTarget
                    showTjuLoginSheet = false
                    pendingTjuTarget = null
                    target?.let(::openTjuTarget)
                }
            },
        )
    }
}

private enum class HomeTjuTarget {
    Courses,
    Gpa,
    Exams,
    EntryQr,
}

private fun missingTjuSummary(loading: Boolean, idleText: String): String {
    return if (loading) "正在同步教务数据..." else idleText
}

@Composable
private fun TjuHomeLoginSheet(
    username: String,
    password: String,
    loading: Boolean,
    error: String?,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onLogin: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    WindowBottomSheet(
        show = true,
        title = "绑定教务网账号",
        onDismissRequest = onDismiss,
        sheetMaxWidth = 560.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "用于同步课表、成绩、考试安排和入校码",
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                fontSize = 14.sp,
            )
            TextField(
                value = username,
                onValueChange = onUsernameChange,
                label = "学号",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = password,
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
            if (!error.isNullOrBlank()) {
                Text(text = error, color = MiuixTheme.colorScheme.error, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("取消")
                }
                Button(
                    onClick = onLogin,
                    enabled = !loading && username.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.weight(1f).height(48.dp),
                ) {
                    Text(if (loading) "同步中..." else "绑定并同步")
                }
            }
        }
    }
}

@Composable
private fun TodayCourseRow(slot: CourseSlot) {
    val status = slot.todayStatus()
    BasicComponent(
        title = slot.title,
        summary = listOf(slot.displayTime, slot.arrange.location).filter { it.isNotBlank() }.joinToString(" · "),
        startAction = {
            Icon(
                imageVector = MiuixIcons.Months,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp),
            )
        },
        endActions = {
            Text(
                if (status == TodayCourseStatus.Current) "正在上课" else "即将上课",
                color = if (status == TodayCourseStatus.Current) Color(0xFF2FD66B) else MiuixTheme.colorScheme.primary,
                fontSize = 12.sp,
            )
        },
    )
}

private enum class TodayCourseStatus {
    Current,
    Upcoming,
    Finished,
}

private fun CourseSlot.todayStatus(): TodayCourseStatus {
    val now = Calendar.getInstance()
    val minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val start = arrange.unitList.minOrNull()?.sectionStartMinute() ?: return TodayCourseStatus.Upcoming
    val end = arrange.unitList.maxOrNull()?.sectionEndMinute() ?: start
    return when {
        minutes > end -> TodayCourseStatus.Finished
        minutes in start..end -> TodayCourseStatus.Current
        else -> TodayCourseStatus.Upcoming
    }
}

private fun Int.sectionStartMinute(): Int? = when (this) {
    1 -> 8 * 60 + 30
    2 -> 9 * 60 + 20
    3 -> 10 * 60 + 25
    4 -> 11 * 60 + 15
    5 -> 13 * 60 + 30
    6 -> 14 * 60 + 20
    7 -> 15 * 60 + 25
    8 -> 16 * 60 + 15
    9 -> 18 * 60 + 30
    10 -> 19 * 60 + 20
    11 -> 20 * 60 + 10
    12 -> 21 * 60
    else -> null
}

private fun Int.sectionEndMinute(): Int? = when (this) {
    1 -> 9 * 60 + 15
    2 -> 10 * 60 + 5
    3 -> 11 * 60 + 10
    4 -> 12 * 60
    5 -> 14 * 60 + 15
    6 -> 15 * 60 + 5
    7 -> 16 * 60 + 10
    8 -> 17 * 60
    9 -> 19 * 60 + 15
    10 -> 20 * 60 + 5
    11 -> 20 * 60 + 55
    12 -> 21 * 60 + 45
    else -> null
}

private fun todayWeekday(): Int {
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

@Composable
private fun ExamRow(exam: TjuExamDto, onClick: () -> Unit) {
    BasicComponent(
        title = exam.name.ifBlank { "未命名考试" },
        summary = listOf(
            exam.date.ifBlank { "时间未安排" },
            exam.arrange.ifBlank { "场次未安排" },
            exam.location.ifBlank { "地点未安排" },
        ).filter { it.isNotBlank() }.joinToString(" · "),
        startAction = {
            Icon(
                imageVector = Icons.Outlined.Assignment,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp),
            )
        },
        onClick = onClick,
    )
}

