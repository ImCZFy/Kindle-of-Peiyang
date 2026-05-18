package me.tju244.kop.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.tju244.kop.RebuildApplication

class CourseReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val payload = CourseReminderPayload(
            courseName = intent.getStringExtra(EXTRA_COURSE_NAME).orEmpty(),
            location = intent.getStringExtra(EXTRA_LOCATION).orEmpty(),
            timeRange = intent.getStringExtra(EXTRA_TIME_RANGE).orEmpty(),
            teacher = intent.getStringExtra(EXTRA_TEACHER).orEmpty(),
            startAtMillis = intent.getLongExtra(EXTRA_START_AT, System.currentTimeMillis()),
        )
        val app = context.applicationContext as RebuildApplication
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                CourseReminderDebugLog.add("提醒触发：${payload.courseName}，准备发送通知")
                val store = app.sessionStore
                RebornNotifications.notifyCourseReminder(
                    context = context,
                    payload = payload,
                    liveUpdateEnabled = store.courseLiveUpdateEnabledFlow.first(),
                    miIslandEnabled = store.miIslandNotificationEnabledFlow.first(),
                    miIslandBypassEnabled = store.miIslandBypassEnabledFlow.first(),
                    miIslandAuthMode = store.miIslandAuthModeFlow.first(),
                    miIslandDisplayMode = store.miIslandDisplayModeFlow.first(),
                )
                CourseReminderScheduler.markNotified(app, payload)
                CourseReminderDebugLog.add("通知已发送：${payload.courseName}，继续调度下一节")
                CourseReminderScheduler.scheduleNext(app)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_COURSE_NAME = "course_name"
        const val EXTRA_LOCATION = "location"
        const val EXTRA_TIME_RANGE = "time_range"
        const val EXTRA_TEACHER = "teacher"
        const val EXTRA_START_AT = "start_at"
    }
}

