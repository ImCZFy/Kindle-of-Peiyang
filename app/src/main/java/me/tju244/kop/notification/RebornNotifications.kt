package me.tju244.kop.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import me.tju244.kop.MainActivity
import me.tju244.kop.R
import me.tju244.kop.notification.miui.MiSuperIslandBridge
import me.tju244.kop.ui.AppDeepLinks

object RebornNotifications {
    const val CHANNEL_COURSE = "course_reminders"
    const val COURSE_NOTIFICATION_ID = 244002

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channels = listOf(
            NotificationChannel(CHANNEL_COURSE, "课程提醒", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "即将上课提醒和课程 Live Update"
            },
        )
        manager.createNotificationChannels(channels)
    }

    fun notifyCourseReminder(
        context: Context,
        payload: CourseReminderPayload,
        liveUpdateEnabled: Boolean,
        miIslandEnabled: Boolean,
        miIslandBypassEnabled: Boolean,
        miIslandAuthMode: Int,
        miIslandDisplayMode: Int,
    ) {
        if (!canPostNotifications(context)) return
        ensureChannels(context)
        val displayMode = MiSuperIslandBridge.DisplayMode.fromIndex(miIslandDisplayMode)
        val liveUpdateParts = payload.liveUpdateDisplayParts(displayMode)
        val title = if (liveUpdateEnabled) liveUpdateParts.first else "课程提醒"
        val text = if (liveUpdateEnabled) {
            liveUpdateParts.second
        } else {
            listOf(payload.courseName, payload.location, payload.displayTimeText(), payload.teacher)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
        }
        val expandedText = listOf(payload.courseName, payload.location, payload.displayTimeText(), payload.teacher)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
        val miIslandSessionId = if (miIslandEnabled) MiSuperIslandBridge.nextCourseSessionId() else 0
        val notificationId = if (miIslandEnabled) {
            MiSuperIslandBridge.courseNotificationId(COURSE_NOTIFICATION_ID, miIslandSessionId)
        } else {
            COURSE_NOTIFICATION_ID
        }
        val builder = NotificationCompat.Builder(context, CHANNEL_COURSE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setContentIntent(openCoursesIntent(context))
            .setAutoCancel(!liveUpdateEnabled)
            .setOngoing(liveUpdateEnabled)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(
                android.R.drawable.ic_menu_my_calendar,
                "课程表",
                openCoursesIntent(context),
            )
            .addAction(
                android.R.drawable.checkbox_on_background,
                "确定",
                dismissCourseReminderIntent(context, notificationId),
            )
        if (liveUpdateEnabled) {
            builder.setSubText("Live Update")
                .setOnlyAlertOnce(true)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(payload.startAtMillis)
                .setRequestPromotedOngoing(true)
        }
        val notification = if (miIslandEnabled) {
            runCatching {
                MiSuperIslandBridge.addCourseReminderExtras(
                    context = context,
                    builder = builder,
                    payload = payload,
                    displayMode = displayMode,
                    sessionId = miIslandSessionId,
                    notificationId = notificationId,
                ).build()
            }.getOrElse {
                builder.build()
            }
        } else {
            builder.build()
        }
        MiSuperIslandBridge.notifyCourseReminder(
            context = context,
            notificationId = notificationId,
            notification = notification,
            bypassRestriction = miIslandBypassEnabled,
            authMode = MiSuperIslandBridge.AuthMode.fromIndex(miIslandAuthMode),
            fallbackNotify = { NotificationManagerCompat.from(context).notify(notificationId, notification) },
        )
        scheduleCourseReminderAutoDismiss(context, notificationId, payload.startAtMillis)
    }

    private fun mainActivityIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun openCoursesIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = AppDeepLinks.ACTION_OPEN_COURSES
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            244021,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun dismissCourseReminderIntent(context: Context, notificationId: Int): PendingIntent {
        val intent = Intent(context, CourseReminderActionReceiver::class.java).apply {
            action = CourseReminderActionReceiver.ACTION_DISMISS_COURSE_REMINDER
            putExtra(CourseReminderActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun scheduleCourseReminderAutoDismiss(context: Context, notificationId: Int, startAtMillis: Long) {
        if (startAtMillis <= System.currentTimeMillis()) return
        val intent = Intent(context, CourseReminderAutoDismissReceiver::class.java).apply {
            action = CourseReminderAutoDismissReceiver.ACTION_AUTO_DISMISS_COURSE_REMINDER
            putExtra(CourseReminderAutoDismissReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            244300 + notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (ExactAlarmPermission.canSchedule(context)) {
            runCatching {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startAtMillis, pendingIntent)
            }.getOrElse {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startAtMillis, pendingIntent)
            }
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startAtMillis, pendingIntent)
        }
        CourseReminderDebugLog.add("已安排上课自动关闭通知：${payloadStartText(startAtMillis)}")
    }

    private fun payloadStartText(startAtMillis: Long): String {
        return java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.CHINA).format(java.util.Date(startAtMillis))
    }

    private fun CourseReminderPayload.liveUpdateDisplayParts(
        displayMode: MiSuperIslandBridge.DisplayMode,
    ): Pair<String, String> {
        val timeText = displayTimeText()
        val rightText = when (displayMode) {
            MiSuperIslandBridge.DisplayMode.CourseLocation -> location
            MiSuperIslandBridge.DisplayMode.CourseTime -> timeText
            MiSuperIslandBridge.DisplayMode.CourseLocationTime -> listOf(location, timeText).filter { it.isNotBlank() }.joinToString(" · ")
            MiSuperIslandBridge.DisplayMode.IconCourse -> courseName
        }.ifBlank { "课程提醒" }
        return when (displayMode) {
            MiSuperIslandBridge.DisplayMode.IconCourse -> rightText to listOf(location, timeText).filter { it.isNotBlank() }.joinToString(" · ")
            else -> courseName to rightText
        }
    }

    private fun canPostNotifications(context: Context): Boolean {
        return Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
}

