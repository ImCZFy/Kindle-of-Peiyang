package me.tju244.kop.ui

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Cancel
import androidx.compose.material.icons.twotone.TaskAlt
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tju244.kop.RebuildApplication
import me.tju244.kop.notification.CourseReminderScheduler
import me.tju244.kop.tju.data.StudyBuilding
import me.tju244.kop.tju.data.StudyCampus
import me.tju244.kop.tju.data.StudyRoom
import me.tju244.kop.tju.data.StudyRoomOccupy
import me.tju244.kop.tju.data.StudyRoomRepository
import me.tju244.kop.tju.network.TjuArrangeDto
import me.tju244.kop.tju.network.TjuCourseDto
import me.tju244.kop.tju.network.TjuExamDto
import me.tju244.kop.tju.network.TjuGpaCourseDto
import me.tju244.kop.tju.network.TjuGpaStatDto
import me.tju244.kop.tju.network.TjuGpaTotalDto
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
import top.yukonga.miuix.kmp.icon.extended.Community
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Create
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.Promotions
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CourseSchedulePage(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as RebuildApplication
    val vm: TjuViewModel = viewModel(factory = TjuViewModelFactory(app))
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val customCoursesJson by app.sessionStore.customCoursesFlow.collectAsStateWithLifecycle(initialValue = "[]")
    val semesterStartTimestamp by app.sessionStore.semesterStartTimestampFlow.collectAsStateWithLifecycle(initialValue = 0L)
    val customCourses = remember(customCoursesJson) { customCoursesJson.decodeCustomCourses() }
    val courses = remember(ui.courses, customCourses) { ui.courses + customCourses }
    val scope = rememberCoroutineScope()
    var selectedSlot by remember { mutableStateOf<CourseSlot?>(null) }
    var addTarget by remember { mutableStateOf<CustomCourseTarget?>(null) }
    var conflictPromptVisible by remember(customCoursesJson, ui.courses) {
        mutableStateOf(ui.courses.findConflictingCustomCourses(customCourses).isNotEmpty())
    }

    CourseScheduleTablePage(
        courses = courses,
        semesterStartTimestamp = semesterStartTimestamp,
        onBack = onBack,
        onOpenCourse = { selectedSlot = it },
        onAddCustomCourse = { addTarget = it },
    )
    if (conflictPromptVisible) {
        val conflicts = ui.courses.findConflictingCustomCourses(customCourses)
        WindowBottomSheet(
            show = true,
            title = "自定义课程冲突",
            onDismissRequest = { conflictPromptVisible = false },
            sheetMaxWidth = 560.dp,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("获取到的课表与 ${conflicts.size} 门自定义课程时间冲突，是否用教务课表覆盖这些自定义课程？", color = MiuixTheme.colorScheme.onBackground)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { conflictPromptVisible = false }, modifier = Modifier.weight(1f)) { Text("保留") }
                    Button(
                        onClick = {
                            val next = customCourses.filterNot { custom -> conflicts.any { it.courseId == custom.courseId } }
                            scope.launch {
                                app.sessionStore.saveCustomCourses(next.encodeCustomCourses())
                                CourseReminderScheduler.scheduleNext(app)
                            }
                            conflictPromptVisible = false
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text("覆盖") }
                }
            }
        }
    }
    selectedSlot?.let { slot ->
        WindowBottomSheet(
            show = true,
            title = "课程详情",
            onDismissRequest = { selectedSlot = null },
            sheetMaxWidth = 620.dp,
        ) {
            CourseDetailContent(
                slot = slot,
                onDeleteCustomCourse = if (slot.course.type == -1) {
                    {
                        scope.launch {
                            val next = customCourses.filterNot { it.matchesCustomCourse(slot.course) }
                            app.sessionStore.saveCustomCourses(next.encodeCustomCourses())
                            CourseReminderScheduler.scheduleNext(app)
                            selectedSlot = null
                        }
                    }
                } else {
                    null
                },
            )
        }
    }
    addTarget?.let { target ->
        WindowBottomSheet(
            show = true,
            title = "添加自定义课程",
            onDismissRequest = { addTarget = null },
            sheetMaxWidth = 620.dp,
        ) {
            CustomCourseEditor(
                target = target,
                onCancel = { addTarget = null },
                onSubmit = { course ->
                    scope.launch {
                        app.sessionStore.saveCustomCourses((customCourses + course).encodeCustomCourses())
                        CourseReminderScheduler.scheduleNext(app)
                        addTarget = null
                    }
                },
            )
        }
    }
}

