package me.tju244.kop.auth.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session")

class SessionStore(private val context: Context) {
    private val tokenKey = stringPreferencesKey("auth_token")
    private val accountKey = stringPreferencesKey("auth_account")
    private val userNumberKey = stringPreferencesKey("auth_user_number")
    private val oobeCompletedKey = booleanPreferencesKey("oobe_completed")
    private val tjuUsernameKey = stringPreferencesKey("tju_username")
    private val tjuPasswordKey = stringPreferencesKey("tju_password")
    private val tjuClassesCacheKey = stringPreferencesKey("tju_classes_cache")
    private val themeModeKey = intPreferencesKey("theme_mode")
    private val fontModeKey = intPreferencesKey("font_mode")
    private val bottomBarLabelModeKey = intPreferencesKey("bottom_bar_label_mode")
    private val navigationBarModeKey = intPreferencesKey("navigation_bar_mode")
    private val tabletRailPositionKey = intPreferencesKey("tablet_rail_position")
    private val bottomBarGlassEnabledKey = booleanPreferencesKey("bottom_bar_glass_enabled")
    private val studyRoomColumnsKey = intPreferencesKey("study_room_columns")
    private val courseNotificationEnabledKey = booleanPreferencesKey("course_notification_enabled")
    private val courseLiveUpdateEnabledKey = booleanPreferencesKey("course_live_update_enabled")
    private val miIslandNotificationEnabledKey = booleanPreferencesKey("mi_island_notification_enabled")
    private val miIslandBypassEnabledKey = booleanPreferencesKey("mi_island_bypass_enabled")
    private val miIslandAuthModeKey = intPreferencesKey("mi_island_auth_mode")
    private val miIslandDisplayModeKey = intPreferencesKey("mi_island_display_mode")
    private val customCoursesKey = stringPreferencesKey("custom_courses")

