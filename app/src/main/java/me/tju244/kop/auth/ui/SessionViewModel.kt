package me.tju244.kop.auth.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import me.tju244.kop.RebuildApplication
import me.tju244.kop.auth.data.AuthRepository
import me.tju244.kop.auth.data.SessionStore
import me.tju244.kop.notification.CourseReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class SessionUiState(
    val token: String = "",
    val sidOrPhone: String = "",
    val password: String = "",
    val initialized: Boolean = false,
    val themeMode: Int = 0,
    val fontMode: Int = 0,
    val bottomBarLabelMode: Int = 1,
    val navigationBarMode: Int = 1, // 0 fixed, 1 floating
    val tabletRailPosition: Int = 0, // 0 left, 1 right
    val bottomBarGlassEnabled: Boolean = true,
    val forumNotificationEnabled: Boolean = true,
    val courseNotificationEnabled: Boolean = true,
    val courseLiveUpdateEnabled: Boolean = true,
    val miIslandNotificationEnabled: Boolean = false,
    val miIslandBypassEnabled: Boolean = false,
    val miIslandAuthMode: Int = 0,
    val miIslandDisplayMode: Int = 2,
    val oobeCompleted: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
)

class SessionViewModel(app: Application) : AndroidViewModel(app) {
    private val appGraph = app as RebuildApplication
    private val store: SessionStore = appGraph.sessionStore
    private val authRepository: AuthRepository = appGraph.authRepository
    private var launchTjuSyncStarted = false

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            store.tokenFlow.collectLatest { tk ->
                _uiState.value = _uiState.value.copy(token = tk, initialized = true)
            }
        }
        viewModelScope.launch {
            store.themeModeFlow.collectLatest { mode ->
                _uiState.value = _uiState.value.copy(themeMode = mode)
            }
        }
        viewModelScope.launch {
            store.fontModeFlow.collectLatest { mode ->
                _uiState.value = _uiState.value.copy(fontMode = mode)
            }
        }
        viewModelScope.launch {
            store.bottomBarLabelModeFlow.collectLatest { mode ->
                _uiState.value = _uiState.value.copy(bottomBarLabelMode = mode)
            }
        }
        viewModelScope.launch {
            store.navigationBarModeFlow.collectLatest { mode ->
                _uiState.value = _uiState.value.copy(navigationBarMode = mode)
            }
        }
        viewModelScope.launch {
            store.tabletRailPositionFlow.collectLatest { position ->
                _uiState.value = _uiState.value.copy(tabletRailPosition = position)
            }
        }
        viewModelScope.launch {
            store.bottomBarGlassEnabledFlow.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(bottomBarGlassEnabled = enabled)
            }
        }
        viewModelScope.launch {
            store.oobeCompletedFlow.collectLatest { completed ->
                _uiState.value = _uiState.value.copy(oobeCompleted = completed)
            }
        }
        viewModelScope.launch {
            store.forumNotificationEnabledFlow.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(forumNotificationEnabled = enabled)
            }
        }
        viewModelScope.launch {
            store.courseNotificationEnabledFlow.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(courseNotificationEnabled = enabled)
            }
        }
        viewModelScope.launch {
            store.courseLiveUpdateEnabledFlow.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(courseLiveUpdateEnabled = enabled)
            }
        }
        viewModelScope.launch {
            store.miIslandNotificationEnabledFlow.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(miIslandNotificationEnabled = enabled)
            }
        }
        viewModelScope.launch {
            store.miIslandBypassEnabledFlow.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(miIslandBypassEnabled = enabled)
            }
        }
        viewModelScope.launch {
            store.miIslandAuthModeFlow.collectLatest { mode ->
                _uiState.value = _uiState.value.copy(miIslandAuthMode = mode)
            }
        }
        viewModelScope.launch {
            store.miIslandDisplayModeFlow.collectLatest { mode ->
                _uiState.value = _uiState.value.copy(miIslandDisplayMode = mode)
            }
        }
        syncTjuOnAppLaunch()
    }

    fun onSidChanged(value: String) {
        _uiState.value = _uiState.value.copy(sidOrPhone = value, error = null)
    }

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun login() {
        val sid = _uiState.value.sidOrPhone.trim()
        val pwd = _uiState.value.password
        if (sid.isEmpty() || pwd.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "账号和密码不能为空")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val loginResult = authRepository.login(sid, pwd)
            if (loginResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = loginResult.exceptionOrNull()?.message ?: "登录失败",
                )
                return@launch
            }
            authRepository.updateToken()
            _uiState.value = _uiState.value.copy(loading = false)
        }
    }

    fun requestLoginCode(phone: String, onResult: (Boolean, String) -> Unit) {
        val normalized = phone.trim()
        if (normalized.isBlank()) {
            onResult(false, "手机号不能为空")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val result = authRepository.requestLoginCode(normalized)
            _uiState.value = _uiState.value.copy(loading = false)
            if (result.isFailure) {
                val message = result.exceptionOrNull()?.message ?: "验证码发送失败"
                _uiState.value = _uiState.value.copy(error = message)
                onResult(false, message)
                return@launch
            }
            onResult(true, "验证码已发送")
        }
    }

    fun loginByCode(phone: String, code: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val normalizedPhone = phone.trim()
        val normalizedCode = code.trim()
        if (normalizedPhone.isBlank() || normalizedCode.isBlank()) {
            onResult(false, "手机号和验证码不能为空")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val loginResult = authRepository.loginByCode(normalizedPhone, normalizedCode)
            if (loginResult.isFailure) {
                val message = loginResult.exceptionOrNull()?.message ?: "验证码登录失败"
                _uiState.value = _uiState.value.copy(loading = false, error = message)
                onResult(false, message)
                return@launch
            }
            authRepository.updateToken()
            _uiState.value = _uiState.value.copy(loading = false)
            onResult(true, "登录成功")
        }
    }

    fun requestResetCode(phone: String, onResult: (Boolean, String) -> Unit) {
        val normalized = phone.trim()
        if (normalized.isBlank()) {
            onResult(false, "手机号不能为空")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val result = authRepository.requestResetCode(normalized)
            _uiState.value = _uiState.value.copy(loading = false)
            if (result.isFailure) {
                val message = result.exceptionOrNull()?.message ?: "验证码发送失败"
                _uiState.value = _uiState.value.copy(error = message)
                onResult(false, message)
                return@launch
            }
            onResult(true, "验证码已发送")
        }
    }

    fun resetPasswordByPhone(
        phone: String,
        code: String,
        newPassword: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        val normalizedPhone = phone.trim()
        val normalizedCode = code.trim()
        val normalizedPassword = newPassword.trim()
        if (normalizedPhone.isBlank() || normalizedCode.isBlank() || normalizedPassword.isBlank()) {
            onResult(false, "请完整填写手机号、验证码和新密码")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val verifyResult = authRepository.verifyResetCode(normalizedPhone, normalizedCode)
            if (verifyResult.isFailure) {
                val message = verifyResult.exceptionOrNull()?.message ?: "验证码校验失败"
                _uiState.value = _uiState.value.copy(loading = false, error = message)
                onResult(false, message)
                return@launch
            }
            val resetResult = authRepository.resetPasswordByPhone(normalizedPhone, normalizedPassword)
            if (resetResult.isFailure) {
                val message = resetResult.exceptionOrNull()?.message ?: "重置密码失败"
                _uiState.value = _uiState.value.copy(loading = false, error = message)
                onResult(false, message)
                return@launch
            }
            _uiState.value = _uiState.value.copy(loading = false)
            onResult(true, "密码重置成功，请使用新密码登录")
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            store.saveThemeMode(mode.coerceIn(0, 2))
        }
    }

    fun setFontMode(mode: Int) {
        viewModelScope.launch {
            store.saveFontMode(mode.coerceIn(0, 1))
        }
    }

    fun setBottomBarLabelMode(mode: Int) {
        viewModelScope.launch {
            store.saveBottomBarLabelMode(mode.coerceIn(0, 1))
        }
    }

    fun setNavigationBarMode(mode: Int) {
        viewModelScope.launch {
            store.saveNavigationBarMode(mode.coerceIn(0, 1))
        }
    }

    fun setTabletRailPosition(position: Int) {
        viewModelScope.launch {
            store.saveTabletRailPosition(position.coerceIn(0, 1))
        }
    }

    fun setBottomBarGlassEnabled(enabled: Boolean) {
        viewModelScope.launch {
            store.saveBottomBarGlassEnabled(enabled)
        }
    }

    fun completeOobe() {
        viewModelScope.launch {
            store.saveOobeCompleted(true)
        }
    }

    fun setForumNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            store.saveForumNotificationEnabled(enabled)
        }
    }

    fun setCourseNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            store.saveCourseNotificationEnabled(enabled)
            if (enabled) {
                CourseReminderScheduler.scheduleNext(appGraph)
            } else {
                CourseReminderScheduler.cancel(appGraph)
            }
        }
    }

    fun setCourseLiveUpdateEnabled(enabled: Boolean) {
        viewModelScope.launch {
            store.saveCourseLiveUpdateEnabled(enabled)
        }
    }

    fun setMiIslandNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            store.saveMiIslandNotificationEnabled(enabled)
        }
    }

    fun setMiIslandBypassEnabled(enabled: Boolean) {
        viewModelScope.launch {
            store.saveMiIslandBypassEnabled(enabled)
        }
    }

    fun setMiIslandAuthMode(mode: Int) {
        viewModelScope.launch {
            store.saveMiIslandAuthMode(mode)
        }
    }

    fun setMiIslandDisplayMode(mode: Int) {
        viewModelScope.launch {
            store.saveMiIslandDisplayMode(mode)
        }
    }

    private fun syncTjuOnAppLaunch() {
        if (launchTjuSyncStarted) return
        launchTjuSyncStarted = true
        viewModelScope.launch {
            CourseReminderScheduler.scheduleNext(appGraph)
            val bundle = runCatching {
                appGraph.tjuAuthRepository.refreshStored()
            }.getOrNull()
            CourseReminderScheduler.scheduleNext(appGraph, bundle)
        }
    }
}