@Composable
private fun CourseScheduleTablePage(
    courses: List<TjuCourseDto>,
    semesterStartTimestamp: Long,
    onBack: () -> Unit,
    onOpenCourse: (CourseSlot) -> Unit,
    onAddCustomCourse: (CustomCourseTarget) -> Unit,
) {
    BackHandler { onBack() }
    val scrollBehavior = MiuixScrollBehavior()
    val maxWeek = courses.maxTeachingWeek()
    val initialWeek = rememberCurrentTeachingWeek(maxWeek, semesterStartTimestamp)
    val scope = rememberCoroutineScope()
    val weekListState = rememberLazyListState(
        initialFirstVisibleItemIndex = (initialWeek - 3).coerceIn(0, (maxWeek - 1).coerceAtLeast(0)),
    )
    val pagerState = rememberPagerState(
        initialPage = (initialWeek - 1).coerceIn(0, maxWeek - 1),
        pageCount = { maxWeek },
    )
    LaunchedEffect(pagerState.currentPage, maxWeek) {
        val target = (pagerState.currentPage - 2).coerceIn(0, (maxWeek - 1).coerceAtLeast(0))
        weekListState.animateScrollToItem(target)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "课表",
                largeTitle = "课表",
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
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 8.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            overscrollEffect = null,
        ) {
            item {
                val selectedWeek = pagerState.currentPage + 1
                Text(
                    "第 ${selectedWeek} 周",
                    color = MiuixTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )
                LazyRow(
                    state = weekListState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                ) {
                    items(maxWeek) { index ->
                        val week = index + 1
                        val selected = week == selectedWeek
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (selected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(16.dp),
                                )
                                .clickable {
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                }
                                .padding(horizontal = 13.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "$week",
                                color = if (selected) Color.White else MiuixTheme.colorScheme.onBackground,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
            item {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    beyondViewportPageCount = 1,
                ) { page ->
                    val week = page + 1
                    val slots = courses.activeSlots(week)
                    if (slots.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("第 ${week} 周暂无课程安排", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        }
                    } else {
                        CourseGrid(
                            week = week,
                            semesterStartTimestamp = semesterStartTimestamp,
                            slots = slots,
                            onOpenCourse = onOpenCourse,
                            onAddCustomCourse = onAddCustomCourse,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseGrid(
    week: Int,
    semesterStartTimestamp: Long,
    slots: List<CourseSlot>,
    onOpenCourse: (CourseSlot) -> Unit,
    onAddCustomCourse: (CustomCourseTarget) -> Unit,
) {
    val rowHeight = 66.dp
    val breakHeight = 34.dp
    fun breaksBefore(unit: Int): Int = listOf(4, 8).count { it < unit }
    fun unitTop(unit: Int) = rowHeight * (unit - 1) + breakHeight * breaksBefore(unit)
    fun unitBottom(unit: Int) = rowHeight * unit + breakHeight * breaksBefore(unit)
    fun breakTop(afterUnit: Int) = rowHeight * afterUnit + breakHeight * listOf(4, 8).count { it < afterUnit }
    val gridHeight = rowHeight * 12 + breakHeight * 2
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(start = 58.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEachIndexed { index, label ->
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(weekDayMonthDay(week, index + 1, semesterStartTimestamp), color = MiuixTheme.colorScheme.onSurfaceVariantSummary, fontSize = 10.sp, maxLines = 1)
                    Text("周$label", color = MiuixTheme.colorScheme.onBackground, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(56.dp).height(gridHeight)) {
                repeat(12) { index ->
                    Box(
                        modifier = Modifier
                            .offset(y = unitTop(index + 1))
                            .height(rowHeight)
                            .width(52.dp),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${index + 1}", color = MiuixTheme.colorScheme.onBackground, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(sectionTimeRange(index + 1), color = MiuixTheme.colorScheme.onSurfaceVariantSummary, fontSize = 8.sp, maxLines = 2)
                        }
                    }
                }
            }
            BoxWithConstraints(modifier = Modifier.weight(1f).height(gridHeight)) {
                val cellWidth = maxWidth / 7
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineColor = Color.Gray.copy(alpha = 0.18f)
                    repeat(13) { row ->
                        val y = if (row == 12) size.height else unitTop(row + 1).toPx().coerceAtMost(size.height)
                        drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    }
                    repeat(8) { col ->
                        val x = size.width / 7f * col
                        drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                    }
                }
                BreakPill(text = "LUNCH BREAK", modifier = Modifier.offset(y = breakTop(4)).height(breakHeight).fillMaxWidth())
                BreakPill(text = "DINNER BREAK", modifier = Modifier.offset(y = breakTop(8)).height(breakHeight).fillMaxWidth())
                repeat(7) { dayIndex ->
                    repeat(12) { unitIndex ->
                        val weekday = dayIndex + 1
                        val unit = unitIndex + 1
                        val occupied = slots.any { slot ->
                            slot.arrange.weekday == weekday && unit in slot.arrange.unitList
                        }
                        if (!occupied) {
                            Box(
                                modifier = Modifier
                                    .offset(x = cellWidth * dayIndex, y = unitTop(unit))
                                    .width(cellWidth - 3.dp)
                                    .height(rowHeight - 3.dp)
                                    .clickable { onAddCustomCourse(CustomCourseTarget(week, weekday, unit)) },
                            )
                        }
                    }
                }
                slots.forEach { slot ->
                    val dayIndex = (slot.arrange.weekday - 1).coerceIn(0, 6)
                    val start = (slot.arrange.unitList.minOrNull() ?: 1).coerceIn(1, 12)
                    val end = (slot.arrange.unitList.maxOrNull() ?: start).coerceIn(start, 12)
                    CourseBlock(
                        slot = slot,
                        onClick = { onOpenCourse(slot) },
                        modifier = Modifier
                            .offset(x = cellWidth * dayIndex, y = unitTop(start))
                            .width(cellWidth - 3.dp)
                            .height(unitBottom(end) - unitTop(start) - 3.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakPill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(horizontal = 4.dp, vertical = 5.dp)
            .background(MiuixTheme.colorScheme.surface.copy(alpha = 0.86f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CourseBlock(slot: CourseSlot, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dayIndex = (slot.arrange.weekday - 1).coerceIn(0, 6)
    val colors = listOf(
        Color(0xFF4D82FF),
        Color(0xFF20B486),
        Color(0xFFFFA33A),
        Color(0xFF9B6BFF),
        Color(0xFFFF5C7A),
        Color(0xFF24A0B5),
        Color(0xFF7C8A99),
    )
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(listOf(colors[dayIndex].copy(alpha = 0.95f), colors[dayIndex].copy(alpha = 0.78f))),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick)
            .padding(5.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(slot.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (slot.arrange.location.isNotBlank()) {
                Text(slot.arrange.location, color = Color.White.copy(alpha = 0.82f), fontSize = 9.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun CourseDetailContent(slot: CourseSlot, onDeleteCustomCourse: (() -> Unit)? = null) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().height(560.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 12.dp),
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                BasicComponent(
                    title = slot.title,
                    summary = listOf(slot.arrange.timeText(), slot.arrange.location).filter { it.isNotBlank() }.joinToString(" · "),
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.Months,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    },
                )
                BasicComponent(
                    title = "任课教师",
                    summary = slot.teachers.ifBlank { "暂无教师信息" },
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.Contacts,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    },
                )
                BasicComponent(
                    title = "课程编号",
                    summary = "课程序号 ${slot.course.courseId.ifBlank { "--" }} · 逻辑班号 ${slot.course.classId.ifBlank { "--" }}",
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.Create,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    },
                )
                BasicComponent(
                    title = "上课地点",
                    summary = listOf(slot.course.campus.takeIf { it.isNotBlank() }?.let { "${it}校区" }.orEmpty(), slot.arrange.location).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "--" },
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.Promotions,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    },
                )
                BasicComponent(
                    title = "上课时间",
                    summary = "${slot.arrange.timeText()} · ${slot.course.weeks.ifBlank { slot.arrange.weekList.toWeekRangeText() }.ifBlank { "周次未知" }}",
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.Months,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    },
                )
                BasicComponent(
                    title = "课程信息",
                    summary = "学分 ${slot.course.credit.ifBlank { "--" }} · ${if (slot.arrange.isExperiment) "实验课" else "理论课"}",
                    startAction = {
                        Icon(
                            imageVector = MiuixIcons.Settings,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    },
                )
            }
        }
        onDeleteCustomCourse?.let { delete ->
            item {
                Button(
                    onClick = delete,
                    modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 8.dp),
                ) {
                    Text("删除自定义课程")
                }
            }
        }
        if (slot.course.arrangeList.size > 1) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(title = "全部安排", summary = "${slot.course.arrangeList.size} 个上课安排")
                    slot.course.arrangeList.forEach { arrange ->
                        BasicComponent(
                            title = arrange.timeText(),
                            summary = listOf(arrange.location, arrange.weekList.toWeekRangeText(), arrange.teacherList.joinToString("、")).filter { it.isNotBlank() }.joinToString(" · "),
                            startAction = {
                                Icon(
                                    imageVector = MiuixIcons.Months,
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(end = 16.dp),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

data class CustomCourseTarget(
    val week: Int,
    val weekday: Int,
    val unit: Int,
)

@Composable
private fun CustomCourseEditor(
    target: CustomCourseTarget,
    onCancel: () -> Unit,
    onSubmit: (TjuCourseDto) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var startUnit by remember(target) { mutableStateOf(target.unit.toString()) }
    var endUnit by remember(target) { mutableStateOf(target.unit.toString()) }
    var startWeek by remember(target) { mutableStateOf(target.week.toString()) }
    var endWeek by remember(target) { mutableStateOf(target.week.toString()) }
    var error by remember { mutableStateOf<String?>(null) }
    val numericKeyboard = KeyboardOptions(keyboardType = KeyboardType.Number)
    LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 620.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        item {
            TextField(value = name, onValueChange = { name = it; error = null }, label = "课程名", singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
            TextField(value = location, onValueChange = { location = it; error = null }, label = "教室", singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                TextField(value = startUnit, onValueChange = { startUnit = it.filter(Char::isDigit); error = null }, label = "开始节", singleLine = true, keyboardOptions = numericKeyboard, modifier = Modifier.weight(1f))
                TextField(value = endUnit, onValueChange = { endUnit = it.filter(Char::isDigit); error = null }, label = "结束节", singleLine = true, keyboardOptions = numericKeyboard, modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                TextField(value = startWeek, onValueChange = { startWeek = it.filter(Char::isDigit); error = null }, label = "开始周", singleLine = true, keyboardOptions = numericKeyboard, modifier = Modifier.weight(1f))
                TextField(value = endWeek, onValueChange = { endWeek = it.filter(Char::isDigit); error = null }, label = "结束周", singleLine = true, keyboardOptions = numericKeyboard, modifier = Modifier.weight(1f))
            }
        }
        item {
            TextField(value = teacher, onValueChange = { teacher = it }, label = "任课教师（选填）", singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        error?.let { message ->
            item { Text(message, color = Color(0xFFE5484D), fontSize = 13.sp) }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Button(onClick = onCancel, modifier = Modifier.weight(1f).height(48.dp)) { Text("取消") }
                Button(
                    onClick = {
                        val startU = startUnit.toIntOrNull()
                        val endU = endUnit.toIntOrNull()
                        val startW = startWeek.toIntOrNull()
                        val endW = endWeek.toIntOrNull()
                        when {
                            name.isBlank() -> error = "请填写课程名"
                            location.isBlank() -> error = "请填写教室"
                            startU == null || endU == null || startU !in 1..12 || endU !in startU..12 -> error = "请填写 1-12 内的有效节次"
                            startW == null || endW == null || startW !in 1..30 || endW !in startW..30 -> error = "请填写有效周次"
                            else -> onSubmit(
                                TjuCourseDto(
                                    name = name.trim(),
                                    classId = "custom-${System.currentTimeMillis()}",
                                    courseId = "custom-${System.currentTimeMillis()}",
                                    campus = "自定义",
                                    weeks = "第 $startW-$endW 周",
                                    teacherList = teacher.split("、", ",", " ").map { it.trim() }.filter { it.isNotBlank() },
                                    arrangeList = listOf(
                                        TjuArrangeDto(
                                            name = name.trim(),
                                            location = location.trim(),
                                            weekday = target.weekday,
                                            weekList = (startW..endW).toList(),
                                            unitList = (startU..endU).toList(),
                                            teacherList = teacher.split("、", ",", " ").map { it.trim() }.filter { it.isNotBlank() },
                                        ),
                                    ),
                                    type = -1,
                                ),
                            )
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                ) { Text("添加") }
            }
        }
    }
}

@Composable
fun GpaPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as RebuildApplication
    val vm: TjuViewModel = viewModel(factory = TjuViewModelFactory(app))
    val ui by vm.uiState.collectAsStateWithLifecycle()
    BackHandler { onBack() }
    val scrollBehavior = MiuixScrollBehavior()
    val stats = ui.gpaStats.filter { it.hasDisplayableGrades() }.sortedBy { it.term.gpaTermSortKey() }
    var gradeColumns by remember { mutableStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "成绩",
                largeTitle = "成绩",
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
            modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.background).miuixScroll().nestedScroll(scrollBehavior.nestedScrollConnection).padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 8.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            overscrollEffect = null,
        ) {
            item { GpaCurveCard(stats = stats, total = ui.gpaTotal) }
            if (stats.isEmpty() && ui.gpaCourses.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        BasicComponent(
                            title = "暂无成绩数据",
                            summary = when {
                                ui.loading -> "正在同步教务数据"
                                ui.error != null -> ui.error!!
                                ui.loggedIn -> "点击重新同步教务数据"
                                else -> "请先在设置中绑定教务网账号"
                            },
                            onClick = { if (ui.loggedIn && !ui.loading) vm.refresh() },
                        )
                    }
                }
            } else if (stats.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        OverlayDropdownPreference(
                            title = "成绩列表列数",
                            summary = "${gradeColumns} 列",
                            items = listOf("单列", "双列"),
                            selectedIndex = (gradeColumns - 1).coerceIn(0, 1),
                            onSelectedIndexChange = { index -> gradeColumns = index + 1 },
                            startAction = {
                                Icon(MiuixIcons.Settings, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                            },
                        )
                    }
                }
                stats.asReversed().forEach { stat ->
                    val courses = stat.courses.displayableGradeCourses()
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            BasicComponent(title = stat.term.toGpaTermLabel(), summary = "加权 ${stat.weighted} · 绩点 ${stat.gpa} · 学分 ${stat.credits}")
                        }
                    }
                    val rows = courses.chunked(gradeColumns.coerceIn(1, 2))
                    items(rows.size) { rowIndex ->
                        val row = rows[rowIndex]
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { course ->
                                GpaCourseStatusCard(course, modifier = Modifier.weight(1f), compact = gradeColumns > 1)
                            }
                            repeat(gradeColumns.coerceIn(1, 2) - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        OverlayDropdownPreference(
                            title = "成绩列表列数",
                            summary = "${gradeColumns} 列",
                            items = listOf("单列", "双列"),
                            selectedIndex = (gradeColumns - 1).coerceIn(0, 1),
                            onSelectedIndexChange = { index -> gradeColumns = index + 1 },
                            startAction = {
                                Icon(MiuixIcons.Settings, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                            },
                        )
                    }
                }
                val rows = ui.gpaCourses.displayableGradeCourses().chunked(gradeColumns.coerceIn(1, 2))
                items(rows.size) { rowIndex ->
                    val row = rows[rowIndex]
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { course ->
                            GpaCourseStatusCard(course, modifier = Modifier.weight(1f), compact = gradeColumns > 1)
                        }
                        repeat(gradeColumns.coerceIn(1, 2) - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamSchedulePage(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as RebuildApplication
    val vm: TjuViewModel = viewModel(factory = TjuViewModelFactory(app))
    val ui by vm.uiState.collectAsStateWithLifecycle()
    BackHandler { onBack() }
    val scrollBehavior = MiuixScrollBehavior()
    val (unfinished, finished) = remember(ui.exams) { ui.exams.partitionExams() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "考试安排",
                largeTitle = "考试安排",
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
            modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.background).miuixScroll().nestedScroll(scrollBehavior.nestedScrollConnection).padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 8.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            overscrollEffect = null,
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(title = "未完成", summary = if (unfinished.isEmpty()) "没有未完成的考试" else "${unfinished.size} 场考试")
                    unfinished.forEach { exam -> ExamInfoComponent(exam = exam, finished = false) }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(title = "已完成", summary = if (finished.isEmpty()) "没有已完成的考试" else "${finished.size} 场考试")
                    finished.take(12).forEach { exam -> ExamInfoComponent(exam = exam, finished = true) }
                }
            }
        }
    }
}

@Composable
private fun ExamInfoComponent(exam: TjuExamDto, finished: Boolean) {
    val remain = exam.remainingText()
    BasicComponent(
        title = exam.name.ifBlank { "未命名考试" },
        summary = listOf(
            exam.date.ifBlank { "时间未安排" },
            exam.arrange.ifBlank { "场次未安排" },
            exam.location.ifBlank { "地点未安排" },
            exam.seat.takeIf { it.isNotBlank() && it != "地点未安排" }?.let { "座位 $it" }.orEmpty(),
            remain,
        ).filter { it.isNotBlank() }.joinToString(" · "),
        startAction = {
            Icon(
                imageVector = if (finished) MiuixIcons.Settings else MiuixIcons.Promotions,
                contentDescription = null,
                tint = if (finished) MiuixTheme.colorScheme.onSurfaceVariantSummary else MiuixTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp),
            )
        },
    )
}

@Composable
fun EntryQrPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as RebuildApplication
    var qrContent by remember { mutableStateOf<String?>(null) }
    var qrError by remember { mutableStateOf<String?>(null) }
    var lastRefresh by remember { mutableStateOf<String?>(null) }
    var refreshSignal by remember { mutableStateOf(0) }
    var refreshing by remember { mutableStateOf(false) }
    val refreshQr: suspend () -> Unit = {
        refreshing = true
        runCatching {
            withContext(Dispatchers.IO) { app.entryQrRepository.getQrContent() }
        }.onSuccess {
            qrContent = it
            qrError = null
            lastRefresh = currentTimeLabel()
        }.onFailure {
            qrContent = null
            qrError = it.message ?: "入校码获取失败"
        }
        refreshing = false
    }
    LaunchedEffect(refreshSignal) {
        if (refreshSignal == 0) delay(1_000)
        while (true) {
            refreshQr()
            delay(150_000)
        }
    }
    BackHandler { onBack() }
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = "入校码",
                largeTitle = "入校码",
                color = Color.Transparent,
                titleColor = MiuixTheme.colorScheme.onBackground,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(MiuixIcons.Back, contentDescription = "返回", tint = MiuixTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = { refreshSignal++ }, enabled = !refreshing) {
                        Icon(
                            imageVector = MiuixIcons.Refresh,
                            contentDescription = "刷新入校码",
                            tint = if (refreshing) MiuixTheme.colorScheme.onSurfaceVariantSummary else MiuixTheme.colorScheme.onBackground,
                        )
                    }
                },
            )
        },
        containerColor = MiuixTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.background).miuixScroll().nestedScroll(scrollBehavior.nestedScrollConnection).padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 8.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            overscrollEffect = null,
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = "入校码",
                        summary = lastRefresh?.let { "更新于 $it，二维码约 3 分钟有效" } ?: (qrError ?: "稍后自动获取融合门户二维码"),
                        startAction = {
                            Icon(MiuixIcons.Promotions, contentDescription = null, tint = MiuixTheme.colorScheme.primary, modifier = Modifier.padding(end = 16.dp))
                        },
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            qrContent != null -> {
                                Image(
                                    bitmap = remember(qrContent) { qrContent!!.toQrBitmap().asImageBitmap() },
                                    contentDescription = "入校码二维码",
                                    modifier = Modifier.width(260.dp).height(260.dp),
                                )
                            }
                            qrError != null -> Text(qrError!!, color = Color(0xFFFF4D4F), modifier = Modifier.padding(horizontal = 12.dp))
                            else -> Text(if (refreshing) "正在刷新..." else "等待自动刷新", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudyRoomPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as RebuildApplication
    val repository = remember(app) { StudyRoomRepository(app.sessionStore) }
    var campuses by remember { mutableStateOf<List<StudyCampus>>(emptyList()) }
    var buildings by remember { mutableStateOf<List<StudyBuilding>>(emptyList()) }
    var buildingOptions by remember { mutableStateOf<List<StudyBuildingOption>>(emptyList()) }
    var rooms by remember { mutableStateOf<List<StudyRoom>>(emptyList()) }
    var selectedCampus by remember { mutableStateOf<StudyCampus?>(null) }
    var selectedBuilding by remember { mutableStateOf<StudyBuildingOption?>(null) }
    var selectedSession by remember { mutableStateOf(StudyRoomRepository.CURRENT_SESSION) }
    var selectedRoomStatus by remember { mutableStateOf(RoomStatusFilter.All) }
    val selectedColumns by app.sessionStore.studyRoomColumnsFlow.collectAsStateWithLifecycle(initialValue = 1)
    val scope = rememberCoroutineScope()
    var selectedRoom by remember { mutableStateOf<StudyRoom?>(null) }
    var selectedRoomSchedule by remember { mutableStateOf<List<StudyRoomOccupy>>(emptyList()) }
    var loadingRoomSchedule by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        runCatching {
            withContext(Dispatchers.IO) { repository.campuses() }
        }.onSuccess { result ->
            campuses = result
            selectedCampus = result.firstOrNull()
            error = null
        }.onFailure {
            error = it.message ?: "校区列表获取失败"
        }
        loading = false
    }
    LaunchedEffect(selectedCampus?.id) {
        val campus = selectedCampus ?: return@LaunchedEffect
        loading = true
        buildings = emptyList()
        buildingOptions = emptyList()
        rooms = emptyList()
        selectedBuilding = null
        runCatching {
            withContext(Dispatchers.IO) { repository.buildings(campus.id) }
        }.onSuccess { result ->
            buildings = result
            buildingOptions = result.toStudyBuildingOptions()
            selectedBuilding = buildingOptions.firstOrNull()
            error = null
        }.onFailure {
            error = it.message ?: "教学楼列表获取失败"
        }
        loading = false
    }
    LaunchedEffect(selectedBuilding?.source?.id, selectedSession) {
        val building = selectedBuilding?.source ?: return@LaunchedEffect
        loading = true
        runCatching {
            withContext(Dispatchers.IO) { repository.rooms(building.id, selectedSession, Date()) }
        }.onSuccess { result ->
            rooms = result
            error = null
        }.onFailure {
            rooms = emptyList()
            error = it.message ?: "教室列表获取失败"
        }
        loading = false
    }

    BackHandler { onBack() }
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = "教室查询",
                largeTitle = "教室查询",
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
            modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.background).miuixScroll().nestedScroll(scrollBehavior.nestedScrollConnection).padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 8.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            overscrollEffect = null,
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = "自习室与空教室",
                        summary = selectedBuilding?.let { "${it.label} · ${selectedSession.studySessionLabel()} · ${selectedRoomStatus.label}" }
                            ?: if (loading) "正在同步教室数据" else "请选择校区和教学楼",
                        startAction = {
                            Icon(MiuixIcons.Community, contentDescription = null, tint = MiuixTheme.colorScheme.primary, modifier = Modifier.padding(end = 16.dp))
                        },
                    )
                }
            }
            if (campuses.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        OverlayDropdownPreference(
                            title = "校区",
                            summary = selectedCampus?.name ?: "请选择校区",
                            items = campuses.map { it.name },
                            selectedIndex = campuses.indexOf(selectedCampus).coerceAtLeast(0),
                            onSelectedIndexChange = { index -> campuses.getOrNull(index)?.let { selectedCampus = it } },
                            startAction = {
                                Icon(MiuixIcons.Community, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                            },
                        )
                    }
                }
            }
            if (buildingOptions.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        OverlayDropdownPreference(
                            title = "教学楼",
                            summary = selectedBuilding?.label ?: "请选择教学楼",
                            items = buildingOptions.map { it.label },
                            selectedIndex = buildingOptions.indexOf(selectedBuilding).coerceAtLeast(0),
                            onSelectedIndexChange = { index -> buildingOptions.getOrNull(index)?.let { selectedBuilding = it } },
                            startAction = {
                                Icon(MiuixIcons.Months, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                            },
                        )
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    OverlayDropdownPreference(
                        title = "教室状态",
                        summary = selectedRoomStatus.label,
                        items = RoomStatusFilter.entries.map { it.label },
                        selectedIndex = RoomStatusFilter.entries.indexOf(selectedRoomStatus).coerceAtLeast(0),
                        onSelectedIndexChange = { index -> selectedRoomStatus = RoomStatusFilter.entries.getOrElse(index) { RoomStatusFilter.All } },
                        startAction = {
                            Icon(MiuixIcons.Settings, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                        },
                    )
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    OverlayDropdownPreference(
                        title = "列表列数",
                        summary = "${selectedColumns} 列",
                        items = listOf("单列", "双列"),
                        selectedIndex = (selectedColumns - 1).coerceIn(0, 1),
                        onSelectedIndexChange = { index ->
                            scope.launch { app.sessionStore.saveStudyRoomColumns(index + 1) }
                        },
                        startAction = {
                            Icon(MiuixIcons.Settings, contentDescription = null, tint = MiuixTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 6.dp))
                        },
                    )
                }
            }
            item {
                val sessions = listOf(
                    StudyRoomRepository.CURRENT_SESSION to "当前",
                ) + (1..12).map { it to "第 $it 节" }
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                ) {
                    items(sessions.size) { index ->
                        val (session, label) = sessions[index]
                        StudyChip(
                            text = label,
                            selected = selectedSession == session,
                            onClick = { selectedSession = session },
                        )
                    }
                }
            }
            val visibleRooms = rooms
                .filter { room -> selectedBuilding?.matches(room) != false }
                .filter { room -> selectedRoomStatus.matches(room) }
            if (error != null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        BasicComponent(title = "查询失败", summary = error.orEmpty())
                    }
                }
            } else if (loading && rooms.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        BasicComponent(title = "正在查询", summary = "正在从自习室接口获取状态")
                    }
                }
            } else if (visibleRooms.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        BasicComponent(title = "暂无教室", summary = "当前筛选条件下没有返回教室")
                    }
                }
            } else {
                val columns = selectedColumns.coerceIn(1, 2)
                val roomRows = visibleRooms.chunked(columns)
                items(roomRows.size) { rowIndex ->
                    val row = roomRows[rowIndex]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { room ->
                            StudyRoomStatusCard(
                                room = room,
                                sessionLabel = selectedSession.studySessionLabel(),
                                compact = columns > 1,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedRoom = room
                                    selectedRoomSchedule = emptyList()
                                    loadingRoomSchedule = true
                                },
                            )
                        }
                        repeat(columns - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(selectedRoom?.id) {
        val room = selectedRoom ?: return@LaunchedEffect
        loadingRoomSchedule = true
        selectedRoomSchedule = emptyList()
        selectedRoomSchedule = runCatching {
            withContext(Dispatchers.IO) { repository.schedule(room.id) }
        }.getOrDefault(emptyList())
        loadingRoomSchedule = false
    }
    selectedRoom?.let { room ->
        WindowBottomSheet(
            show = true,
            title = room.name,
            onDismissRequest = { selectedRoom = null },
            sheetMaxWidth = 560.dp,
        ) {
            RoomScheduleSheet(
                room = room,
                occupies = selectedRoomSchedule,
                loading = loadingRoomSchedule,
            )
        }
    }
}

@Composable
private fun <T> StudySelectorRow(
    values: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
    ) {
        items(values.size) { index ->
            val value = values[index]
            StudyChip(
                text = label(value),
                selected = value == selected,
                onClick = { onSelect(value) },
            )
        }
    }
}

