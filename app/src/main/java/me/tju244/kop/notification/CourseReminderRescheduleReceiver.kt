package me.tju244.kop.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.tju244.kop.RebuildApplication

class CourseReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as RebuildApplication
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                CourseReminderDebugLog.add("系统事件触发重新调度：${intent.action.orEmpty()}")
                CourseReminderScheduler.scheduleNext(app)
            } finally {
                pending.finish()
            }
        }
    }
}

