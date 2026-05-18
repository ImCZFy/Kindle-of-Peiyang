package me.tju244.kop.ui

import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

internal fun Modifier.miuixScroll(): Modifier =
    scrollEndHaptic().overScrollVertical()