    val tokenFlow: Flow<String> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[tokenKey] ?: ""
    }

    val oobeCompletedFlow: Flow<Boolean> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[oobeCompletedKey] ?: false
    }

    val tjuUsernameFlow: Flow<String> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[tjuUsernameKey] ?: ""
    }

    val tjuPasswordFlow: Flow<String> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[tjuPasswordKey] ?: ""
    }

    val tjuClassesCacheFlow: Flow<String> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[tjuClassesCacheKey] ?: ""
    }

    val themeModeFlow: Flow<Int> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[themeModeKey] ?: 0
    }

    val fontModeFlow: Flow<Int> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[fontModeKey] ?: 0
    }

    val bottomBarLabelModeFlow: Flow<Int> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[bottomBarLabelModeKey] ?: 1
    }

    val navigationBarModeFlow: Flow<Int> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[navigationBarModeKey] ?: 1
    }

    val tabletRailPositionFlow: Flow<Int> = context.sessionDataStore.data.map { prefs: Preferences ->
        (prefs[tabletRailPositionKey] ?: 0).coerceIn(0, 1)
    }

    val bottomBarGlassEnabledFlow: Flow<Boolean> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[bottomBarGlassEnabledKey] ?: true
    }

    val studyRoomColumnsFlow: Flow<Int> = context.sessionDataStore.data.map { prefs: Preferences ->
        (prefs[studyRoomColumnsKey] ?: 1).coerceIn(1, 2)
    }

    val courseNotificationEnabledFlow: Flow<Boolean> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[courseNotificationEnabledKey] ?: true
    }

    val courseLiveUpdateEnabledFlow: Flow<Boolean> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[courseLiveUpdateEnabledKey] ?: true
    }

    val miIslandNotificationEnabledFlow: Flow<Boolean> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[miIslandNotificationEnabledKey] ?: false
    }

    val miIslandBypassEnabledFlow: Flow<Boolean> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[miIslandBypassEnabledKey] ?: false
    }

    val miIslandAuthModeFlow: Flow<Int> = context.sessionDataStore.data.map { prefs: Preferences ->
        (prefs[miIslandAuthModeKey] ?: 0).coerceIn(0, 1)
    }

    val miIslandDisplayModeFlow: Flow<Int> = context.sessionDataStore.data.map { prefs: Preferences ->
        (prefs[miIslandDisplayModeKey] ?: 2).coerceIn(0, 3)
    }

    val customCoursesFlow: Flow<String> = context.sessionDataStore.data.map { prefs: Preferences ->
        prefs[customCoursesKey] ?: "[]"
    }

    suspend fun currentToken(): String = tokenFlow.first()
    suspend fun currentOobeCompleted(): Boolean = oobeCompletedFlow.first()
    suspend fun currentAccount(): String = context.sessionDataStore.data.map { prefs -> prefs[accountKey] ?: "" }.first()
    suspend fun currentTjuUsername(): String = tjuUsernameFlow.first()
    suspend fun currentTjuPassword(): String = tjuPasswordFlow.first()
    suspend fun currentTjuClassesCache(): String = tjuClassesCacheFlow.first()
    suspend fun currentCourseNotificationEnabled(): Boolean = courseNotificationEnabledFlow.first()
    suspend fun currentCourseLiveUpdateEnabled(): Boolean = courseLiveUpdateEnabledFlow.first()
    suspend fun currentMiIslandNotificationEnabled(): Boolean = miIslandNotificationEnabledFlow.first()
    suspend fun currentMiIslandBypassEnabled(): Boolean = miIslandBypassEnabledFlow.first()
    suspend fun currentMiIslandAuthMode(): Int = miIslandAuthModeFlow.first()
    suspend fun currentMiIslandDisplayMode(): Int = miIslandDisplayModeFlow.first()
    suspend fun currentCustomCourses(): String = customCoursesFlow.first()

    suspend fun saveToken(token: String) {
        context.sessionDataStore.edit { it[tokenKey] = token }
    }

    suspend fun saveOobeCompleted(completed: Boolean) {
        context.sessionDataStore.edit { it[oobeCompletedKey] = completed }
    }

    suspend fun saveThemeMode(mode: Int) {
        context.sessionDataStore.edit { it[themeModeKey] = mode }
    }

    suspend fun saveFontMode(mode: Int) {
        context.sessionDataStore.edit { it[fontModeKey] = mode }
    }

    suspend fun saveBottomBarLabelMode(mode: Int) {
        context.sessionDataStore.edit { it[bottomBarLabelModeKey] = mode }
    }

    suspend fun saveNavigationBarMode(mode: Int) {
        context.sessionDataStore.edit { it[navigationBarModeKey] = mode }
    }

    suspend fun saveTabletRailPosition(position: Int) {
        context.sessionDataStore.edit { it[tabletRailPositionKey] = position.coerceIn(0, 1) }
    }

    suspend fun saveUserNumber(userNumber: String) {
        context.sessionDataStore.edit { it[userNumberKey] = userNumber }
    }

    suspend fun saveAccount(account: String) {
        context.sessionDataStore.edit { it[accountKey] = account }
    }

    suspend fun saveTjuCredentials(username: String, password: String) {
        context.sessionDataStore.edit {
            it[tjuUsernameKey] = username
            it[tjuPasswordKey] = password
        }
    }

    suspend fun saveTjuClassesCache(cache: String) {
        context.sessionDataStore.edit { it[tjuClassesCacheKey] = cache }
    }

    suspend fun clearTjuCredentials() {
        context.sessionDataStore.edit {
            it.remove(tjuUsernameKey)
            it.remove(tjuPasswordKey)
            it.remove(tjuClassesCacheKey)
        }
    }

    suspend fun currentUserNumber(): String {
        return context.sessionDataStore.data.map { prefs -> prefs[userNumberKey] ?: "" }.first()
    }

    suspend fun saveBottomBarGlassEnabled(enabled: Boolean) {
        context.sessionDataStore.edit { it[bottomBarGlassEnabledKey] = enabled }
    }

    suspend fun saveStudyRoomColumns(columns: Int) {
        context.sessionDataStore.edit { it[studyRoomColumnsKey] = columns.coerceIn(1, 2) }
    }

    suspend fun saveCourseNotificationEnabled(enabled: Boolean) {
        context.sessionDataStore.edit { it[courseNotificationEnabledKey] = enabled }
    }

    suspend fun saveCourseLiveUpdateEnabled(enabled: Boolean) {
        context.sessionDataStore.edit { it[courseLiveUpdateEnabledKey] = enabled }
    }

    suspend fun saveMiIslandNotificationEnabled(enabled: Boolean) {
        context.sessionDataStore.edit { it[miIslandNotificationEnabledKey] = enabled }
    }

    suspend fun saveMiIslandBypassEnabled(enabled: Boolean) {
        context.sessionDataStore.edit { it[miIslandBypassEnabledKey] = enabled }
    }

    suspend fun saveMiIslandAuthMode(mode: Int) {
        context.sessionDataStore.edit { it[miIslandAuthModeKey] = mode.coerceIn(0, 1) }
    }

    suspend fun saveMiIslandDisplayMode(mode: Int) {
        context.sessionDataStore.edit { it[miIslandDisplayModeKey] = mode.coerceIn(0, 3) }
    }

    suspend fun saveCustomCourses(json: String) {
        context.sessionDataStore.edit { it[customCoursesKey] = json.ifBlank { "[]" } }
    }

    suspend fun clear() {
        context.sessionDataStore.edit {
            it.remove(tokenKey)
            it.remove(accountKey)
            it.remove(userNumberKey)
            it.remove(tjuUsernameKey)
            it.remove(tjuPasswordKey)
            it.remove(tjuClassesCacheKey)
        }
    }
}

