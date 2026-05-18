package me.tju244.kop.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppDeepLinks {
    const val ACTION_OPEN_COURSES = "me.tju244.kop.action.OPEN_COURSES"

    private var pendingAction: String? = null
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun dispatch(action: String?) {
        if (action == ACTION_OPEN_COURSES) {
            pendingAction = action
            _events.tryEmit(action)
        }
    }

    fun consumePending(): String? {
        val action = pendingAction
        pendingAction = null
        return action
    }
}

