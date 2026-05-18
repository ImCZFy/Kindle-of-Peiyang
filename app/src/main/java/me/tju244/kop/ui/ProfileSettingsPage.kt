package me.tju244.kop.ui

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.tju244.kop.R
import me.tju244.kop.RebuildApplication
import me.tju244.kop.lake.network.LakeUserProfileUi
import me.tju244.kop.lake.network.LakePostUi
import me.tju244.kop.lake.ui.LakeViewModel
import me.tju244.kop.lake.ui.LakeViewModelFactory
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
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Community
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Create
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.Promotions
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Hide
import top.yukonga.miuix.kmp.icon.extended.Show
import me.tju244.kop.ui.lake.*
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
@Composable
fun ProfileSettingsPage(onBack: () -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as RebuildApplication
    val vm: LakeViewModel = viewModel(factory = LakeViewModelFactory(app))
    val ui by vm.uiState.collectAsStateWithLifecycle()
    var nicknameInput by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { vm.loadCurrentUserProfile() }
    LaunchedEffect(ui.currentUserProfile?.nickname) {
        if (nicknameInput.isBlank()) {
            nicknameInput = ui.currentUserProfile?.nickname.orEmpty()
        }
    }
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
                title = "个人资料",
                largeTitle = "个人资料",
                color = if (LocalAppDarkMode.current) Color.Transparent else MiuixTheme.colorScheme.surface,
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
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            overscrollEffect = null,
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AvatarImage(
                                avatar = ui.currentUserProfile?.avatar.orEmpty().ifBlank { ui.currentUserAvatar },
                                author = ui.currentUserProfile?.nickname.orEmpty().ifBlank { ui.currentUserName.ifBlank { "我" } },
                                size = 58.dp,
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    ui.currentUserProfile?.nickname.orEmpty().ifBlank { ui.currentUserName.ifBlank { "未获取昵称" } },
                                    color = MiuixTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    "UID ${ui.currentUserProfile?.uid ?: ui.currentUserUid ?: 0}",
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    fontSize = 12.sp,
                                )
                                val username = ui.currentUserProfile?.username.orEmpty()
                                if (username.isNotBlank()) {
                                    Text(
                                        username,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("个人资料设置", color = MiuixTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        TextField(
                            value = nicknameInput,
                            onValueChange = { nicknameInput = it },
                            label = "昵称",
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                vm.updateProfileNickname(nicknameInput) { success, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    if (success) vm.loadCurrentUserProfile()
                                }
                            },
                            enabled = !ui.profileSaving,
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                        ) {
                            Text(if (ui.profileSaving) "保存中..." else "保存资料")
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("当前已接入昵称修改接口；头像接口暂未开放，先展示接口返回头像。", color = MiuixTheme.colorScheme.onSurfaceVariantSummary, fontSize = 12.sp)
                    }
                }
            }
            item {
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Text("退出登录")
                }
            }
        }
    }
}

