package me.tju244.kop.notification.miui

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.xzakota.hyper.notification.focus.FocusNotification
import com.xzakota.hyper.notification.island.model.TextInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.tju244.kop.MainActivity
import me.tju244.kop.R
import me.tju244.kop.notification.CourseReminderActionReceiver
import me.tju244.kop.notification.CourseReminderPayload
import me.tju244.kop.notification.displayTimeText
import me.tju244.kop.ui.AppDeepLinks
import rikka.shizuku.Shizuku
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

object MiSuperIslandBridge {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val notificationSeq = AtomicInteger(0)
    private const val SHIZUKU_REQUEST_CODE = 24431
    private const val COURSE_NOTIFICATION_ID_WINDOW = 10_000

    enum class AuthMode {
        Direct,
        Shizuku;

        companion object {
            fun fromIndex(index: Int): AuthMode = if (index == 1) Shizuku else Direct
        }
    }

    enum class DisplayMode {
        CourseLocation,
        CourseTime,
        CourseLocationTime,
        IconCourse;

        companion object {
            fun fromIndex(index: Int): DisplayMode = entries.getOrElse(index.coerceIn(0, 3)) { CourseLocationTime }
        }
    }

    fun nextCourseSessionId(): Int {
        val seq = notificationSeq.updateAndGet { value -> (value + 1) % COURSE_NOTIFICATION_ID_WINDOW }
        val timePart = (System.currentTimeMillis() % COURSE_NOTIFICATION_ID_WINDOW).toInt()
        return ((timePart * 31) + seq).floorMod(COURSE_NOTIFICATION_ID_WINDOW - 1) + 1
    }

    fun courseNotificationId(baseId: Int, sessionId: Int): Int {
        return baseId + sessionId.coerceIn(1, COURSE_NOTIFICATION_ID_WINDOW - 1)
    }

