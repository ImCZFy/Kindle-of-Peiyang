package me.tju244.kop.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class CourseReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_DISMISS_COURSE_REMINDER -> {
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, RebornNotifications.COURSE_NOTIFICATION_ID)
                NotificationManagerCompat.from(context).cancel(notificationId)
            }
        }
    }

    companion object {
        const val ACTION_DISMISS_COURSE_REMINDER = "me.tju244.kop.action.DISMISS_COURSE_REMINDER"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}

