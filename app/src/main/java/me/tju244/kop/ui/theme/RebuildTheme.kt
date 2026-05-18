package me.tju244.kop.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import me.tju244.kop.R
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.TextStyles
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.defaultTextStyles
import top.yukonga.miuix.kmp.theme.lightColorScheme

@Composable
fun RebuildTheme(themeMode: Int = 0, fontMode: Int = 0, content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    val darkMode = when (themeMode) {
        1 -> false
        2 -> true
        else -> systemDark
    }
    val appFontFamily = remember(fontMode) {
        if (fontMode == 1) internalFontFamily() else null
    }
    val textStyles = remember(appFontFamily) {
        appFontFamily?.let { internalFontTextStyles(it) } ?: defaultTextStyles()
    }
    val view = LocalView.current
    DisposableEffect(view, darkMode) {
        val window = view.context.findActivity()?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkMode
                isAppearanceLightNavigationBars = !darkMode
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isStatusBarContrastEnforced = false
                window.isNavigationBarContrastEnforced = false
            }
        }
        onDispose { }
    }
    CompositionLocalProvider(
        LocalAppDarkMode provides darkMode,
        LocalAppFontFamily provides appFontFamily,
    ) {
        MiuixTheme(
            colors = if (darkMode) {
                darkColorScheme(
                    background = Color.Black,
                    surface = Color.Black,
                    surfaceVariant = Color(0xFF242424),
                    surfaceContainer = Color(0xFF242424),
                    surfaceContainerHigh = Color(0xFF242424),
                    surfaceContainerHighest = Color(0xFF2D2D2D),
                    secondaryVariant = Color(0xFF2D2D2D),
                    dividerLine = Color(0xFF393939),
                )
            } else {
                lightColorScheme(
                    background = Color(0xFFF4F5F7),
                    surface = Color(0xFFF4F5F7),
                    surfaceContainer = Color.White,
                    surfaceVariant = Color.White,
                    secondaryVariant = Color(0xFFE9EDF3),
                )
            },
            textStyles = textStyles,
            content = content,
        )
    }
}

val LocalAppDarkMode = compositionLocalOf { false }
val LocalAppFontFamily = compositionLocalOf<FontFamily?> { null }

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun internalFontFamily(): FontFamily =
    FontFamily(
        Font(R.font.oppo_sans_4_0, weight = FontWeight.Normal),
        Font(R.font.oppo_sans_4_0, weight = FontWeight.Medium),
        Font(R.font.oppo_sans_4_0, weight = FontWeight.SemiBold),
        Font(R.font.oppo_sans_4_0, weight = FontWeight.Bold),
    )

private fun internalFontTextStyles(family: FontFamily): TextStyles {
    fun style(size: Int, weight: FontWeight? = null, lineHeightEm: Float? = null): TextStyle {
        return TextStyle(
            fontSize = size.sp,
            fontWeight = weight,
            fontFamily = family,
            lineHeight = lineHeightEm?.em ?: androidx.compose.ui.unit.TextUnit.Unspecified,
        )
    }
    return defaultTextStyles(
        main = style(17),
        paragraph = style(17, lineHeightEm = 1.2f),
        body1 = style(16),
        body2 = style(14),
        button = style(17),
        footnote1 = style(13),
        footnote2 = style(11),
        headline1 = style(17),
        headline2 = style(16),
        subtitle = style(14, FontWeight.Bold),
        title1 = style(32),
        title2 = style(24),
        title3 = style(20),
        title4 = style(18),
    )
}