    fun addCourseReminderExtras(
        context: Context,
        builder: NotificationCompat.Builder,
        payload: CourseReminderPayload,
        displayMode: DisplayMode,
        sessionId: Int,
        notificationId: Int,
    ): NotificationCompat.Builder {
        if (!isLikelyMiuiDevice()) return builder
        val leftText = payload.courseName
        val rightText = when (displayMode) {
            DisplayMode.CourseLocation -> payload.location
            DisplayMode.CourseTime -> payload.displayTimeText()
            DisplayMode.CourseLocationTime -> listOf(payload.location, payload.displayTimeText()).filter { it.isNotBlank() }.joinToString(" · ")
            DisplayMode.IconCourse -> payload.courseName
        }.ifBlank { "课程提醒" }
        val extras = FocusNotification.buildV3 {
            val lightIcon = Icon.createWithResource(context, R.drawable.tju_badge_tp)
            val darkIcon = Icon.createWithResource(context, R.drawable.tju_badge_tp)
            val appKey = createPicture("miui.focus.pic_app", lightIcon)
            val islandKey = createPicture("miui.focus.pic_island", darkIcon)
            val smallIslandKey = createPicture("miui.land.pic_island", darkIcon)
            val darkKey = createPicture("miui.focus.pic_app_dark", darkIcon)
            business = "course_reminder_$sessionId"
            isShowNotification = true
            islandFirstFloat = true
            enableFloat = false
            updatable = true
            aodTitle = payload.courseName.take(20)
            ticker = payload.courseName
            tickerPic = appKey
            chatInfo {
                picProfile = islandKey
                title = payload.courseName
                content = rightText
                appIconPkg = context.packageName
            }
            island {
                islandProperty = 1
                highlightColor = "#3B82F6"
                bigIslandArea {
                    imageTextInfoLeft {
                        type = 1
                        picInfo {
                            type = 1
                            pic = islandKey
                        }
                        textInfo {
                            title = leftText
                            showHighlightColor = true
                        }
                    }
                    this.textInfo = TextInfo().apply {
                        title = rightText
                        showHighlightColor = true
                        narrowFont = false
                    }
                }
                smallIslandArea {
                    combinePicInfo {
                        picInfo {
                            type = 1
                            pic = smallIslandKey
                        }
                        progressInfo {
                            progress = 100
                            colorReach = "#3B82F6"
                            colorUnReach = "#333333"
                        }
                    }
                }
            }
            baseInfo {
                type = 2
                title = payload.courseName
                content = listOf(payload.location, payload.displayTimeText(), payload.teacher).filter { it.isNotBlank() }.joinToString(" · ")
            }
            textButton {
                addActionInfo {
                    val actionIntent = PendingIntent.getActivity(
                        context,
                        24439,
                        Intent(context, MainActivity::class.java).apply {
                            action = AppDeepLinks.ACTION_OPEN_COURSES
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                    action = createAction(
                        "course_open",
                        Notification.Action.Builder(
                            Icon.createWithResource(context, android.R.drawable.ic_menu_my_calendar),
                            "课程表",
                            actionIntent,
                        ).build(),
                    )
                    actionTitle = "课程表"
                }
                addActionInfo {
                    val actionIntent = PendingIntent.getBroadcast(
                        context,
                        notificationId,
                        Intent(context, CourseReminderActionReceiver::class.java).apply {
                            action = CourseReminderActionReceiver.ACTION_DISMISS_COURSE_REMINDER
                            putExtra(CourseReminderActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                    action = createAction(
                        "course_dismiss",
                        Notification.Action.Builder(
                            Icon.createWithResource(context, android.R.drawable.checkbox_on_background),
                            "确定",
                            actionIntent,
                        ).build(),
                    )
                    actionTitle = "确定"
                }
            }
            picInfo {
                type = 1
                pic = appKey
                picDark = darkKey
            }
        }
        return builder.addExtras(extras)
    }

    fun notifyCourseReminder(
        context: Context,
        notificationId: Int,
        notification: Notification,
        bypassRestriction: Boolean,
        authMode: AuthMode,
        fallbackNotify: () -> Unit,
    ) {
        if (!isLikelyMiuiDevice()) {
            fallbackNotify()
            return
        }
        if (!bypassRestriction || authMode == AuthMode.Direct) {
            safeNotify(context, notificationId, notification)
            return
        }
        scope.launch {
            val uid = findXmsfUid(authMode)
            val blocked = uid != null && setXmsfNetworkingEnabled(context, authMode, uid, enabled = false)
            try {
                safeNotify(context, notificationId, notification)
                if (blocked) delay(160L)
            } finally {
                if (blocked && uid != null) {
                    setXmsfNetworkingEnabled(context, authMode, uid, enabled = true)
                }
            }
        }
    }

    private fun safeNotify(context: Context, notificationId: Int, notification: Notification) {
        if (!canPostNotifications(context)) return
        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    private fun canPostNotifications(context: Context): Boolean {
        return Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    fun isLikelyMiuiDevice(): Boolean {
        val brand = "${Build.BRAND} ${Build.MANUFACTURER}".lowercase()
        return brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco")
    }

    fun shizukuStatusText(): String {
        return when {
            !isShizukuAvailable() -> "Shizuku 未运行"
            isShizukuGranted() -> "Shizuku 已授权"
            else -> "Shizuku 未授权"
        }
    }

    fun isShizukuAvailable(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    fun isShizukuGranted(): Boolean = runCatching {
        Shizuku.pingBinder() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    fun requestShizukuPermission(): Boolean = runCatching {
        if (!Shizuku.pingBinder()) return@runCatching false
        if (Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) return@runCatching true
        Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
        true
    }.getOrDefault(false)

    private fun findXmsfUid(authMode: AuthMode): Int? {
        val output = runPrivilegedCommandForOutput(authMode, "cmd package list packages -U com.xiaomi.xmsf")
            ?: runPrivilegedCommandForOutput(authMode, "pm list packages -U com.xiaomi.xmsf")
        val uidText = output?.substringAfter("uid:", missingDelimiterValue = "")?.takeWhile { it.isDigit() }
        return uidText?.toIntOrNull()
    }

    private fun runPrivilegedCommand(authMode: AuthMode, command: String): Boolean {
        return runPrivilegedCommandForOutput(authMode, command) != null
    }

    private fun setXmsfNetworkingEnabled(context: Context, authMode: AuthMode, uid: Int, enabled: Boolean): Boolean {
        return when (authMode) {
            AuthMode.Direct -> false
            AuthMode.Shizuku -> setXmsfNetworkingEnabledByShizuku(context, uid, enabled)
        }
    }

    private fun setXmsfNetworkingEnabledByShizuku(context: Context, uid: Int, enabled: Boolean): Boolean {
        if (!isShizukuGranted()) return false
        val args = Shizuku.UserServiceArgs(
            ComponentName(context, MiIslandPrivilegedService::class.java),
        )
            .processNameSuffix("mi_island_privileged")
            .tag("mi_island_privileged")
            .version(1)
            .daemon(false)
        val latch = CountDownLatch(1)
        val serviceRef = AtomicReference<IMiIslandPrivilegedService?>()
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                serviceRef.set(IMiIslandPrivilegedService.Stub.asInterface(service))
                latch.countDown()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                serviceRef.set(null)
                latch.countDown()
            }
        }
        return runCatching {
            Shizuku.bindUserService(args, connection)
            if (!latch.await(4, TimeUnit.SECONDS)) return@runCatching false
            serviceRef.get()?.setXmsfNetworkingEnabled(uid, enabled) == true
        }.getOrDefault(false).also {
            runCatching {
                serviceRef.get()?.destroy()
                Shizuku.unbindUserService(args, connection, true)
            }
        }
    }

    private fun runPrivilegedCommandForOutput(authMode: AuthMode, command: String): String? {
        return when (authMode) {
            AuthMode.Direct -> null
            AuthMode.Shizuku -> runShizukuCommandForOutput(command)
        }
    }

    private fun runRootCommandForOutput(command: String): String? = runCatching {
        val process = ProcessBuilder("su", "-c", command).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val code = process.waitFor()
        if (code == 0) output else null
    }.getOrNull()

    private fun runShizukuCommandForOutput(command: String): String? = runCatching {
        if (!isShizukuGranted()) return@runCatching null
        val method = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java,
        ).apply { isAccessible = true }
        val process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val code = process.waitFor()
        if (code == 0) output else null
    }.getOrNull()

    private fun Int.floorMod(mod: Int): Int = ((this % mod) + mod) % mod
}

