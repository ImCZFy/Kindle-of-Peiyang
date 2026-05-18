package me.tju244.kop.tju.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import me.tju244.kop.RebuildApplication
import me.tju244.kop.tju.data.TjuAuthRepository
import me.tju244.kop.tju.network.TjuClassesBundle
import me.tju244.kop.tju.network.TjuCourseDto
import me.tju244.kop.tju.network.TjuExamDto
import me.tju244.kop.tju.network.TjuGpaCourseDto
import me.tju244.kop.tju.network.TjuGpaStatDto
import me.tju244.kop.tju.network.TjuGpaTotalDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TjuUiState(
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val loggedIn: Boolean = false,
    val hasCache: Boolean = false,
    val error: String? = null,
    val courses: List<TjuCourseDto> = emptyList(),
    val exams: List<TjuExamDto> = emptyList(),
    val gpaTotal: TjuGpaTotalDto? = null,
    val gpaCourses: List<TjuGpaCourseDto> = emptyList(),
    val gpaStats: List<TjuGpaStatDto> = emptyList(),
)

class TjuViewModel(
    private val repository: TjuAuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TjuUiState())
    val uiState: StateFlow<TjuUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val (username, password) = repository.storedCredentials()
            val cached = repository.cachedBundle()
            _uiState.value = _uiState.value.copy(
                username = username,
                password = password,
                loggedIn = username.isNotBlank(),
            ).withBundle(cached)
            if (username.isNotBlank() && password.isNotBlank()) {
                refresh()
            }
        }
    }

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun login(onSuccess: () -> Unit = {}) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(loading = true, error = null)
            runCatching {
                repository.loginAndFetch(state.username, state.password)
            }.onSuccess { bundle ->
                _uiState.value = _uiState.value.withBundle(bundle).copy(
                    loading = false,
                    loggedIn = true,
                    error = null,
                )
                onSuccess()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    loggedIn = false,
                    error = error.message ?: "教务网登录失败",
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            runCatching {
                repository.refreshStored()
            }.onSuccess { bundle ->
                _uiState.value = _uiState.value.withBundle(bundle).copy(
                    loading = false,
                    loggedIn = true,
                    error = null,
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = error.message ?: "教务网数据刷新失败",
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = TjuUiState()
        }
    }
}

private fun TjuUiState.withBundle(bundle: TjuClassesBundle?): TjuUiState {
    if (bundle == null) return this
    return copy(
        hasCache = true,
        courses = bundle.courses,
        exams = bundle.exams,
        gpaTotal = bundle.gpa?.total,
        gpaCourses = bundle.gpa?.courses.orEmpty(),
        gpaStats = bundle.gpa?.stats.orEmpty(),
    )
}

class TjuViewModelFactory(
    private val app: RebuildApplication,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TjuViewModel(app.tjuAuthRepository) as T
    }
}

