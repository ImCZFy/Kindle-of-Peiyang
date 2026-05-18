package me.tju244.kop.ui

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.tju244.kop.R
import me.tju244.kop.ui.effect.BgEffectBackground
import me.tju244.kop.ui.theme.LocalAppDarkMode
import kotlinx.coroutines.CancellationException
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.Promotions
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Hide
import top.yukonga.miuix.kmp.icon.extended.Show
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
@Composable
fun AboutPage(onBack: () -> Unit) {
    var backProgress by remember { mutableStateOf(0f) }
    val backShiftPx = with(LocalDensity.current) { 38.dp.toPx() }
    var aboutPane by remember { mutableStateOf(AboutPane.Main) }
    var selectedOpenSource by remember { mutableStateOf<OpenSourceProject?>(null) }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val packageInfo = remember(context.packageName) {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
    } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode.toLong()
    }
    val versionName = packageInfo.versionName ?: "未知版本"
    val navigateBack: () -> Unit = {
        when (aboutPane) {
            AboutPane.Main -> onBack()
            AboutPane.OpenSource -> {
                aboutPane = AboutPane.Main
                selectedOpenSource = null
            }
            AboutPane.OpenSourceDetail -> {
                aboutPane = AboutPane.OpenSource
            }
        }
    }
    PredictiveBackHandler(enabled = true) { progress ->
        try {
            progress.collect { evt -> backProgress = evt.progress.coerceIn(0f, 1f) }
            navigateBack()
        } catch (e: CancellationException) {
            throw e
        } finally {
            backProgress = 0f
        }
    }
    BackHandler(enabled = true) { navigateBack() }
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberLayerBackdrop()
    val listState = rememberLazyListState()
    val plainListState = rememberLazyListState()
    val heroProgress by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (listState.firstVisibleItemScrollOffset / 280f).coerceIn(0f, 1f)
            }
        }
    }
    Scaffold(
        topBar = {
            val pageTitle = when (aboutPane) {
                AboutPane.Main -> "关于"
                AboutPane.OpenSource -> "开放源代码"
                AboutPane.OpenSourceDetail -> selectedOpenSource?.name ?: "开源项目"
            }
            TopAppBar(
                title = pageTitle,
                largeTitle = pageTitle,
                color = if (aboutPane == AboutPane.Main) Color.Transparent else if (LocalAppDarkMode.current) Color.Transparent else MiuixTheme.colorScheme.surface,
                titleColor = MiuixTheme.colorScheme.onBackground,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        AnimatedContent(
            targetState = aboutPane,
            transitionSpec = {
                val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                (slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { it * dir } + fadeIn(tween(200))) togetherWith
                    (slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { -it * dir } + fadeOut(tween(150)))
            },
            label = "about-pane-transition",
        ) { pane ->
        when (pane) {
            AboutPane.Main -> {
                BgEffectBackground(
                    dynamicBackground = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val eased = backProgress * backProgress
                            translationX = backShiftPx * eased
                            val scale = 1f - 0.015f * eased
                            scaleX = scale
                            scaleY = scale
                            alpha = 1f - 0.08f * eased
                        }
                        .background(MiuixTheme.colorScheme.background),
                    bgModifier = Modifier.layerBackdrop(backdrop),
                    effectBackground = true,
                ) {
                    AboutHeroHeader(
                        versionName = versionName,
                        versionCode = versionCode,
                        packageName = context.packageName,
                        progress = heroProgress,
                        modifier = Modifier
                            .padding(
                                top = innerPadding.calculateTopPadding() + 52.dp,
                                start = 12.dp,
                                end = 12.dp,
                            ),
                    )
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .miuixScroll()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding(),
                        ),
                        overscrollEffect = null,
                    ) {
                        item { Spacer(Modifier.height(392.dp)) }
                        item {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                SmallTitle(text = "项目")
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    BasicComponent(title = "Kindle of Peiyang", summary = "面向天津大学学生的教务综合工具，提供课程表、成绩查询、考试安排、入校码和教室查询等功能。")
                                    ArrowPreference(
                                        title = "GitHub 项目地址",
                                        summary = "github.com/ImCZFy/Kindle-of-Peiyang",
                                        onClick = { uriHandler.openUri("https://github.com/ImCZFy/Kindle-of-Peiyang") },
                                    )
                                    ArrowPreference(
                                        title = "开放源代码",
                                        summary = "查看本应用使用的第三方开源组件",
                                        onClick = { aboutPane = AboutPane.OpenSource },
                                    )
                                }
                            }
                        }
                        item {
                            AboutSection(
                                title = "构建信息",
                                items = listOf(
                                    AboutItem("应用包名", context.packageName),
                                    AboutItem("Namespace", "me.tju244.kop"),
                                    AboutItem("SDK", "min 26 / target 37 / compile 37"),
                                ),
                            )
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }

            AboutPane.OpenSource -> {
                LazyColumn(
                    state = plainListState,
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
                        bottom = innerPadding.calculateBottomPadding() + 24.dp,
                    ),
                    overscrollEffect = null,
                ) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            OpenSourceProjects.forEach { project ->
                                ArrowPreference(
                                    title = project.name,
                                    summary = project.summary,
                                    onClick = {
                                        selectedOpenSource = project
                                        aboutPane = AboutPane.OpenSourceDetail
                                    },
                                )
                            }
                        }
                    }
                }
            }

            AboutPane.OpenSourceDetail -> {
                selectedOpenSource?.let { project ->
                    LazyColumn(
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
                            bottom = innerPadding.calculateBottomPadding() + 24.dp,
                        ),
                        overscrollEffect = null,
                    ) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                BasicComponent(title = project.name, summary = project.description)
                                BasicComponent(title = "许可证", summary = project.license)
                                BasicComponent(title = "使用位置", summary = project.usage)
                            }
                        }
                        item {
                            Card(
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .fillMaxWidth(),
                            ) {
                                ArrowPreference(
                                    title = "项目主页",
                                    summary = project.url,
                                    onClick = { uriHandler.openUri(project.url) },
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

private enum class AboutPane {
    Main,
    OpenSource,
    OpenSourceDetail,
}

private data class AboutItem(
    val title: String,
    val summary: String,
)

private data class OpenSourceProject(
    val name: String,
    val summary: String,
    val description: String,
    val usage: String,
    val license: String,
    val url: String,
)

private val OpenSourceProjects = listOf(
    OpenSourceProject(
        name = "Miuix",
        summary = "HyperOS 风格 Compose UI 组件",
        description = "提供 TopAppBar、NavigationBar、Card、Preference、图标和模糊相关组件，是当前界面结构的主要基础。",
        usage = "miuix-ui-android / miuix-preference-android / miuix-icons-android / miuix-blur-android",
        license = "Apache License 2.0",
        url = "https://github.com/miuix-kotlin-multiplatform/miuix",
    ),
    OpenSourceProject(
        name = "微北洋 Flutter 客户端",
        summary = "天外天工作室维护的微北洋 Flutter 客户端",
        description = "提供微北洋移动端功能设计和校园服务实现参考，KOP 的校园教务体验延续了相关开源项目的探索。",
        usage = "功能设计与校园服务实现参考",
        license = "未知",
        url = "https://github.com/twtstudio/WePeiYang-Flutter/",
    ),
    OpenSourceProject(
        name = "AndroidX Compose",
        summary = "Android 声明式 UI 工具包",
        description = "用于构建页面、列表、动画、图片和输入控件等核心界面能力。",
        usage = "activity-compose / compose-ui / compose-foundation / compose-tooling",
        license = "Apache License 2.0",
        url = "https://developer.android.com/jetpack/androidx/releases/compose",
    ),
    OpenSourceProject(
        name = "AndroidX Lifecycle / Navigation / DataStore",
        summary = "状态、路由和本地偏好存储",
        description = "提供 ViewModel 生命周期状态订阅、Compose 导航能力和登录/界面设置持久化。",
        usage = "lifecycle-runtime-ktx / lifecycle-viewmodel-compose / navigation-compose / datastore-preferences",
        license = "Apache License 2.0",
        url = "https://developer.android.com/jetpack/androidx",
    ),
    OpenSourceProject(
        name = "Kotlin Coroutines",
        summary = "异步任务和 Flow 状态流",
        description = "用于网络请求、登录流程、分页加载和界面状态分发。",
        usage = "kotlinx-coroutines-android",
        license = "Apache License 2.0",
        url = "https://github.com/Kotlin/kotlinx.coroutines",
    ),
    OpenSourceProject(
        name = "Retrofit",
        summary = "类型安全的 HTTP API 客户端",
        description = "用于定义教务、课程、成绩、考试、入校码与教室查询相关接口，并配合 Gson 转换 JSON 数据。",
        usage = "retrofit / converter-gson",
        license = "Apache License 2.0",
        url = "https://github.com/square/retrofit",
    ),
    OpenSourceProject(
        name = "OkHttp",
        summary = "HTTP 客户端和网络日志",
        description = "作为 Retrofit 的底层网络客户端，并提供调试阶段的请求日志能力。",
        usage = "okhttp / logging-interceptor",
        license = "Apache License 2.0",
        url = "https://github.com/square/okhttp",
    ),
    OpenSourceProject(
        name = "Backdrop",
        summary = "Compose 背景模糊与玻璃效果",
        description = "用于底部导航、About 动效背景和局部视觉层次中的 backdrop / blur / lens 效果。",
        usage = "io.github.kyant0:backdrop",
        license = "Apache License 2.0",
        url = "https://github.com/kyant0/AndroidLiquidGlass",
    ),
)

@Composable
private fun AboutSection(
    title: String,
    items: List<AboutItem>,
) {
    Column(modifier = Modifier.padding(top = 14.dp)) {
        SmallTitle(text = title)
        Card(modifier = Modifier.fillMaxWidth()) {
            items.forEach { item ->
                BasicComponent(
                    title = item.title,
                    summary = item.summary,
                )
            }
        }
    }
}

@Composable
private fun AboutHeroHeader(
    versionName: String,
    versionCode: Long,
    packageName: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = 1f - progress
                val scale = 1f - progress * 0.05f
                scaleX = scale
                scaleY = scale
            },
    ) {
        Image(
            painter = painterResource(R.drawable.app_icon),
            contentDescription = "北洋之炬",
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Fit,
        )
        Text(
            "北洋之炬",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Version $versionName ($versionCode)",
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            fontSize = 14.sp,
        )
        Text(
            text = packageName,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Card(
            modifier = Modifier
                .padding(top = 18.dp)
                .fillMaxWidth(),
        ) {
            BasicComponent(
                title = "Light up your campus life with Kindle.",
                summary = "校园聚合服务，尽在 KOP。",
            )
        }
    }
}

