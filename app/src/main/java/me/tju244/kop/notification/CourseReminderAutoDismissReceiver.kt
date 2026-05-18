package me.tju244.kop.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CourseReminderAutoDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, RebornNotifications.COURSE_NOTIFICATION_ID)
        context.getSystemService(NotificationManager::class.java)?.cancel(notificationId)
        CourseReminderDebugLog.add("上课时间到，自动关闭课程提醒通知：$notificationId")
    }

    companion object {
        const val ACTION_AUTO_DISMISS_COURSE_REMINDER = "me.tju244.kop.action.AUTO_DISMISS_COURSE_REMINDER"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}