@Composable
private fun StudyChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = if (selected) Color.White else MiuixTheme.colorScheme.onBackground,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Composable
private fun StudyRoomStatusCard(
    room: StudyRoom,
    sessionLabel: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    StatusHeroCard(
        title = room.name.ifBlank { "未命名教室" },
        summary = if (room.isFree) "${sessionLabel}空闲" else "${sessionLabel}占用",
        success = room.isFree,
        footer = "教室 ID ${room.id}",
        modifier = modifier.clickable(enabled = onClick != null) { onClick?.invoke() },
        compact = compact,
    )
}

private fun nextStudyRoomDays(): List<Date> {
    val calendar = Calendar.getInstance(Locale.CHINA).apply {
        time = Date()
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return List(7) {
        calendar.time.also {
            calendar.add(Calendar.DATE, 1)
        }
    }
}

private fun Date.toStudyRoomDateKey(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(this)

private fun Date.toStudyRoomDateLabel(): String =
    SimpleDateFormat("MM/dd", Locale.CHINA).format(this)

private fun Date.toStudyRoomWeekLabel(): String {
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val calendar = Calendar.getInstance(Locale.CHINA).apply { time = this@toStudyRoomWeekLabel }
    val index = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    return labels[index]
}

private fun String.toStudyRoomDateKey(): String =
    Regex("""\d{4}-\d{2}-\d{2}""")
        .find(this)
        ?.value
        ?: trim()

@Composable
private fun RoomScheduleSheet(room: StudyRoom, occupies: List<StudyRoomOccupy>, loading: Boolean) {
    val days = remember { nextStudyRoomDays() }
    val occupiedByDay = remember(occupies) {
        occupies
            .filter { it.sessionIndex in 1..12 }
            .groupBy { it.date.toStudyRoomDateKey() }
            .mapValues { entry -> entry.value.map { it.sessionIndex }.toSet() }
    }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BasicComponent(
            title = room.name,
            summary = if (loading) "正在获取教室占用情况" else "未来 7 天占用情况",
        )
        RoomScheduleTable(days = days, occupiedByDay = occupiedByDay)
    }
}

@Composable
private fun RoomScheduleTable(days: List<Date>, occupiedByDay: Map<String, Set<Int>>) {
    val lineColor = MiuixTheme.colorScheme.outline.copy(alpha = 0.22f)
    val freeColor = MiuixTheme.colorScheme.surfaceContainer
    val occupiedColor = Color(0xFF5B2028)
    val todayKey = remember { Date().toStudyRoomDateKey() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MiuixTheme.colorScheme.surface)
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.width(38.dp).height(44.dp))
            days.forEach { day ->
                val isToday = day.toStudyRoomDateKey() == todayKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isToday) MiuixTheme.colorScheme.primary.copy(alpha = 0.14f) else freeColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            day.toStudyRoomDateLabel(),
                            color = if (isToday) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onBackground,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                        Text(
                            day.toStudyRoomWeekLabel(),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            fontSize = 10.sp,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        (1..12).forEach { section ->
            if (section == 5) {
                RoomScheduleBreak("LUNCH BREAK")
            } else if (section == 9) {
                RoomScheduleBreak("DINNER BREAK")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(freeColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$section", color = MiuixTheme.colorScheme.onSurfaceVariantSummary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                days.forEach { day ->
                    val occupied = occupiedByDay[day.toStudyRoomDateKey()]?.contains(section) == true
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (occupied) occupiedColor else freeColor)
                            .then(
                                if (occupied) Modifier else Modifier.background(lineColor.copy(alpha = 0.06f)),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (occupied) {
                            Text("占用", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomScheduleBreak(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(MiuixTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun GpaCurveCard(stats: List<TjuGpaStatDto>, total: TjuGpaTotalDto?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        BasicComponent(
            title = "成绩曲线",
            summary = if (total != null) "总加权 ${total.displayScore} · 总绩点 ${total.gpa} · 总学分 ${total.displayCredit}" else "按学期统计加权成绩",
        )
        GpaCurve(stats = stats)
    }
}

@Composable
private fun GpaCurve(stats: List<TjuGpaStatDto>) {
    val drawableStats = stats.filter { it.weighted > 0.0 }
    if (drawableStats.size < 2) {
        BasicComponent(title = "曲线数据不足", summary = "至少两个学期后会显示变化趋势")
        return
    }
    val values = drawableStats.map { it.weighted }
    val min = ((values.minOrNull() ?: 0.0) - 5.0).coerceAtLeast(0.0)
    val max = ((values.maxOrNull() ?: 100.0) + 5.0).coerceAtMost(100.0)
    val primary = MiuixTheme.colorScheme.primary
    val muted = MiuixTheme.colorScheme.onSurfaceVariantSummary
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            val usableHeight = size.height - 24f
            val points = values.mapIndexed { index, value ->
                val x = size.width * index / values.lastIndex.toFloat()
                val ratio = ((value - min) / (max - min)).toFloat().coerceIn(0f, 1f)
                Offset(x, usableHeight - usableHeight * ratio + 10f)
            }
            repeat(4) { row ->
                val y = 10f + usableHeight * row / 3f
                drawLine(muted.copy(alpha = 0.18f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            }
            for (i in 0 until points.lastIndex) {
                drawLine(primary, points[i], points[i + 1], strokeWidth = 5f)
            }
            points.forEach { point ->
                drawCircle(Color.White, radius = 8f, center = point)
                drawCircle(primary, radius = 5f, center = point)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            drawableStats.forEach { stat ->
                Text(stat.term.toGpaTermLabel(), color = MiuixTheme.colorScheme.onSurfaceVariantSummary, fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun GpaTermCard(stat: TjuGpaStatDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        BasicComponent(title = stat.term.toGpaTermLabel(), summary = "加权 ${stat.weighted} · 绩点 ${stat.gpa} · 学分 ${stat.credits}")
        stat.courses.filter { it.score > 0.0 || it.rawScore.isNotBlank() }.forEach { course ->
            GpaCourseStatusCard(course, modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun GpaCourseCard(course: TjuGpaCourseDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        GpaCourseStatusCard(course, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun GpaCourseStatusCard(course: TjuGpaCourseDto, modifier: Modifier = Modifier, compact: Boolean = false) {
    val failed = course.isFailedCourse()
    val displayScore = course.rawScore.ifBlank { course.score.takeIf { it > 0.0 }?.toString().orEmpty() }.ifBlank { "未出分" }
    StatusHeroCard(
        title = course.name,
        summary = "${course.semester} · ${course.classType.ifBlank { "课程" }} · ${course.credit} 学分",
        success = !failed,
        footer = "总评 $displayScore",
        modifier = modifier,
        compact = compact,
    )
}

private fun List<TjuGpaCourseDto>.displayableGradeCourses(): List<TjuGpaCourseDto> {
    return filter { it.score > 0.0 || it.rawScore.isNotBlank() }
}

@Composable
private fun StatusHeroCard(
    title: String,
    summary: String,
    success: Boolean,
    footer: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val stateColor = if (success) Color(0xFF2FD66B) else Color(0xFFFF4D4F)
    val container = if (success) Color(0xFF143D28) else Color(0xFF471C22)
    val icon: ImageVector = if (success) Icons.TwoTone.TaskAlt else Icons.TwoTone.Cancel
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 132.dp else 112.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(container),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = stateColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = if (compact) 42.dp else 44.dp, y = if (compact) 38.dp else 40.dp)
                .size(if (compact) 128.dp else 132.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp, top = 14.dp, end = if (compact) 24.dp else 96.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(summary, color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(footer, color = Color.White.copy(alpha = 0.82f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

data class CourseSlot(
    val course: TjuCourseDto,
    val arrange: TjuArrangeDto,
) {
    val title: String get() = if (arrange.isExperiment && !arrange.name.isNullOrBlank()) "[实验] ${arrange.name}" else course.name
    val teachers: String get() = if (arrange.isExperiment && arrange.teacherList.isNotEmpty()) arrange.teacherList.joinToString("、") else course.teachers
    val displayTime: String get() = arrange.timeText(includeClock = true)
}

fun List<TjuCourseDto>.activeSlots(week: Int): List<CourseSlot> {
    return flatMap { course ->
        course.arrangeList
            .filter { arrange -> arrange.weekList.isEmpty() || week in arrange.weekList }
            .filter { arrange -> arrange.weekday in 1..7 && arrange.unitList.isNotEmpty() }
            .map { arrange -> CourseSlot(course, arrange) }
    }.sortedWith(compareBy<CourseSlot> { it.arrange.weekday }.thenBy { it.arrange.unitList.minOrNull() ?: 0 })
}

fun List<TjuCourseDto>.maxTeachingWeek(): Int {
    return flatMap { course -> course.arrangeList.flatMap { it.weekList } }.maxOrNull()?.coerceAtLeast(1) ?: 24
}

@Composable
fun rememberCurrentTeachingWeek(maxWeek: Int, semesterStartTimestamp: Long = 0L): Int = remember(maxWeek, semesterStartTimestamp) {
    calculateCurrentTeachingWeek(maxWeek, semesterStartTimestamp)
}

private fun calculateCurrentTeachingWeek(maxWeek: Int, semesterStartTimestamp: Long = 0L): Int {
    val now = Calendar.getInstance()
    val start = semesterStartCalendar(semesterStartTimestamp) ?: fallbackTermStart(now)
    val days = TimeUnit.MILLISECONDS.toDays(now.timeInMillis - start.timeInMillis)
    return (days / 7 + 1).toInt().coerceIn(1, maxWeek.coerceAtLeast(1))
}

private fun semesterStartCalendar(timestamp: Long): Calendar? {
    if (timestamp <= 0L) return null
    val millis = if (timestamp < 10_000_000_000L) timestamp * 1000L else timestamp
    return Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

private fun fallbackTermStart(now: Calendar): Calendar {
    val year = now.get(Calendar.YEAR)
    val candidates = listOf(
        fixedTermStart(year - 1, Calendar.SEPTEMBER, 1),
        fixedTermStart(year, Calendar.FEBRUARY, 17),
        fixedTermStart(year, Calendar.SEPTEMBER, 1),
    ).filter { !it.after(now) }
    return candidates.maxByOrNull { it.timeInMillis } ?: now
}

private fun fixedTermStart(year: Int, month: Int, day: Int): Calendar {
    return Calendar.getInstance().apply {
        clear()
        set(year, month, day)
        while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}

private fun weekDayMonthDay(week: Int, weekday: Int, semesterStartTimestamp: Long = 0L): String {
    val now = Calendar.getInstance()
    val start = semesterStartCalendar(semesterStartTimestamp) ?: fallbackTermStart(now)
    val day = start.clone() as Calendar
    day.add(Calendar.DAY_OF_MONTH, (week - 1).coerceAtLeast(0) * 7 + (weekday - 1).coerceIn(0, 6))
    return "${(day.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}-${day.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}"
}

private val customCourseGson = Gson()

internal fun String.decodeCustomCourses(): List<TjuCourseDto> {
    if (isBlank()) return emptyList()
    return runCatching {
        val type = object : TypeToken<List<TjuCourseDto>>() {}.type
        customCourseGson.fromJson<List<TjuCourseDto>>(this, type).orEmpty()
            .filter { it.type == -1 && it.name.isNotBlank() }
    }.getOrDefault(emptyList())
}

private fun List<TjuCourseDto>.encodeCustomCourses(): String {
    return customCourseGson.toJson(filter { it.type == -1 })
}

private fun TjuCourseDto.matchesCustomCourse(other: TjuCourseDto): Boolean {
    if (type != -1 || other.type != -1) return false
    if (courseId.isNotBlank() && other.courseId.isNotBlank()) return courseId == other.courseId
    if (classId.isNotBlank() && other.classId.isNotBlank()) return classId == other.classId
    return name == other.name && arrangeList == other.arrangeList
}

private fun List<TjuCourseDto>.findConflictingCustomCourses(customCourses: List<TjuCourseDto>): List<TjuCourseDto> {
    return customCourses.filter { custom ->
        any { official ->
            official.type != -1 && official.conflictsWith(custom)
        }
    }
}

private fun TjuCourseDto.conflictsWith(other: TjuCourseDto): Boolean {
    return arrangeList.any { left ->
        other.arrangeList.any { right ->
            left.weekday == right.weekday &&
                left.weekList.intersects(right.weekList) &&
                left.unitList.intersects(right.unitList)
        }
    }
}

private fun List<Int>.intersects(other: List<Int>): Boolean {
    if (isEmpty() || other.isEmpty()) return true
    val small = if (size <= other.size) this else other
    val large = if (size <= other.size) other.toSet() else toSet()
    return small.any { it in large }
}

private fun TjuArrangeDto.timeText(includeClock: Boolean = false): String {
    val day = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日").getOrNull(weekday - 1) ?: "周$weekday"
    val units = when {
        unitList.isEmpty() -> "节次未知"
        unitList.size == 1 -> "第 ${unitList.first()} 节"
        else -> "第 ${unitList.minOrNull()}-${unitList.maxOrNull()} 节"
    }
    val clock = unitList.toClockRange()
    return if (includeClock && clock.isNotBlank()) "$day $units $clock" else "$day $units"
}

private fun sectionTimeRange(section: Int): String = sectionTimeRangeOrNull(section)?.let { "${it.first}\n${it.second}" }.orEmpty()

private fun List<Int>.toClockRange(): String {
    val start = minOrNull()?.let { sectionTimeRangeOrNull(it)?.first }.orEmpty()
    val end = maxOrNull()?.let { sectionTimeRangeOrNull(it)?.second }.orEmpty()
    return if (start.isNotBlank() && end.isNotBlank()) "$start-$end" else ""
}

private fun sectionTimeRangeOrNull(section: Int): Pair<String, String>? {
    return when (section) {
        1 -> "08:30" to "09:15"
        2 -> "09:20" to "10:05"
        3 -> "10:25" to "11:10"
        4 -> "11:15" to "12:00"
        5 -> "13:30" to "14:15"
        6 -> "14:20" to "15:05"
        7 -> "15:25" to "16:10"
        8 -> "16:15" to "17:00"
        9 -> "18:30" to "19:15"
        10 -> "19:20" to "20:05"
        11 -> "20:10" to "20:55"
        12 -> "21:00" to "21:45"
        else -> null
    }
}

private fun List<Int>.toWeekRangeText(): String {
    if (isEmpty()) return ""
    val sorted = sorted()
    val suffix = when {
        sorted.size <= 1 -> ""
        sorted.zipWithNext().all { (a, b) -> b - a == 2 } && sorted.first().mod(2) == 1 -> " 单周"
        sorted.zipWithNext().all { (a, b) -> b - a == 2 } && sorted.first().mod(2) == 0 -> " 双周"
        else -> ""
    }
    return "第 ${sorted.first()}-${sorted.last()} 周$suffix"
}

private fun String.gpaTermSortKey(): Int {
    Regex("""([12])H(\d{2})""").find(this)?.let { match ->
        val term = match.groupValues[1].toIntOrNull() ?: 0
        val year = match.groupValues[2].toIntOrNull() ?: 0
        return (2000 + year) * 10 + term
    }
    Regex("""(20\d{2})-(20\d{2}).*第\s*([12])\s*学期""").find(this)?.let { match ->
        val endYear = match.groupValues[2].toIntOrNull() ?: 0
        val term = match.groupValues[3].toIntOrNull() ?: 0
        return endYear * 10 + term
    }
    return 0
}

private fun String.toGpaTermLabel(): String {
    Regex("""([12])H(\d{2})""").find(this)?.let { match ->
        val term = match.groupValues[1]
        val endYear = 2000 + match.groupValues[2].toInt()
        return "${endYear - 1}-${endYear} 学年第 ${term} 学期"
    }
    return this
}

private fun TjuGpaStatDto.hasDisplayableGrades(): Boolean =
    credits > 0.0 || weighted > 0.0 || gpa > 0.0 || courses.any { it.score > 0.0 || it.rawScore.isNotBlank() }

private fun TjuGpaCourseDto.isFailedCourse(): Boolean {
    val raw = rawScore.trim()
    if (raw.equals("F", ignoreCase = true)) return true
    if (raw.equals("P", ignoreCase = true)) return false
    val numeric = raw.toDoubleOrNull() ?: score.takeIf { it > 0.0 }
    return numeric != null && numeric < 60.0
}

private fun Int.studySessionLabel(): String = when (this) {
    StudyRoomRepository.CURRENT_SESSION -> "当前节次"
    StudyRoomRepository.ALL_SESSIONS -> "全部教室"
    else -> "第 $this 节"
}

private data class StudyBuildingOption(
    val source: StudyBuilding,
    val label: String,
    val roomPrefix: String? = null,
) {
    fun matches(room: StudyRoom): Boolean = roomPrefix == null || room.name.trim().startsWith(roomPrefix)
}

private enum class RoomStatusFilter(val label: String) {
    All("所有教室"),
    Free("仅空闲"),
    Occupied("仅占用");

    fun matches(room: StudyRoom): Boolean = when (this) {
        All -> true
        Free -> room.isFree
        Occupied -> !room.isFree
    }
}

private fun List<StudyBuilding>.toStudyBuildingOptions(): List<StudyBuildingOption> =
    flatMap { building ->
        if (building.name.contains("45") && building.name.contains("46")) {
            listOf(
                StudyBuildingOption(source = building, label = building.name.replace("45楼、46楼", "45楼"), roomPrefix = "45楼"),
                StudyBuildingOption(source = building, label = building.name.replace("45楼、46楼", "46楼"), roomPrefix = "46楼"),
            )
        } else {
            listOf(StudyBuildingOption(source = building, label = building.name))
        }
    }

private fun String.toQrBitmap(size: Int = 720): Bitmap {
    val hints = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        EncodeHintType.MARGIN to 1,
    )
    val matrix = QRCodeWriter().encode(this, BarcodeFormat.QR_CODE, size, size, hints)
    return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also { bitmap ->
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
    }
}

private fun currentTimeLabel(): String {
    val now = Calendar.getInstance()
    val hour = now.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
    val minute = now.get(Calendar.MINUTE).toString().padStart(2, '0')
    val second = now.get(Calendar.SECOND).toString().padStart(2, '0')
    return "$hour:$minute:$second"
}

private fun List<TjuExamDto>.partitionExams(): Pair<List<TjuExamDto>, List<TjuExamDto>> {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val finished = mutableListOf<TjuExamDto>()
    val unfinished = mutableListOf<TjuExamDto>()
    forEach { exam ->
        val time = exam.date.toExamDateMillis()
        if (time != null && time < today) finished += exam else unfinished += exam
    }
    return unfinished.sortedWith(compareBy<TjuExamDto> { it.date.toExamDateMillis() ?: Long.MAX_VALUE }.thenBy { it.name }) to
        finished.sortedByDescending { it.date.toExamDateMillis() ?: 0L }
}

private fun TjuExamDto.remainingText(): String {
    val dateMillis = date.toExamDateMillis() ?: return if (date.isBlank() || date == "时间未安排") "时间未安排" else ""
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val days = TimeUnit.MILLISECONDS.toDays(dateMillis - today)
    return when {
        days < 0 -> "已结束"
        days == 0L -> "今天"
        else -> "${days}天后"
    }
}

private fun String.toExamDateMillis(): Long? {
    val normalized = trim()
    if (normalized.isBlank() || normalized == "时间未安排") return null
    val parts = normalized.take(10).split("-")
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    return Calendar.getInstance().apply {
        clear()
        set(year, month - 1, day)
    }.timeInMillis
}

