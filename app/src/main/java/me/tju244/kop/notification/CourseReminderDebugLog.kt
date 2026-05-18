package me.tju244.kop.notification

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object CourseReminderDebugLog {
    private const val MAX_LINES = 300
    private val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINA)
    private val _lines = MutableStateFlow<List<String>>(emptyList())

    val lines: StateFlow<List<String>> = _lines.asStateFlow()

    fun add(message: String) {
        val line = "[${formatter.format(Date())}] $message"
        _lines.update { old -> (old + line).takeLast(MAX_LINES) }
    }

    fun clear() {
        _lines.value = emptyList()
    }
}

