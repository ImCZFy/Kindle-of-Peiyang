package me.tju244.kop.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.first
import me.tju244.kop.RebuildApplication
import me.tju244.kop.tju.network.TjuClassesBundle
import me.tju244.kop.tju.network.TjuCourseDto

object CourseReminderScheduler {
    private const val REQUEST_CODE_NEXT = 244200
    private const val PREFS_NAME = "course_reminder_state"
    private const val KEY_LAST_NOTIFIED = "last_notified_key"
    private val logTimeFormatter = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)

    suspend fun scheduleNext(app: RebuildApplication, bundle: TjuClassesBundle? = null) {
        CourseReminderDebugLog.add("scheduleNext 开始：${if (bundle == null) "读取本地缓存" else "使用刚同步的数据"}")
        val store = app.sessionStore
        if (!store.courseNotificationEnabledFlow.first()) {
            CourseReminderDebugLog.add("课程提醒已关闭，取消下一节提醒")
            cancel(app)
            return
        }
        val classesBundle = bundle ?: readCachedBundle(app)
        val courses = classesBundle?.courses.orEmpty() + readCustomCourses(app)
        val semesterStartTimestamp = store.currentSemesterStartTimestamp()
        CourseReminderDebugLog.add("课程数据：教务 ${classesBundle?.courses.orEmpty().size}，自定义 ${courses.count { it.type == -1 }}")
        if (semesterStartTimestamp > 0L) {
            CourseReminderDebugLog.add("学期起点：${store.currentSemesterStartAt().ifBlank { semesterStartTimestamp.toString() }}")
        }
        if (courses.isEmpty()) {
            CourseReminderDebugLog.add("没有课程数据，取消下一节提醒")
            cancel(app)
            return
        }
        val now = System.currentTimeMillis()
        val lastNotifiedKey = app.reminderPrefs().getString(KEY_LAST_NOTIFIED, null)
        val payload = courseReminderPayloads(courses = courses, daysAhead = 30, semesterStartTimestamp = semesterStartTimestamp)
            .firstOrNull { candidate ->
                val alreadyNotifiedInReminderWindow = candidate.reminderKey() == lastNotifiedKey &&
                    calculateReminderTriggerMillis(candidate) <= now
                if (alreadyNotifiedInReminderWindow) {
                    CourseReminderDebugLog.add("跳过已提醒课程：${candidate.courseName}，上课 ${candidate.startAtMillis.formatLogTime()}")
                }
                !alreadyNotifiedInReminderWindow
            }
        cancel(app)
        if (payload == null) {
            CourseReminderDebugLog.add("未找到未来课程，不调度提醒")
            return
        }
        CourseReminderDebugLog.add(
            "下一节：${payload.courseName}，${payload.location}，上课 ${payload.startAtMillis.formatLogTime()}，提醒 ${calculateReminderTriggerMillis(payload).formatLogTime()}",
        )
        schedulePayload(app, payload)
    }

    private fun schedulePayload(app: RebuildApplication, payload: CourseReminderPayload) {
        val triggerAt = calculateReminderTriggerMillis(payload)
        val now = System.currentTimeMillis()
        if (payload.startAtMillis <= now) {
            CourseReminderDebugLog.add("下一节课程已经开始，跳过：${payload.courseName}")
            return
        }
        val normalizedTrigger = triggerAt.coerceAtLeast(now + 2_000L)
        CourseReminderDebugLog.add("精确闹钟权限：${ExactAlarmPermission.statusText(app)}")
        val intent = Intent(app, CourseReminderReceiver::class.java).apply {
            putExtra(CourseReminderReceiver.EXTRA_COURSE_NAME, payload.courseName)
            putExtra(CourseReminderReceiver.EXTRA_LOCATION, payload.location)
            putExtra(CourseReminderReceiver.EXTRA_TIME_RANGE, payload.timeRange)
            putExtra(CourseReminderReceiver.EXTRA_TEACHER, payload.teacher)
            putExtra(CourseReminderReceiver.EXTRA_START_AT, payload.startAtMillis)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            app,
            REQUEST_CODE_NEXT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        fun setInexactFallback() {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, normalizedTrigger, pendingIntent)
            CourseReminderDebugLog.add("已调度 setAndAllowWhileIdle：${normalizedTrigger.formatLogTime()}")
        }
        if (ExactAlarmPermission.canSchedule(app)) {
            runCatching {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, normalizedTrigger, pendingIntent)
                CourseReminderDebugLog.add("已调度 setExactAndAllowWhileIdle：${normalizedTrigger.formatLogTime()}")
            }.getOrElse {
                CourseReminderDebugLog.add("精确提醒调度失败，降级：${it.javaClass.simpleName}")
                setInexactFallback()
            }
        } else {
            CourseReminderDebugLog.add("没有精确闹钟权限，降级为静默非精确提醒")
            setInexactFallback()
        }
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_NEXT,
            Intent(context, CourseReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun markNotified(context: Context, payload: CourseReminderPayload) {
        context.reminderPrefs().edit().putString(KEY_LAST_NOTIFIED, payload.reminderKey()).apply()
        CourseReminderDebugLog.add("记录已提醒课程：${payload.courseName}，上课 ${payload.startAtMillis.formatLogTime()}")
    }

    private fun Long.formatLogTime(): String = logTimeFormatter.format(Date(this))

    private fun CourseReminderPayload.reminderKey(): String = listOf(courseName, location, startAtMillis).joinToString("|")

    private fun Context.reminderPrefs() = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private suspend fun readCachedBundle(app: RebuildApplication): TjuClassesBundle? {
        val raw = app.sessionStore.currentTjuClassesCache()
        if (raw.isBlank()) return null
        return runCatching { Gson().fromJson(raw, TjuClassesBundle::class.java) }.getOrNull()
    }

    private suspend fun readCustomCourses(app: RebuildApplication): List<TjuCourseDto> {
        val raw = app.sessionStore.currentCustomCourses()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<TjuCourseDto>>() {}.type
            Gson().fromJson<List<TjuCourseDto>>(raw, type).orEmpty()
                .filter { it.type == -1 && it.name.isNotBlank() }
        }.getOrDefault(emptyList())
    }
}

