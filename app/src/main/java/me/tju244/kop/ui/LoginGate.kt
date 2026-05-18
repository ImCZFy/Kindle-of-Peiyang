package me.tju244.kop.ui

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
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
fun LoginGate(
    sid: String,
    password: String,
    loading: Boolean,
    error: String?,
    onSidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onRequestLoginCode: (String, (Boolean, String) -> Unit) -> Unit,
    onLoginByCode: (String, String, (Boolean, String) -> Unit) -> Unit,
    onRequestResetCode: (String, (Boolean, String) -> Unit) -> Unit,
    onResetPassword: (String, String, String, (Boolean, String) -> Unit) -> Unit,
) {
    val context = LocalContext.current
    var showLoginSheet by remember { mutableStateOf(false) }
    var loginSheetTab by remember { mutableIntStateOf(0) } // 0 密码 1 验证码 2 忘记密码
    var codePhone by remember { mutableStateOf("") }
    var codeValue by remember { mutableStateOf("") }
    var resetPhone by remember { mutableStateOf("") }
    var resetCode by remember { mutableStateOf("") }
    var resetPassword by remember { mutableStateOf("") }
    var loginCodeCountdown by remember { mutableIntStateOf(0) }
    var resetCodeCountdown by remember { mutableIntStateOf(0) }
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(loginCodeCountdown) {
        if (loginCodeCountdown > 0) {
            kotlinx.coroutines.delay(1000)
            loginCodeCountdown -= 1
        }
    }
    LaunchedEffect(resetCodeCountdown) {
        if (resetCodeCountdown > 0) {
            kotlinx.coroutines.delay(1000)
            resetCodeCountdown -= 1
        }
    }
    LaunchedEffect(Unit) { shown = true }
    val logoOffset by animateDpAsState(
        targetValue = if (shown) 0.dp else 18.dp,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "login-logo-offset",
    )
    val actionAlpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 420, delayMillis = 120),
        label = "login-action-alpha",
    )
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(22.dp),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 128.dp)
                .offset(y = logoOffset),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.app_icon),
                contentDescription = "北洋之炬",
                modifier = Modifier.size(82.dp),
            )
            Text("北洋之炬", fontWeight = FontWeight.Bold, color = LakeColors.text)
            Text("学在北洋 | 一手掌握", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 14.dp)
                .graphicsLayer { alpha = actionAlpha },
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = { showLoginSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text("登录北洋之炬")
            }
            Text("登录后继续使用论坛与校园服务", color = LakeColors.muted, fontSize = 13.sp)
        }
        WindowBottomSheet(
            show = showLoginSheet,
            title = "账号登录",
            onDismissRequest = { showLoginSheet = false },
            sheetMaxWidth = 560.dp,
            outsideMargin = DpSize(16.dp, 28.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TabRow(
                    tabs = listOf("密码登录", "验证码", "忘记密码"),
                    selectedTabIndex = loginSheetTab,
                    onTabSelected = { loginSheetTab = it },
                    modifier = Modifier.fillMaxWidth(),
                    minWidth = 0.dp,
                    maxWidth = 132.dp,
                    itemSpacing = 4.dp,
                )
                when (loginSheetTab) {
                    0 -> {
                        TextField(
                            value = sid,
                            onValueChange = onSidChange,
                            label = "学号/手机号",
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.Username },
                        )
                        var passwordVisible by remember { mutableStateOf(false) }
                        TextField(
                            value = password, onValueChange = onPasswordChange, label = "密码", singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.Password },
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
                        Button(onClick = onLogin, enabled = !loading, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                            Text(if (loading) "登录中..." else "登录")
                        }
                    }
                    1 -> {
                        TextField(
                            value = codePhone,
                            onValueChange = { codePhone = it },
                            label = "手机号",
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.PhoneNumber },
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextField(
                                value = codeValue,
                                onValueChange = { codeValue = it },
                                label = "验证码",
                                singleLine = true,
                                modifier = Modifier.weight(1f).semantics { contentType = ContentType.SmsOtpCode },
                            )
                            Button(
                                onClick = {
                                    onRequestLoginCode(codePhone) { ok, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        if (ok) loginCodeCountdown = 60
                                    }
                                },
                                enabled = !loading && loginCodeCountdown == 0,
                                modifier = Modifier.height(44.dp),
                            ) {
                                Text(if (loginCodeCountdown > 0) "${loginCodeCountdown}s" else "获取验证码")
                            }
                        }
                        Button(
                            onClick = {
                                onLoginByCode(codePhone, codeValue) { ok, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    if (ok) showLoginSheet = false
                                }
                            },
                            enabled = !loading,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                        ) {
                            Text(if (loading) "登录中..." else "验证码登录")
                        }
                    }
                    else -> {
                        TextField(
                            value = resetPhone,
                            onValueChange = { resetPhone = it },
                            label = "手机号",
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.PhoneNumber },
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextField(
                                value = resetCode,
                                onValueChange = { resetCode = it },
                                label = "验证码",
                                singleLine = true,
                                modifier = Modifier.weight(1f).semantics { contentType = ContentType.SmsOtpCode },
                            )
                            Button(
                                onClick = {
                                    onRequestResetCode(resetPhone) { ok, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        if (ok) resetCodeCountdown = 60
                                    }
                                },
                                enabled = !loading && resetCodeCountdown == 0,
                                modifier = Modifier.height(44.dp),
                            ) {
                                Text(if (resetCodeCountdown > 0) "${resetCodeCountdown}s" else "获取验证码")
                            }
                        }
                        TextField(
                            value = resetPassword,
                            onValueChange = { resetPassword = it },
                            label = "新密码",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.NewPassword },
                        )
                        Button(
                            onClick = {
                                onResetPassword(resetPhone, resetCode, resetPassword) { ok, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    if (ok) {
                                        loginSheetTab = 0
                                        onSidChange(resetPhone)
                                        onPasswordChange("")
                                        resetPassword = ""
                                        resetCode = ""
                                    }
                                }
                            },
                            enabled = !loading,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                        ) {
                            Text(if (loading) "提交中..." else "重置密码")
                        }
                    }
                }
                if (!error.isNullOrBlank()) {
                    Text(error, color = MiuixTheme.colorScheme.error)
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

